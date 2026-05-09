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
package com.hojoai.omniasst.sysagent.service.conversation.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hojoai.common.response.Response;
import com.hojoai.common.response.ResponseStatusBasic;
import com.hojoai.omniasst.common.api.conversation.ConversationAddMessageResponse;
import com.hojoai.omniasst.common.api.conversation.ConversationAddMessageRequest;
import com.hojoai.omniasst.common.api.conversation.MessageContentItem;
import com.hojoai.omniasst.common.api.memory.dto.Memory;
import com.hojoai.omniasst.common.api.memory.dto.StoreMessageDTO;
import com.hojoai.omniasst.common.utils.MemoryUtils;
import com.hojoai.omniasst.sysagent.common.RedisService;
import com.hojoai.omniasst.sysagent.constans.ConversationConstant;
import com.hojoai.omniasst.sysagent.service.conversation.bean.DeleteChatRequest;
import com.hojoai.omniasst.sysagent.service.conversation.bean.RechatRequest;
import com.hojoai.omniasst.sysagent.service.api.HojoModelAPI;
import com.hojoai.omniasst.sysagent.service.conversation.ConversationUtils;
import com.hojoai.omniasst.sysagent.interaction.sse.SseClient;
import com.hojoai.omniasst.sysagent.service.api.ConversationAPI;
import com.hojoai.omniasst.sysagent.service.conversation.bean.IntentionModelRequest;
import com.hojoai.omniasst.sysagent.service.conversation.bean.IntentionModelResponse;
import com.hojoai.omniasst.sysagent.service.conversation.bean.ConversationResponse;
import com.hojoai.omniasst.sysagent.service.conversation.bean.ReplaceMessageRequest;
import com.hojoai.omniasst.sysagent.service.conversation.bean.ConversationRequest;
import com.hojoai.omniasst.sysagent.service.conversation.bean.StopMessage;
import com.hojoai.omniasst.sysagent.service.conversation.ConversationService;
import com.hojoai.omniasst.sysagent.service.conversation.manager.SseEmitterManager;
import com.hojoai.omniasst.sysagent.service.conversation.memory.MemoryService;
import com.hojoai.omniasst.sysagent.service.conversation.manager.HeadphoneDailyConversationService;
import com.hojoai.omniasst.sysagent.service.speech.ASRService;
import com.hojoai.omniasst.sysagent.service.speech.impl.HojoASRServiceImp;
import com.hojoai.omniasst.sysagent.service.conversation.manager.RedisMessageListenerConfig;
import com.hojoai.routing.engine.ConversationContext;
import com.hojoai.routing.engine.IntentionService;
import com.hojoai.routing.engine.TaskSequence;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;

/**
 * 对话主流程实现：异步 SSE 执行 chat/rechat，串联 {@link HojoModelAPI} 意图、{@link IntentionService} 多智能体、
 * {@link ConversationAPI} 落库、{@link MemoryService} 上下文、{@link HeadphoneDailyConversationService} 耳机单日单会话及 Redis 停止广播。
 */
@Service
public class ConversationServiceImpl implements ConversationService {
    private static final Logger logger = LoggerFactory.getLogger(ConversationServiceImpl.class);

    @Resource(type = HojoASRServiceImp.class)
    private ASRService asrService;

    @Resource
    private IntentionService intentionService;

    @Resource
    private SseClient sseClient;

    @Resource
    private HeadphoneDailyConversationService headphoneDailyConversationService;

    @Resource
    private ConversationAPI conversationAPI;

    @Resource
    private HojoModelAPI hojoModelAPI;

    @Resource
    private RedisService redisService;

    @Resource
    private MemoryService memoryService;

    @Resource
    private ScheduledExecutorService heartbeatScheduler;

    @Resource
    private SseEmitterManager sseEmitterManager;

    @Resource
    private RedisTemplate<String, String> redisTemplate;

    @Resource
    private ConversationUtils conversationUtils;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final Map<String, List<ConversationAddMessageRequest>> conversationIdToStoreMessage = new ConcurrentHashMap<>();

    private final Map<String, String> conversationIdToTitle = new ConcurrentHashMap<>();

    private final Map<String, String> conversationIdToAvatarId = new ConcurrentHashMap<>();

    private static class ConversationInfo {
        private final String conversationId;
        private final String messageId;

        public ConversationInfo(String conversationId, String messageId) {
            this.conversationId = conversationId;
            this.messageId = messageId;
        }

        public String getConversationId() {
            return conversationId;
        }

        public String getMessageId() {
            return messageId;
        }
    }

    private String buildKey(String conversationId, String messageId) {
        return conversationId + ":" + messageId;
    }

    /** 执行对话主逻辑的线程池，避免阻塞 Servlet 容器线程。 */
    private static final ExecutorService SSE_EXECUTOR =
            Executors.newCachedThreadPool(r -> new Thread(r, "sse-worker"));

    /**
     * {@inheritDoc}
     */
    @Override
    public ScheduledFuture<?> healthCheck(SseEmitter emitter) {
        return heartbeatScheduler.scheduleAtFixedRate(
                () -> {
                    try {
                        emitter.send(SseEmitter.event().name(ConversationConstant.EVENT_NAME_PING).data(""));
                    } catch (IOException e) {
                        logger.error("health check fail");
                    }
                },
                0,
                10,
                TimeUnit.SECONDS
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void processChat(ConversationRequest request, SseEmitter emitter, String userId) {
        try {
            CompletableFuture.runAsync(() -> {
                callChatRequest(request, emitter, userId);
            }, SSE_EXECUTOR);
        } catch (Exception e) {
            try {
                emitter.completeWithError(e);
            } catch (Exception emitterException) {
                logger.error("create health future: ", emitterException);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void processRechat(RechatRequest request, SseEmitter emitter, String userId) {
        try {
            CompletableFuture.runAsync(() -> {
                callRechatRequest(request, emitter, userId);
            }, SSE_EXECUTOR);
        } catch (Exception e) {
            try {
                emitter.completeWithError(e);
            } catch (Exception emitterException) {
                logger.error("create health future: ", emitterException);
            }
        }
    }

    private void callRechatRequest(RechatRequest request, SseEmitter emitter, String userId){
        logger.info("user rechat request: conversationId={}, messageId={}", request.getConversationId(), request.getMessageId());
        sendLoadingEvent(emitter);
        boolean bSave = true;
        ConversationInfo conversationInfo = createConversationAndMessageId(
                request.isHeadphoneMode(), request.getConversationId(), userId, userNowDateFrom(request));
        request.setConversationId(conversationInfo.getConversationId());
        sendConversationIdAndMessageId(emitter, conversationInfo.getConversationId(), conversationInfo.getMessageId());
        sseEmitterManager.registerEmitter(conversationInfo.getConversationId(), conversationInfo.getMessageId(), emitter);
        String key = buildKey(conversationInfo.getConversationId(), conversationInfo.getMessageId());
        if (StringUtils.isNotEmpty(request.getAvatarId())) {
            conversationIdToAvatarId.put(key, request.getAvatarId());
        }
        try {
            if (shouldStop(conversationInfo.getConversationId(), conversationInfo.getMessageId())) {
                logger.info("Rechat stopped before processing: conversationId={}, messageId={}", conversationInfo.getConversationId(), conversationInfo.getMessageId());
                return;
            }
            
            IntentionModelRequest modelRequest = buildRechatModelRequest(request);
            ResponseEntity<IntentionModelResponse> intentionModelResponseEntity = hojoModelAPI.callIntentionModel(modelRequest);
            if (intentionModelResponseEntity.getStatusCode().is2xxSuccessful()){
                IntentionModelResponse intentionResponse = intentionModelResponseEntity.getBody();
                logger.info("rechat [{}] sa response: {}", conversationInfo.getConversationId(), intentionResponse);
                if (intentionResponse != null && intentionResponse.getStatusCode().equals(200)) {
                    if (intentionResponse.getData().getAction().equals(ConversationConstant.INTENTION_ACTION_REJECT)){
                        bSave = false;
                        if (StringUtils.isNotEmpty(intentionResponse.getData().getAnswer())) {
                            ConversationResponse conversationResponse = conversationUtils.buildConversationTextResponse(conversationInfo.getConversationId(), conversationInfo.getMessageId(),
                                    conversationUtils.generaConversationContentId(), intentionResponse.getData().getAnswer(), ConversationConstant.CONTENT_SUB_TYPE_BODY, true);
                            sendConversationMessage(conversationResponse, emitter, ConversationConstant.ROLE_ASSISTANT, ConversationConstant.MODEL_ROLE_SYSTEM, null, userId);
                        }
                    } else if (intentionResponse.getData().getAction().equals(ConversationConstant.INTENTION_ACTION_AGENT)) {
                        String title = intentionResponse.getData().getTitle();
                        if (StringUtils.isNotEmpty(title)) {
                            conversationIdToTitle.put(key, title);
                        }
                        ConversationContext context = ConversationContext.builder()
                                .conversationId(conversationInfo.getConversationId())
                                .messageId(conversationInfo.getMessageId())
                                .userId(userId)
                                .input(modelRequest.getInput())
                                .headphoneMode(request.isHeadphoneMode())
                                .build();
                        List<TaskSequence> taskSequences = convertToTaskSequence(intentionResponse.getData().getTaskSequence());
                        intentionService.intentDispatch(context, taskSequences, emitter, data -> {
                            List<ConversationAddMessageRequest> messages = conversationIdToStoreMessage.getOrDefault(key, new ArrayList<>());
                            messages.addAll(data);
                            conversationIdToStoreMessage.put(key, messages);
                        });
                    }
                }else {
                    logger.error("call intention model inner error: {}", intentionResponse);
                }
            }else {
                logger.error("call intention model api error: {}", intentionModelResponseEntity);
            }
        } catch (Exception e) {
            logger.error("Error processing rechat request", e);
        } finally {
            emitter.complete();
            if (bSave){
                saveReChatMessage(request, conversationInfo.getMessageId(), userId, request.isHeadphoneMode());
            }
            conversationIdToStoreMessage.remove(key);
            conversationIdToTitle.remove(key);
            conversationIdToAvatarId.remove(key);
            sseEmitterManager.unregisterEmitter(conversationInfo.getConversationId(), conversationInfo.getMessageId());
        }
    }

    private IntentionModelRequest buildRechatModelRequest(RechatRequest request) {
        IntentionModelRequest modelRequest = new IntentionModelRequest();
        modelRequest.setSessionId(request.getConversationId());
        List<IntentionModelRequest.MessageContext> contextList = getMemoryContext(request.getConversationId(), request.getMessageId(), false);
        modelRequest.setContext(contextList);
        
        // 转换 Inputs 为 Input
        if (request.getInputs() != null) {
            IntentionModelRequest.Input input = conversationUtils.convertInputsToInput(request.getInputs(), request.getAvatarId());
            modelRequest.setInput(input);
        }
        
        return modelRequest;
    }

    private void callChatRequest(ConversationRequest request, SseEmitter emitter, String userId) {
        //create conversation and message id
        logger.info("user call request: {}", request.toString());
        sendLoadingEvent(emitter);
        Long startTime = System.currentTimeMillis();
        ConversationInfo conversationInfo = createConversationAndMessageId(
                request.isHeadphoneMode(), request.getConversationId(), userId, userNowDateFrom(request));
        sendConversationIdAndMessageId(emitter, conversationInfo.getConversationId(), conversationInfo.getMessageId());
        
        request.setConversationId(conversationInfo.getConversationId());
        sseEmitterManager.registerEmitter(conversationInfo.getConversationId(), conversationInfo.getMessageId(), emitter);
        String key = buildKey(conversationInfo.getConversationId(), conversationInfo.getMessageId());
        if (StringUtils.isNotEmpty(request.getAvatarId())) {
            conversationIdToAvatarId.put(key, request.getAvatarId());
        }
        boolean bSave = true;
        try {
            // 检查是否需要停止
            if (shouldStop(conversationInfo.getConversationId(), conversationInfo.getMessageId())) {
                logger.info("Conversation stopped before processing: conversationId={}, messageId={}", conversationInfo.getConversationId(), conversationInfo.getMessageId());
                return;
            }
            
            IntentionModelRequest intentionModelRequest = buildIntentionModelRequest(request);
            logger.info("[{}]sa request: {}", conversationInfo.getConversationId(), intentionModelRequest);
            ResponseEntity<IntentionModelResponse> intentionModelResponseEntity = hojoModelAPI.callIntentionModel(intentionModelRequest);
            if (intentionModelResponseEntity.getStatusCode().is2xxSuccessful()){
                IntentionModelResponse intentionResponse = intentionModelResponseEntity.getBody();
                logger.info("[{}] sa response: {}", conversationInfo.getConversationId(), intentionResponse);
                if (intentionResponse != null && intentionResponse.getStatusCode().equals(200)) {
                    if (intentionResponse.getData().getAction().equals(ConversationConstant.INTENTION_ACTION_REJECT)){
                        bSave = false;
                        if (StringUtils.isNotEmpty(intentionResponse.getData().getAnswer())) {
                            ConversationResponse conversationResponse = conversationUtils.buildConversationTextResponse(conversationInfo.getConversationId(), conversationInfo.getMessageId(),
                                    conversationUtils.generaConversationContentId(), intentionResponse.getData().getAnswer(), ConversationConstant.CONTENT_SUB_TYPE_BUBBLE, true);
                            sendConversationMessage(conversationResponse, emitter, ConversationConstant.ROLE_ASSISTANT, ConversationConstant.MODEL_ROLE_SYSTEM, null, userId);
                        }
                    } else if (intentionResponse.getData().getAction().equals(ConversationConstant.INTENTION_ACTION_AGENT)) {
                        String title = intentionResponse.getData().getTitle();
                        if (StringUtils.isNotEmpty(title)) {
                            conversationIdToTitle.put(key, title);
                        }
                        ConversationContext context = ConversationContext.builder()
                                .conversationId(conversationInfo.getConversationId())
                                .messageId(conversationInfo.getMessageId())
                                .userId(userId)
                                .input(intentionModelRequest.getInput())
                                .headphoneMode(request.isHeadphoneMode())
                                .build();
                        List<TaskSequence> taskSequences = convertToTaskSequence(intentionResponse.getData().getTaskSequence());
                        intentionService.intentDispatch(context, taskSequences, emitter, data -> {
                            List<ConversationAddMessageRequest> messages = conversationIdToStoreMessage.getOrDefault(key, new ArrayList<>());
                            messages.addAll(data);
                            conversationIdToStoreMessage.put(key, messages);
                        });
                    }
                }else {
                    logger.error("call intention model inner error: {}", intentionResponse);
                }
            }else {
                logger.error("call intention model api error: {}", intentionModelResponseEntity);
            }
        }catch (Exception e){
            logger.error("call model request error", e);
            try {
                emitter.send(SseEmitter.event().name(ConversationConstant.EVENT_NAME_ERROR).data(e.getMessage()));
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }finally {
            emitter.complete();
            if (bSave){
                saveChatMessage(request, conversationInfo.getMessageId(), startTime, userId, request.isHeadphoneMode());
            }
            conversationIdToStoreMessage.remove(key);
            conversationIdToTitle.remove(key);
            conversationIdToAvatarId.remove(key);
            sseEmitterManager.unregisterEmitter(conversationInfo.getConversationId(), conversationInfo.getMessageId());
        }
    }

    private void saveChatMessage(ConversationRequest request, String messageId, Long startTime, String userId,
                                 boolean headphoneMode) {
        if (headphoneMode) {
            logger.info("saveChatMessage headphoneMode=true, conversationId={}", request.getConversationId());
        }
        String key = buildKey(request.getConversationId(), messageId);
        String avatarId = conversationIdToAvatarId.getOrDefault(key, null);
        List<ConversationAddMessageRequest> messages = new ArrayList<>();

        //add user message
        if (CollectionUtils.isNotEmpty(request.getContents())){
            ConversationAddMessageRequest userMessage = new ConversationAddMessageRequest();
            userMessage.setConversationId(request.getConversationId());
            userMessage.setRole(ConversationConstant.ROLE_USER);
            userMessage.setResponseMessageId(messageId);
            userMessage.setTimestamp(startTime);
            userMessage.setUserId(userId);
            if (avatarId != null) {
                userMessage.setAvatarId(avatarId);
            }

            List<MessageContentItem> userContentItems = new ArrayList<>();
            if (request.getContents() != null) {
                for (ConversationRequest.ContentItem contentItem : request.getContents()) {
                    MessageContentItem item = new MessageContentItem();
                    item.setType(contentItem.getType());
                    item.setText(contentItem.getText());
                    item.setUrl(contentItem.getUrl());
                    item.setContentId(conversationUtils.generaConversationContentId());
                    item.setExtra(contentItem.getExtra());
                    item.setRole(ConversationConstant.ROLE_USER);
                    item.setSubType(ConversationConstant.CONTENT_SUB_TYPE_BODY);
                    userContentItems.add(item);
                }
            }
            userMessage.setContent(userContentItems);
            messages.add(userMessage);
        }

        List<ConversationAddMessageRequest> conversationMessages = conversationIdToStoreMessage.get(key);
        if (CollectionUtils.isNotEmpty(conversationMessages)){
            for (ConversationAddMessageRequest message : conversationMessages) {
                message.setUserId(userId);
                if (avatarId != null) {
                    message.setAvatarId(avatarId);
                }
            }
            messages.addAll(conversationMessages);
        }

        if (CollectionUtils.isNotEmpty(messages)){
            ResponseEntity<Response<List<ConversationAddMessageResponse>>> responseResponseEntity =
                    conversationAPI.saveConversationMessages(messages, headphoneMode);
            if (responseResponseEntity.getStatusCode().is2xxSuccessful() && responseResponseEntity.getBody().getResCode().equals(ResponseStatusBasic.OK.getResCode())){
                saveContextToRedis(request.getConversationId(), messages);
                saveConversationTitle(request.getConversationId(), messageId);
                saveMessagesToMemory(userId, messages, responseResponseEntity.getBody().getResContent());
            }
        }
    }

    private void saveReChatMessage(RechatRequest request, String messageId, String userId, boolean headphoneMode) {
        if (headphoneMode) {
            logger.info("saveReChatMessage headphoneMode=true, conversationId={}", request.getConversationId());
        }
        String key = buildKey(request.getConversationId(), messageId);
        String avatarId = conversationIdToAvatarId.getOrDefault(key, null);
        List<ConversationAddMessageRequest> conversationMessages = conversationIdToStoreMessage.get(key);
        if (avatarId != null && CollectionUtils.isNotEmpty(conversationMessages)) {
            for (ConversationAddMessageRequest message : conversationMessages) {
                message.setAvatarId(avatarId);
            }
        }
        if (CollectionUtils.isNotEmpty(conversationMessages)){
            ReplaceMessageRequest replaceMessageRequest = new ReplaceMessageRequest();
            replaceMessageRequest.setConversationId(request.getConversationId());
            replaceMessageRequest.setOldMessageId(request.getMessageId());
            replaceMessageRequest.setNewMessageRequest(conversationMessages.get(0));
            ResponseEntity<Response<Void>> responseEntity = conversationAPI.replaceConversationMessage(replaceMessageRequest);
            if (responseEntity.getStatusCode().is2xxSuccessful()){
                logger.info("replace message success: {}", request);
                saveContextToRedis(request.getConversationId(), conversationMessages);
                saveConversationTitle(request.getConversationId(), messageId);
                saveMessagesToMemory(userId, conversationMessages, null);
            }
        }
    }

    private void saveConversationTitle(String conversationId, String messageId){
        String key = buildKey(conversationId, messageId);
        String title = conversationIdToTitle.get(key);
        if (StringUtils.isNotEmpty(title)){
            try {
                ResponseEntity<Response<Void>> responseEntity = conversationAPI.updateConversationTitle(conversationId, title);
                if (responseEntity.getStatusCode().is2xxSuccessful()){
                    logger.info("save conversation title success: conversationId={}, title={}", conversationId, title);
                }else{
                    logger.error("save conversation title failed: conversationId={}, title={}", conversationId, title);
                }
            } catch (Exception e) {
                logger.error("save conversation title error: conversationId={}, title={}", conversationId, title, e);
            }
        }
    }

    private IntentionModelRequest buildIntentionModelRequest(ConversationRequest request){
        IntentionModelRequest modelRequest = new IntentionModelRequest();
        modelRequest.setSessionId(request.getConversationId());
        if (!CollectionUtils.isEmpty(request.getContents())) {
            List<IntentionModelRequest.MessageContext> contextList = new ArrayList<>(getMemoryContext(request.getConversationId(), null, true));

            for (ConversationRequest.ContentItem contentItem : request.getContents()) {
                IntentionModelRequest.MessageContext context = new IntentionModelRequest.MessageContext();
                context.setRole(ConversationConstant.ROLE_USER);
                context.setSubType(ConversationConstant.CONTENT_SUB_TYPE_BODY);
                String type = contentItem.getType();
                if (ConversationConstant.CONTENT_TYPE_TEXT.equals(type)) {
                    context.setContentType(ConversationConstant.CONTENT_TYPE_TEXT);
                    context.setText(contentItem.getText());
                } else if (ConversationConstant.CONTENT_TYPE_IMAGE.equals(type) || ConversationConstant.CONTENT_TYPE_FILE.equals(type)) {
                    context.setContentType(ConversationConstant.CONTENT_TYPE_FILE);
                    context.setFile(contentItem.getUrl());
                } else if (ConversationConstant.CONTENT_TYPE_AUDIO.equals(type)) {
                    context.setContentType(ConversationConstant.CONTENT_TYPE_TEXT);
                    String text = asrService.asrFileToText(contentItem.getUrl());
                    context.setText(text);
                    contentItem.setText(text);
                } else if (ConversationConstant.CONTENT_TYPE_VIDEO.equals(type)) {
                    context.setContentType(ConversationConstant.CONTENT_TYPE_FILE);
                    context.setFile(contentItem.getUrl());
                }
                contextList.add(context);
            }

            modelRequest.setContext(contextList);
        }
        
        // 转换 Inputs 为 Input
        if (request.getInputs() != null) {
            IntentionModelRequest.Input input = conversationUtils.convertInputsToInput(request.getInputs(), request.getAvatarId());
            modelRequest.setInput(input);
        }
        
        return modelRequest;
    }

    private List<IntentionModelRequest.MessageContext> getMemoryContext(String conversationId, String filterMessageId, boolean useCache){
        List<MessageContentItem> messageContentItems = memoryService.getMemory(conversationId, filterMessageId, useCache);
        List<IntentionModelRequest.MessageContext> contexts = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(messageContentItems)){
            for (MessageContentItem item : messageContentItems){
                if (item.getType().equals(ConversationConstant.CONTENT_TYPE_AUDIO)){
                    contexts.add(conversationUtils.audioContentItem2MessageContext(item));
                }else {
                    contexts.add(conversationUtils.messageContentItem2MessageContext(item));
                }
            }
        }
        return contexts;
    }

    private void sendLoadingEvent(SseEmitter emitter) {
        try {
            emitter.send(SseEmitter.event().name(ConversationConstant.EVENT_NAME_LOADING).data(ConversationConstant.LOADING_MESSAGE));
        } catch (IOException e) {
            logger.error("send loading event error", e);
        }
    }

    private void sendConversationIdAndMessageId(SseEmitter emitter, String conversationId, String messageId) {
        try {
            ConversationResponse idResponse = new ConversationResponse();
            idResponse.setConversationId(conversationId);
            idResponse.setMessageId(messageId);
            emitter.send(SseEmitter.event().name(ConversationConstant.EVENT_NAME_MESSAGE).data(objectMapper.writeValueAsString(idResponse)));
            logger.info("Sent conversationId and messageId: {}, {}", conversationId, messageId);
        } catch (IOException e) {
            logger.error("Failed to send conversationId and messageId", e);
        }
    }

    private void sendConversationMessage(ConversationResponse conversationResponse, SseEmitter emitter, String omniRole, String contextRole, String agent, String userId) throws Exception {
        emitter.send(SseEmitter.event().name(ConversationConstant.EVENT_NAME_MESSAGE).data(objectMapper.writeValueAsString(conversationResponse)));
        ConversationAddMessageRequest conversationAddMessageRequest = buildStoreMessage(conversationResponse, omniRole, contextRole, agent, userId);
        String key = buildKey(conversationResponse.getConversationId(), conversationResponse.getMessageId());
        List<ConversationAddMessageRequest> messages = conversationIdToStoreMessage.getOrDefault(key, new ArrayList<>());
        messages.add(conversationAddMessageRequest);
        conversationIdToStoreMessage.put(key, messages);
    }

    private ConversationAddMessageRequest buildStoreMessage(ConversationResponse conversationResponse, String omniRole, String contextRole, String agent, String userId){
        ConversationAddMessageRequest conversationAddMessageRequest = new ConversationAddMessageRequest();
        conversationAddMessageRequest.setConversationId(conversationResponse.getConversationId());
        conversationAddMessageRequest.setMessageId(conversationResponse.getMessageId());
        conversationAddMessageRequest.setRole(omniRole);
        conversationAddMessageRequest.setUserId(userId);
        List<MessageContentItem> items = new ArrayList<>();
        for (ConversationResponse.DataItem item : conversationResponse.getData()){
            MessageContentItem messageContentItem = new MessageContentItem();
            messageContentItem.setRole(contextRole);
            messageContentItem.setAgent(agent);
            messageContentItem.setType(item.getType());
            messageContentItem.setText(item.getText());
            messageContentItem.setUrl(item.getUrl());
            messageContentItem.setSubType(item.getSubType());
            messageContentItem.setContentId(item.getContentId());
            items.add(messageContentItem);
        }
        conversationAddMessageRequest.setContent(items);
        return conversationAddMessageRequest;
    }

    private static String userNowDateFrom(ConversationRequest request) {
        return request.getInputs() != null ? request.getInputs().getUserNowDate() : null;
    }

    private static String userNowDateFrom(RechatRequest request) {
        return request.getInputs() != null ? request.getInputs().getUserNowDate() : null;
    }

    private ConversationInfo createConversationAndMessageId(boolean headphoneMode, String conversationId, String userId,
                                                              String userNowDate) {
        Map<String, String> conversationAndMessageId = headphoneDailyConversationService
                .createConversationAndMessageId(headphoneMode, conversationId, userId, userNowDate);
        if (conversationAndMessageId.size() == 1) {
            Map.Entry<String, String> entry = conversationAndMessageId.entrySet().iterator().next();
            return new ConversationInfo(entry.getKey(), entry.getValue());
        }
        return new ConversationInfo("", "");
    }

    private void saveContextToRedis(String conversationId, List<ConversationAddMessageRequest> messages) {
        if (StringUtils.isEmpty(conversationId) || CollectionUtils.isEmpty(messages)){
            return;
        }
        List<MessageContentItem> messageContentItems = messages.stream().flatMap(item -> item.getContent().stream()).toList();
        try {
            String redisKey = ConversationUtils.CONVERSATION_CONTEXT_PRE + conversationId;
            List<MessageContentItem> existingContext = (List<MessageContentItem>)(redisService.get(redisKey));
            if (existingContext == null){
                existingContext = new ArrayList<>();
            }
            existingContext.addAll(messageContentItems);
            // 不可直接 subList：ArrayList$SubList 写入 Redis 后 Jackson 无法反序列化
            if (existingContext.size() > 50) {
                existingContext = new ArrayList<>(existingContext.subList(0, 50));
            }
            redisService.set(redisKey, existingContext, ConversationUtils.CONVERSATION_CONTEXT_TIMEOUT, TimeUnit.MINUTES);
            logger.info("Saved context to Redis for conversationId: {}, total items: {}", conversationId, existingContext.size());
        } catch (Exception e) {
            logger.error("Failed to save context to Redis for conversationId: {}", conversationId, e);
        }
    }

    private void saveMessagesToMemory(String userId, List<ConversationAddMessageRequest> messages, List<ConversationAddMessageResponse> addMessages) {
        try {
            if (StringUtils.isEmpty(userId)) {
                logger.warn("Cannot save messages to memory: userId is empty");
                return;
            }
            
            if (CollectionUtils.isEmpty(messages)) {
                return;
            }
            
            List<StoreMessageDTO> storeMessageDTOList = new ArrayList<>();

            int index = 0;
            for (ConversationAddMessageRequest message : messages) {
                if (StringUtils.isEmpty(message.getMessageId())) {
                    message.setMessageId(addMessages.get(index).getMessageId());
                }
                
                StoreMessageDTO storeMessageDTO = new StoreMessageDTO();
                storeMessageDTO.setMessageId(message.getMessageId());
                
                // 转换角色：ROLE_USER -> HUMAN, ROLE_ASSISTANT -> AI
                String role = message.getRole();
                if (ConversationConstant.ROLE_USER.equals(role)) {
                    storeMessageDTO.setRole(Memory.Role.HUMAN);
                } else if (ConversationConstant.ROLE_ASSISTANT.equals(role)) {
                    storeMessageDTO.setRole(Memory.Role.AI);
                } else {
                    storeMessageDTO.setRole(Memory.Role.AI);
                }
                
                // 通过 MessageContentItem 获取内容
                StringBuilder contentBuilder = new StringBuilder();
                if (CollectionUtils.isNotEmpty(message.getContent())) {
                    for (MessageContentItem item : message.getContent()) {
                        if (StringUtils.isNotEmpty(item.getText())) {
                            if (!contentBuilder.isEmpty()) {
                                contentBuilder.append("\n");
                            }
                            contentBuilder.append(item.getText());
                        }
                    }
                }
                storeMessageDTO.setContent(contentBuilder.toString());
                storeMessageDTOList.add(storeMessageDTO);
                index++;
            }
            
            if (CollectionUtils.isNotEmpty(storeMessageDTOList)) {
                MemoryUtils.storeMessageList(ConversationConstant.PA_MEMORY_GROUP_ID, userId, storeMessageDTOList);
                logger.info("Saved {} messages to memory for userId: {}", storeMessageDTOList.size(), userId);
            }
        } catch (Exception e) {
            logger.error("Failed to save messages to memory", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean deleteChatMessage(DeleteChatRequest request) {
        logger.info("Processing delete chat request: conversationId={}, messageId={}", 
            request.getConversationId(), request.getMessageId());
        
        try {
            boolean success = memoryService.deleteMessageAndReloadToRedis(
                request.getConversationId(), 
                request.getMessageId()
            );
            
            if (success) {
                logger.info("Successfully deleted chat message and reloaded to Redis: conversationId={}, messageId={}", 
                    request.getConversationId(), request.getMessageId());
            } else {
                logger.error("Failed to delete chat message: conversationId={}, messageId={}", 
                    request.getConversationId(), request.getMessageId());
            }
            
            return success;
        } catch (Exception e) {
            logger.error("Error processing delete chat request: conversationId={}, messageId={}", 
                request.getConversationId(), request.getMessageId(), e);
            return false;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean deleteChatConversation(DeleteChatRequest request) {
        logger.info("Processing delete chat: conversationId={}, messageId={}", 
            request.getConversationId(), request.getMessageId());
        
        try {
            boolean success = memoryService.deleteChatConversation(request);
            
            if (success) {
                logger.info("Successfully deleted chat: conversationId={}, messageId={}", 
                    request.getConversationId(), request.getMessageId());
            } else {
                logger.error("Failed to delete chat: conversationId={}, messageId={}", 
                    request.getConversationId(), request.getMessageId());
            }
            
            return success;
        } catch (Exception e) {
            logger.error("Error processing delete chat: conversationId={}, messageId={}", 
                request.getConversationId(), request.getMessageId(), e);
            return false;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean stopConversation(String conversationId, String messageId) {
        logger.info("Stopping conversation: conversationId={}, messageId={}", conversationId, messageId);
        
        try {
            if (sseEmitterManager.hasEmitter(conversationId, messageId)) {
                logger.info("Emitter found on local machine, stopping locally: conversationId={}, messageId={}", conversationId, messageId);
                return sseEmitterManager.stopConversation(conversationId, messageId);
            } else {
                // 多实例部署：由订阅 {@link RedisMessageListenerConfig#STOP_CHANNEL} 的节点处理停止
                logger.info("Emitter not found on local machine, publishing to Redis: conversationId={}, messageId={}", conversationId, messageId);
                
                StopMessage stopMessage = new StopMessage(conversationId, messageId);
                String message = objectMapper.writeValueAsString(stopMessage);

                redisTemplate.execute((RedisCallback<Object>) (connection) -> {
                    connection.publish(RedisMessageListenerConfig.STOP_CHANNEL.getBytes(), message.getBytes());
                    return null;
                });
                
                logger.info("Published stop message to Redis: {}", stopMessage);
                return true;
            }
        } catch (Exception e) {
            logger.error("Failed to stop conversation: conversationId={}, messageId={}", conversationId, messageId, e);
            return false;
        }
    }

    private boolean shouldStop(String conversationId, String messageId) {
        return sseEmitterManager.shouldStop(conversationId, messageId);
    }

    /**
     * 将意图模型返回的 {@link IntentionModelResponse.Data.TaskSequence} 列表
     * 转换为路由引擎契约的 {@link TaskSequence} 列表。
     * <p>
     * routing 引擎设置 {@code headphone} 和 {@code mapBubbleSubtypeToSpeech} 标志，
     * 初始值设为 false。
     * </p>
     */
    private List<TaskSequence> convertToTaskSequence(List<IntentionModelResponse.Data.TaskSequence> original) {
        if (original == null) {
            return List.of();
        }
        return original.stream().map(ts -> {
            TaskSequence seq = new TaskSequence();
            seq.setTask(ts.getTask());
            seq.setTaskId(ts.getTaskId());
            seq.setArgs(ts.getArgs());
            seq.setTargetAgent(ts.getTargetAgent());
            seq.setHeadphone(false);
            seq.setMapBubbleSubtypeToSpeech(false);
            return seq;
        }).toList();
    }
}