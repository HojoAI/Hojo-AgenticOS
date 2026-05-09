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
package com.hojoai.routing.engine;

import com.hojoai.omniasst.common.api.conversation.ConversationAddMessageRequest;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.function.Consumer;

/**
 * 子智能体调用契约：向模型侧 agent HTTP/SSE 接口发起请求并拼装待落库消息。
 */
public interface AgentService {

    /**
     * 执行单步任务：携带 {@code taskSequence} 与上下文请求下游，流式结果写入 SSE，结束后回调持久化列表。
     *
     * @param context       对话上下文
     * @param taskSequence  当前子任务（目标 agent、参数等）
     * @param emitter       SSE
     * @param dataHandler   聚合后的 management 消息列表
     */
    void callAgent(ConversationContext context,
                   TaskSequence taskSequence,
                   SseEmitter emitter,
                   Consumer<List<ConversationAddMessageRequest>> dataHandler);
}
