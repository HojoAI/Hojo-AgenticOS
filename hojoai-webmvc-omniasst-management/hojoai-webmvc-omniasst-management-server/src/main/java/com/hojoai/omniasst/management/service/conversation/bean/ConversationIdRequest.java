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

/**
 * The request bean for generating conversation id.
 *
 * @author zhangxuepeng
 * @version 1.0, 2025/12/12
 * @since hojoai-webmvc-omniasst-management 1.0.0
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ConversationIdRequest {

    /**
     * 对话ID，可选；存在时复用并生成新的消息ID
     */
    private String conversationId;

    /**
     * 用户ID（必填，用于新建对话）
     */
    private String userId;

    public String getConversationId() {
        return conversationId;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}


