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
import com.hojoai.omniasst.management.dao.conversation.ConversationDAO;
import com.hojoai.omniasst.management.dao.conversation.ConversationMapper;
import com.hojoai.omniasst.management.dao.conversation.po.Conversation;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Repository;

import java.util.Date;


/**
 * The DAO implementation for conversation.
 *
 * @author zhangxuepeng
 * @version 1.0, 2025/12/10
 * @since hojoai-webmvc-omniasst-management 1.0.0
 */
@Repository
public class ConversationDAOImpl implements ConversationDAO {

    @Resource
    private ConversationMapper conversationMapper;

    @Override
    public void insertConversation(Conversation conversation) {
        conversationMapper.insert(conversation);
    }

    @Override
    public Conversation selectConversationByConversationId(String conversationId) {
        LambdaQueryWrapper<Conversation> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(Conversation::getConversationId, conversationId);
        return conversationMapper.selectOne(lambdaQueryWrapper);
    }

    @Override
    public Page<Conversation> selectConversationPage(Page<Conversation> page, LambdaQueryWrapper<Conversation> wrapper) {
        return conversationMapper.selectPage(page, wrapper);
    }

    @Override
    public int deleteConversationByConversationId(String conversationId) {
        LambdaQueryWrapper<Conversation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Conversation::getConversationId, conversationId);
        return conversationMapper.delete(wrapper);
    }

    @Override
    public int updateConversationTitleByConversationId(String conversationId, String title) {
        LambdaUpdateWrapper<Conversation> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(Conversation::getConversationId, conversationId)
               .set(Conversation::getTitle, title);
        return conversationMapper.update(null, wrapper);
    }

    @Override
    public int updateConversationUpdatedAtByConversationId(String conversationId) {
        LambdaUpdateWrapper<Conversation> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(Conversation::getConversationId, conversationId)
               .set(Conversation::getUpdatedAt, new Date());
        return conversationMapper.update(null, wrapper);
    }

    @Override
    public int updateConversationIsReadByConversationId(String conversationId, Boolean isRead) {
        LambdaUpdateWrapper<Conversation> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(Conversation::getConversationId, conversationId)
               .set(Conversation::getIsRead, isRead);
        return conversationMapper.update(null, wrapper);
    }

    @Override
    public int updateConversationModifyTimeByConversationId(String conversationId, Long modifyTime) {
        LambdaUpdateWrapper<Conversation> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(Conversation::getConversationId, conversationId)
               .set(Conversation::getModifyTime, modifyTime);
        return conversationMapper.update(null, wrapper);
    }

    @Override
    public boolean existsUnreadConversation(String userId) {
        LambdaQueryWrapper<Conversation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Conversation::getUserId, userId)
               .and(w -> w.eq(Conversation::getIsRead, false).or().isNull(Conversation::getIsRead))
               .last("LIMIT 1");
        return conversationMapper.selectOne(wrapper) != null;
    }
}

