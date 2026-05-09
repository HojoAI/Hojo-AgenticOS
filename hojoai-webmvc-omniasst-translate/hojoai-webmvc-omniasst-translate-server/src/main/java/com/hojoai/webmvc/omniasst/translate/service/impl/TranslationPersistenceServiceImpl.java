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
package com.hojoai.webmvc.omniasst.translate.service.impl;

import com.alibaba.nacos.shaded.com.google.common.collect.Lists;
import com.alibaba.nacos.shaded.com.google.common.collect.Maps;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.hojoai.common.jackson.JacksonUtil;
import com.hojoai.omniasst.common.api.conversation.ConversationPushRequest;
import com.hojoai.omniasst.common.api.conversation.MessageContentItem;
import com.hojoai.omniasst.common.constans.conversation.AgentType;
import com.hojoai.omniasst.common.constans.conversation.ContextRole;
import com.hojoai.omniasst.common.constans.conversation.MessageType;
import com.hojoai.omniasst.common.constans.conversation.SubType;
import com.hojoai.webmvc.omniasst.translate.api.ManagementServiceClient;
import com.hojoai.webmvc.omniasst.translate.api.dto.ContentVal;
import com.hojoai.webmvc.omniasst.translate.api.dto.ContentValCard;
import com.hojoai.webmvc.omniasst.translate.api.dto.ConversationInfo;
import com.hojoai.webmvc.omniasst.translate.api.dto.PAPushRequestDTO;
import com.hojoai.webmvc.omniasst.translate.configure.RedisUtils;
import com.hojoai.webmvc.omniasst.translate.dao.TranslationConversationDAO;
import com.hojoai.webmvc.omniasst.translate.dao.TranslationMessageDAO;
import com.hojoai.webmvc.omniasst.translate.dao.po.TranslationConversation;
import com.hojoai.webmvc.omniasst.translate.dao.po.TranslationMessage;
import com.hojoai.webmvc.omniasst.translate.service.TranslationPersistenceService;
import com.hojoai.webmvc.omniasst.translate.service.llm.bean.GenerateTitleRequest;
import com.hojoai.webmvc.omniasst.translate.service.llm.bean.GenerateTitleResponse;
import com.hojoai.webmvc.omniasst.translate.service.llm.bean.TitleAndAbstract;
import com.hojoai.webmvc.omniasst.translate.service.llm.bean.TranslationContext;
import com.hojoai.webmvc.omniasst.translate.service.llm.client.TitleGenerationModelClient;
import com.hojoai.webmvc.omniasst.translate.websocket.bean.dto.TranslateResultMessage;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 翻译结果持久化：落库会话与消息；会话结束时抢锁、更新时长、调用 LLM 生成标题摘要并推送 management。
 */
@Service
public class TranslationPersistenceServiceImpl implements TranslationPersistenceService {

    private static final Logger logger = LoggerFactory.getLogger(TranslationPersistenceServiceImpl.class);
    /** 翻译消息业务 ID 前缀 */
    private static final String TRANSLATION_MESSAGE_ID_PREFIX = "TM";
    /** 新建会话时的默认标题 */
    private static final String DEFAULT_TITLE = "新的翻译";

    @Resource
    private TranslationConversationDAO conversationDAO;

    @Resource
    private TranslationMessageDAO messageDAO;

    @Resource
    private TitleGenerationModelClient titleGenerationModelClient;

    @Resource
    private TransactionTemplate transactionTemplate;

    @Resource
    private ManagementServiceClient managementServiceClient;

    @Resource
    private RedisUtils redisUtils;

    /**
     * {@inheritDoc}
     */
    @Async
    @Override
    public void persistTranslateResult(TranslateResultMessage message, String conversationId) {
        logger.info("persist translate result: {}", message);
        if (message == null) {
            logger.warn("TranslateResultMessage is null, skip persistence");
            return;
        }

        String chatId = message.getChatId();
        String userId = message.getUserId();
        Integer isEnd = message.getIsEnd();

        if (StringUtils.isBlank(chatId) || StringUtils.isBlank(userId)) {
            logger.warn("chatId or userId is blank, skip persistence. chatId: {}, userId: {}", chatId, userId);
            return;
        }

        if (isEnd != null && isEnd == 1) {
            boolean tryLock = redisUtils.tryLock(message.getChatId(), Duration.ofHours(1));
            if (!tryLock) {
                return;
            }
            // 立即提交状态更新事务，不阻塞后续 LLM 调用
            transactionTemplate.execute(status -> {
                handleConversationEnd(message);
                return null;
            });
            // LLM 调用在事务外执行，不阻塞状态更新
            String summary = generateAndUpdateTitleAndAbstract(message);
            this.pushTransResultToApp(message, conversationId, summary);
            return;
        }

        // 正常翻译消息也使用独立事务
        transactionTemplate.execute(status -> {
            handleTranslationMessage(message);
            return null;
        });
    }

    private void handleConversationEnd(TranslateResultMessage message) {
        String chatId = message.getChatId();
        Long startTime = message.getStartTime();
        Long endTime = message.getEndTime();

        if (startTime == null || endTime == null) {
            logger.warn("startTime or endTime is null for conversation end, chatId: {}", chatId);
            return;
        }

        Long duration = endTime - startTime;

        conversationDAO.updateTime(chatId, startTime, endTime);
        conversationDAO.updateDurationAndStatus(chatId, duration, 0);

        logger.info("Conversation ended, chatId: {}, startTime: {}, endTime: {}, duration: {}ms", 
                chatId, startTime, endTime, duration);
    }

    private void handleTranslationMessage(TranslateResultMessage message) {
        String chatId = message.getChatId();
        String userId = message.getUserId();
        String originalText = message.getOriginalText();
        String translatedText = message.getTranslatedText();
        Long timestamp = message.getTimestamp();

        if (StringUtils.isBlank(originalText) || StringUtils.isBlank(translatedText) || timestamp == null) {
            logger.warn("originalText, translatedText or timestamp is blank/null, skip persistence. userId: {}, chatId: {}", userId, chatId);
            return;
        }

        TranslationConversation conversation = conversationDAO.selectByConversationId(chatId);
        if (conversation == null) {
            createConversation(message);
        }

        TranslationMessage existingMessage = messageDAO.selectByConversationIdAndTimestamp(chatId, timestamp);
        if (existingMessage != null) {
            logger.info("Message already exists, skip insertion. userId: {}, chatId: {}, timestamp: {}", userId, chatId, timestamp);
            return;
        }

        insertMessage(message);
    }

    private void createConversation(TranslateResultMessage message) {
        TranslationConversation conversation = new TranslationConversation();
        conversation.setTranslationConversationId(message.getChatId());
        conversation.setUserId(message.getUserId());
        conversation.setSourceLanguage(message.getSourceLanguage());
        conversation.setTargetLanguage(message.getTargetLanguage());
        conversation.setTitle(DEFAULT_TITLE);
        conversation.setSummary(message.getOriginalText());
        conversation.setIsActive(1);

        conversationDAO.insertConversation(conversation);
        logger.info("Created new conversation, chatId: {}, userId: {}", message.getChatId(), message.getUserId());
    }

    private void insertMessage(TranslateResultMessage message) {
        TranslationMessage translationMessage = new TranslationMessage();
        translationMessage.setTranslationMessageId(TRANSLATION_MESSAGE_ID_PREFIX + IdWorker.getIdStr());
        translationMessage.setTranslationConversationId(message.getChatId());
        translationMessage.setSourceLanguage(message.getSourceLanguage());
        translationMessage.setTargetLanguage(message.getTargetLanguage());
        translationMessage.setOriginalText(message.getOriginalText());
        translationMessage.setTranslatedText(message.getTranslatedText());
        translationMessage.setDetectedLanguage(message.getDetectedLanguage());
        translationMessage.setTimestamp(message.getTimestamp());

        messageDAO.insertMessage(translationMessage);
        logger.info("Inserted translation message, chatId: {}, timestamp: {}", message.getChatId(), message.getTimestamp());
    }

    /**
     * 拉取会话全部消息，组装 {@link GenerateTitleRequest} 调用 LLM，并回写标题与摘要。
     */
    private String generateAndUpdateTitleAndAbstract(TranslateResultMessage message) {
        String chatId = message.getChatId();
        String sourceLanguage = message.getSourceLanguage();
        String targetLanguage = message.getTargetLanguage();
        try {
            // Query all messages for this conversation
            List<TranslationMessage> messages = messageDAO.selectByConversationId(chatId);

            if (messages == null || messages.isEmpty()) {
                logger.warn("No messages found for conversation, chatId: {}", chatId);
                return null;
            }

            // Build request for LLM
            GenerateTitleRequest request = new GenerateTitleRequest();
            request.setSourceLanguage(sourceLanguage);
            request.setTargetLanguage(targetLanguage);

            List<TranslationContext> contexts = new ArrayList<>();
            for (TranslationMessage msg : messages) {
                TranslationContext context = new TranslationContext();

                // Determine role based on detected language
                String role = determineRole(msg.getDetectedLanguage(), sourceLanguage, targetLanguage);
                context.setRole(role);
                context.setSourceText(msg.getOriginalText());
                context.setTargetText(msg.getTranslatedText());

                contexts.add(context);
            }
            request.setContext(contexts);

            // Call LLM to generate title and abstract
            logger.info("Calling LLM to generate title and abstract for chatId: {}", chatId);
            GenerateTitleResponse response = titleGenerationModelClient.generateTitle(request);

            if (response == null || response.getData() == null) {
                logger.warn("LLM returned null response for chatId: {}", chatId);
                return null;
            }

            TitleAndAbstract data = response.getData();
            String title = data.getTitle();
            String abstractText = data.getAbstractText();

            conversationDAO.updateTitleAndSummaryByConversationId(chatId, title, abstractText);
            logger.info("Successfully generated and updated title and abstract for chatId: {}, title: {}", chatId, title);
            return abstractText;
        } catch (Exception e) {
            logger.error("Failed to generate title and abstract for chatId: {}", chatId, e);
            return null;
        }
    }

    /**
     * 会话结束后：构造 PA 推送与会话消息推送，分别调用 management 的 push / pushMessage。
     */
    private void pushTransResultToApp(TranslateResultMessage message, String conversationId, String summary) {
        TranslationConversation translationConversation = conversationDAO.selectByConversationId(message.getChatId());
        if (Objects.isNull(translationConversation)) {
            return;
        }

        ContentValCard.CardData cardData = new ContentValCard.CardData();
        cardData.setChatId(message.getChatId());
        cardData.setSubtitle(summary);
        cardData.setTitle(translationConversation.getTitle());

        ContentValCard contentValCard = new ContentValCard();
        contentValCard.setData(cardData);

        ContentVal bubbleContentVal = new ContentVal();
        bubbleContentVal.setType("text");
        bubbleContentVal.setSubType(SubType.BUBBLE.getValue());
        bubbleContentVal.setText("<tag_translate>翻译搞定,先放这儿了。后面有需要随时喊我。");

        ContentVal cardContentVal = new ContentVal();
        cardContentVal.setType("text");
        cardContentVal.setSubType(SubType.CARD.getValue());
        cardContentVal.setText(JacksonUtil.bean2Json(contentValCard));

        ConversationInfo conversationInfo = new ConversationInfo();
        conversationInfo.setConversationId(conversationId);
        conversationInfo.setTranslationId(message.getChatId());
        conversationInfo.setRole("assistant");
        conversationInfo.setTimestamp(System.currentTimeMillis());
        conversationInfo.setCreatedAt(translationConversation.getCreatedAt().toString());
        conversationInfo.setContentJson(Lists.newArrayList(bubbleContentVal, cardContentVal));

        PAPushRequestDTO paPushRequestDTO = new PAPushRequestDTO();
        paPushRequestDTO.setUserIdList(Lists.newArrayList(message.getUserId()));
        paPushRequestDTO.setType("translation");
        paPushRequestDTO.setBroadcast(Boolean.FALSE);
        paPushRequestDTO.setAction("action.pa.message");
        paPushRequestDTO.setTitle(translationConversation.getTitle());
        paPushRequestDTO.setContent(translationConversation.getSummary());
        paPushRequestDTO.setConversationInfo(conversationInfo);

        logger.info("[翻译卡片推送]: {}", JacksonUtil.bean2Json(paPushRequestDTO));
        managementServiceClient.push(paPushRequestDTO);

        MessageContentItem bubbleMessageContentItem = new MessageContentItem();
        bubbleMessageContentItem.setType(MessageType.TEXT.getValue());
        bubbleMessageContentItem.setText("<tag_translate>翻译搞定,先放这儿了。后面有需要随时喊我。");
        bubbleMessageContentItem.setUrl(StringUtils.EMPTY);
        bubbleMessageContentItem.setAgent(AgentType.TRANSLATOR.getValue());
        bubbleMessageContentItem.setRole(ContextRole.AGENT.getValue());
        bubbleMessageContentItem.setSubType(SubType.BUBBLE.getValue());
        bubbleMessageContentItem.setMetadata(Maps.newHashMap());

        MessageContentItem cardMessageContentItem = new MessageContentItem();
        cardMessageContentItem.setType(MessageType.TEXT.getValue());
        cardMessageContentItem.setText(JacksonUtil.bean2Json(contentValCard));
        cardMessageContentItem.setUrl(StringUtils.EMPTY);
        cardMessageContentItem.setAgent(AgentType.TRANSLATOR.getValue());
        cardMessageContentItem.setRole(ContextRole.AGENT.getValue());
        cardMessageContentItem.setSubType(SubType.CARD.getValue());
        cardMessageContentItem.setMetadata(Maps.newHashMap());

        ConversationPushRequest pushMessageRequest = new ConversationPushRequest();
        pushMessageRequest.setUserId(message.getUserId());
        pushMessageRequest.setTitle(translationConversation.getTitle());
        pushMessageRequest.setConversationId(conversationId);
        pushMessageRequest.setContent(Lists.newArrayList(bubbleMessageContentItem, cardMessageContentItem));
        managementServiceClient.pushMessage(pushMessageRequest);
        logger.info("[翻译消息推送]: {}", JacksonUtil.bean2Json(pushMessageRequest));
    }

    private String determineRole(String detectedLanguage, String sourceLanguage, String targetLanguage) {
        if (detectedLanguage == null) {
            return "me";
        }

        if (detectedLanguage.equals(sourceLanguage)) {
            return "me";
        } else if (detectedLanguage.equals(targetLanguage)) {
            return "them";
        } else {
            return "me";
        }
    }
}

