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
package com.hojoai.gateway.omniasst.voice.doubao.speech.protocol;

import lombok.Getter;

/**
 * 消息类型低 4 bit 标志：是否带序号、是否含 event 等（与 {@link MsgType} 组合出现在帧头）。
 */
@Getter
public enum MsgTypeFlagBits {
    NO_SEQ((byte) 0),           // Non-terminating packet without sequence number
    POSITIVE_SEQ((byte) 0b1),   // Non-terminating packet with positive sequence number
    LAST_NO_SEQ((byte) 0b10),   // Terminating packet without sequence number
    NEGATIVE_SEQ((byte) 0b11),  // Terminating packet with negative sequence number
    WITH_EVENT((byte) 0b100);   // Packet containing event number

    private final byte value;

    MsgTypeFlagBits(byte value) {
        this.value = value;
    }

    /**
     * @param value 低 4 bit 原始值
     * @return 标志枚举
     * @throws IllegalArgumentException 未知标志
     */
    public static MsgTypeFlagBits fromValue(int value) {
        for (MsgTypeFlagBits flag : MsgTypeFlagBits.values()) {
            if (flag.value == value) {
                return flag;
            }
        }
        throw new IllegalArgumentException("Unknown MsgTypeFlagBits value: " + value);
    }
} 