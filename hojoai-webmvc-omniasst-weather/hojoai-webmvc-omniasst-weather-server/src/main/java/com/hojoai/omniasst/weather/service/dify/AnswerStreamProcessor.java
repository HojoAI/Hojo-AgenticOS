/*
 * Copyright 2026 HojoAI
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.hojoai.omniasst.weather.service.dify;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.hojoai.omniasst.weather.entity.DifyStreamEvent;
import com.hojoai.omniasst.weather.entity.SAAgentResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;

/**
 * Dify 流式响应处理器（状态机）。
 * 职责：
 * 1. 使用 {@link JsonArrayStreamAccumulator} 做 JSON 补全；
 * 2. 基于 {@link AnswerStreamState} 做状态机管理，实现 a、b、c 的增量输出；
 * 3. 将 Dify 的事件映射为 {@link SAAgentResponse} 并通过 {@link SseEmitter} 推送给前端。
 *
 * @author wangjian
 * @since 2026-01-22
 */
public class AnswerStreamProcessor {

    private static final Logger logger = LoggerFactory.getLogger(AnswerStreamProcessor.class);

    private final ObjectMapper objectMapper;
    private final SseEmitter emitter;
    private final JsonArrayStreamAccumulator accumulator;
    private final AnswerStreamState state;
    private final PartialJsonTextExtractor partialJsonExtractor;
    private volatile boolean isCompleted = false;

    public AnswerStreamProcessor(ObjectMapper objectMapper, SseEmitter emitter) {
        this.objectMapper = objectMapper;
        this.emitter = emitter;
        this.accumulator = new JsonArrayStreamAccumulator();
        this.state = new AnswerStreamState();
        this.partialJsonExtractor = new PartialJsonTextExtractor();
    }

    /**
     * 处理单个 Dify 流事件。不向调用方抛出受检异常；SSE 发送失败时在内部调用 {@link #sendErrorFallbackResponse()}。
     *
     * @param event 流事件，可为 null（此时直接忽略）
     */
    public void handleEvent(DifyStreamEvent event) {
        if (event == null) {
            logger.warn("收到空事件，跳过处理");
            return;
        }
        String eventType = event.getEvent();
        // 支持 message 和 agent_message 两种事件类型
        if ("message".equals(eventType) || "agent_message".equals(eventType)) {
            handleMessageEvent(event);
        } else if ("message_end".equals(eventType)) {//不处理end事件，dify平台返回不稳定，使用agent_message的最后又card的逻辑来发送整体报文
            handleMessageEndEvent(event);
        } else {
            // 其它事件目前不处理，例如 tts_message、agent_thought
            logger.debug("忽略 Dify 事件类型: {}", eventType);
        }
    }

    private void handleMessageEvent(DifyStreamEvent event) {
        state.setConversationId(event.getConversationId());
        state.setMessageId(event.getMessageId());

        String answerChunk = event.getAnswer();
        if (answerChunk == null || answerChunk.isEmpty()) {
            return;
        }

        // 通过 JSON 补全累积出完整的数组
        ArrayNode arrayNode = accumulator.appendAndTryParse(answerChunk, objectMapper);
        
        // 如果JSON还不完整，尝试从buffer中提取text字段（流式处理）
        if (arrayNode == null) {
            extractTextFromPartialJson(event);
            return;
        }
        
        if (arrayNode.isEmpty()) {
            return;
        }

        int currentArraySize = arrayNode.size();
        
        // 处理所有数组元素（包括已存在的和新增加的），实现text字段的流式更新
        for (int i = 0; i < currentArraySize; i++) {
            ObjectNode itemNode = (ObjectNode) arrayNode.get(i);
            if (itemNode != null) {
                String type = itemNode.path("type").asText(null);
                String subType = itemNode.path("subType").asText(null);
                String currentText = itemNode.path("text").asText("");

                if (currentText == null) {
                    currentText = "";
                }

                // 获取该元素之前已发送的text内容
                String previousText = state.getItemTextContent(i);

                // 如果text字段有变化（增长或首次出现），计算并发送增量部分
                if (!currentText.equals(previousText)) {
                    String deltaText;
                    
                    if (previousText == null || previousText.isEmpty()) {
                        // 首次出现，发送完整内容（作为增量）
                        deltaText = currentText;
                    } else if (currentText.startsWith(previousText)) {
                        // text字段增长，只发送增量部分
                        deltaText = currentText.substring(previousText.length());
                    } else {
                        // text字段被替换（不常见，但需要处理），发送完整内容
                        deltaText = currentText;
                    }

                    // 发送增量text内容
                    if (!deltaText.isEmpty()) {
                        sendDataItem(type, subType, deltaText, event);
                        
                        // 更新状态：记录已发送的完整text内容和长度
                        state.setItemTextContent(i, currentText);
                        state.setItemTextLength(i, currentText.length());
                    }
                } else {
                    logger.debug("Text字段无变化 - 数组索引: {}, type: {}, subType: {}, text: {}", 
                            i, type, subType, currentText);
                }
            }
        }

        // 更新已处理的数组大小
        state.setProcessedArraySize(currentArraySize);
    }

    /**
     * 从部分JSON中提取text字段（当JSON还不完整时）
     *
     * @param event Dify事件
     */
    private void extractTextFromPartialJson(DifyStreamEvent event) {
        String buffer = accumulator.getBuffer();
        if (buffer == null || buffer.isEmpty()) {
            return;
        }

        // 使用PartialJsonTextExtractor提取text字段信息
        List<PartialJsonTextExtractor.TextFieldInfo> textFields = partialJsonExtractor.extractTextFields(buffer);
        
        // 处理每个提取到的text字段
        for (PartialJsonTextExtractor.TextFieldInfo fieldInfo : textFields) {
            processTextIncrement(fieldInfo.getItemIndex(), fieldInfo.getType(), 
                    fieldInfo.getSubType(), fieldInfo.getText(), event);
        }
    }
    
    /**
     * 处理text字段的增量更新
     *
     * @param itemIndex  数组索引
     * @param type       类型
     * @param subType    子类型
     * @param currentText 当前text内容
     * @param event      Dify事件
     */
    private void processTextIncrement(int itemIndex, String type, String subType, String currentText, DifyStreamEvent event) {
        if (currentText == null) {
            currentText = "";
        }
        
        // 获取之前已发送的text内容
        String previousText = state.getItemTextContent(itemIndex);
        
        // 如果text字段有变化
        if (!currentText.equals(previousText)) {
            String deltaText;
            
            if (previousText == null || previousText.isEmpty()) {
                // 首次出现
                deltaText = currentText;
            } else if (currentText.startsWith(previousText)) {
                // text字段增长
                deltaText = currentText.substring(previousText.length());
            } else {
                // text字段被替换
                deltaText = currentText;
            }
            
            // 发送增量text内容
            if (!deltaText.isEmpty()) {
                sendDataItem(type, subType, deltaText, event);
                
                // 更新状态
                state.setItemTextContent(itemIndex, currentText);
                state.setItemTextLength(itemIndex, currentText.length());
            }
        }
    }

    /**
     * 发送数据项到前端
     *
     * @param type     类型
     * @param subType  子类型
     * @param text     文本内容
     * @param event    Dify 事件
     */
    private void sendDataItem(String type, String subType, String text, DifyStreamEvent event) {
        SAAgentResponse.SAAgentDataItem dataItem = new SAAgentResponse.SAAgentDataItem();
        dataItem.setType(type);
        dataItem.setSubType(subType);
        dataItem.setText(text);

        SAAgentResponse response = new SAAgentResponse();
        response.setConversationId(state.getConversationId());
        response.setMessageId(state.getMessageId());
        response.setData(dataItem);
        response.setFinished(Boolean.FALSE);
        // 使用 Dify 的 created_at（秒）或当前时间（毫秒转秒）
        Long ts = event.getCreatedAt();
        if (ts == null) {
            ts = System.currentTimeMillis() / 1000;
        }
        response.setTimestamp(ts);

        try {
            // 记录响应报文日志
            String responseJson = objectMapper.writeValueAsString(response);
            logger.info("接口返回响应报文: {}", responseJson);
            
            emitter.send(response);
            
            // 如果 subType 为 card，发送完该报文后直接发送结束消息
            if ("card".equals(subType)) {
                logger.info("检测到 subType 为 card，发送完报文后直接发送结束消息");
                sendMessageEndResponse(event);
            }
        } catch (IOException e) {
            logger.error("SSE 发送 SAAgentResponse 失败", e);
            sendErrorFallbackResponse();
        } catch (Exception e) {
            logger.warn("序列化响应报文失败", e);
            try {
                emitter.send(response);
                
                // 如果 subType 为 card，发送完该报文后直接发送结束消息
                if ("card".equals(subType)) {
                    logger.info("检测到 subType 为 card，发送完报文后直接发送结束消息");
                    sendMessageEndResponse(event);
                }
            } catch (IOException ioException) {
                logger.error("SSE 发送 SAAgentResponse 失败", ioException);
                sendErrorFallbackResponse();
            }
        }
    }

    /**
     * 发送结束消息响应
     *
     * @param event Dify 事件
     */
    private void sendMessageEndResponse(DifyStreamEvent event) {
        logger.info("========== sendMessageEndResponse 开始执行 ==========");
        // 构造一个 finished=true 的结束消息，data包含所有累加的数据
        SAAgentResponse response = new SAAgentResponse();
        response.setConversationId(event.getConversationId() != null ? event.getConversationId() : state.getConversationId());
        // message_end 事件中没有 messageId，这里优先使用已有状态
        response.setMessageId(state.getMessageId() != null ? state.getMessageId() : event.getId());
        
        // 获取所有累加的数据（直接是JSON数组）
        ArrayNode finalDataArray = buildFinalDataItem();
        response.setData(finalDataArray);
        response.setFinished(Boolean.TRUE);
        
        Long ts = event.getCreatedAt();
        if (ts == null) {
            ts = System.currentTimeMillis() / 1000;
        }
        response.setTimestamp(ts);

        try {
            // 记录响应报文日志
            String responseJson = objectMapper.writeValueAsString(response);
            logger.info("接口返回响应报文（结束）: {}", responseJson);
            
            emitter.send(response);
        } catch (IOException e) {
            logger.error("SSE 发送结束 SAAgentResponse 失败", e);
            sendErrorFallbackResponse();
            return;
        } catch (Exception e) {
            logger.warn("序列化响应报文失败", e);
            try {
                emitter.send(response);
            } catch (IOException ioException) {
                logger.error("SSE 发送结束 SAAgentResponse 失败", ioException);
                sendErrorFallbackResponse();
                return;
            }
        }

        logger.info("准备完成 SSE 连接");
        emitter.complete();
        isCompleted = true;
        logger.info("========== sendMessageEndResponse 执行完成 ==========");
    }

    private void handleMessageEndEvent(DifyStreamEvent event) {
        // 先判断 sseEmitter 是否已完成，如果完成了就不发送报文了
        if (isCompleted) {
            logger.info("SSE Emitter 已完成，跳过 handleMessageEndEvent 处理");
            return;
        }
        sendMessageEndResponse(event);
    }
    
    /**
     * 构建包含所有累加数据的最终数据项
     * 从accumulator获取完整的JSON数组，直接返回数组对象
     *
     * @return 包含所有累加数据的ArrayNode（JSON数组），如果没有数据则返回null
     */
    private ArrayNode buildFinalDataItem() {
        try {
            // 尝试从accumulator获取完整的JSON数组
            String buffer = accumulator.getBuffer();
            if (buffer == null || buffer.isEmpty()) {
                logger.debug("accumulator buffer为空，无法构建最终数据项");
                return null;
            }
            
            // 尝试解析为JSON数组
            ArrayNode arrayNode = objectMapper.readValue(buffer, ArrayNode.class);
            if (arrayNode == null || arrayNode.isEmpty()) {
                logger.debug("解析的JSON数组为空，无法构建最终数据项");
                return null;
            }
            
            return arrayNode;
            
        } catch (Exception e) {
            logger.error("构建最终数据项失败", e);
            return null;
        }
    }
    
    /**
     * 发送错误兜底响应（两帧数据）：第一帧 {@code finished=false}，第二帧 {@code finished=true}，随后 {@code complete}。
     */
    public void sendErrorFallbackResponse() {
        String errorMessage = "抱歉啊...出于某些原因，这个问题现在我没法回答你，也许你可以换个问法试试？";
        long timestamp = System.currentTimeMillis() / 1000;
        
        // 创建数据项
        SAAgentResponse.SAAgentDataItem dataItem = new SAAgentResponse.SAAgentDataItem();
        dataItem.setType("text");
        dataItem.setSubType("bubble");
        dataItem.setText(errorMessage);
        
        // 第一帧：finished=false
        SAAgentResponse firstFrame = new SAAgentResponse();
        firstFrame.setConversationId(state.getConversationId() != null ? state.getConversationId() : "");
        firstFrame.setMessageId(state.getMessageId() != null ? state.getMessageId() : "");
        firstFrame.setData(dataItem);
        firstFrame.setFinished(Boolean.FALSE);
        firstFrame.setTimestamp(timestamp);
        
        // 第二帧：finished=true
        SAAgentResponse secondFrame = new SAAgentResponse();
        secondFrame.setConversationId(state.getConversationId() != null ? state.getConversationId() : "");
        secondFrame.setMessageId(state.getMessageId() != null ? state.getMessageId() : "");
        secondFrame.setData(dataItem);
        secondFrame.setFinished(Boolean.TRUE);
        secondFrame.setTimestamp(timestamp);
        
        try {
            // 发送第一帧
            String firstFrameJson = objectMapper.writeValueAsString(firstFrame);
            logger.info("发送错误兜底响应（第一帧）: {}", firstFrameJson);
            emitter.send(firstFrame);
            
            // 发送第二帧
            String secondFrameJson = objectMapper.writeValueAsString(secondFrame);
            logger.info("发送错误兜底响应（第二帧）: {}", secondFrameJson);
            emitter.send(secondFrame);
            
            // 完成SSE连接
            emitter.complete();
            isCompleted = true;
        } catch (Exception e) {
            logger.error("发送错误兜底响应失败", e);
            try {
                emitter.completeWithError(e);
                isCompleted = true;
            } catch (Exception ex) {
                logger.error("完成SSE连接失败", ex);
            }
        }
    }
}

