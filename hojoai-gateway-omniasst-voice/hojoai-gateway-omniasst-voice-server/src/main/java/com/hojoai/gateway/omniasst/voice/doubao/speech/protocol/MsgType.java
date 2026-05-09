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
 * 协议首字节中的消息类型（高 4 bit），区分全量请求/响应、纯音频、错误等。
 */
@Getter
public enum MsgType {
    INVALID((byte) 0),
    FULL_CLIENT_REQUEST((byte) 0b1),
    AUDIO_ONLY_CLIENT((byte) 0b10),
    FULL_SERVER_RESPONSE((byte) 0b1001),
    AUDIO_ONLY_SERVER((byte) 0b1011),
    FRONT_END_RESULT_SERVER((byte) 0b1100),
    ERROR((byte) 0b1111);

    private final byte value;

    MsgType(byte value) {
        this.value = value;
    }

    /**
     * @param value 4 bit 类型码
     * @return 枚举
     * @throws IllegalArgumentException 未知类型
     */
    public static MsgType fromValue(int value) {
        for (MsgType type : MsgType.values()) {
            if (type.value == value) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown MsgType value: " + value);
    }
}