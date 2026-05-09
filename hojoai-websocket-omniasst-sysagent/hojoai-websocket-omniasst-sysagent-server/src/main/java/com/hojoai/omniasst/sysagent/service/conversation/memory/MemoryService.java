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
package com.hojoai.omniasst.sysagent.service.conversation.memory;

import com.hojoai.omniasst.common.api.conversation.MessageContentItem;
import com.hojoai.omniasst.sysagent.service.conversation.bean.DeleteChatRequest;

import java.util.List;

/**
 * 会话记忆：从 Redis 或 management 拉取消息列表，并支持删除后回写缓存。
 */
public interface MemoryService {

    /**
     * 获取会话消息平面列表，可选先读 Redis。
     *
     * @param conversationId   会话 ID
     * @param filterMessageId  非空时跳过该 messageId（不纳入结果）
     * @param useCache         是否优先使用 Redis
     * @return 消息项列表，可能为 {@code null}
     */
    List<MessageContentItem> getMemory(String conversationId, String filterMessageId, boolean useCache);

    /**
     * 调用 management 删除单条消息后，重新加载会话消息并写入 Redis。
     *
     * @param conversationId 会话 ID
     * @param messageId      消息 ID
     * @return 是否整体成功
     */
    boolean deleteMessageAndReloadToRedis(String conversationId, String messageId);

    /**
     * 删除整个会话（management）并删除 Redis 中该会话上下文 key。
     *
     * @param request 删除参数
     * @return 是否成功
     */
    boolean deleteChatConversation(DeleteChatRequest request);
}