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

/**
 * 停止信号检查接口。
 * <p>
 * 由 {@link com.hojoai.omniasst.sysagent.service.conversation.manager.SseEmitterManager} 实现，
 * 闭源路由引擎仅依赖此接口以实现停止检查。
 * </p>
 */
public interface StopSignalProvider {

    /**
     * 检查指定会话+消息是否设置了停止标志。
     *
     * @param conversationId 会话 ID
     * @param messageId      消息 ID
     * @return true 表示用户已请求停止，路由引擎应中断后续分发
     */
    boolean shouldStop(String conversationId, String messageId);
}
