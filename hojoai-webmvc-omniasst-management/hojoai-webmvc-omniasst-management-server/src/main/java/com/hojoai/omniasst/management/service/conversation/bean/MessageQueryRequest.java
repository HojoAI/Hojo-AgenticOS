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
package com.hojoai.omniasst.management.service.conversation.bean;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.hojoai.omniasst.management.common.PageRequest;

import java.util.Objects;

/**
 * The request bean for querying messages in a conversation.
 *
 * @author zhangxuepeng
 * @version 1.0, 2025/12/10
 * @since hojoai-webmvc-omniasst-management 1.0.0
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MessageQueryRequest extends PageRequest {

    /**
     * 对话ID（必填）
     */
    private String conversationId;

    /**
     * 消息角色（可选）
     */
    private String role;

    public String getConversationId() {
        return conversationId;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        MessageQueryRequest that = (MessageQueryRequest) o;
        return Objects.equals(conversationId, that.conversationId) &&
                Objects.equals(role, that.role);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), conversationId, role);
    }
}

