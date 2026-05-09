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
package com.hojoai.omniasst.management.dao.conversation.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hojoai.omniasst.management.dao.conversation.MessageDAO;
import com.hojoai.omniasst.management.dao.conversation.MessageMapper;
import com.hojoai.omniasst.management.dao.conversation.po.Message;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Repository;

/**
 * The DAO implementation for message.
 *
 * @author zhangxuepeng
 * @version 1.0, 2025/12/10
 * @since hojoai-webmvc-omniasst-management 1.0.0
 */
@Repository
public class MessageDAOImpl implements MessageDAO {

    @Resource
    private MessageMapper messageMapper;

    @Override
    public void insertMessage(Message message) {
        messageMapper.insert(message);
    }

    @Override
    public Page<Message> selectMessagePage(Page<Message> page, LambdaQueryWrapper<Message> wrapper) {
        return messageMapper.selectPage(page, wrapper);
    }

    @Override
    public Message selectMessageByMessageId(String messageId) {
        LambdaQueryWrapper<Message> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Message::getMessageId, messageId).last("LIMIT 1");
        return messageMapper.selectOne(wrapper);
    }

    @Override
    public int updateMessageByMessageId(Message message) {
        LambdaQueryWrapper<Message> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Message::getMessageId, message.getMessageId());
        return messageMapper.update(message, wrapper);
    }

    @Override
    public boolean insertOrUpdateMessage(Message message) {
        return messageMapper.insertOrUpdate(message);
    }

    @Override
    public int deleteMessageByMessageId(String messageId) {
        LambdaQueryWrapper<Message> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Message::getMessageId, messageId);
        return messageMapper.delete(wrapper);
    }

    @Override
    public int deleteMessageByConversationId(String conversationId) {
        LambdaQueryWrapper<Message> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Message::getConversationId, conversationId);
        return messageMapper.delete(wrapper);
    }

    @Override
    public int updateMessagesIsReadByConversationId(String conversationId, Boolean isRead) {
        LambdaUpdateWrapper<Message> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(Message::getConversationId, conversationId)
               .set(Message::getIsRead, isRead);
        return messageMapper.update(null, wrapper);
    }
}

