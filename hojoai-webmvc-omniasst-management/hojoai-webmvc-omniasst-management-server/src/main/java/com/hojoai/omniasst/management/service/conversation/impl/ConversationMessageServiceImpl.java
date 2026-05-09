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
package com.hojoai.omniasst.management.service.conversation.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hojoai.common.jackson.JacksonUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.hojoai.common.date.DateUtil;
import com.hojoai.common.page.IPage;
import com.hojoai.common.response.ResponseStatusBasic;
import com.hojoai.omniasst.common.api.conversation.ConversationAddMessageRequest;
import com.hojoai.omniasst.common.api.conversation.ConversationAddMessageResponse;
import com.hojoai.omniasst.common.api.conversation.MessageContentItem;
import com.hojoai.omniasst.common.constans.conversation.MessageCategory;
import com.hojoai.omniasst.common.constans.conversation.SystemRole;
import com.hojoai.omniasst.management.common.BusinessException;
import com.hojoai.omniasst.management.common.ConversationRoleEnum;
import com.hojoai.omniasst.management.common.GlobalConstants;
import com.hojoai.omniasst.management.common.ResponseStatusExtra;
import com.hojoai.omniasst.management.dao.conversation.ConversationDAO;
import com.hojoai.omniasst.management.dao.conversation.MessageDAO;
import com.hojoai.omniasst.management.dao.conversation.po.Conversation;
import com.hojoai.omniasst.management.dao.conversation.po.Message;
import com.hojoai.omniasst.management.dao.avatar.AvatarDAO;
import com.hojoai.omniasst.management.dao.avatar.po.Avatar;
import com.hojoai.omniasst.management.service.avatar.util.AvatarUtils;
import com.hojoai.omniasst.common.api.conversation.ConversationPushRequest;
import com.hojoai.omniasst.management.service.conversation.ConversationMessageService;
import com.hojoai.omniasst.management.service.conversation.bean.ConversationIdRequest;
import com.hojoai.omniasst.management.service.conversation.bean.ConversationIdResponse;
import com.hojoai.omniasst.management.service.conversation.bean.ConversationListResponse;
import com.hojoai.omniasst.management.service.conversation.bean.ConversationQueryRequest;
import com.hojoai.omniasst.management.service.conversation.bean.MessageListResponse;
import com.hojoai.omniasst.management.service.conversation.bean.MessageQueryRequest;
import com.hojoai.omniasst.management.utils.ThrowUtils;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * The service implementation for conversation message.
 *
 * @author zhangxuepeng
 * @version 1.0, 2025/12/10
 * @since hojoai-webmvc-omniasst-management 1.0.0
 */
@Service
public class ConversationMessageServiceImpl implements ConversationMessageService {

    private static final Logger logger = LoggerFactory.getLogger(ConversationMessageServiceImpl.class);

    private static final String DEFAULT_TITLE = "新的对话";
    private static final String CONVERSATION_ID_PREFIX = "C";
    private static final String MESSAGE_ID_PREFIX = "M";

    @Resource
    private ConversationDAO conversationDAO;

    @Resource
    private MessageDAO messageDAO;

    @Resource
    private AvatarDAO avatarDAO;

    @Override
    public ConversationIdResponse generateConversationIds(ConversationIdRequest request) {
        ThrowUtils.ThrowIf(request == null, ResponseStatusBasic.ILLEGAL_ARGUMENT, "request cannot be null");
        ThrowUtils.ThrowIf(StringUtils.isBlank(request.getUserId()), ResponseStatusBasic.ILLEGAL_ARGUMENT, "userId cannot be blank");

        String conversationId = request.getConversationId();
        if (StringUtils.isEmpty(conversationId)) {
            conversationId = CONVERSATION_ID_PREFIX + IdWorker.getIdStr();
        }

        String messageId = MESSAGE_ID_PREFIX + IdWorker.getIdStr();
        logger.info("Reserve messageId success, conversationId: {}, messageId: {}", conversationId, messageId);

        ConversationIdResponse response = new ConversationIdResponse();
        response.setConversationId(conversationId);
        response.setMessageId(messageId);
        return response;
    }

    @Override
    public ConversationAddMessageResponse appendMessage(ConversationAddMessageRequest request) {
        ThrowUtils.ThrowIf(!ConversationRoleEnum.exists(request.getRole()), ResponseStatusBasic.ILLEGAL_ARGUMENT, "Invalid message role");

        List<MessageContentItem> contentItems = request.getContent();
        ThrowUtils.ThrowIf(contentItems == null || contentItems.isEmpty(), ResponseStatusBasic.ILLEGAL_ARGUMENT, "Message content cannot be empty");

        String conversationId = request.getConversationId();
        ThrowUtils.ThrowIf(StringUtils.isBlank(conversationId), ResponseStatusBasic.ILLEGAL_ARGUMENT, "conversationId cannot be blank");
        Conversation conversation = conversationDAO.selectConversationByConversationId(conversationId);
//        ThrowUtils.ThrowIf(conversation == null, ResponseStatusExtra.CONVERSATION_NOT_FOUND, "Conversation not found");
        if (conversation == null){
            conversation = new Conversation();
            conversation.setConversationId(conversationId);
            conversation.setUserId(request.getUserId());
            conversation.setTitle(DEFAULT_TITLE);
            conversation.setIsRead(true);
            Date now = new Date();
            conversation.setCreatedAt(now);
            conversation.setUpdatedAt(now);
            conversation.setModifyTime(System.currentTimeMillis());
            conversationDAO.insertConversation(conversation);
            logger.info("Create conversation success (reserve), conversationId: {}, userId: {}", conversationId, request.getUserId());
        }

        String messageId = request.getMessageId();
        if (StringUtils.isEmpty(messageId)){
            messageId = MESSAGE_ID_PREFIX + IdWorker.getIdStr();
        }

        Message message = createMessageFromRequest(messageId, conversationId, request);
        messageDAO.insertMessage(message);
        logger.info("Insert or update message success, conversationId: {}, messageId: {}", conversationId, messageId);

        if (message.getIsRead() != null && !message.getIsRead()) {
            conversationDAO.updateConversationIsReadByConversationId(conversationId, message.getIsRead());
        }
        conversationDAO.updateConversationModifyTimeByConversationId(conversationId, System.currentTimeMillis());

        ConversationAddMessageResponse response = new ConversationAddMessageResponse();
        response.setConversationId(conversationId);
        response.setMessageId(messageId);
        response.setTitle(conversation.getTitle());
        response.setRole(request.getRole());
        response.setUserId(request.getUserId());
        response.setMessageType(message.getMessageType());
        return response;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<ConversationAddMessageResponse> appendMessages(List<ConversationAddMessageRequest> requests) {
        return requests.stream()
                .map(this::appendMessage)
                .collect(Collectors.toList());
    }

    @Override
    public IPage<ConversationListResponse> listByUser(ConversationQueryRequest request) {

        long pageId = request.getPageId();
        long pageSize = request.getPageSize();
        String title = request.getTitle();
        String sortField = request.getSortField();
        String sortOrder = request.getSortOrder();

        Page<Conversation> page = new Page<>(pageId, pageSize);
        LambdaQueryWrapper<Conversation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Conversation::getUserId, request.getUserId()).like(StringUtils.isNotBlank(title), Conversation::getTitle, title);

        boolean desc = StringUtils.equalsIgnoreCase(sortOrder, GlobalConstants.SORT_ORDER_DESC);
        if (StringUtils.isNotBlank(sortField)) {
            if ("createdAt".equalsIgnoreCase(sortField) || "created_at".equalsIgnoreCase(sortField)) {
                wrapper.orderBy(true, !desc, Conversation::getCreatedAt);
            } else if ("title".equalsIgnoreCase(sortField)) {
                wrapper.orderBy(true, !desc, Conversation::getTitle);
            } else {
                wrapper.orderByDesc(Conversation::getUpdatedAt);
            }
        } else {
            wrapper.orderByDesc(Conversation::getModifyTime);
        }

        Page<Conversation> result = conversationDAO.selectConversationPage(page, wrapper);

        List<ConversationListResponse> content = result.getRecords().stream().map(item -> {
            ConversationListResponse response = new ConversationListResponse();
            response.setConversationId(item.getConversationId());
            response.setUserId(item.getUserId());
            response.setTitle(item.getTitle());
            response.setIsRead(item.getIsRead());
            response.setCreatedAt(DateUtil.formatAsSecond(item.getCreatedAt()));
            response.setUpdatedAt(item.getModifyTime());
            return response;
        }).toList();

        return IPage.newInstance((int) result.getCurrent(), (int) result.getSize(), result.getTotal(), content);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public IPage<MessageListResponse> listMessages(MessageQueryRequest request) {
        Conversation conversation = findConversation(request.getConversationId());
        
        long pageId = request.getPageId();
        long pageSize = request.getPageSize();
        String sortField = request.getSortField();
        String sortOrder = request.getSortOrder();
        String role = request.getRole();

        Page<Message> page = new Page<>(pageId, pageSize);
        LambdaQueryWrapper<Message> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Message::getConversationId, request.getConversationId()).eq(StringUtils.isNotBlank(role), Message::getRole, role);

        boolean desc = StringUtils.equalsIgnoreCase(sortOrder, GlobalConstants.SORT_ORDER_DESC);
        if (StringUtils.isNotBlank(sortField)) {
            if ("createdAt".equalsIgnoreCase(sortField) || "created_at".equalsIgnoreCase(sortField)) {
                wrapper.orderBy(true, !desc, Message::getCreatedAt);
            } else if ("role".equalsIgnoreCase(sortField)) {
                wrapper.orderBy(true, !desc, Message::getRole);
            } else {
                wrapper.orderByDesc(Message::getCreatedAt);
            }
        } else {
            wrapper.orderByDesc(Message::getCreatedAt);
        }

        Page<Message> result = messageDAO.selectMessagePage(page, wrapper);
        List<MessageListResponse> content = result.getRecords().stream().map(item -> {
            MessageListResponse response = new MessageListResponse();
            response.setMessageId(item.getMessageId());
            response.setConversationId(item.getConversationId());
            response.setRole(item.getRole());
            response.setContentJson(parseContent(item.getContentJson()));
            response.setCreatedAt(DateUtil.formatAsSecond(item.getCreatedAt()));
            response.setTimestamp(item.getTimestamp());
            response.setIsRead(item.getIsRead());
            response.setMessageType(item.getMessageType());
            if (StringUtils.isNotBlank(item.getAvatarId())) {
                Avatar avatar = avatarDAO.selectAvatarByAvatarId(item.getAvatarId());
                if (avatar != null) {
                    response.setAvatar(AvatarUtils.convertToAvatarItem(avatar));
                }
            }
            
            return response;
        }).toList();

        // 将对话和对话中的所有消息都设置为已读，但不更新modify_time（只更新is_read）
        conversationDAO.updateConversationIsReadByConversationId(request.getConversationId(), true);
        int updatedMessagesCount = messageDAO.updateMessagesIsReadByConversationId(request.getConversationId(), true);
        logger.info("Mark conversation and messages as read, conversationId: {}, updated messages count: {}",
                request.getConversationId(), updatedMessagesCount);

        return IPage.newInstance((int) result.getCurrent(), (int) result.getSize(), result.getTotal(), content);
    }

    private Conversation findConversation(String conversationId) {
        Conversation conversation = conversationDAO.selectConversationByConversationId(conversationId);
        ThrowUtils.ThrowIf(conversation == null, ResponseStatusExtra.CONVERSATION_NOT_FOUND, "Conversation not found");
        return conversation;
    }

    private List<MessageContentItem> parseContent(String contentJson) {
        try {
            return JacksonUtil.json2Bean(contentJson, new TypeReference<List<MessageContentItem>>() {
            });
        } catch (Exception e) {
            logger.warn("Parse message content failed, contentJson={}", contentJson, e);
            throw new BusinessException(ResponseStatusBasic.GENERAL_ERROR, "Parse message content failed");
        }
    }

    private Message createMessageFromRequest(String messageId, String conversationId, ConversationAddMessageRequest request) {
        Message message = new Message();
        message.setMessageId(messageId);
        message.setConversationId(conversationId);
        message.setRole(request.getRole());
        message.setContentJson(JacksonUtil.bean2Json(request.getContent()));
        message.setTimestamp(request.getTimestamp());
        message.setResponseMessageId(request.getResponseMessageId());
        message.setAvatarId(request.getAvatarId());
        message.setIsRead(request.getIsRead() != null ? request.getIsRead() : true);
        return message;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean replaceMessage(String conversationId, ConversationAddMessageRequest newMessageRequest, String oldMessageId) {
        int deleted = messageDAO.deleteMessageByMessageId(oldMessageId);
        logger.info("Delete old message, messageId: {}, deleted: {}", oldMessageId, deleted);

        Message newMessage = createMessageFromRequest(newMessageRequest.getMessageId(), conversationId, newMessageRequest);
        messageDAO.insertMessage(newMessage);
        logger.info("Insert new message, messageId: {}", newMessageRequest.getMessageId());

        LambdaQueryWrapper<Message> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Message::getConversationId, conversationId)
               .eq(Message::getResponseMessageId, oldMessageId);

        Page<Message> page = new Page<>(1, 1000);
        List<Message> messagesToUpdate = messageDAO.selectMessagePage(page, wrapper).getRecords();
        for (Message message : messagesToUpdate) {
            message.setResponseMessageId(newMessageRequest.getMessageId());
            message.setAvatarId(newMessageRequest.getAvatarId());
            messageDAO.updateMessageByMessageId(message);
        }

        if (newMessage.getIsRead() != null && !newMessage.getIsRead()) {
            conversationDAO.updateConversationIsReadByConversationId(conversationId, newMessage.getIsRead());
        }
        conversationDAO.updateConversationModifyTimeByConversationId(conversationId, System.currentTimeMillis());

        logger.info("Replace message success, conversationId: {}, newMessageId: {}, oldMessageId: {}", 
                   conversationId, newMessageRequest.getMessageId(), oldMessageId);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteMessage(String conversationId, String messageId) {
        ThrowUtils.ThrowIf(StringUtils.isBlank(conversationId), ResponseStatusBasic.ILLEGAL_ARGUMENT, "conversationId cannot be blank");
        ThrowUtils.ThrowIf(StringUtils.isBlank(messageId), ResponseStatusBasic.ILLEGAL_ARGUMENT, "messageId cannot be blank");

        Conversation conversation = conversationDAO.selectConversationByConversationId(conversationId);
        ThrowUtils.ThrowIf(conversation == null, ResponseStatusExtra.CONVERSATION_NOT_FOUND, "Conversation not found");

        int deleted = messageDAO.deleteMessageByMessageId(messageId);
        logger.info("Delete message, conversationId: {}, messageId: {}, deleted: {}", conversationId, messageId, deleted);

//        ThrowUtils.ThrowIf(deleted == 0, ResponseStatusExtra.MESSAGE_ID_NOT_FOUND, "Message not found");

        conversationDAO.updateConversationModifyTimeByConversationId(conversationId, System.currentTimeMillis());

        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteConversation(String conversationId) {
        ThrowUtils.ThrowIf(StringUtils.isBlank(conversationId), ResponseStatusBasic.ILLEGAL_ARGUMENT, "conversationId cannot be blank");

        int deletedMessagesCount = messageDAO.deleteMessageByConversationId(conversationId);

        int deletedConversation = conversationDAO.deleteConversationByConversationId(conversationId);

        logger.info("Delete conversation, conversationId: {}, deleted messages: {}, deleted conversation: {}", 
                   conversationId, deletedMessagesCount, deletedConversation);

        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateConversationTitle(String conversationId, String title) {
        ThrowUtils.ThrowIf(StringUtils.isBlank(conversationId), ResponseStatusBasic.ILLEGAL_ARGUMENT, "conversationId cannot be blank");
        ThrowUtils.ThrowIf(StringUtils.isBlank(title), ResponseStatusBasic.ILLEGAL_ARGUMENT, "title cannot be blank");

        Conversation conversation = conversationDAO.selectConversationByConversationId(conversationId);
        ThrowUtils.ThrowIf(conversation == null, ResponseStatusExtra.CONVERSATION_NOT_FOUND, "Conversation not found");

        int updated = conversationDAO.updateConversationTitleByConversationId(conversationId, title);
        
        conversationDAO.updateConversationModifyTimeByConversationId(conversationId, System.currentTimeMillis());
        
        logger.info("Update conversation title, conversationId: {}, title: {}, updated: {}", conversationId, title, updated);

        return updated > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ConversationAddMessageResponse pushMessage(ConversationPushRequest request) {
        ThrowUtils.ThrowIf(request == null, ResponseStatusBasic.ILLEGAL_ARGUMENT, "request cannot be null");
        ThrowUtils.ThrowIf(StringUtils.isBlank(request.getUserId()), ResponseStatusBasic.ILLEGAL_ARGUMENT, "userId cannot be blank");

        List<MessageContentItem> contentItems = request.getContent();
        ThrowUtils.ThrowIf(contentItems == null || contentItems.isEmpty(), ResponseStatusBasic.ILLEGAL_ARGUMENT, "Message content cannot be empty");

        String conversationId = request.getConversationId();
        Conversation conversation = null;
        
        // 如果没有conversationId，则创建一个新的对话
        if (StringUtils.isBlank(conversationId)) {
            conversationId = CONVERSATION_ID_PREFIX + IdWorker.getIdStr();
            conversation = new Conversation();
            conversation.setConversationId(conversationId);
            conversation.setUserId(request.getUserId());
            conversation.setTitle(StringUtils.isBlank(request.getTitle()) ? DEFAULT_TITLE : request.getTitle());
            conversation.setIsRead(false);
            conversation.setModifyTime(System.currentTimeMillis());
            conversationDAO.insertConversation(conversation);
            logger.info("Create conversation success for push message, conversationId: {}, userId: {}", conversationId, request.getUserId());
        } else {
            // 如果有conversationId，验证对话是否存在
            conversation = conversationDAO.selectConversationByConversationId(conversationId);
            ThrowUtils.ThrowIf(conversation == null, ResponseStatusExtra.CONVERSATION_NOT_FOUND, "Conversation not found");
            // 验证userId是否匹配
            ThrowUtils.ThrowIf(!request.getUserId().equals(conversation.getUserId()), ResponseStatusBasic.ILLEGAL_ARGUMENT, "UserId does not match conversation");
        }

        // 生成消息ID
        String messageId = MESSAGE_ID_PREFIX + IdWorker.getIdStr();
        String role = SystemRole.ASSISTANT.getValue();

        // 创建消息
        Message message = new Message();
        message.setMessageId(messageId);
        message.setConversationId(conversationId);
        message.setRole(role);
        message.setContentJson(JacksonUtil.bean2Json(contentItems));
        message.setTimestamp(System.currentTimeMillis());
        message.setIsRead(false);
        message.setMessageType(MessageCategory.PUSH.getValue());
        messageDAO.insertMessage(message);
        logger.info("Insert push message success, conversationId: {}, messageId: {}, role: {}", conversationId, messageId, role);

        conversationDAO.updateConversationIsReadByConversationId(conversationId, false);
        conversationDAO.updateConversationModifyTimeByConversationId(conversationId, System.currentTimeMillis());

        ConversationAddMessageResponse response = new ConversationAddMessageResponse();
        response.setConversationId(conversationId);
        response.setMessageId(messageId);
        response.setUserId(request.getUserId());
        response.setTitle(conversation.getTitle());
        response.setRole(role);
        response.setMessageType(message.getMessageType());
        return response;
    }

    @Override
    public boolean areAllConversationsRead(String userId) {
        ThrowUtils.ThrowIf(StringUtils.isBlank(userId), ResponseStatusBasic.ILLEGAL_ARGUMENT, "userId cannot be blank");

        // 直接通过SQL查询是否存在未读对话
        boolean hasUnread = conversationDAO.existsUnreadConversation(userId);
        logger.info("Check all conversations read status, userId: {}, allRead: {}", userId, !hasUnread);
        return !hasUnread;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateMessageExtra(String conversationId, String messageId, String contentId, Object extra) {
        ThrowUtils.ThrowIf(StringUtils.isBlank(conversationId), ResponseStatusBasic.ILLEGAL_ARGUMENT, "conversationId cannot be blank");
        ThrowUtils.ThrowIf(StringUtils.isBlank(messageId), ResponseStatusBasic.ILLEGAL_ARGUMENT, "messageId cannot be blank");

        // 先查询消息是否存在
        Message message = messageDAO.selectMessageByMessageId(messageId);
        ThrowUtils.ThrowIf(message == null, ResponseStatusExtra.MESSAGE_ID_NOT_FOUND, "Message not found");
        
        // 验证消息是否属于指定对话
        ThrowUtils.ThrowIf(!conversationId.equals(message.getConversationId()), 
                          ResponseStatusExtra.MESSAGE_ID_NOT_FOUND, "Message does not belong to specified conversation");

        // 解析现有的contentJson
        List<MessageContentItem> contentItems = parseContent(message.getContentJson());
        
        // 根据contentId查找对应的content item并更新extra
        boolean found = false;
        for (MessageContentItem item : contentItems) {
            if (contentId.equals(item.getContentId())) {
                item.setExtra(extra);
                found = true;
                break;
            }
        }
        
        if (!found) {
            logger.warn("ContentId not found in message content, conversationId: {}, messageId: {}, contentId: {}", 
                       conversationId, messageId, contentId);
            return false;
        }
        
        // 更新消息的contentJson字段
        message.setContentJson(JacksonUtil.bean2Json(contentItems));
        int updated = messageDAO.updateMessageByMessageId(message);
        
        // 更新对话的modify_time，因为修改了消息内容
        conversationDAO.updateConversationModifyTimeByConversationId(conversationId, System.currentTimeMillis());
        
        logger.info("Update message extra, conversationId: {}, messageId: {}, contentId: {}, updated: {}", 
                   conversationId, messageId, contentId, updated);

        return updated > 0;
    }
}
