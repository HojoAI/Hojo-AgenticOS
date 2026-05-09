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
package com.hojoai.omniasst.management.common;

public enum VoiceProviderEnum {
    HOJO("hojo", "恒聚"),
    MINIMAX("minimax", "MINIMAX");

    private final String code;
    private final String name;

    VoiceProviderEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public static VoiceProviderEnum fromCode(String code) {
        if (code == null || code.isBlank()) {
            return HOJO;
        }
        String t = code.trim();
        for (VoiceProviderEnum provider : values()) {
            if (provider.code.equalsIgnoreCase(t)) {
                return provider;
            }
        }
        return HOJO;
    }

    /**
     * 是否已登记的厂商代码（忽略首尾空白与大小写）。
     */
    public static boolean isValid(String code) {
        if (code == null || code.isBlank()) {
            return false;
        }
        String t = code.trim();
        for (VoiceProviderEnum provider : values()) {
            if (provider.code.equalsIgnoreCase(t)) {
                return true;
            }
        }
        return false;
    }

    /** 规范化为枚举中的 code（小写），未知则返回 null */
    public static String normalizeCode(String code) {
        if (code == null || code.isBlank()) {
            return null;
        }
        String t = code.trim();
        for (VoiceProviderEnum provider : values()) {
            if (provider.code.equalsIgnoreCase(t)) {
                return provider.code;
            }
        }
        return null;
    }
}
