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

/** 帧头长度档位，实际字节数为 {@code value * 4}。 */
@Getter
public enum HeaderSizeBits {
    HeaderSize4((byte) 1),
    HeaderSize8((byte) 2),
    HeaderSize12((byte) 3),
    HeaderSize16((byte) 4),
    ;

    private final byte value;

    HeaderSizeBits(byte b) {
        this.value = b;
    }

    /**
     * @param value 头部长度编码
     * @return 枚举
     * @throws IllegalArgumentException 未知取值
     */
    public static HeaderSizeBits fromValue(int value) {
        for (HeaderSizeBits type : HeaderSizeBits.values()) {
            if (type.value == value) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown HeaderSizeBits value: " + value);
    }
}
