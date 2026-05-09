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
 * 消息业务大类：普通会话或推送类消息。
 */
public enum MessageCategory {
    /** 用户主动发起的聊天 */
    CHAT("Chat"),
    /** 系统或服务端推送 */
    PUSH("Push");

    private final String value;

    MessageCategory(String value) {
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
     * Get MessageCategory by string value
     * @param value the string value
     * @return MessageCategory enum, or null if not found
     */
    public static MessageCategory fromValue(String value) {
        for (MessageCategory category : MessageCategory.values()) {
            if (category.value.equals(value)) {
                return category;
            }
        }
        return null;
    }
}
