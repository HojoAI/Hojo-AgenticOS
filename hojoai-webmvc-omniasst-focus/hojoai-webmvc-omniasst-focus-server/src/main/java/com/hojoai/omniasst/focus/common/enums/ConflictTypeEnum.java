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
 * Conflict Type Enum
 *
 * @author My
 * @version 1.0 2024/12/29
 * @since hojoai-webmvc-omniasst.focus 1.0.0
 */
public enum ConflictTypeEnum {

    FULL_OVERLAP(1, "Full Overlap"),
    PARTIAL_OVERLAP(2, "Partial Overlap"),
    ADJACENT(3, "Adjacent");

    private final Integer code;
    private final String value;

    ConflictTypeEnum(Integer code, String value) {
        this.code = code;
        this.value = value;
    }

    public Integer getCode() {
        return code;
    }

    public String getValue() {
        return value;
    }

    /**
     * Get enum by code
     */
    public static ConflictTypeEnum getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (ConflictTypeEnum typeEnum : ConflictTypeEnum.values()) {
            if (typeEnum.getCode().equals(code)) {
                return typeEnum;
            }
        }
        return null;
    }
}

