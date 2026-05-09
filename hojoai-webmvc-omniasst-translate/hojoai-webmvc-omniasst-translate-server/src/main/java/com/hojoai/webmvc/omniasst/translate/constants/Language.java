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
package com.hojoai.webmvc.omniasst.translate.constants;

/**
 * 内置语言枚举（示例/常量用途），值为小写英文标识。
 */
public enum Language {
    /** 中文 */
    CHINESE("chinese"),
    /** 英文 */
    ENGLISH("english"),
    /** 法文 */
    FRENCH("french");

    private final String value;

    Language(String value) { this.value = value; }
    public String getValue() { return value; }

    /**
     * 按字符串解析枚举，忽略大小写。
     *
     * @param value 语言标识
     * @return 匹配项，无法匹配时为 null
     */
    public static Language fromString(String value) {
        if (value == null) return null;
        for (Language lang : Language.values()) {
            if (lang.value.equalsIgnoreCase(value)) return lang;
        }
        return null;
    }
}
