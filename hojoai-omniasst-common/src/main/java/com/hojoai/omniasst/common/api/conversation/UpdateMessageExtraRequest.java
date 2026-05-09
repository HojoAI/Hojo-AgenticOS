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

/**
 * 更新指定消息某内容块扩展字段（extra）的请求体。
 */
public class UpdateMessageExtraRequest {

    /**
     * 对话ID
     */
    private String conversationId;

    /**
     * 消息ID
     */
    private String messageId;

    /**
     * 内容ID
     */
    private String contentId;

    /**
     * 额外信息
     */
    private Object extra;

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
        return "UpdateMessageExtraRequest{" +
                "conversationId='" + conversationId + '\'' +
                ", messageId='" + messageId + '\'' +
                ", contentId='" + contentId + '\'' +
                ", extra=" + extra +
                '}';
    }
}