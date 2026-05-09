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

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.lang3.ObjectUtils;

/**
 * The enum for conversation role.
 *
 * @author zhangxuepeng
 * @version 1.0, 2025/12/10
 * @since hojoai-webmvc-omniasst-management 1.0.0
 */
public enum ConversationRoleEnum {

    USER("user", "用户"),
    ASSISTANT("assistant", "助手"),
    SYSTEM("system", "系统");

    private final String value;
    private final String text;

    ConversationRoleEnum(String value, String text) {
        this.value = value;
        this.text = text;
    }

    /**
     * 获取值列表
     *
     * @return 值列表
     */
    public static List<String> getValues() {
        return Arrays.stream(values()).map(item -> item.value).collect(Collectors.toList());
    }

    /**
     * 根据 value 获取枚举
     *
     * @param value 枚举值
     * @return 枚举对象，如果不存在返回 null
     */
    public static ConversationRoleEnum getEnumByValue(String value) {
        if (ObjectUtils.isEmpty(value)) {
            return null;
        }
        for (ConversationRoleEnum anEnum : ConversationRoleEnum.values()) {
            if (anEnum.value.equals(value)) {
                return anEnum;
            }
        }
        return null;
    }

    /**
     * 判断角色值是否存在
     *
     * @param value 角色值
     * @return true 表示存在
     */
    public static boolean exists(String value) {
        return getEnumByValue(value) != null;
    }

    public String getValue() {
        return value;
    }

    public String getText() {
        return text;
    }
}

