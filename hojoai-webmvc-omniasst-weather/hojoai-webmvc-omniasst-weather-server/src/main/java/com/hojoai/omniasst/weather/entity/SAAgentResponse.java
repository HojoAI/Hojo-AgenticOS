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

/**
 * SA智能体响应DTO
 * 
 * @author wangjian
 * @since 2026-01-22
 */
public class SAAgentResponse {
    
    /**
     * 会话ID
     */
    private String conversationId;
    
    /**
     * 消息ID
     */
    private String messageId;
    
    /**
     * 数据
     * 流式传输时：SAAgentDataItem
     * 最后一帧（finished=true）时：List<SAAgentDataItem> 或 ArrayNode（JSON数组）
     */
    private Object data;
    
    /**
     * 时间戳
     */
    private Long timestamp;
    
    /**
     * 是否完成
     */
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

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public Boolean getFinished() {
        return finished;
    }

    public void setFinished(Boolean finished) {
        this.finished = finished;
    }
    
    /**
     * SA数据项
     */
    public static class SAAgentDataItem {
        
        /**
         * 类型
         */
        private String type;
        
        /**
         * 子类型
         */
        private String subType;
        
        /**
         * 文本内容
         */
        private String text;

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
    }
}

