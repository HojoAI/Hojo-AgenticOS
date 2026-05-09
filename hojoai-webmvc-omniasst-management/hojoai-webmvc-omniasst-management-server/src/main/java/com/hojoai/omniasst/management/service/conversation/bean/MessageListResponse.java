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
import com.hojoai.omniasst.common.api.avatar.AvatarItem;
import com.hojoai.omniasst.common.api.conversation.MessageContentItem;

import java.util.List;

/**
 * The response bean for conversation message item.
 *
 * @author zhangxuepeng
 * @version 1.0, 2025/12/10
 * @since hojoai-webmvc-omniasst-management 1.0.0
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MessageListResponse {

    /**
     * 消息ID
     */
    private String messageId;

    /**
     * 对话ID
     */
    private String conversationId;

    /**
     * 消息角色
     */
    private String role;

    /**
     * 消息内容（JSON 数组）
     */
    private List<MessageContentItem> contentJson;

    private Long timestamp;

    /**
     * 形象信息
     */
    private AvatarItem avatar;

    /**
     * 是否已读
     */
    private Boolean isRead;

    /**
     * 消息类型
     */
    private String messageType;

    /**
     * 创建时间（yyyy-MM-dd HH:mm:ss）
     */
    private String createdAt;

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

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

    public List<MessageContentItem> getContentJson() {
        return contentJson;
    }

    public void setContentJson(List<MessageContentItem> contentJson) {
        this.contentJson = contentJson;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public AvatarItem getAvatar() {
        return avatar;
    }

    public void setAvatar(AvatarItem avatar) {
        this.avatar = avatar;
    }

    public Boolean getIsRead() {
        return isRead;
    }

    public void setIsRead(Boolean isRead) {
        this.isRead = isRead;
    }

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }
}
