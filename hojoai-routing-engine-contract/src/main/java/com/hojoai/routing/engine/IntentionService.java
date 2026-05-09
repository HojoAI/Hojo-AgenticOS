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
 * 意图调度契约：按模型返回的任务序列依次调用各 {@link AgentService}，并在停止标志出现时中断。
 */
public interface IntentionService {

    /**
     * 顺序执行任务序列中的子智能体调用，将落库消息通过 {@code dataHandler} 回传。
     *
     * @param context       当前对话上下文（会话、用户输入等）
     * @param taskSequences 模型规划的任务列表
     * @param emitter       SSE，用于下发加载态与流式内容
     * @param dataHandler   每个任务完成后待持久化的消息列表消费者
     */
    void intentDispatch(ConversationContext context,
                         List<TaskSequence> taskSequences,
                         SseEmitter emitter,
                         Consumer<List<ConversationAddMessageRequest>> dataHandler);
}
