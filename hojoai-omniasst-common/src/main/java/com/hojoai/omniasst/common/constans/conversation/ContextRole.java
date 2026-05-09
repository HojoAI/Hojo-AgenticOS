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
package com.hojoai.omniasst.common.constans.conversation;

/**
 * 消息在上下文中的角色维度（与系统 role 区分，用于 UI 或编排）。
 */
public enum ContextRole {
    /** 智能体侧 */
    AGENT("agent"),
    /** 用户侧 */
    USER("user");

    private final String value;

    ContextRole(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return value;
    }

    /**
     * Get ContextRole by string value
     * @param value the string value
     * @return ContextRole enum, or null if not found
     */
    public static ContextRole fromValue(String value) {
        for (ContextRole role : ContextRole.values()) {
            if (role.value.equals(value)) {
                return role;
            }
        }
        return null;
    }
}
