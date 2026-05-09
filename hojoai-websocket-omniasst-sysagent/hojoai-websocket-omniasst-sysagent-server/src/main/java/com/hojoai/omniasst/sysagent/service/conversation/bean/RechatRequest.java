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
 * 重聊请求：基于已有会话与用户补充上下文再次调用意图模型。
 */
public class RechatRequest {
    /** 会话 ID。 */
    private String conversationId;
    /** 锚定消息 ID（与业务约定一致）。 */
    private String messageId;
    /** 数字人形象 ID，影响助手性别等侧写。 */
    private String avatarId;
    /** 用户、地理位置等侧信息。 */
    private ConversationRequest.Inputs inputs;

    /** 耳机模式：为 true 时与 {@link ConversationRequest#headphoneMode} 语义一致，按业务日单会话。 */
    private boolean headphoneMode = false;

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

    public ConversationRequest.Inputs getInputs() {
        return inputs;
    }

    public void setInputs(ConversationRequest.Inputs inputs) {
        this.inputs = inputs;
    }

    public boolean isHeadphoneMode() {
        return headphoneMode;
    }

    public void setHeadphoneMode(boolean headphoneMode) {
        this.headphoneMode = headphoneMode;
    }

    @Override
    public String toString() {
        return "RechatRequest{" +
                "conversationId='" + conversationId + '\'' +
                ", messageId='" + messageId + '\'' +
                ", avatarId='" + avatarId + '\'' +
                ", inputs=" + inputs +
                ", headphoneMode=" + headphoneMode +
                '}';
    }
}