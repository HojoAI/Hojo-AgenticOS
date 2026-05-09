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
import com.hojoai.omniasst.common.api.conversation.ConversationAddMessageRequest;
import com.hojoai.omniasst.sysagent.common.ServiceDiscoveryUtil;
import com.hojoai.omniasst.sysagent.constans.ConversationConstant;
import com.hojoai.omniasst.sysagent.interaction.sse.SseClient;
import com.hojoai.omniasst.sysagent.service.conversation.ConversationUtils;
import com.hojoai.omniasst.sysagent.service.conversation.bean.ConversationResponse;
import com.hojoai.omniasst.sysagent.service.conversation.manager.SseEmitterManager;
import com.hojoai.routing.engine.AgentService;
import com.hojoai.routing.engine.ConversationContext;
import com.hojoai.routing.engine.TaskSequence;
import com.hojoai.routing.engine.UserInput;
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
 * 天气智能体：发现 {@code omniasst-weather} 并调用其对话接口。
 */
@Service
public class WeatherAgentServiceImpl extends AgentServiceImpl {
    private static final Logger logger = LoggerFactory.getLogger(WeatherAgentServiceImpl.class);

    /** 天气微服务注册名。 */
    private static final String WEATHER_SERVICE_NAME = "hojoai-webmvc-omniasst-weather";
    /** 天气 agent 路径。 */
    private static final String WEATHER_API_PATH = "/omniasst-weather/weather-agent/chat";
    
    @Resource
    private ServiceDiscoveryUtil serviceDiscoveryUtil;
    
    @Resource
    private SseClient sseClient;
    
    @Resource
    private SseEmitterManager sseEmitterManager;
    
    @Resource
    private ConversationUtils conversationUtils;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    public WeatherAgentServiceImpl() {
        this.agentName = ConversationConstant.AGENT_WEATHER;
    }

    @Override
    protected String getAgentChineseName() {
        return ConversationConstant.AGENT_WEATHER_CN;
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
            String weatherServiceUrl = getWeatherServiceUrl();
            if (StringUtils.isEmpty(weatherServiceUrl)) {
                logger.error("Weather service not found in Nacos");
                throw new RuntimeException("Weather service unavailable");
            }
            
            logger.info("[{}] Weather service URL: {}", conversationId, weatherServiceUrl);
            
            // 2. 构建请求参数
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("userId", userId);
            requestBody.put("query", taskSequence.getTask());
            Map<String, String> inputs = new HashMap<>();
            String location = buildLocationFromInputs(context.getInput());
            if (StringUtils.isNotEmpty(location)){
                inputs.put("location", location);
                requestBody.put("inputs", inputs);
            }

            logger.info("[{}] Call Weather agent with params: {}", conversationId, requestBody);

            Map<String, StringBuilder> answerBuilderMap = new HashMap<>();
            Map<String, String> contentIdMap = new HashMap<>();
            AtomicReference<String> contentId = new AtomicReference<>("");
            
            sseClient.sendRequest(weatherServiceUrl, requestBody, data -> {
                try {
                    if (sseEmitterManager.shouldStop(conversationId, messageId)) {
                        return;
                    }
                    
                    String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new java.util.Date());
                    logger.info("[{}, {}] Weather agent response data: {}, time: {}", conversationId, messageId, data, timestamp);
                    
                    JsonNode jsonData = objectMapper.readTree(data);
                    JsonNode dataNode = jsonData.get("data");
                    JsonNode finishedNode = jsonData.get("finished");
                    boolean finished = finishedNode != null && finishedNode.asBoolean();
                    
                    if (dataNode != null && !finished) {
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
                                buildAnswerBuilderMapFromArray(dataNode, answerBuilderMap, taskSequence);
                            } else if (dataNode.isObject()) {
                                buildAnswerBuilderMapFromObject(dataNode, answerBuilderMap, taskSequence);
                            }
                        }
                    }
                } catch (Exception e) {
                    logger.error("Failed to parse Weather SSE response data: " + data, e);
                }
            });
            
            if (!answerBuilderMap.isEmpty()) {
                dataHandler.accept(buildManagementConversationMessage(conversationId, messageId, answerBuilderMap, contentIdMap, context.getUserId()));
            }
        } catch (Exception e) {
            logger.error("Error occurred during Weather agent call", e);
            throw new RuntimeException("Error occurred during Weather agent call", e);
        }
    }

    private String getWeatherServiceUrl() {
        String baseUrl = serviceDiscoveryUtil.getServiceBaseUrl(WEATHER_SERVICE_NAME);
        if (baseUrl == null) {
            return null;
        }
        return baseUrl + WEATHER_API_PATH;
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
            
            String subType = item.has("subType") ? item.get("subType").asText("") : "";
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
        
        String subType = dataNode.has("subType") ? dataNode.get("subType").asText("") : "";
        JsonNode textNode = dataNode.get("text");
        if (textNode != null && StringUtils.isNotEmpty(textNode.asText())) {
            String text = textNode.asText();
            appendManagementTextWithBubbleSpeechDuplicateIfNeeded(taskSequence, answerBuilderMap, subType, text);
        }
    }

    private String buildLocationFromInputs(UserInput inputs) {
        if (inputs == null) {
            return null;
        }

        // 如果没有 location，则从 user_longitude 和 user_latitude 拼接
        String longitude = inputs.getLongitude();
        String latitude = inputs.getLatitude();

        if (StringUtils.isNotEmpty(longitude) && StringUtils.isNotEmpty(latitude)) {
            return longitude + "," + latitude;
        }

        return null;
    }
}
