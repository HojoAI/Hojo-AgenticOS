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

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hojoai.common.page.IPage;
import com.hojoai.webmvc.omniasst.translate.dao.TranslationConversationDAO;
import com.hojoai.webmvc.omniasst.translate.dao.TranslationMessageDAO;
import com.hojoai.webmvc.omniasst.translate.dao.po.TranslationConversation;
import com.hojoai.webmvc.omniasst.translate.dao.po.TranslationMessage;
import com.hojoai.webmvc.omniasst.translate.service.TranslationConversationService;
import com.hojoai.webmvc.omniasst.translate.service.dto.*;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 翻译会话查询实现：基于 MyBatis-Plus 分页查询会话与消息并转换为 API 响应。
 */
@Service
public class TranslationConversationServiceImpl implements TranslationConversationService {

    private static final Logger logger = LoggerFactory.getLogger(TranslationConversationServiceImpl.class);
    /** 排序方向：降序 */
    private static final String SORT_ORDER_DESC = "desc";
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Resource
    private TranslationConversationDAO conversationDAO;

    @Resource
    private TranslationMessageDAO messageDAO;

    /**
     * {@inheritDoc}
     */
    @Override
    public IPage<TranslationConversationListResponse> listByUser(TranslationConversationQueryRequest request) {
        int pageId = request.getPageId() != null ? request.getPageId() : 1;
        int pageSize = request.getPageSize() != null ? request.getPageSize() : 10;
        String title = request.getTitle();
        String sortField = request.getSortField();
        String sortOrder = request.getSortOrder();

        logger.info("Query conversation list, userId: {}, pageId: {}, pageSize: {}, title: {}", 
                request.getUserId(), pageId, pageSize, title);

        boolean desc = StringUtils.equalsIgnoreCase(sortOrder, SORT_ORDER_DESC);
        Page<TranslationConversation> page = new Page<>(pageId, pageSize);
        Page<TranslationConversation> result = conversationDAO.selectConversationPage(
                page, request.getUserId(), title, sortField, desc);

        logger.info("Query result, totalCount: {}, records size: {}", result.getTotal(), result.getRecords().size());

        List<TranslationConversationListResponse> responseList = result.getRecords().stream()
                .map(this::convertToListResponse)
                .collect(Collectors.toList());

        return IPage.newInstance((int) result.getCurrent(), (int) result.getSize(), result.getTotal(), responseList);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TranslationMessagePageResponse listMessages(TranslationMessageQueryRequest request) {
        String conversationId = request.getTranslationConversationId();
        int pageId = request.getPageId() != null ? request.getPageId() : 1;
        int pageSize = request.getPageSize() != null ? request.getPageSize() : 10;
        String sortField = request.getSortField();
        String sortOrder = request.getSortOrder();

        // 1. 查询会话头信息（标题、摘要等）
        TranslationConversation conversation = conversationDAO.selectByConversationId(conversationId);

        // 2. 分页查询消息列表
        boolean desc = StringUtils.equalsIgnoreCase(sortOrder, SORT_ORDER_DESC);
        Page<TranslationMessage> page = new Page<>(pageId, pageSize);
        Page<TranslationMessage> result = messageDAO.selectMessagePage(page, conversationId, sortField, desc);
        List<TranslationMessageListResponse> content = result.getRecords().stream()
                .map(this::convertToMessageResponse)
                .collect(Collectors.toList());

        IPage<TranslationMessageListResponse> messagesPage = IPage.newInstance(
                (int) result.getCurrent(), (int) result.getSize(), result.getTotal(), content);

        // 3. 组装分页结果与会话元数据
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

    private TranslationConversationListResponse convertToListResponse(TranslationConversation conversation) {
        TranslationConversationListResponse response = new TranslationConversationListResponse();
        response.setTranslationConversationId(conversation.getTranslationConversationId());
        response.setUserId(conversation.getUserId());
        response.setTitle(conversation.getTitle());
        response.setSummary(conversation.getSummary());
        response.setSourceLanguage(conversation.getSourceLanguage());
        response.setTargetLanguage(conversation.getTargetLanguage());
        response.setStartTime(conversation.getStartTime());
        response.setEndTime(conversation.getEndTime());
        response.setDuration(conversation.getDuration());
        response.setIsActive(conversation.getIsActive());
        response.setCreatedAt(conversation.getCreatedAt() != null ? 
                DATE_FORMAT.format(conversation.getCreatedAt()) : null);
        response.setUpdatedAt(conversation.getUpdatedAt() != null ? 
                DATE_FORMAT.format(conversation.getUpdatedAt()) : null);
        return response;
    }

    private TranslationMessageListResponse convertToMessageResponse(TranslationMessage message) {
        TranslationMessageListResponse response = new TranslationMessageListResponse();
        response.setTranslationMessageId(message.getTranslationMessageId());
        response.setTranslationConversationId(message.getTranslationConversationId());
        response.setSourceLanguage(message.getSourceLanguage());
        response.setTargetLanguage(message.getTargetLanguage());
        response.setOriginalText(message.getOriginalText());
        response.setTranslatedText(message.getTranslatedText());
        response.setDetectedLanguage(message.getDetectedLanguage());
        response.setTimestamp(message.getTimestamp());
        response.setCreatedAt(message.getCreatedAt() != null ? 
                DATE_FORMAT.format(message.getCreatedAt()) : null);
        return response;
    }
}

