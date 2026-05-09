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
package com.hojoai.omniasst.common.constans.conversation;

/**
 * 对话中子代理或技能类型，用于多模态/多场景路由。
 */
public enum AgentType {
    /** 普通对话 */
    CHAT("chat"),
    /** 带联网搜索的对话 */
    CHAT_WEBSEARCH("chat_websearch"),
    /** 文档类能力 */
    DOC("doc"),
    /** 翻译 */
    TRANSLATOR("translator"),
    /** 会议相关 */
    MEETING("meeting"),
    /** 日程/事件聚焦 */
    FOCUS("event");

    private final String value;

    AgentType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return value;
    }

    /**
     * Get AgentType by string value
     * @param value the string value
     * @return AgentType enum, or null if not found
     */
    public static AgentType fromValue(String value) {
        for (AgentType type : AgentType.values()) {
            if (type.value.equals(value)) {
                return type;
            }
        }
        return null;
    }
}
