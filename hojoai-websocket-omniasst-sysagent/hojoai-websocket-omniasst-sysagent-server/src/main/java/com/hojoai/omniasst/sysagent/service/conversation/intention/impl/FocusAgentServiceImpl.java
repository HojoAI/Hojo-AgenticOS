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
import com.hojoai.routing.engine.AgentService;
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
 * 日程/专注类智能体：经 Nacos 发现 {@code omniasst-focus} 服务并调用其对话 SSE 接口。
 */
@Service
public class FocusAgentServiceImpl extends AgentServiceImpl {
    private static final Logger logger = LoggerFactory.getLogger(FocusAgentServiceImpl.class);

    /** 注册中心中的 focus 微服务名。 */
    private static final String FOCUS_SERVICE_NAME = "hojoai-webmvc-omniasst-focus";
    /** focus 侧日程对话 API 路径前缀。 */
    private static final String FOCUS_API_PATH = "/omniasst-focus/api/v1/schedule/conversation/";
    
    @Resource
    private ServiceDiscoveryUtil serviceDiscoveryUtil;
    
    @Resource
    private SseClient sseClient;
    
    @Resource
    private SseEmitterManager sseEmitterManager;
    
    @Resource
    private ConversationUtils conversationUtils;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    public FocusAgentServiceImpl() {
        this.agentName = ConversationConstant.AGENT_FOCUS;
    }

    @Override
    protected String getAgentChineseName() {
        return ConversationConstant.AGENT_FOCUS_CN;
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
            String focusServiceUrl = getFocusServiceUrl(userId);
            if (StringUtils.isEmpty(focusServiceUrl)) {
                logger.error("Focus service not found in Nacos for userId: {}", userId);
                throw new RuntimeException("Focus service unavailable");
            }
            
            logger.info("[{}] Focus service URL: {}", conversationId, focusServiceUrl);
            
            // 2. 从 args 中提取 focusConversationId
            String focusConversationId = extractFocusConversationId(taskSequence.getArgs());
            
            // 3. 构建请求参数
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("conversationId", conversationId);
            requestBody.put("focusConversationId", focusConversationId != null ? focusConversationId : "");
            requestBody.put("queryStr", taskSequence.getTask());
            requestBody.put("input", context.getInput());
            requestBody.put("headphone", taskSequence.isHeadphone());
            
            logger.info("[{}] Call Focus agent with params: {}", conversationId, requestBody);

            Map<String, StringBuilder> answerBuilderMap = new HashMap<>();
            Map<String, String> contentIdMap = new HashMap<>();
            AtomicReference<String> contentId = new AtomicReference<>("");
            
            sseClient.sendRequest(focusServiceUrl, requestBody, data -> {
                try {
                    if (sseEmitterManager.shouldStop(conversationId, messageId)) {
                        return;
                    }
                    
                    String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new java.util.Date());
                    logger.info("[{}, {}] Focus agent response data: {}, time: {}", conversationId, messageId, data, timestamp);
                    
                    JsonNode jsonData = objectMapper.readTree(data);
                    JsonNode dataNode = jsonData.get("data");
                    JsonNode finishedNode = jsonData.get("finished");
                    boolean finished = finishedNode != null && finishedNode.asBoolean();

                    if (dataNode != null) {
                        JsonNode answerNode = dataNode.get("text");
                        if (answerNode != null && StringUtils.isNotEmpty(answerNode.asText())) {
                            String subType = dataNode.has("subType") ? dataNode.get("subType").asText("") : "";
                            String answer = answerNode.asText();
                            contentId.set(contentIdMap.getOrDefault(subType, conversationUtils.generaConversationContentId()));
                            contentIdMap.put(subType, contentId.get());
                            ConversationResponse conversationResponse = conversationUtils.buildConversationTextResponse(
                                    conversationId, messageId, contentId.get(), answer, subType, finished);
                            send(emitter, ConversationConstant.EVENT_NAME_MESSAGE, conversationResponse);
                            answerBuilderMap.computeIfAbsent(subType, k -> new StringBuilder()).append(answer);
                        }
                    }
                    
                    if (finished) {
                        ConversationResponse conversationResponse = conversationUtils.buildConversationTextResponse(
                                conversationId, messageId, contentId.get(), "", "", finished);
                        send(emitter, ConversationConstant.EVENT_NAME_MESSAGE, conversationResponse);
                    }
                } catch (Exception e) {
                    logger.error("Failed to parse Focus SSE response data: " + data, e);
                }
            });
            
            if (!answerBuilderMap.isEmpty()) {
                dataHandler.accept(buildManagementConversationMessage(conversationId, messageId, answerBuilderMap, contentIdMap, userId));
            }
        } catch (Exception e) {
            logger.error("Error occurred during Focus agent call", e);
            throw new RuntimeException("Error occurred during Focus agent call", e);
        }
    }

    private String getFocusServiceUrl(String userId) {
        String baseUrl = serviceDiscoveryUtil.getServiceBaseUrl(FOCUS_SERVICE_NAME);
        if (baseUrl == null) {
            return null;
        }
        return baseUrl + FOCUS_API_PATH + userId;
    }

    private String extractFocusConversationId(List<Map<String, String>> args) {
        if (args == null || args.isEmpty()) {
            return null;
        }
        
        for (Map<String, String> arg : args) {
            if (arg.containsKey("focusConversationId")) {
                return arg.get("focusConversationId");
            }
        }
        return null;
    }
}
