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
package com.hojoai.omniasst.common.api.conversation;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

/**
 * 向已有会话追加一条消息的入参模型（多模态 content 列表）。
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ConversationAddMessageRequest {

    /**
     * 对话ID（必填）
     */
    private String conversationId;

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 消息ID（必填）
     */
    private String messageId;

    /**
     * 消息角色：user/assistant/system
     */
    private String role;

    /**
     * 消息内容数组（多模态）
     */
    private List<MessageContentItem> content;

    /**
     * 时间戳
     */
    private Long timestamp;

    /**
     * 响应消息ID
     */
    private String responseMessageId;

    /**
     * 形象ID
     */
    private String avatarId;

    /**
     * 是否已读，默认为 true
     */
    private Boolean isRead;

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

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public List<MessageContentItem> getContent() {
        return content;
    }

    public void setContent(List<MessageContentItem> content) {
        this.content = content;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public String getResponseMessageId() {
        return responseMessageId;
    }

    public void setResponseMessageId(String responseMessageId) {
        this.responseMessageId = responseMessageId;
    }

    public String getAvatarId() {
        return avatarId;
    }

    public void setAvatarId(String avatarId) {
        this.avatarId = avatarId;
    }

    public Boolean getIsRead() {
        return isRead;
    }

    public void setIsRead(Boolean isRead) {
        this.isRead = isRead;
    }
}
