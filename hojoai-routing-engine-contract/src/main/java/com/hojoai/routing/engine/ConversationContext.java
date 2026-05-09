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
package com.hojoai.routing.engine;

/**
 * 单次「用户轮次」的上下文：会话维度 + 用户画像与地理信息。
 */
public class ConversationContext {

    /** 会话 ID。 */
    private final String conversationId;

    /** 当前轮消息 ID。 */
    private final String messageId;

    /** 用户 ID。 */
    private final String userId;

    /** 用户侧信息（地理、昵称等），供模型与智能体使用。 */
    private final UserInput input;

    /** 耳机模式：为 true 时聊天/默认路由可串联多智能体调用。 */
    private final boolean headphoneMode;

    private ConversationContext(Builder builder) {
        this.conversationId = builder.conversationId;
        this.messageId = builder.messageId;
        this.userId = builder.userId;
        this.input = builder.input;
        this.headphoneMode = builder.headphoneMode;
    }

    public String getConversationId() {
        return conversationId;
    }

    public String getMessageId() {
        return messageId;
    }

    public String getUserId() {
        return userId;
    }

    public UserInput getInput() {
        return input;
    }

    public boolean isHeadphoneMode() {
        return headphoneMode;
    }

    /**
     * 复合键 {@code conversationId + messageId}，用于时间戳 Map、停止判断等。
     */
    public String getKey() {
        return this.conversationId + this.messageId;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private String conversationId;
        private String messageId;
        private String userId;
        private UserInput input;
        private boolean headphoneMode;

        private Builder() {
        }

        public Builder conversationId(String conversationId) {
            this.conversationId = conversationId;
            return this;
        }

        public Builder messageId(String messageId) {
            this.messageId = messageId;
            return this;
        }

        public Builder userId(String userId) {
            this.userId = userId;
            return this;
        }

        public Builder input(UserInput input) {
            this.input = input;
            return this;
        }

        public Builder headphoneMode(boolean headphoneMode) {
            this.headphoneMode = headphoneMode;
            return this;
        }

        public ConversationContext build() {
            if (conversationId == null || conversationId.isEmpty()) {
                throw new IllegalArgumentException("conversationId cannot be null or empty");
            }
            if (messageId == null || messageId.isEmpty()) {
                throw new IllegalArgumentException("messageId cannot be null or empty");
            }
            return new ConversationContext(this);
        }
    }
}
