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

import java.io.Serializable;

/**
 * 通过 Redis Pub/Sub 广播的停止指令载荷，由 {@link com.hojoai.omniasst.sysagent.service.conversation.manager.RedisMessageListenerConfig} 消费。
 */
public class StopMessage implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 会话 ID。 */
    private String conversationId;
    /** 消息 ID。 */
    private String messageId;
    /** 发送停止指令的时间戳（毫秒）。 */
    private long timestamp;

    public StopMessage() {
    }

    public StopMessage(String conversationId, String messageId) {
        this.conversationId = conversationId;
        this.messageId = messageId;
        this.timestamp = System.currentTimeMillis();
    }

    public StopMessage(String conversationId, String messageId, Long timestamp) {
        this.conversationId = conversationId;
        this.messageId = messageId;
        this.timestamp = timestamp;
    }

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

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "StopMessage{" +
                "conversationId='" + conversationId + '\'' +
                ", messageId='" + messageId + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}
