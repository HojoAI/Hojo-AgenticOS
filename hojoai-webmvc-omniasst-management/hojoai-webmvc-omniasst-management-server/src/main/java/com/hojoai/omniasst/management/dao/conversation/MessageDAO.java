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
package com.hojoai.omniasst.management.dao.conversation;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hojoai.omniasst.management.dao.conversation.po.Message;

/**
 * The DAO interface for message.
 *
 * @author zhangxuepeng
 * @version 1.0, 2025/12/10
 * @since hojoai-webmvc-omniasst-management 1.0.0
 */
public interface MessageDAO {

    void insertMessage(Message message);

    Page<Message> selectMessagePage(Page<Message> page, LambdaQueryWrapper<Message> wrapper);

    /**
     * 根据消息ID查询消息
     *
     * @param messageId 消息ID
     * @return 消息实体
     */
    Message selectMessageByMessageId(String messageId);

    /**
     * 按 messageId 更新消息
     *
     * @param message 消息实体（需包含 messageId）
     * @return 更新数量
     */
    int updateMessageByMessageId(Message message);

    /**
     * 插入或更新消息
     *
     * @param message 消息实体
     * @return 操作结果
     */
    boolean insertOrUpdateMessage(Message message);

    /**
     * 根据消息ID删除消息
     *
     * @param messageId 消息ID
     * @return 删除数量
     */
    int deleteMessageByMessageId(String messageId);

    /**
     * 根据对话ID删除消息
     *
     * @param conversationId 对话ID
     * @return 删除数量
     */
    int deleteMessageByConversationId(String conversationId);

    /**
     * 根据对话ID批量更新消息的已读状态
     *
     * @param conversationId 对话ID
     * @param isRead 已读状态
     * @return 更新数量
     */
    int updateMessagesIsReadByConversationId(String conversationId, Boolean isRead);
}

