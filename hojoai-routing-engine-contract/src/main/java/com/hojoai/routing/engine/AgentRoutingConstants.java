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
 * 意图路由引擎使用的 Agent 字符串常量与 SSE 事件名常量。
 * <p>
 * 从 {@link com.hojoai.omniasst.sysagent.constans.ConversationConstant} 提取的子集，
 * 仅包含路由引擎需要的常量，避免闭源模块依赖完整的 ConversationConstant。
 * </p>
 */
public class AgentRoutingConstants {

    /** 智能体：通用聊天。 */
    public static final String AGENT_CHAT = "chat";

    /** 智能体：联网搜索聊天。 */
    public static final String AGENT_CHAT_WEBSEARCH = "chat_websearch";

    /** 智能体：文档/图文。 */
    public static final String AGENT_DOC = "doc";

    /** 智能体：翻译。 */
    public static final String AGENT_TRANSLATOR = "translator";

    /** 智能体：会议。 */
    public static final String AGENT_MEETING = "meeting";

    /** 智能体：日程/专注（focus 服务）。 */
    public static final String AGENT_FOCUS = "event";

    /** 智能体：音乐。 */
    public static final String AGENT_MUSIC = "music";

    /** 智能体：天气。 */
    public static final String AGENT_WEATHER = "weather";

    /** 智能体：播客/资讯推荐。 */
    public static final String AGENT_PODCAST = "podcast";

    /** SSE 事件名：即将调用的子智能体类型。 */
    public static final String EVENT_NAME_AGENT_TYPE = "agentType";

    private AgentRoutingConstants() {
    }
}
