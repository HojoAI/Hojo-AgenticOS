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
import com.hojoai.omniasst.weather.entity.DifyChatRequest;
import com.hojoai.omniasst.weather.entity.DifyStreamEvent;
import com.hojoai.omniasst.weather.entity.SAAgentResponse;
import jakarta.annotation.Resource;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Dify服务
 * 
 * @author wangjian
 * @since 2026-01-20
 */
@Service
public class DifyService {
    
    private static final Logger logger = LoggerFactory.getLogger(DifyService.class);
    
    @Resource
    private ObjectMapper objectMapper;
    
    @Resource
    private HttpClientManager httpClientManager;
    
    @Value("${dify.api.url}")
    private String difyApiUrl;
    
    @Value("${dify.api.token}")
    private String difyApiToken;
    
    /**
     * BufferedReader缓冲区大小（字符），用于流式读取优化
     * 默认16KB字符缓冲区，约等于32KB字节（UTF-8）
     */
    private static final int READER_BUFFER_SIZE = 16 * 1024;

    /**
     * 调用 Dify 天气智能体，并通过 SseEmitter 将结果以流式方式返回。
     *
     * @param userId          用户ID
     * @param query           查询内容
     * @param conversationId  会话ID（可选，用于多轮对话）
     * @param inputs          输入参数（可选，用于传递额外参数，如 location）
     * @param emitter         SSE 发射器；异步在后台线程写入，异常时发送兜底帧并完成连接
     */
    public void callWeatherAgent(String userId, String query, String conversationId, Map<String, Object> inputs, SseEmitter emitter) {
        // 保存当前线程的MDC上下文，以便在异步线程中使用
        Map<String, String> contextMap = MDC.getCopyOfContextMap();
        
        CompletableFuture.runAsync(() -> {
            try {
                // 在异步线程中恢复MDC上下文，确保traceId能够传递
                if (contextMap != null) {
                    MDC.setContextMap(contextMap);
                }
                doCallWeatherAgent(userId, query, conversationId, inputs, emitter);
            } finally {
                // 异步任务完成后清除MDC，避免内存泄漏
                MDC.clear();
            }
        });
    }

    /**
     * 执行实际的 Dify API 调用
     *
     * @param userId          用户ID
     * @param query           查询内容
     * @param conversationId  会话ID（可选，用于多轮对话）
     * @param inputs          输入参数（可选，用于传递额外参数，如 location）
     * @param emitter         SSE 发射器
     */
    private void doCallWeatherAgent(String userId, String query, String conversationId, Map<String, Object> inputs, SseEmitter emitter) {
        long startTime = System.currentTimeMillis();
        String url = buildApiUrl();
        
        try {
            logger.info("调用 Dify 接口开始, url: {}, userId: {}, query: {}, conversationId: {}", url, userId, query, conversationId);
            
            // 使用共享的HTTP客户端（连接池），避免每次创建新客户端
            CloseableHttpClient httpClient = httpClientManager.getHttpClient();
            HttpPost httpPost = createHttpPostRequest(url, userId, query, conversationId, inputs);
            executeHttpRequest(httpClient, httpPost, emitter);
            
            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;
            logger.info("调用 Dify 接口完成, url: {}, userId: {}, 耗时: {}ms", url, userId, duration);
            
        } catch (Exception e) {
            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;
            logger.error("调用 Dify 接口异常, url: {}, userId: {}, 耗时: {}ms", url, userId, duration, e);
            sendErrorFallbackResponse(emitter);
        }
    }

    /**
     * 构建 API 请求 URL
     *
     * @return 完整的 API URL
     */
    private String buildApiUrl() {
        String url = difyApiUrl;
        if (!url.endsWith("/")) {
            url = url + "/";
        }
        return url + "chat-messages";
    }

    /**
     * 创建 HTTP POST 请求对象
     *
     * @param url            API URL
     * @param userId         用户ID
     * @param query          查询内容
     * @param conversationId 会话ID（可选，用于多轮对话）
     * @param inputs         输入参数（可选，用于传递额外参数，如 location）
     * @return HttpPost 请求对象
     */
    private HttpPost createHttpPostRequest(String url, String userId, String query, String conversationId, Map<String, Object> inputs) {
        HttpPost httpPost = new HttpPost(url);
        httpPost.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + difyApiToken);
        httpPost.setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE + "; charset=UTF-8");

        DifyChatRequest request = buildDifyChatRequest(userId, query, conversationId, inputs);
        try {
            String body = objectMapper.writeValueAsString(request);
            httpPost.setEntity(new StringEntity(body, StandardCharsets.UTF_8));
        } catch (Exception e) {
            logger.error("构建请求体失败", e);
            throw new RuntimeException("构建请求体失败", e);
        }

        return httpPost;
    }

    /**
     * 执行 HTTP 请求并处理响应
     *
     * @param httpClient HTTP 客户端
     * @param httpPost   HTTP POST 请求
     * @param emitter    SSE 发射器
     */
    private void executeHttpRequest(CloseableHttpClient httpClient, HttpPost httpPost, SseEmitter emitter) {
        ClassicHttpResponse response = null;
        try {
            response = httpClient.executeOpen(null, httpPost, null);
            handleHttpResponse(response, emitter);
        } catch (Exception e) {
            logger.error("执行 HTTP 请求失败", e);
            sendErrorFallbackResponse(emitter);
        } finally {
            if (response != null && response.getEntity() != null) {
                EntityUtils.consumeQuietly(response.getEntity());
            }
        }
    }

    /**
     * 处理 HTTP 响应
     *
     * @param response HTTP 响应
     * @param emitter  SSE 发射器
     */
    private void handleHttpResponse(ClassicHttpResponse response, SseEmitter emitter) {
        int status = response.getCode();
        if (status < 200 || status >= 300) {
            handleErrorResponse(response, status, emitter);
            return;
        }

        try (InputStream inputStream = response.getEntity().getContent()) {
            processStreamResponse(inputStream, emitter);
        } catch (Exception e) {
            logger.error("读取流式响应失败", e);
            sendErrorFallbackResponse(emitter);
        }
    }

    /**
     * 处理错误响应
     *
     * @param response HTTP 响应
     * @param status   HTTP 状态码
     * @param emitter  SSE 发射器
     */
    private void handleErrorResponse(ClassicHttpResponse response, int status, SseEmitter emitter) {
        String errorBody = "";
        try {
            if (response.getEntity() != null) {
                errorBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
            }
        } catch (Exception e) {
            logger.warn("读取错误响应体失败", e);
        }
        
        logger.error("调用 Dify 接口失败, status: {}, body: {}", status, errorBody);
        sendErrorFallbackResponse(emitter);
    }

    /**
     * 处理流式响应数据
     *
     * @param inputStream 输入流
     * @param emitter     SSE 发射器
     */
    private void processStreamResponse(InputStream inputStream, SseEmitter emitter) {
        // 使用自定义缓冲区大小的BufferedReader，优化流式读取性能
        // READER_BUFFER_SIZE设置为16KB字符，约等于32KB字节（UTF-8编码）
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(inputStream, StandardCharsets.UTF_8), 
                READER_BUFFER_SIZE)) {
            AnswerStreamProcessor processor = new AnswerStreamProcessor(objectMapper, emitter);
            String line;
            
            while ((line = reader.readLine()) != null) {
                if (isValidSseLine(line)) {
                    parseAndProcessEvent(line, processor);
                }
            }
        } catch (Exception e) {
            logger.error("处理流式响应失败", e);
            sendErrorFallbackResponse(emitter);
        }
    }

    /**
     * 判断是否为有效的 SSE 数据行
     *
     * @param line 读取的行
     * @return 是否为有效的 SSE 数据行
     */
    private boolean isValidSseLine(String line) {
        if (line == null || line.isEmpty()) {
            return false;
        }
        if (!line.startsWith("data:")) {
            return false;
        }
        String json = line.substring("data:".length()).trim();
        return !json.isEmpty() && !"[DONE]".equals(json);
    }

    /**
     * 解析并处理 SSE 事件
     *
     * @param line      SSE 数据行
     * @param processor 流式响应处理器
     */
    private void parseAndProcessEvent(String line, AnswerStreamProcessor processor) {
        String json = line.substring("data:".length()).trim();
        try {
            DifyStreamEvent event = objectMapper.readValue(json, DifyStreamEvent.class);
            processor.handleEvent(event);
        } catch (Exception e) {
            logger.error("解析 Dify 流式事件失败, json: {}", json, e);
            // 如果解析失败，检查是否是 message_end 事件
            if (json.contains("message_end")) {
                logger.error("重要：检测到 message_end 事件但解析失败！原始数据: {}", json);
            }
        }
    }

    /**
     * 构建 Dify 聊天请求对象
     *
     * @param userId          用户ID
     * @param query           查询内容
     * @param conversationId  会话ID（可选，用于多轮对话）
     * @param inputs          输入参数（可选，用于传递额外参数，如 location）
     * @return DifyChatRequest
     */
    private DifyChatRequest buildDifyChatRequest(String userId, String query, String conversationId, Map<String, Object> inputs) {
        DifyChatRequest request = new DifyChatRequest();
        
        // 从 inputs 中提取 location 字段，如果有值则设置到 DifyChatRequest 的 inputs 中
        Map<String, Object> difyInputs = Collections.emptyMap();
        if (inputs != null && inputs.containsKey("location")) {
            Object location = inputs.get("location");
            if (location != null && !location.toString().trim().isEmpty()) {
                difyInputs = Collections.singletonMap("location", location);
            }
        }
        request.setInputs(difyInputs);
        
        request.setQuery(query);
        request.setResponse_mode("streaming");
        // 如果 conversationId 存在且不为空，则设置到请求中，否则设置为空字符串
        request.setConversation_id(conversationId != null && !conversationId.trim().isEmpty() ? conversationId : "");
        request.setUser(userId);
        request.setFiles(null);
        return request;
    }
    
    /**
     * 发送错误兜底响应（两帧数据）
     * 第一帧：finished=false
     * 第二帧：finished=true
     *
     * @param emitter SSE 发射器
     */
    private void sendErrorFallbackResponse(SseEmitter emitter) {
        String errorMessage = "抱歉啊...出于某些原因，这个问题现在我没法回答你，也许你可以换个问法试试？";
        long timestamp = System.currentTimeMillis() / 1000;
        
        // 创建数据项
        SAAgentResponse.SAAgentDataItem dataItem = new SAAgentResponse.SAAgentDataItem();
        dataItem.setType("text");
        dataItem.setSubType("bubble");
        dataItem.setText(errorMessage);
        
        // 第一帧：finished=false
        SAAgentResponse firstFrame = new SAAgentResponse();
        firstFrame.setConversationId("");
        firstFrame.setMessageId("");
        firstFrame.setData(dataItem);
        firstFrame.setFinished(Boolean.FALSE);
        firstFrame.setTimestamp(timestamp);
        
        // 第二帧：finished=true
        SAAgentResponse secondFrame = new SAAgentResponse();
        secondFrame.setConversationId("");
        secondFrame.setMessageId("");
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
        } catch (Exception e) {
            logger.error("发送错误兜底响应失败", e);
            try {
                emitter.completeWithError(e);
            } catch (Exception ex) {
                logger.error("完成SSE连接失败", ex);
            }
        }
    }
}
