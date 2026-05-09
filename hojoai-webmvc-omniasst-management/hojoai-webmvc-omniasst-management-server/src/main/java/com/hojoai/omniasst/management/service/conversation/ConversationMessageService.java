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
package com.hojoai.omniasst.management.service.conversation;

import com.hojoai.common.page.IPage;
import com.hojoai.omniasst.common.api.conversation.ConversationAddMessageRequest;
import com.hojoai.omniasst.common.api.conversation.ConversationAddMessageResponse;
import com.hojoai.omniasst.common.api.conversation.ConversationPushRequest;
import com.hojoai.omniasst.management.service.conversation.bean.ConversationListResponse;
import com.hojoai.omniasst.management.service.conversation.bean.ConversationIdRequest;
import com.hojoai.omniasst.management.service.conversation.bean.ConversationQueryRequest;
import com.hojoai.omniasst.management.service.conversation.bean.MessageListResponse;
import com.hojoai.omniasst.management.service.conversation.bean.MessageQueryRequest;
import com.hojoai.omniasst.management.service.conversation.bean.ConversationIdResponse;

import java.util.List;

/**
 * The service interface for conversation message.
 *
 * @author zhangxuepeng
 * @version 1.0, 2025/12/10
 * @since hojoai-webmvc-omniasst-management 1.0.0
 */
public interface ConversationMessageService {

    /**
     * 追加消息
     *
     * @param request 对话消息请求
     * @return 对话消息响应
     */
    ConversationAddMessageResponse appendMessage(ConversationAddMessageRequest request);

    /**
     * 批量追加消息
     *
     * @param requests 对话消息请求列表
     * @return 对话消息响应列表
     */
    List<ConversationAddMessageResponse> appendMessages(List<ConversationAddMessageRequest> requests);

    /**
     * 生成新的对话ID和消息ID（预占，不落库）
     *
     * @param request 预占请求
     * @return 对话ID与消息ID
     */
    ConversationIdResponse generateConversationIds(ConversationIdRequest request);

    /**
     * 根据用户ID查询对话列表（分页）
     *
     * @param request 查询请求（含分页、排序、过滤）
     * @return 对话列表分页
     */
    IPage<ConversationListResponse> listByUser(ConversationQueryRequest request);

    /**
     * 根据对话ID查询消息列表（分页）
     *
     * @param request 查询请求（含分页、排序、过滤）
     * @return 消息列表分页
     */
    IPage<MessageListResponse> listMessages(MessageQueryRequest request);

    /**
     * 替换消息
     *
     * @param conversationId 对话ID
     * @param newMessageRequest 新消息请求对象
     * @param oldMessageId 旧消息ID
     * @return 操作结果
     */
    boolean replaceMessage(String conversationId, ConversationAddMessageRequest newMessageRequest, String oldMessageId);

    /**
     * 删除消息
     *
     * @param conversationId 对话ID
     * @param messageId 消息ID
     * @return 操作结果
     */
    boolean deleteMessage(String conversationId, String messageId);

    /**
     * 删除对话
     *
     * @param conversationId 对话ID
     * @return 操作结果
     */
    boolean deleteConversation(String conversationId);

    /**
     * 更新对话标题
     *
     * @param conversationId 对话ID
     * @param title 新标题
     * @return 操作结果
     */
    boolean updateConversationTitle(String conversationId, String title);

    /**
     * 保存推送消息
     * 如果没有conversationId则创建一个新的对话，如果有则直接添加到对应conversation中
     *
     * @param request 推送消息请求
     * @return 对话消息响应
     */
    ConversationAddMessageResponse pushMessage(ConversationPushRequest request);

    /**
     * 判断当前用户的所有对话是否都已读
     *
     * @param userId 用户ID
     * @return true表示所有对话都已读，false表示存在未读对话
     */
    boolean areAllConversationsRead(String userId);

    /**
     * 更新消息的额外信息
     *
     * @param conversationId 对话ID
     * @param messageId 消息ID
     * @param contentId 内容ID
     * @param extra 额外信息
     * @return 操作结果
     */
    boolean updateMessageExtra(String conversationId, String messageId, String contentId, Object extra);
}
