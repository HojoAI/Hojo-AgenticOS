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
package com.hojoai.omniasst.management.dao.conversation.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.util.Date;

/**
 * The entity class for message.
 *
 * @author zhangxuepeng
 * @version 1.0, 2025/12/10
 * @since hojoai-webmvc-omniasst-management 1.0.0
 */
@TableName(value = "messages")
public class Message implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField(value = "message_id")
    private String messageId;

    @TableField(value = "conversation_id")
    private String conversationId;

    /**
     * 消息角色：user/assistant/system
     */
    @TableField(value = "role")
    private String role;

    /**
     * 结构化内容（JSON格式，支持多模态）
     */
    @TableField(value = "content_json")
    private String contentJson;

    /**
     * 时间戳
     */
    @TableField(value = "timestamp")
    private Long timestamp;

    /**
     * 响应消息ID
     */
    @TableField(value = "response_message_id")
    private String responseMessageId;

    /**
     * 形象ID
     */
    @TableField(value = "avatar_id")
    private String avatarId;

    /**
     * 是否已读
     */
    @TableField(value = "is_read")
    private Boolean isRead;

    /**
     * 消息类型
     */
    @TableField(value = "message_type")
    private String messageType;

    @TableField(value = "created_at")
    private Date createdAt;

    @TableField(value = "updated_at")
    private Date updatedAt;

    public Message() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public String getContentJson() {
        return contentJson;
    }

    public void setContentJson(String contentJson) {
        this.contentJson = contentJson;
    }

    public Long getTimestamp() { return timestamp; }

    public void setTimestamp(Long timestamp) { this.timestamp = timestamp; }

    public String getResponseMessageId() { return responseMessageId; }

    public void setResponseMessageId(String responseMessageId) { this.responseMessageId = responseMessageId; }

    public String getAvatarId() { return avatarId; }

    public void setAvatarId(String avatarId) { this.avatarId = avatarId; }

    public Boolean getIsRead() { return isRead; }

    public void setIsRead(Boolean isRead) { this.isRead = isRead; }

    public String getMessageType() { return messageType; }

    public void setMessageType(String messageType) { this.messageType = messageType; }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }
}

