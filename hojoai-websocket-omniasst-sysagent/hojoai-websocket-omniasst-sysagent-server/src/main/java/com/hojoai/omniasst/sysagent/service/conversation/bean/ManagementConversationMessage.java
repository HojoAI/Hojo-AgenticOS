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
import java.util.List;

/**
 * management 返回的单条会话消息视图（含多段内容），与列表查询接口 JSON 对齐。
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ManagementConversationMessage {
    /** 会话 ID。 */
    private String conversationId;
    /** 消息 ID。 */
    private String messageId;
    /** 关联的回复消息 ID（若有）。 */
    private String responseMessageId;
    /** 消息角色。 */
    private String role;
    /** 形象 ID。 */
    private String avatarId;
    /** 消息时间戳。 */
    private Long timestamp;
    /** 内容块列表。 */
    private List<MessageContentItem> content;
    
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

    public String getResponseMessageId() {
        return responseMessageId;
    }

    public void setResponseMessageId(String responseMessageId) {
        this.responseMessageId = responseMessageId;
    }

    public String getRole() {
        return role;
    }
    
    public void setRole(String role) {
        this.role = role;
    }

    public String getAvatarId() {
        return avatarId;
    }

    public void setAvatarId(String avatarId) {
        this.avatarId = avatarId;
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

    /**
     * 单段内容：文本、链接或扩展字段。
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class MessageContentItem {
        /** 内容类型，如 text、image。 */
        private String type;
        /** 子类型，如 bubble、body。 */
        private String subType;
        /** 文本正文。 */
        private String text;
        /** 非文本资源的 URL。 */
        private String url;
        /** 产生该段的智能体标识。 */
        private String agent;
        /** 模型/展示角色。 */
        private String role;
        /** 内容块 ID。 */
        private String contentId;
        /** 扩展 JSON。 */
        private Object extra;
        
        public String getType() {
            return type;
        }
        
        public void setType(String type) {
            this.type = type;
        }
        
        public String getSubType() {
            return subType;
        }
        
        public void setSubType(String subType) {
            this.subType = subType;
        }
        
        public String getText() {
            return text;
        }
        
        public void setText(String text) {
            this.text = text;
        }
        
        public String getUrl() {
            return url;
        }
        
        public void setUrl(String url) {
            this.url = url;
        }
        
        public String getAgent() {
            return agent;
        }
        
        public void setAgent(String agent) {
            this.agent = agent;
        }

        public String getRole() {
            return role;
        }

        public void setRole(String role) {
            this.role = role;
        }

        public String getContentId() {
            return contentId;
        }

        public void setContentId(String contentId) {
            this.contentId = contentId;
        }

        public Object getExtra() {
            return extra;
        }

        public void setExtra(Object extra) {
            this.extra = extra;
        }

        @Override
        public String toString() {
            return "MessageContentItem{" +
                    "type='" + type + '\'' +
                    ", subType='" + subType + '\'' +
                    ", text='" + text + '\'' +
                    ", url='" + url + '\'' +
                    ", agent='" + agent + '\'' +
                    ", role='" + role + '\'' +
                    ", contentId='" + contentId + '\'' +
                    ", extra=" + extra +
                    '}';
        }
    }

    @Override
    public String toString() {
        return "ManagementConversationMessage{" +
                "conversationId='" + conversationId + '\'' +
                ", messageId='" + messageId + '\'' +
                ", responseMessageId='" + responseMessageId + '\'' +
                ", role='" + role + '\'' +
                ", avatarId='" + avatarId + '\'' +
                ", timestamp=" + timestamp +
                ", content=" + content +
                '}';
    }
}