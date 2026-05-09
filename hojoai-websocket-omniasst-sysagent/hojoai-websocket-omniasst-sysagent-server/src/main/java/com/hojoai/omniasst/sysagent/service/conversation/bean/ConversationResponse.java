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
 * 通过 SSE 下发给前端的对话事件载荷（单帧可含多段内容）。
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ConversationResponse {
    /** 会话 ID。 */
    private String conversationId;
    /** 消息 ID。 */
    private String messageId;
    /** 形象 ID。 */
    private String avatarId;
    /** 本帧内容块列表。 */
    private List<DataItem> data;
    /** 与落库对齐的时间戳。 */
    private Long timestamp;
    /** 是否为本轮最后一条。 */
    private Boolean finished;
    
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

    public String getAvatarId() {
        return avatarId;
    }

    public void setAvatarId(String avatarId) {
        this.avatarId = avatarId;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public List<DataItem> getData() {
        return data;
    }
    
    public void setData(List<DataItem> data) {
        this.data = data;
    }
    
    public Boolean getFinished() {
        return finished;
    }
    
    public void setFinished(Boolean finished) {
        this.finished = finished;
    }
    
    /** SSE 单帧内的一段内容（文本或资源）。 */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class DataItem {
        /** 内容类型：text、image、file、audio 等。 */
        private String type;
        /** type 为 text 时的正文。 */
        private String text;
        /** 非 text 时的资源地址。 */
        private String url;
        /** 子类型（如 bubble、body）。 */
        private String subType;
        /** 内容块 ID。 */
        private String contentId;
        
        public String getType() {
            return type;
        }
        
        public void setType(String type) {
            this.type = type;
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

        public String getSubType() {
            return subType;
        }

        public void setSubType(String subType) {
            this.subType = subType;
        }

        public String getContentId() {
            return contentId;
        }

        public void setContentId(String contentId) {
            this.contentId = contentId;
        }
    }
}