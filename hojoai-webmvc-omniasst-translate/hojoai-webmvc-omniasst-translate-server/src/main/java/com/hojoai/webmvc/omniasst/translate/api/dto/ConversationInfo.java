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
package com.hojoai.webmvc.omniasst.translate.api.dto;

import java.util.List;

/**
 * 推送载荷中与会话上下文相关的元数据与内容列表。
 */
public class ConversationInfo {

    /** 业务会话 ID */
    private String conversationId;

    /** 翻译侧会话/任务 ID（chatId） */
    private String translationId;

    /** 消息角色，如 assistant */
    private String role;

    /** 结构化内容列表（气泡、卡片等） */
    private List<ContentVal> contentJson;

    /** 时间戳（毫秒） */
    private Long timestamp;

    /** 会话创建时间字符串 */
    private String createdAt;

    public String getConversationId() {
        return conversationId;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }

    public String getTranslationId() {
        return translationId;
    }

    public void setTranslationId(String translationId) {
        this.translationId = translationId;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public List<ContentVal> getContentJson() {
        return contentJson;
    }

    public void setContentJson(List<ContentVal> contentJson) {
        this.contentJson = contentJson;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

}
