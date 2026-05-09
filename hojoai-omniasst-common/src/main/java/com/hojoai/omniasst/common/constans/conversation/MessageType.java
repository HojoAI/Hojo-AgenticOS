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
 * 对话消息内容类型，与前端/网关约定的 type 字符串一致。
 */
public enum MessageType {
    /** 纯文本 */
    TEXT("text"),
    /** 图片 */
    IMAGE("image"),
    /** 音频 */
    AUDIO("audio"),
    /** 通用文件 */
    FILE("file");

    private final String value;

    MessageType(String value) {
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
     * Get MessageType by string value
     * @param value the string value
     * @return MessageType enum, or null if not found
     */
    public static MessageType fromValue(String value) {
        for (MessageType type : MessageType.values()) {
            if (type.value.equals(value)) {
                return type;
            }
        }
        return null;
    }
}
