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

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hojoai.common.date.DateUtil;
import com.hojoai.common.page.IPage;
import com.hojoai.omniasst.management.common.GlobalConstants;
import com.hojoai.omniasst.management.dao.translation.TranslationConversationDAO;
import com.hojoai.omniasst.management.dao.translation.TranslationMessageDAO;
import com.hojoai.omniasst.management.dao.translation.po.TranslationConversation;
import com.hojoai.omniasst.management.dao.translation.po.TranslationMessage;
import com.hojoai.omniasst.management.service.translation.TranslationConversationService;
import com.hojoai.omniasst.management.service.translation.bean.TranslationConversationListResponse;
import com.hojoai.omniasst.management.service.translation.bean.TranslationConversationQueryRequest;
import com.hojoai.omniasst.management.service.translation.bean.TranslationMessageListResponse;
import com.hojoai.omniasst.management.service.translation.bean.TranslationMessagePageResponse;
import com.hojoai.omniasst.management.service.translation.bean.TranslationMessageQueryRequest;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * The service implementation for translation conversation and messages.
 *
 * @author zhangxuepeng
 * @version 1.0, 2025/12/16
 * @since hojoai-webmvc-omniasst-management 1.0.0
 */
@Service
public class TranslationConversationServiceImpl implements TranslationConversationService {

    @Resource
    private TranslationConversationDAO translationConversationDAO;

    @Resource
    private TranslationMessageDAO translationMessageDAO;

    @Override
    public IPage<TranslationConversationListResponse> listByUser(TranslationConversationQueryRequest request) {
        long pageId = request.getPageId();
        long pageSize = request.getPageSize();
        String title = request.getTitle();
        String sortField = request.getSortField();
        String sortOrder = request.getSortOrder();

        boolean desc = StringUtils.equalsIgnoreCase(sortOrder, GlobalConstants.SORT_ORDER_DESC);
        Page<TranslationConversation> page = new Page<>(pageId, pageSize);
        Page<TranslationConversation> result = translationConversationDAO.selectConversationPage(page, request.getUserId(), title, sortField, desc);
        List<TranslationConversationListResponse> content = result.getRecords().stream().map(item -> {
            TranslationConversationListResponse response = new TranslationConversationListResponse();
            response.setTranslationConversationId(item.getTranslationConversationId());
            response.setUserId(item.getUserId());
            response.setTitle(item.getTitle());
            response.setSummary(item.getSummary());
            response.setSourceLanguage(item.getSourceLanguage());
            response.setTargetLanguage(item.getTargetLanguage());
            response.setStartTime(item.getStartTime());
            response.setEndTime(item.getEndTime());
            response.setDuration(item.getDuration());
            response.setIsActive(item.getIsActive());
            response.setCreatedAt(DateUtil.formatAsSecond(item.getCreatedAt()));
            response.setUpdatedAt(DateUtil.formatAsSecond(item.getUpdatedAt()));
            return response;
        }).toList();

        return IPage.newInstance((int) result.getCurrent(), (int) result.getSize(), result.getTotal(), content);
    }

    @Override
    public TranslationMessagePageResponse listMessages(TranslationMessageQueryRequest request) {
        String conversationId = request.getTranslationConversationId();
        long pageId = request.getPageId();
        long pageSize = request.getPageSize();
        String sortField = request.getSortField();
        String sortOrder = request.getSortOrder();

        // Query conversation info
        TranslationConversation conversation = translationConversationDAO.selectByConversationId(conversationId);

        // Query messages page
        boolean desc = StringUtils.equalsIgnoreCase(sortOrder, GlobalConstants.SORT_ORDER_DESC);
        Page<TranslationMessage> page = new Page<>(pageId, pageSize);
        Page<TranslationMessage> result = translationMessageDAO.selectMessagePage(page, conversationId, sortField, desc);
        List<TranslationMessageListResponse> content = result.getRecords().stream().map(item -> {
            TranslationMessageListResponse response = new TranslationMessageListResponse();
            response.setTranslationMessageId(item.getTranslationMessageId());
            response.setTranslationConversationId(item.getTranslationConversationId());
            response.setSourceLanguage(item.getSourceLanguage());
            response.setTargetLanguage(item.getTargetLanguage());
            response.setOriginalText(item.getOriginalText());
            response.setTranslatedText(item.getTranslatedText());
            response.setDetectedLanguage(item.getDetectedLanguage());
            response.setTimestamp(item.getTimestamp());
            response.setCreatedAt(DateUtil.formatAsSecond(item.getCreatedAt()));
            return response;
        }).toList();

        IPage<TranslationMessageListResponse> messagesPage = IPage.newInstance(
                (int) result.getCurrent(), (int) result.getSize(), result.getTotal(), content);

        // Build response with conversation info and pagination data
        TranslationMessagePageResponse response = new TranslationMessagePageResponse();
        response.setPage(messagesPage);
        
        if (conversation != null) {
            response.setTitle(conversation.getTitle());
            response.setSummary(conversation.getSummary());
            response.setStartTime(conversation.getStartTime());
            response.setEndTime(conversation.getEndTime());
            response.setDuration(conversation.getDuration());
            response.setIsActive(conversation.getIsActive());
            response.setSourceLanguage(conversation.getSourceLanguage());
            response.setTargetLanguage(conversation.getTargetLanguage());
        }

        return response;
    }
}