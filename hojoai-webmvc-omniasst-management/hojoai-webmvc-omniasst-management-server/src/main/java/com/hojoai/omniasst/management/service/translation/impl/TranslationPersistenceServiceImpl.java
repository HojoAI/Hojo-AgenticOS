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
package com.hojoai.omniasst.management.service.translation.impl;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.hojoai.common.asserts.Asserts;
import com.hojoai.omniasst.management.dao.translation.TranslationConversationDAO;
import com.hojoai.omniasst.management.dao.translation.TranslationMessageDAO;
import com.hojoai.omniasst.management.dao.translation.po.TranslationConversation;
import com.hojoai.omniasst.management.dao.translation.po.TranslationMessage;
import com.hojoai.omniasst.management.llm.bean.GenerateTitleRequest;
import com.hojoai.omniasst.management.llm.bean.GenerateTitleResponse;
import com.hojoai.omniasst.management.llm.bean.TitleAndAbstract;
import com.hojoai.omniasst.management.llm.bean.TranslationContext;
import com.hojoai.omniasst.management.llm.client.TitleGenerationModelClient;
import com.hojoai.omniasst.management.service.translation.TranslationPersistenceService;
import com.hojoai.omniasst.management.service.translation.bean.TranslateResultMessage;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * The service implementation for translation persistence.
 *
 * @author zhangxuepeng
 * @version 1.0, 2025/12/16
 * @since hojoai-webmvc-omniasst-management 1.0.0
 */
@Service
public class TranslationPersistenceServiceImpl implements TranslationPersistenceService {

    private static final Logger logger = LoggerFactory.getLogger(TranslationPersistenceServiceImpl.class);

    private static final String TRANSLATION_MESSAGE_ID_PREFIX = "TM";
    private static final String DEFAULT_TITLE = "新的翻译";

    @Resource
    private TranslationConversationDAO translationConversationDAO;

    @Resource
    private TranslationMessageDAO translationMessageDAO;

    @Resource
    private TitleGenerationModelClient titleGenerationModelClient;

    @Override
    public void persistTranslateResult(TranslateResultMessage message) {
        if (message == null) {
            logger.warn("Translate result message is null, ignore.");
            return;
        }

        Asserts.notBlank(message.getChatId(), "chatId");
        Asserts.notBlank(message.getUserId(), "userId");

        String chatId = message.getChatId();
        String userId = message.getUserId();
        String sourceLanguage = message.getSourceLanguage();
        String targetLanguage = message.getTargetLanguage();
        Long timestamp = message.getTimestamp();
        Integer isEnd = message.getIsEnd();

        // Handle end of translation session
        if (isEnd != null && isEnd == 1) {
            Asserts.notNull(message.getStartTime(), "startTime");
            Asserts.notNull(message.getEndTime(), "endTime");

            Long startTime = message.getStartTime();
            Long endTime = message.getEndTime();
            Long duration = endTime - startTime;

            // Update start time, end time, duration and set conversation as ended
            translationConversationDAO.updateTimeByConversationId(chatId, startTime, endTime);
            translationConversationDAO.updateDurationAndStatus(chatId, duration, 0);
            logger.info("Updated conversation time and status, chatId: {}, startTime: {}, endTime: {}, duration: {}ms, isActive: 0", 
                    chatId, startTime, endTime, duration);

            // Generate and update title and abstract
            generateAndUpdateTitleAndAbstract(chatId, sourceLanguage, targetLanguage);
            return;
        }

        // Validate fields for normal translation messages
        Asserts.notBlank(message.getOriginalText(), "originalText");
        Asserts.notBlank(message.getTranslatedText(), "translatedText");
        Asserts.notNull(timestamp, "timestamp");
        Asserts.gtZero(timestamp, "timestamp");
        TranslationConversation conversation = translationConversationDAO.selectByConversationId(chatId);
        if (conversation == null) {
            TranslationConversation newConversation = new TranslationConversation();
            newConversation.setTranslationConversationId(chatId);
            newConversation.setUserId(userId);
            newConversation.setSourceLanguage(sourceLanguage);
            newConversation.setTargetLanguage(targetLanguage);
            newConversation.setTitle(DEFAULT_TITLE);
            newConversation.setSummary(message.getOriginalText());
            newConversation.setIsActive(1); // Set conversation as active
            translationConversationDAO.insertConversation(newConversation);
            logger.info("Create translation conversation success, chatId: {}, userId: {}, title: {}", chatId, userId, DEFAULT_TITLE);
        }

        TranslationMessage exists = translationMessageDAO.selectByConversationIdAndTimestamp(chatId, timestamp);
        if (exists != null) {
            logger.info("Translation message already exists, chatId: {}, timestamp: {}", chatId, timestamp);
            return;
        }

        TranslationMessage messageEntity = new TranslationMessage();
        messageEntity.setTranslationMessageId(TRANSLATION_MESSAGE_ID_PREFIX + IdWorker.getIdStr());
        messageEntity.setTranslationConversationId(chatId);
        messageEntity.setSourceLanguage(sourceLanguage);
        messageEntity.setTargetLanguage(targetLanguage);
        messageEntity.setOriginalText(message.getOriginalText());
        messageEntity.setTranslatedText(message.getTranslatedText());
        messageEntity.setDetectedLanguage(message.getDetectedLanguage());
        messageEntity.setTimestamp(timestamp);
        translationMessageDAO.insertMessage(messageEntity);
        logger.info("Insert translation message success, chatId: {}, timestamp: {}", chatId, timestamp);
    }

    /**
     * Generate title and abstract using LLM and update to database.
     *
     * @param chatId         conversation id
     * @param sourceLanguage source language
     * @param targetLanguage target language
     */
    private void generateAndUpdateTitleAndAbstract(String chatId, String sourceLanguage, String targetLanguage) {
        try {
            // Query all messages for this conversation
            List<TranslationMessage> messages = translationMessageDAO.selectByConversationId(chatId);
            
            if (messages == null || messages.isEmpty()) {
                logger.warn("No messages found for conversation, chatId: {}", chatId);
                return;
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
                return;
            }

            TitleAndAbstract data = response.getData();
            String title = data.getTitle();
            String abstractText = data.getAbstractText();

            // Update database
            translationConversationDAO.updateTitleAndSummaryByConversationId(chatId, title, abstractText);
            logger.info("Successfully generated and updated title and abstract for chatId: {}, title: {}", chatId, title);

        } catch (Exception e) {
            logger.error("Failed to generate title and abstract for chatId: {}", chatId, e);
        }
    }

    /**
     * Determine role based on detected language.
     *
     * @param detectedLanguage detected language
     * @param sourceLanguage   source language
     * @param targetLanguage   target language
     * @return role ("me" or "them")
     */
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
