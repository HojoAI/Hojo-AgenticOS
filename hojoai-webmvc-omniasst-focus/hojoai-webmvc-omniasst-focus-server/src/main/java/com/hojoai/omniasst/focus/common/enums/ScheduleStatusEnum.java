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
 * Schedule Status Enum
 *
 * @author My
 * @version 1.0 2024/12/29
 * @since hojoai-webmvc-omniasst.focus 1.0.0
 */
public enum ScheduleStatusEnum {

    PENDING(0, "Pending"),
    IN_PROGRESS(1, "In Progress"),
    COMPLETED(2, "Completed"),
    CANCELLED(3, "Cancelled");

    private final Integer code;
    private final String value;

    ScheduleStatusEnum(Integer code, String value) {
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
    public static ScheduleStatusEnum getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (ScheduleStatusEnum statusEnum : ScheduleStatusEnum.values()) {
            if (statusEnum.getCode().equals(code)) {
                return statusEnum;
            }
        }
        return null;
    }
}

