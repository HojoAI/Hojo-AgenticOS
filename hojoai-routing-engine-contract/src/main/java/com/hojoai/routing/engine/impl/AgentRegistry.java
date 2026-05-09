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
package com.hojoai.routing.engine.impl;

import com.hojoai.routing.engine.AgentService;

/**
 * 按 {@code agentType} 解析 {@link AgentService} 的注册表契约。
 * <p>
 * 由 {@code sysagent} 中的 {@code SysagentAgentRegistry} 实现；意图调度实现依赖本接口以查找各垂直智能体。
 * </p>
 */
public interface AgentRegistry {

    /**
     * 根据意图模型返回的 {@code targetAgent} 标识解析已注册的 {@link AgentService}。
     *
     * @param agentType 与 {@link com.hojoai.routing.engine.AgentRoutingConstants} 中 AGENT_* 一致的字符串
     * @return 对应实现，未注册时返回 {@code null}
     */
    AgentService getAgent(String agentType);
}
