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
import com.hojoai.omniasst.sysagent.constans.ConversationConstant;
import com.hojoai.omniasst.sysagent.interaction.sse.SseClient;
import com.hojoai.omniasst.sysagent.service.conversation.ConversationUtils;
import com.hojoai.omniasst.sysagent.service.conversation.bean.ConversationResponse;
import com.hojoai.omniasst.common.api.conversation.ConversationAddMessageRequest;
import com.hojoai.omniasst.sysagent.service.conversation.impl.ConversationServiceImpl;
import com.hojoai.routing.engine.AgentService;
import com.hojoai.routing.engine.AgentTimestampRegistry;
import com.hojoai.routing.engine.ConversationContext;
import com.hojoai.routing.engine.TaskSequence;
import com.hojoai.omniasst.sysagent.service.conversation.manager.SseEmitterManager;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

/**
 * 默认聊天智能体实现：通过 {@link SseClient} 调用模型侧 {@code /v1/callAgent}，解析 SSE JSON 并组装落库消息。
 * <p>各垂直智能体可继承本类仅覆写 {@code agentName} 与展示名；日程/音乐等可覆写 {@link #callAgent} 走独立微服务。</p>
 */
@Primary
@Service
public class AgentServiceImpl implements AgentService {
    private static final Logger logger = LoggerFactory.getLogger(ConversationServiceImpl.class);

    @Value("${hojoai.model.api.url}")
    private String modelApiBaseUrl;
    
    private String getAgentServiceUrl() {
        return modelApiBaseUrl + "/v1/callAgent";
    }
    
    @Resource
    private SseClient sseClient;
    
    @Resource
    private SseEmitterManager sseEmitterManager;
    
    @Resource
    private ConversationUtils conversationUtils;
    
    private final ObjectMapper objectMapper = new ObjectMapper();

    protected String agentName = ConversationConstant.AGENT_CHAT;

    protected String getAgentChineseName() {
        return ConversationConstant.AGENT_CHAT_CN;
    }

    /**
     * 耳机 + 垂直智能体：对 {@code bubble} 落库时同时写入一份 {@link ConversationConstant#CONTENT_SUB_TYPE_SPEECH} 副本（正文相同）。
     */
    protected void appendManagementTextWithBubbleSpeechDuplicateIfNeeded(
            TaskSequence taskSequence,
            Map<String, StringBuilder> answerBuilderMap,
            String rawSubType,
            String text) {
        String subType = rawSubType == null ? "" : rawSubType;
        answerBuilderMap.computeIfAbsent(subType, k -> new StringBuilder()).append(text);
        if (taskSequence.isMapBubbleSubtypeToSpeech() && ConversationConstant.CONTENT_SUB_TYPE_BUBBLE.equals(subType)) {
            answerBuilderMap.computeIfAbsent(ConversationConstant.CONTENT_SUB_TYPE_SPEECH, k -> new StringBuilder()).append(text);
        }
    }

    /**
     * 下发一段文本 SSE；耳机垂直智能体场景下若 {@code subType} 为 {@code bubble}，再追加一帧相同正文、{@code subType=speech}。
     * {@code answerBuilderMap} 非 null 时按 subtype 同步追加（与 bubble 复制规则一致）。
     */
    protected void emitTextChunkWithBubbleSpeechDuplicateIfNeeded(
            TaskSequence taskSequence,
            String conversationId,
            String messageId,
            Map<String, String> contentIdMap,
            AtomicReference<String> contentIdRef,
            String rawSubType,
            String text,
            boolean finished,
            SseEmitter emitter,
            Map<String, StringBuilder> answerBuilderMapOrNull) throws Exception {
        String subType = rawSubType == null ? "" : rawSubType;
        sendOneTextChunk(conversationId, messageId, contentIdMap, contentIdRef, subType, text, finished, emitter, answerBuilderMapOrNull);
        if (taskSequence.isMapBubbleSubtypeToSpeech() && ConversationConstant.CONTENT_SUB_TYPE_BUBBLE.equals(subType)) {
            sendOneTextChunk(conversationId, messageId, contentIdMap, contentIdRef,
                    ConversationConstant.CONTENT_SUB_TYPE_SPEECH, text, finished, emitter, answerBuilderMapOrNull);
        }
    }

    private void sendOneTextChunk(
            String conversationId,
            String messageId,
            Map<String, String> contentIdMap,
            AtomicReference<String> contentIdRef,
            String subType,
            String text,
            boolean finished,
            SseEmitter emitter,
            Map<String, StringBuilder> answerBuilderMapOrNull) throws Exception {
        contentIdRef.set(contentIdMap.getOrDefault(subType, conversationUtils.generaConversationContentId()));
        contentIdMap.put(subType, contentIdRef.get());
        ConversationResponse response = conversationUtils.buildConversationTextResponse(
                conversationId, messageId, contentIdRef.get(), text, subType, finished);
        send(emitter, ConversationConstant.EVENT_NAME_MESSAGE, response);
        if (answerBuilderMapOrNull != null) {
            answerBuilderMapOrNull.computeIfAbsent(subType, k -> new StringBuilder()).append(text);
        }
    }

    protected String getLoadMessage(){
        if (ConversationConstant.AGENT_CHAT.equals(this.agentName)){
            return ConversationConstant.CHAT_LOADING_MESSAGE;
        } else if (ConversationConstant.AGENT_CHAT_WEBSEARCH.equals(this.agentName)) {
            return ConversationConstant.CHAT_WEBSEARCH_LOADING_MESSAGE;
        } else {
            return String.format(ConversationConstant.AGENT_LOADING_MESSAGE_TEMPLATE, getAgentChineseName());
        }
    }

    protected void sendAgentLoadingEvent(SseEmitter emitter) {
        try {
            emitter.send(SseEmitter.event().name(ConversationConstant.EVENT_NAME_LOADING).data(getLoadMessage()));
        } catch (IOException e) {
            logger.error("send agent loading event error", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void callAgent(ConversationContext context, TaskSequence taskSequence, SseEmitter emitter, Consumer<List<ConversationAddMessageRequest>> dataHandler) {
        String conversationId = context.getConversationId();
        String messageId = context.getMessageId();
        Map<String, String> contentIdMap = new HashMap<>();
        AtomicReference<String> contentId = new AtomicReference<>("");
        
        sendAgentLoadingEvent(emitter);
        try {
            logger.info("[{} call agent task: {}]", conversationId, taskSequence);
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("taskId", taskSequence.getTaskId());
            requestBody.put("args", taskSequence.getArgs() == null ? List.of() : taskSequence.getArgs());
            requestBody.put("agent", taskSequence.getTargetAgent());
            requestBody.put("task", taskSequence.getTask());
            requestBody.put("headphone", taskSequence.isHeadphone());
            requestBody.put("input", context.getInput());
            Map<String, StringBuilder> answerBuilderMap = new HashMap<>();
            sseClient.sendRequest(getAgentServiceUrl(), requestBody, data -> {
                try {
                    if (sseEmitterManager.shouldStop(conversationId, messageId)) {
                        return;
                    }
                    
                    String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new java.util.Date());
                    logger.info("[{}, {}] call agent response data: {}, time: {}", conversationId, messageId, data, timestamp);
                    JsonNode jsonData = objectMapper.readTree(data);
                    JsonNode dataNode = jsonData.get("data");
                    JsonNode finishedNode = jsonData.get("finished");
                    boolean finished = finishedNode != null && finishedNode.asBoolean();
                    if (dataNode != null) {
                        JsonNode answerNode = dataNode.get("answer");
                        if (answerNode != null && StringUtils.isNotEmpty(answerNode.asText())) {
                            String subType = dataNode.has("subType") ? dataNode.get("subType").asText("") : "";
                            String answer = answerNode.asText();
                            emitTextChunkWithBubbleSpeechDuplicateIfNeeded(
                                    taskSequence, conversationId, messageId, contentIdMap, contentId,
                                    subType, answer, finished, emitter, answerBuilderMap);
                        }
                    }

                    if (finished) {
                        ConversationResponse conversationResponse = conversationUtils.buildConversationTextResponse(conversationId, messageId, contentId.get(), "", "", finished);
                        send(emitter, ConversationConstant.EVENT_NAME_MESSAGE, conversationResponse);
                    }
                } catch (Exception e) {
                    logger.error("Failed to parse SSE response data: " + data, e);
                }
            });
            
            if (!answerBuilderMap.isEmpty()) {
                dataHandler.accept(buildManagementConversationMessage(conversationId, messageId, answerBuilderMap, contentIdMap, context.getUserId()));
            }
        } catch (Exception e) {
            throw new RuntimeException("Error occurred during agent call", e);
        }
    }

    protected List<ConversationAddMessageRequest> buildManagementConversationMessage(String conversationId, String messageId, Object obj){
        return buildManagementConversationMessage(conversationId, messageId, obj, null, null);
    }

    protected List<ConversationAddMessageRequest> buildManagementConversationMessage(String conversationId, String messageId, Object obj, Map<String, String> contentIdMap){
        return buildManagementConversationMessage(conversationId, messageId, obj, contentIdMap, null);
    }

    protected List<ConversationAddMessageRequest> buildManagementConversationMessage(String conversationId, String messageId, Object obj, Map<String, String> contentIdMap, String userId){
        if (obj instanceof Map){
            Map<String, StringBuilder> answerBuilderMap = (Map<String, StringBuilder>) obj;
            Map<String, String> typeToTextMap = new HashMap<>();
            for (Map.Entry<String, StringBuilder> entry : answerBuilderMap.entrySet()) {
                typeToTextMap.put(entry.getKey(), entry.getValue().toString());
            }
            ConversationAddMessageRequest message = conversationUtils.buildManagementConversationTextMessage(conversationId, messageId, ConversationConstant.ROLE_ASSISTANT, ConversationConstant.AGENT, this.agentName, typeToTextMap, contentIdMap, userId);
            message.setTimestamp(AgentTimestampRegistry.getTimestamp(conversationId + messageId));
            return List.of(message);
        }
        return new ArrayList<>();
    }

    protected void send(SseEmitter emitter, String eventName, ConversationResponse response) throws Exception {
        response.setTimestamp(AgentTimestampRegistry.getTimestamp(response.getConversationId() + response.getMessageId()));
        emitter.send(SseEmitter.event().name(eventName).data(objectMapper.writeValueAsString(response)));
    }
}