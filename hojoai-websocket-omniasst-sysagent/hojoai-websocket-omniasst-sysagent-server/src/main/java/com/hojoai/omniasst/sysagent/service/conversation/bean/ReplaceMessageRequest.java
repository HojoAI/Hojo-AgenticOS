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

import com.hojoai.omniasst.common.api.conversation.ConversationAddMessageRequest;

/**
 * 替换会话中某条历史消息：指定旧 messageId 与新消息体。
 */
public class ReplaceMessageRequest {

    /**
     * 对话ID（必填）
     */
    private String conversationId;

    /**
     * 旧消息ID（必填）
     */
    private String oldMessageId;

    /**
     * 新消息请求对象（必填）
     */
    private ConversationAddMessageRequest newMessageRequest;

    public String getConversationId() {
        return conversationId;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }

    public String getOldMessageId() {
        return oldMessageId;
    }

    public void setOldMessageId(String oldMessageId) {
        this.oldMessageId = oldMessageId;
    }

    public ConversationAddMessageRequest getNewMessageRequest() {
        return newMessageRequest;
    }

    public void setNewMessageRequest(ConversationAddMessageRequest newMessageRequest) {
        this.newMessageRequest = newMessageRequest;
    }
}

