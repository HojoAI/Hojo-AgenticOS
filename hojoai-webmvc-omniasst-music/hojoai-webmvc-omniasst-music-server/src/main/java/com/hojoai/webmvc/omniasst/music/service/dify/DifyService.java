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
package com.hojoai.webmvc.omniasst.music.service.dify;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hojoai.webmvc.omniasst.music.service.dify.dto.DifyWorkflowEvent;
import com.hojoai.webmvc.omniasst.music.service.dify.dto.DifyRequest;
import com.hojoai.webmvc.omniasst.music.service.dify.dto.SAAgentResponse;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import jakarta.annotation.Resource;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Dify服务
 * 
 * @author shengxianfei
 * @since 2026-01-20
 */
@Service
public class DifyService {
    
    private static final Logger logger = LoggerFactory.getLogger(DifyService.class);
    
    @Resource
    private ObjectMapper objectMapper;
    
    @Value("${dify.api.url}")
    private String difyApiUrl;
    
    @Value("${dify.api.token}")
    private String difyApiToken;
    
    private final ExecutorService executorService = Executors.newCachedThreadPool();
    
    /**
     * 调用Dify音乐智能体并流式返回给SA
     * <p>在缓存线程池中异步执行：请求 Dify Workflow（SSE），按 text_chunk 增量解析 bubble/card，
     * 映射为 {@link SAAgentResponse} 推送；收到 workflow_finished 后发结束帧；整体失败时下发兜底内容。
     * 
     * @param userId 用户ID
     * @param query 查询内容
     * @param emitter SSE发射器，用于向客户端推送事件
     */
    public void callDifyMusicAgent(String userId, String query, SseEmitter emitter) {
        executorService.execute(() -> {
            CloseableHttpClient httpClient = null;
            // 将 workflowRunId 和 taskId 声明提到外层，以便在 catch 块中使用
            String workflowRunId = null;
            String taskId = null;
            
            try {
                logger.info("开始调用Dify音乐智能体: userId={}, query={}", userId, query);
                
                // 构建Dify请求
                DifyRequest difyRequest = new DifyRequest(userId, query);
                String requestBody = objectMapper.writeValueAsString(difyRequest);
                
                logger.debug("Dify请求体: {}", requestBody);
                
                // 创建HTTP客户端
                RequestConfig requestConfig = RequestConfig.custom()
                        .setConnectTimeout(30000)
                        .setSocketTimeout(60000)
                        .build();
                
                httpClient = HttpClients.custom()
                        .setDefaultRequestConfig(requestConfig)
                        .build();
                
                // 创建POST请求
                HttpPost httpPost = new HttpPost(difyApiUrl);
                httpPost.setHeader("Content-Type", "application/json");
                httpPost.setHeader("Authorization", "Bearer " + difyApiToken);
                httpPost.setEntity(new StringEntity(requestBody, StandardCharsets.UTF_8));
                
                // 执行请求
                HttpResponse response = httpClient.execute(httpPost);
                int statusCode = response.getStatusLine().getStatusCode();
                
                logger.info("Dify响应状态码: {}", statusCode);
                
                if (statusCode != 200) {
                    String errorBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
                    logger.error("Dify请求失败: statusCode={}, body={}", statusCode, errorBody);
                    emitter.completeWithError(new RuntimeException("Dify请求失败: " + statusCode));
                    return;
                }
                
                // 读取SSE流
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(response.getEntity().getContent(), StandardCharsets.UTF_8));
                
                String line;
                StringBuilder fullText = new StringBuilder();
                
                // 状态机：跟踪当前处理状态
                enum State {
                    WAITING_BUBBLE,      // 等待bubble的text字段
                    EXTRACTING_BUBBLE,   // 正在提取bubble text
                    WAITING_CARD,        // 等待card的text字段
                    EXTRACTING_CARD      // 正在提取card text
                }
                State currentState = State.WAITING_BUBBLE;
                StringBuilder bubbleText = new StringBuilder();
                StringBuilder cardText = new StringBuilder();
                String lastSentBubble = "";
                String lastSentCard = "";
                boolean hasContentToSend = false;
                int textChunkCount = 0;
                
                while ((line = reader.readLine()) != null) {
                    // SSE格式: data: {...}
                    if (line.startsWith("data: ")) {
                        String jsonData = line.substring(6).trim();
                        
                        if (jsonData.isEmpty()) {
                            continue;
                        }
                        
                        logger.debug("收到Dify SSE消息: {}", jsonData);
                        
                        try {
                            DifyWorkflowEvent event = objectMapper.readValue(jsonData, DifyWorkflowEvent.class);
                            
                            // 保存workflowRunId和taskId
                            if (event.getWorkflowRunId() != null) {
                                workflowRunId = event.getWorkflowRunId();
                            }
                            if (event.getTaskId() != null) {
                                taskId = event.getTaskId();
                            }
                            
                            // 处理text_chunk事件 - 流式返回内容
                            if ("text_chunk".equals(event.getEvent()) && 
                                event.getData() != null && 
                                event.getData().getText() != null) {
                                
                                textChunkCount++;
                                String textChunk = event.getData().getText();
                                
                                // 累积所有内容（包括空白），以保证JSON完整性
                                fullText.append(textChunk);
                                String accumulated = fullText.toString();
                                
                                // 每10个chunk打印一次进度
                                if (textChunkCount % 10 == 0) {
                                    logger.info(" 已处理 {} 个text_chunk，当前状态: {}, 累积长度: {}", 
                                        textChunkCount, currentState, accumulated.length());
                                }
                                
                                // 状态机处理
                                if (currentState == State.WAITING_BUBBLE) {
                                    // 同时检测bubble和card（支持格式化JSON，冒号后可能有空格）
                                    if (accumulated.contains("\"subType\"") &&
                                        (accumulated.contains("\"bubble\"") || accumulated.contains(": \"bubble\""))) {
                                        currentState = State.EXTRACTING_BUBBLE;
                                        logger.info("检测到bubble，开始提取（累积长度: {}）", accumulated.length());
                                    } else if (accumulated.contains("\"subType\"") &&
                                               (accumulated.contains("\"card\"") || accumulated.contains(": \"card\""))) {
                                        // 如果没有bubble但有card，直接开始处理card
                                        currentState = State.EXTRACTING_CARD;
                                        logger.info("检测到card（跳过bubble），开始提取（累积长度: {}）", accumulated.length());
                                    }
                                }
                                
                                if (currentState == State.EXTRACTING_BUBBLE) {
                                    // 尝试提取bubble的text内容
                                    String extracted = extractTextFieldContent(accumulated, "bubble");
                                    if (extracted != null) {
                                        logger.debug("提取到bubble内容（长度: {}），上次长度: {}", extracted.length(), lastSentBubble.length());
                                        // 计算增量部分（新增的内容）
                                        if (!extracted.equals(lastSentBubble)) {
                                            // 提取增量：extracted比lastSentBubble新增的部分
                                            String incrementalText = extracted.substring(lastSentBubble.length());
                                            
                                            if (!incrementalText.isEmpty()) {
                                                SAAgentResponse bubbleResponse = buildStreamingBubbleResponse(
                                                        workflowRunId, taskId, incrementalText, false);
                                                String sseData = objectMapper.writeValueAsString(bubbleResponse) + "\n\n";
                                                emitter.send(SseEmitter.event().data(sseData));
                                                
                                                hasContentToSend = true;
                                                logger.info("流式发送bubble增量（增量长度: {}, 累计: {}, 内容: {}）", 
                                                    incrementalText.length(), extracted.length(),
                                                    incrementalText.length() > 30 ? incrementalText.substring(0, 30) + "..." : incrementalText);
                                            }
                                            
                                            lastSentBubble = extracted;
                                        }
                                        
                                        // 检测bubble是否完成
                                        if (isBubbleComplete(accumulated)) {
                                            currentState = State.WAITING_CARD;
                                            logger.info("bubble文本提取完成，等待card");
                                        }
                                    }
                                }
                                
                                if (currentState == State.WAITING_CARD) {
                                    // 检测card（支持格式化JSON，冒号后可能有空格）
                                    if (accumulated.contains("\"subType\"") && 
                                        (accumulated.contains("\"card\"") || accumulated.contains(": \"card\""))) {
                                        currentState = State.EXTRACTING_CARD;
                                        logger.info("检测到card，开始提取（累积长度: {}）", accumulated.length());
                                    }
                                }
                                
                                if (currentState == State.EXTRACTING_CARD) {
                                    // 尝试提取card的text内容
                                    String extracted = extractTextFieldContent(accumulated, "card");
                                    if (extracted != null) {
                                        logger.debug("提取到card内容（长度: {}），上次长度: {}", extracted.length(), lastSentCard.length());
                                        // 计算增量部分（新增的内容）
                                        if (!extracted.equals(lastSentCard)) {
                                            // 提取增量：extracted比lastSentCard新增的部分
                                            String incrementalText = extracted.substring(lastSentCard.length());
                                            
                                            if (!incrementalText.isEmpty()) {
                                                // 只发送card的增量，不包含bubble
                                                SAAgentResponse cardResponse = buildStreamingCardResponse(
                                                        workflowRunId, taskId, incrementalText, false);
                                                String sseData = objectMapper.writeValueAsString(cardResponse) + "\n\n";
                                                emitter.send(SseEmitter.event().data(sseData));
                                                
                                                hasContentToSend = true;
                                                logger.info("流式发送card增量（增量长度: {}, 累计: {}, 内容预览: {}...）", 
                                                    incrementalText.length(), extracted.length(),
                                                    incrementalText.length() > 50 ? incrementalText.substring(0, 50) : incrementalText);
                                            }
                                            
                                            lastSentCard = extracted;
                                        }
                                    }
                                }
                            }
                            
                            // 处理workflow_finished事件 - 发送最终结果
                            if ("workflow_finished".equals(event.getEvent())) {
                                logger.info("收到workflow_finished事件");
                                
                                if (hasContentToSend) {
                                    // 已经流式发送过内容，最后发送一个完整的bubble+card（finished=true）
                                    SAAgentResponse finishedResponse;
                                    if (!lastSentBubble.isEmpty() && !lastSentCard.isEmpty()) {
                                        // 如果bubble和card都有，发送完整的bubble+card
                                        finishedResponse = buildStreamingBubbleAndCardResponse(
                                            workflowRunId, taskId, lastSentBubble, lastSentCard, true);
                                        logger.info("发送完整的bubble+card（finished=true）: bubble长度={}, card长度={}", 
                                            lastSentBubble.length(), lastSentCard.length());
                                    } else if (!lastSentBubble.isEmpty()) {
                                        // 只有bubble，发送完整bubble
                                        finishedResponse = buildStreamingBubbleResponse(workflowRunId, taskId, lastSentBubble, true);
                                        logger.info("发送完整的bubble（finished=true）: 长度={}", lastSentBubble.length());
                                    } else if (!lastSentCard.isEmpty()) {
                                        // 只有card，发送完整card
                                        finishedResponse = buildStreamingCardResponse(workflowRunId, taskId, lastSentCard, true);
                                        logger.info("发送完整的card（finished=true）: 长度={}", lastSentCard.length());
                                    } else {
                                        // 理论上不应该到这里
                                        finishedResponse = buildErrorResponse(workflowRunId, taskId, "流式发送完成");
                                        finishedResponse.setFinished(true);
                                        logger.warn("hasContentToSend=true但没有实际内容");
                                    }
                                    
                                    String sseData = objectMapper.writeValueAsString(finishedResponse) + "\n\n";
                                    emitter.send(SseEmitter.event().data(sseData));
                                } else {
                                    // 如果从头到尾都没有流式发送过内容，使用兜底数据进行流式返回
                                    logger.warn("未成功流式发送内容，使用兜底数据");
                                    sendFallbackData(workflowRunId, taskId, emitter);
                                }
                                
                                break;
                            }
                            
                        } catch (Exception e) {
                            logger.warn("解析Dify消息失败: {}", jsonData, e);
                        }
                    }
                }
                
                reader.close();
                
                // 完成SSE
                emitter.complete();
                
            } catch (Exception e) {
                // 出现未捕获的异常（如网络断连、IO异常等），下发兜底数据
                logger.error("调用Dify音乐智能体失败，出现未捕获的异常，准备下发兜底数据。异常类型: {}, 异常信息: {}", 
                    e.getClass().getSimpleName(), e.getMessage(), e);
                
                try {
                    // 尝试下发兜底数据
                    logger.warn("开始下发兜底数据以保证用户体验");
                    sendFallbackData(workflowRunId, taskId, emitter);
                    emitter.complete();
                    logger.info("兜底数据下发成功，SSE连接正常关闭");
                } catch (Exception fallbackException) {
                    // 如果兜底数据下发也失败，记录日志并完成连接
                    logger.error("兜底数据下发失败，无法恢复", fallbackException);
                    emitter.completeWithError(e);
                }
            } finally {
                if (httpClient != null) {
                    try {
                        httpClient.close();
                    } catch (Exception e) {
                        logger.error("关闭HTTP客户端失败", e);
                    }
                }
            }
        });
    }
    
    /**
     * 构建SA响应
     * 
     * @param workflowRunId workflow运行ID
     * @param taskId 任务ID
     * @param textContent 文本内容（JSON数组字符串）
     * @param finished 是否完成
     */
    private SAAgentResponse buildSAResponse(String workflowRunId, String taskId, String textContent, boolean finished) throws Exception {
        SAAgentResponse response = new SAAgentResponse();
        // 使用workflowRunId作为conversationId，taskId作为messageId
        response.setConversationId(workflowRunId != null ? workflowRunId : "");
        response.setMessageId(taskId != null ? taskId : "");
        response.setAvatarId("");
        response.setTimestamp(System.currentTimeMillis());
        response.setFinished(finished);
        
        List<SAAgentResponse.SAAgentDataItem> dataList = new ArrayList<>();
        
        if (textContent != null && !textContent.isEmpty()) {
            // 解析textContent中的JSON数组，如果失败则抛出异常
            List<?> answerList = objectMapper.readValue(textContent, List.class);
            
            for (Object item : answerList) {
                if (item instanceof java.util.Map) {
                    @SuppressWarnings("unchecked")
                    java.util.Map<String, Object> itemMap = (java.util.Map<String, Object>) item;
                    
                    SAAgentResponse.SAAgentDataItem dataItem = new SAAgentResponse.SAAgentDataItem();
                    dataItem.setType((String) itemMap.get("type"));
                    dataItem.setSubType((String) itemMap.get("subType"));
                    dataItem.setText((String) itemMap.get("text"));
                    dataItem.setUrl((String) itemMap.getOrDefault("url", ""));
                    
                    dataList.add(dataItem);
                }
            }
            
            logger.info("成功解析text内容，包含{}个数据项", dataList.size());
        } else {
            // 如果没有找到有效的text内容，返回默认消息
            SAAgentResponse.SAAgentDataItem defaultItem = new SAAgentResponse.SAAgentDataItem();
            defaultItem.setType("text");
            defaultItem.setSubType("bubble");
            defaultItem.setText("未找到相关音乐推荐");
            defaultItem.setUrl("");
            dataList.add(defaultItem);
            
            logger.warn("未找到有效的text内容");
        }
        
        response.setData(dataList);
        
        return response;
    }
    
    /**
     * 构建错误响应
     */
    private SAAgentResponse buildErrorResponse(String workflowRunId, String taskId, String errorMessage) {
        SAAgentResponse response = new SAAgentResponse();
        response.setConversationId(workflowRunId != null ? workflowRunId : "");
        response.setMessageId(taskId != null ? taskId : "");
        response.setAvatarId("");
        response.setTimestamp(System.currentTimeMillis());
        response.setFinished(true);
        
        List<SAAgentResponse.SAAgentDataItem> dataList = new ArrayList<>();
        SAAgentResponse.SAAgentDataItem errorItem = new SAAgentResponse.SAAgentDataItem();
        errorItem.setType("text");
        errorItem.setSubType("bubble");
        errorItem.setText(errorMessage);
        errorItem.setUrl("");
        dataList.add(errorItem);
        
        response.setData(dataList);
        
        return response;
    }
    
    /**
     * 从累积的JSON文本中提取指定类型（bubble或card）的text字段内容
     * 支持流式提取：即使text内容还未完整，也能提取已到达的部分
     */
    private String extractTextFieldContent(String jsonText, String subType) {
        try {
            // 找到对应subType的位置（支持格式化JSON，冒号后可能有空格）
            int subTypeIndex = findSubTypeIndex(jsonText, subType);
            if (subTypeIndex == -1) {
                return null;
            }
            
            // 从subType位置往后查找text字段（支持格式化JSON）
            int textStartIndex = findTextFieldStart(jsonText, subTypeIndex);
            if (textStartIndex == -1) {
                // text字段还未出现，返回null
                return null;
            }
            
            // text内容的起始位置
            int contentStart = textStartIndex;
            if (contentStart >= jsonText.length()) {
                // text字段的引号还未完全出现
                return "";
            }
            
            // 查找text内容的结束位置（需要处理转义的引号）
            StringBuilder rawExtracted = new StringBuilder();
            boolean escaped = false;
            
            for (int i = contentStart; i < jsonText.length(); i++) {
                char c = jsonText.charAt(i);
                
                if (escaped) {
                    // 转义字符：保留原样（暂时）
                    rawExtracted.append('\\').append(c);
                    escaped = false;
                } else if (c == '\\') {
                    // 遇到转义符，标记但不立即添加
                    escaped = true;
                } else if (c == '"') {
                    // 遇到未转义的引号，说明text字段结束
                    break;
                } else {
                    rawExtracted.append(c);
                }
            }
            
            // 反转义JSON转义字符（因为Jackson序列化时会再次转义）
            String rawContent = rawExtracted.toString();
            String unescaped = unescapeJson(rawContent);
            
            return unescaped;
            
        } catch (Exception e) {
            logger.debug("提取{}的text字段失败", subType, e);
            return null;
        }
    }
    
    /**
     * 反转义JSON字符串
     */
    private String unescapeJson(String escaped) {
        if (escaped == null || escaped.isEmpty()) {
            return escaped;
        }
        return escaped
                .replace("\\\"", "\"")
                .replace("\\\\", "\\")
                .replace("\\n", "\n")
                .replace("\\r", "\r")
                .replace("\\t", "\t");
    }
    
    /**
     * 查找subType字段的位置（支持格式化JSON，冒号后可能有空格）
     */
    private int findSubTypeIndex(String jsonText, String subType) {
        // 尝试无空格格式：\"subType\":\"bubble\"
        String pattern1 = "\"subType\":\"" + subType + "\"";
        int index = jsonText.indexOf(pattern1);
        if (index != -1) {
            return index;
        }
        
        // 尝试有空格格式：\"subType\": \"bubble\"
        String pattern2 = "\"subType\": \"" + subType + "\"";
        return jsonText.indexOf(pattern2);
    }
    
    /**
     * 从指定位置往后查找text字段的起始位置（返回引号后的位置）
     * 支持格式化JSON，冒号后可能有空格
     */
    private int findTextFieldStart(String jsonText, int fromIndex) {
        // 查找\"text\":\"模式
        int index1 = jsonText.indexOf("\"text\":\"", fromIndex);
        // 查找\"text\": \"模式（有空格）
        int index2 = jsonText.indexOf("\"text\": \"", fromIndex);
        
        if (index1 == -1 && index2 == -1) {
            return -1;
        }
        
        // 取两者中最先出现的
        int index;
        if (index1 == -1) {
            index = index2;
            return index + 9; // \"text\": \" 长度为9
        } else if (index2 == -1) {
            index = index1;
            return index + 8; // \"text\":\" 长度为8
        } else {
            if (index1 < index2) {
                return index1 + 8;
            } else {
                return index2 + 9;
            }
        }
    }
    
    /**
     * 判断bubble是否已经完成
     */
    private boolean isBubbleComplete(String jsonText) {
        // 查找bubble对象的结束标志
        try {
            int bubbleIndex = findSubTypeIndex(jsonText, "bubble");
            if (bubbleIndex == -1) {
                return false;
            }
            
            // 从bubble位置开始，找到text字段的结束引号后，再找到对象的结束括号
            int textStartIndex = findTextFieldStart(jsonText, bubbleIndex);
            if (textStartIndex == -1) {
                return false;
            }
            
            // textStartIndex已经指向内容起始位置
            int contentStart = textStartIndex;
            boolean escaped = false;
            int textEndIndex = -1;
            
            // 找到text字段的结束引号
            for (int i = contentStart; i < jsonText.length(); i++) {
                char c = jsonText.charAt(i);
                if (escaped) {
                    escaped = false;
                } else if (c == '\\') {
                    escaped = true;
                } else if (c == '"') {
                    textEndIndex = i;
                    break;
                }
            }
            
            if (textEndIndex == -1) {
                return false;
            }
            
            // 检查text字段结束后是否有}或者下一个对象的开始
            String remaining = jsonText.substring(textEndIndex);
            // 支持格式化JSON（可能有空格、换行）
            return remaining.contains("}") && 
                   (remaining.contains(",") || remaining.contains("\"subType\""));
            
        } catch (Exception e) {
            logger.debug("判断bubble是否完成失败", e);
            return false;
        }
    }
    
    /**
     * 构建只包含bubble的流式响应（data是单个对象）
     */
    private SAAgentResponse buildStreamingBubbleResponse(String workflowRunId, String taskId, String bubbleText, boolean finished) {
        SAAgentResponse response = new SAAgentResponse();
        response.setConversationId(workflowRunId != null ? workflowRunId : "");
        response.setMessageId(taskId != null ? taskId : "");
        response.setAvatarId("");
        response.setTimestamp(System.currentTimeMillis());
        response.setFinished(finished);
        
        SAAgentResponse.SAAgentDataItem bubbleItem = new SAAgentResponse.SAAgentDataItem();
        bubbleItem.setType("text");
        bubbleItem.setSubType("bubble");
        bubbleItem.setText(bubbleText);
        bubbleItem.setUrl("");
        
        // 流式响应时data是单个对象
        response.setData(bubbleItem);
        return response;
    }
    
    /**
     * 构建只包含card的流式响应（data是单个对象）
     */
    private SAAgentResponse buildStreamingCardResponse(String workflowRunId, String taskId, String cardText, boolean finished) {
        SAAgentResponse response = new SAAgentResponse();
        response.setConversationId(workflowRunId != null ? workflowRunId : "");
        response.setMessageId(taskId != null ? taskId : "");
        response.setAvatarId("");
        response.setTimestamp(System.currentTimeMillis());
        response.setFinished(finished);
        
        SAAgentResponse.SAAgentDataItem cardItem = new SAAgentResponse.SAAgentDataItem();
        cardItem.setType("text");
        cardItem.setSubType("card");
        cardItem.setText(cardText);
        cardItem.setUrl("");
        
        // 流式响应时data是单个对象
        response.setData(cardItem);
        return response;
    }
    
    /**
     * 构建包含bubble和card的流式响应
     */
    private SAAgentResponse buildStreamingBubbleAndCardResponse(String workflowRunId, String taskId, 
                                                                  String bubbleText, String cardText, boolean finished) {
        SAAgentResponse response = new SAAgentResponse();
        response.setConversationId(workflowRunId != null ? workflowRunId : "");
        response.setMessageId(taskId != null ? taskId : "");
        response.setAvatarId("");
        response.setTimestamp(System.currentTimeMillis());
        response.setFinished(finished);
        
        List<SAAgentResponse.SAAgentDataItem> dataList = new ArrayList<>();
        
        // bubble item
        SAAgentResponse.SAAgentDataItem bubbleItem = new SAAgentResponse.SAAgentDataItem();
        bubbleItem.setType("text");
        bubbleItem.setSubType("bubble");
        bubbleItem.setText(bubbleText);
        bubbleItem.setUrl("");
        dataList.add(bubbleItem);
        
        // card item
        SAAgentResponse.SAAgentDataItem cardItem = new SAAgentResponse.SAAgentDataItem();
        cardItem.setType("text");
        cardItem.setSubType("card");
        cardItem.setText(cardText);
        cardItem.setUrl("");
        dataList.add(cardItem);
        
        response.setData(dataList);
        return response;
    }
    
    /**
     * 发送兜底数据（模拟流式返回）
     */
    private void sendFallbackData(String workflowRunId, String taskId, SseEmitter emitter) {
        try {
            // 兜底数据
            String fallbackBubbleText = "<tag_pout>亲爱的你啊，我懂你的心意。这次未能找到你喜欢的，要不先听听任素汐唱这首亲爱的你，温柔又带着点倔强，像在说：'下次我肯定行'。";
            String fallbackCardText = "{\"name\":\"MusicCard\",\"type\":\"component\",\"data\":{\"songId\":\"CC3C2628609A76867417EB8D74D97624\",\"songName\":\"亲爱的你啊\",\"duration\":\"235750\",\"coverImgUrl\":\"http://p1.music.126.net/s0D6UTU1YUtMcOCtwKHaRA==/109951170776174055.jpg\",\"playUrl\":\"http://iot202.music.126.net/Yml6PWlvdCZjaGFubmVsPWhvam8mbWFyaz1vc19hbmRyY2FyLHF1YWxpdHlfaGlyZXMmc2NlbmU9YW5kcmNhcg/20260122170731/eb65cabc4efe931422a448d747cbac60/jdymusic/obj/wo3DlMOGwrbDjj7DisKw/59522824637/527a/d700/f30d/0ba6bdab897287ef8caad73b7cace8d2.flac\",\"artistName\":\"任素汐\",\"recommendReason\":\"温柔中带着倔强，像在说：'我一直在等你'\"}}";
            
            logger.info("开始发送兜底数据流式返回");
            
            // 1. 流式发送bubble文本（分段发送）
            int bubbleChunkSize = 10; // 每次发送10个字符
            for (int i = 0; i < fallbackBubbleText.length(); i += bubbleChunkSize) {
                int end = Math.min(i + bubbleChunkSize, fallbackBubbleText.length());
                String chunk = fallbackBubbleText.substring(i, end);
                
                SAAgentResponse bubbleResponse = buildStreamingBubbleResponse(workflowRunId, taskId, chunk, false);
                String sseData = objectMapper.writeValueAsString(bubbleResponse) + "\n\n";
                emitter.send(SseEmitter.event().data(sseData));
                
                logger.debug("兜底数据：发送bubble增量（位置: {}-{}, 内容: {}）", i, end, chunk);
                
                // 模拟延迟
                Thread.sleep(50);
            }
            
            logger.info("兜底数据：bubble发送完成");
            
            // 2. 流式发送card文本（分段发送）
            int cardChunkSize = 50; // 每次发送50个字符
            for (int i = 0; i < fallbackCardText.length(); i += cardChunkSize) {
                int end = Math.min(i + cardChunkSize, fallbackCardText.length());
                String chunk = fallbackCardText.substring(i, end);
                
                SAAgentResponse cardResponse = buildStreamingCardResponse(workflowRunId, taskId, chunk, false);
                String sseData = objectMapper.writeValueAsString(cardResponse) + "\n\n";
                emitter.send(SseEmitter.event().data(sseData));
                
                logger.debug("兜底数据：发送card增量（位置: {}-{}）", i, end);
                
                // 模拟延迟
                Thread.sleep(50);
            }
            
            logger.info("兜底数据：card发送完成");
            
            // 3. 最后发送完整的bubble+card（finished=true）
            SAAgentResponse finalResponse = buildStreamingBubbleAndCardResponse(
                workflowRunId, taskId, fallbackBubbleText, fallbackCardText, true);
            String sseData = objectMapper.writeValueAsString(finalResponse) + "\n\n";
            emitter.send(SseEmitter.event().data(sseData));
            
            logger.info("兜底数据：发送完整内容（finished=true）");
            
        } catch (Exception e) {
            logger.error("发送兜底数据失败", e);
            try {
                SAAgentResponse errorResponse = buildErrorResponse(workflowRunId, taskId, "系统繁忙，请稍后重试");
                String sseData = objectMapper.writeValueAsString(errorResponse) + "\n\n";
                emitter.send(SseEmitter.event().data(sseData));
            } catch (Exception ex) {
                logger.error("发送错误响应失败", ex);
            }
        }
    }
}

