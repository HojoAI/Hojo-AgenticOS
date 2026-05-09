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
package com.hojoai.omniasst.sysagent.service.conversation.bean;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * management 返回的会话与首条消息标识。
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CreateConversationIdResponse {
    /** 会话 ID。 */
    private String conversationId;

    /** 本轮用户消息 ID（或 management 分配的消息 ID）。 */
    private String messageId;

    public String getConversationId() {
        return conversationId;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }
}
