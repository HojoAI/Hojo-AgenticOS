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
package com.hojoai.omniasst.focus.common.enums;

/**
 * Conflict Level Enum
 *
 * @author My
 * @version 1.0 2024/12/29
 * @since hojoai-webmvc-omniasst.focus 1.0.0
 */
public enum ConflictLevelEnum {

    MINOR(1, "Minor", "<=15 minutes"),
    MODERATE(2, "Moderate", "15-60 minutes"),
    SEVERE(3, "Severe", ">60 minutes");

    private final Integer code;
    private final String value;
    private final String message;

    ConflictLevelEnum(Integer code, String value, String message) {
        this.code = code;
        this.value = value;
        this.message = message;
    }

    public Integer getCode() {
        return code;
    }

    public String getValue() {
        return value;
    }

    public String getMessage() {
        return message;
    }

    /**
     * Get enum by code
     */
    public static ConflictLevelEnum getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (ConflictLevelEnum levelEnum : ConflictLevelEnum.values()) {
            if (levelEnum.getCode().equals(code)) {
                return levelEnum;
            }
        }
        return null;
    }

    /**
     * Get conflict level by overlap minutes
     */
    public static ConflictLevelEnum getByOverlapMinutes(int overlapMinutes) {
        if (overlapMinutes <= 15) {
            return MINOR;
        } else if (overlapMinutes <= 60) {
            return MODERATE;
        } else {
            return SEVERE;
        }
    }
}

