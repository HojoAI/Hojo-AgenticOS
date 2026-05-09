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
package com.hojoai.omniasst.sysagent.service.conversation.intention.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hojoai.omniasst.sysagent.common.ServiceDiscoveryUtil;
import com.hojoai.omniasst.sysagent.constans.ConversationConstant;
import com.hojoai.omniasst.sysagent.interaction.sse.SseClient;
import com.hojoai.omniasst.sysagent.service.conversation.ConversationUtils;
import com.hojoai.omniasst.sysagent.service.conversation.bean.ConversationResponse;
import com.hojoai.omniasst.common.api.conversation.ConversationAddMessageRequest;
import com.hojoai.routing.engine.ConversationContext;
import com.hojoai.routing.engine.TaskSequence;
import com.hojoai.omniasst.sysagent.service.conversation.manager.SseEmitterManager;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

/**
 * 音乐智能体：发现 {@code omniasst-music} 并调用其查询 SSE 接口。
 */
@Service
public class MusicAgentServiceImpl extends AgentServiceImpl {
    private static final Logger logger = LoggerFactory.getLogger(MusicAgentServiceImpl.class);

    /** 音乐微服务注册名。 */
    private static final String MUSIC_SERVICE_NAME = "hojoai-webmvc-omniasst-music";
    /** 音乐 agent HTTP 路径。 */
    private static final String MUSIC_API_PATH = "/omniasst-music/music-agent/query";
    
    @Resource
    private ServiceDiscoveryUtil serviceDiscoveryUtil;
    
    @Resource
    private SseClient sseClient;
    
    @Resource
    private SseEmitterManager sseEmitterManager;
    
    @Resource
    private ConversationUtils conversationUtils;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    public MusicAgentServiceImpl() {
        this.agentName = ConversationConstant.AGENT_MUSIC;
    }

    @Override
    protected String getAgentChineseName() {
        return ConversationConstant.AGENT_MUSIC_CN;
    }
    
    @Override
    public void callAgent(ConversationContext context, TaskSequence taskSequence,
                          SseEmitter emitter, Consumer<List<ConversationAddMessageRequest>> dataHandler) {
        String conversationId = context.getConversationId();
        String messageId = context.getMessageId();
        String userId = context.getUserId();
        
        sendAgentLoadingEvent(emitter);
        
        try {
            // 1. 从 Nacos 获取服务地址
            String musicServiceUrl = getMusicServiceUrl();
            if (StringUtils.isEmpty(musicServiceUrl)) {
                logger.error("Music service not found in Nacos");
                throw new RuntimeException("Music service unavailable");
            }
            
            logger.info("[{}] Music service URL: {}", conversationId, musicServiceUrl);
            
            // 2. 构建请求参数
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("userId", userId);
            requestBody.put("query", taskSequence.getTask());
            requestBody.put("input", context.getInput());
            
            logger.info("[{}] Call Music agent with params: {}", conversationId, requestBody);

            Map<String, StringBuilder> answerBuilderMap = new HashMap<>();
            Map<String, String> contentIdMap = new HashMap<>();
            AtomicReference<String> contentId = new AtomicReference<>("");
            
            sseClient.sendRequest(musicServiceUrl, requestBody, data -> {
                try {
                    if (sseEmitterManager.shouldStop(conversationId, messageId)) {
                        return;
                    }
                    
                    String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new java.util.Date());
                    logger.info("[{}, {}] Music agent response data: {}, time: {}", conversationId, messageId, data, timestamp);
                    
                    JsonNode jsonData = objectMapper.readTree(data);
                    JsonNode dataNode = jsonData.get("data");
                    JsonNode finishedNode = jsonData.get("finished");
                    boolean finished = finishedNode != null && finishedNode.asBoolean();
                    
                    if (dataNode != null && !finished) {
                        // 单个数据对象处理（流式响应）
                        JsonNode answerNode = dataNode.get("text");
                        if (answerNode != null && StringUtils.isNotEmpty(answerNode.asText())) {
                            String subType = dataNode.has("subType") ? dataNode.get("subType").asText("") : "";
                            String answer = answerNode.asText();
                            emitTextChunkWithBubbleSpeechDuplicateIfNeeded(
                                    taskSequence, conversationId, messageId, contentIdMap, contentId,
                                    subType, answer, finished, emitter, null);
                        }
                    }
                    
                    if (finished) {
                        ConversationResponse conversationResponse = conversationUtils.buildConversationTextResponse(
                                conversationId, messageId, contentId.get(), "", "", finished);
                        send(emitter, ConversationConstant.EVENT_NAME_MESSAGE, conversationResponse);
                        
                        // finished 时，按 subType 分类收集 text
                        if (dataNode != null) {
                            if (dataNode.isArray()) {
                                // 数组：遍历数组按 subType 分类收集
                                buildAnswerBuilderMapFromArray(dataNode, answerBuilderMap, taskSequence);
                            } else if (dataNode.isObject()) {
                                // 对象：直接提取 subType 和 text 收集
                                buildAnswerBuilderMapFromObject(dataNode, answerBuilderMap, taskSequence);
                            }
                        }
                    }
                } catch (Exception e) {
                    logger.error("Failed to parse Music SSE response data: " + data, e);
                }
            });
            
            if (!answerBuilderMap.isEmpty()) {
                dataHandler.accept(buildManagementConversationMessage(conversationId, messageId, answerBuilderMap, contentIdMap, userId));
            }
        } catch (Exception e) {
            logger.error("Error occurred during Music agent call", e);
            throw new RuntimeException("Error occurred during Music agent call", e);
        }
    }

    private String getMusicServiceUrl() {
        String baseUrl = serviceDiscoveryUtil.getServiceBaseUrl(MUSIC_SERVICE_NAME);
        if (baseUrl == null) {
            return null;
        }
        return baseUrl + MUSIC_API_PATH;
    }
    
    private void buildAnswerBuilderMapFromArray(JsonNode dataNode, Map<String, StringBuilder> answerBuilderMap,
            TaskSequence taskSequence) {
        if (dataNode == null || !dataNode.isArray()) {
            return;
        }
        
        for (JsonNode item : dataNode) {
            if (item == null || !item.isObject()) {
                continue;
            }
            
            // 获取 subType，如果没有则使用空字符串作为默认分类
            String subType = item.has("subType") ? item.get("subType").asText("") : "";
            
            // 获取 text 内容
            JsonNode textNode = item.get("text");
            if (textNode != null && StringUtils.isNotEmpty(textNode.asText())) {
                String text = textNode.asText();
                appendManagementTextWithBubbleSpeechDuplicateIfNeeded(taskSequence, answerBuilderMap, subType, text);
            }
        }
    }
    
    private void buildAnswerBuilderMapFromObject(JsonNode dataNode, Map<String, StringBuilder> answerBuilderMap,
            TaskSequence taskSequence) {
        if (dataNode == null || !dataNode.isObject()) {
            return;
        }
        
        // 获取 subType，如果没有则使用空字符串作为默认分类
        String subType = dataNode.has("subType") ? dataNode.get("subType").asText("") : "";
        
        // 获取 text 内容
        JsonNode textNode = dataNode.get("text");
        if (textNode != null && StringUtils.isNotEmpty(textNode.asText())) {
            String text = textNode.asText();
            appendManagementTextWithBubbleSpeechDuplicateIfNeeded(taskSequence, answerBuilderMap, subType, text);
        }
    }
}
