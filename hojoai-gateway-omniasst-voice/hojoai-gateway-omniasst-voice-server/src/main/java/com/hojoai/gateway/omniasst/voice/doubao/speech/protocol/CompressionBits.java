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

/** 负载压缩方式（帧头中的 4 bit）。 */
@Getter
public enum CompressionBits {
    None_((byte) 0),
    Gzip((byte) 0b1),
    Custom((byte) 0b11),
    ;

    private final byte value;

    CompressionBits(byte b) {
        this.value = b;
    }

    /**
     * @param value 压缩编码
     * @return 枚举
     * @throws IllegalArgumentException 未知取值
     */
    public static CompressionBits fromValue(int value) {
        for (CompressionBits type : CompressionBits.values()) {
            if (type.value == value) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown CompressionBits value: " + value);
    }
}
