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
package com.hojoai.omniasst.weather.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Dify 流式事件 DTO，对应每一行 data: {...}
 *
 * @author wangjian
 * @since 2026-01-22
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class DifyStreamEvent {

    /**
     * 事件类型：message、message_end、tts_message 等
     */
    private String event;

    /**
     * 消息ID
     */
    @JsonProperty("message_id")
    private String messageId;

    /**
     * 会话ID
     */
    @JsonProperty("conversation_id")
    private String conversationId;

    /**
     * 文本答案（流式）
     */
    private String answer;

    /**
     * 创建时间戳（秒）
     */
    @JsonProperty("created_at")
    private Long createdAt;

    /**
     * 结束事件中的ID
     */
    private String id;

    /**
     * 扩展元数据
     */
    private Object metadata;

    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
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

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public Long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Long createdAt) {
        this.createdAt = createdAt;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Object getMetadata() {
        return metadata;
    }

    public void setMetadata(Object metadata) {
        this.metadata = metadata;
    }
}

