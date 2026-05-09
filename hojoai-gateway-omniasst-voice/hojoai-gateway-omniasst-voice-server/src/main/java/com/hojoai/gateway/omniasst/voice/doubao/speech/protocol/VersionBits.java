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

/** 协议版本号（帧头高 4 bit）。 */
@Getter
public enum VersionBits {
    Version1((byte) 1),
    Version2((byte) 2),
    Version3((byte) 3),
    Version4((byte) 4),
    ;

    private final byte value;

    VersionBits(byte b) {
        this.value = b;
    }

    /**
     * @param value 版本码
     * @return 枚举
     * @throws IllegalArgumentException 未知版本
     */
    public static VersionBits fromValue(int value) {
        for (VersionBits type : VersionBits.values()) {
            if (type.value == value) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown VersionBits value: " + value);
    }
}
