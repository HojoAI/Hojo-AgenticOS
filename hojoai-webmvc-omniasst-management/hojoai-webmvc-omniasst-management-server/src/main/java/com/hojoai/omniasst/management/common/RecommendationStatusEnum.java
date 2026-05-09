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

import org.apache.commons.lang3.ObjectUtils;

import java.util.Arrays;
import java.util.List;

public enum RecommendationStatusEnum {

    ACTIVE("active", "有效"),
    IGNORED("ignored", "已忽略"),
    EXPIRED("expired", "已过期");

    private final String value;
    private final String text;

    RecommendationStatusEnum(String value, String text) {
        this.value = value;
        this.text = text;
    }

    public static List<String> getValues() {
        return Arrays.stream(values()).map(item -> item.value).toList();
    }

    public static RecommendationStatusEnum getEnumByValue(String value) {
        if (ObjectUtils.isEmpty(value)) {
            return null;
        }
        for (RecommendationStatusEnum item : values()) {
            if (item.value.equals(value)) {
                return item;
            }
        }
        return null;
    }

    public static boolean exists(String value) {
        return getEnumByValue(value) != null;
    }

    public String getValue() { return value; }
    public String getText() { return text; }
}
