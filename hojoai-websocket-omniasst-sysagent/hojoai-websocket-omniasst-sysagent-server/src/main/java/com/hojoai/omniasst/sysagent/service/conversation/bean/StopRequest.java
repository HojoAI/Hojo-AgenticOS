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

/**
 * 前端请求停止当前轮生成时提交的参数。
 */
public class StopRequest {
    /** 会话 ID。 */
    private String conversationId;
    /** 当前 assistant 消息/轮次 ID，与 SSE 注册键一致。 */
    private String messageId;

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

    @Override
    public String toString() {
        return "StopRequest{" +
                "conversationId='" + conversationId + '\'' +
                ", messageId='" + messageId + '\'' +
                '}';
    }
}
