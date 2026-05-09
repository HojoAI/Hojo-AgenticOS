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
package com.hojoai.omniasst.sysagent.service.conversation;

import com.hojoai.omniasst.sysagent.service.conversation.bean.DeleteChatRequest;
import com.hojoai.omniasst.sysagent.service.conversation.bean.RechatRequest;
import com.hojoai.omniasst.sysagent.service.conversation.bean.ConversationRequest;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.concurrent.ScheduledFuture;

/**
 * 对话域核心服务：SSE 聊天/重聊、management 删除、以及本地或 Redis 广播停止生成。
 */
public interface ConversationService {

    /**
     * 注册 SSE 心跳任务，周期性发送 {@code ping} 事件。
     *
     * @param emitter 当前连接的 SSE
     * @return 可取消的定时任务句柄
     */
    ScheduledFuture<?> healthCheck(SseEmitter emitter);

    /**
     * 异步处理用户发起的新对话请求并写入 {@code emitter}。
     *
     * @param request 首轮对话参数
     * @param emitter SSE 输出
     * @param userId  用户标识
     */
    void processChat(ConversationRequest request, SseEmitter emitter, String userId);

    /**
     * 异步处理重聊（基于历史消息重新生成）。
     *
     * @param request 重聊参数
     * @param emitter SSE 输出
     * @param userId  用户标识
     */
    void processRechat(RechatRequest request, SseEmitter emitter, String userId);

    /**
     * 删除单条消息并刷新 Redis 上下文缓存。
     *
     * @param request 会话与消息 ID
     * @return 是否成功
     */
    boolean deleteChatMessage(DeleteChatRequest request);

    /**
     * 删除整个会话并清理 Redis 缓存。
     *
     * @param request 会话标识
     * @return 是否成功
     */
    boolean deleteChatConversation(DeleteChatRequest request);

    /**
     * 停止指定会话下的生成：本机有 SSE 则直接 complete，否则向 Redis 发布停止消息。
     *
     * @param conversationId 会话 ID
     * @param messageId      消息 ID
     * @return 是否已处理（发布或本地停止）
     */
    boolean stopConversation(String conversationId, String messageId);
}