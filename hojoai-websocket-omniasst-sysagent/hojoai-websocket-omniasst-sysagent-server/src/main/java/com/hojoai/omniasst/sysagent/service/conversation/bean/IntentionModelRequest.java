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
import com.hojoai.routing.engine.UserInput;

import java.util.List;

/**
 * 调用意图模型（SA）的请求体：历史上下文 + 用户侧 {@link Input}。
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class IntentionModelRequest {
    /** 会话/设备侧会话标识。 */
    private String sessionId;

    /** 设备类型（手机、眼镜等），由协议约定。 */
    private String deviceType;

    /** 多轮消息上下文。 */
    private List<IntentionModelRequest.MessageContext> context;

    /** 用户画像与地理信息。 */
    private IntentionModelRequest.Input input;

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    public List<IntentionModelRequest.MessageContext> getContext() {
        return context;
    }

    public void setContext(List<IntentionModelRequest.MessageContext> context) {
        this.context = context;
    }

    public IntentionModelRequest.Input getInput() {
        return input;
    }

    public void setInput(IntentionModelRequest.Input input) {
        this.input = input;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class MessageContext {
        /** 模型角色：user/system/agent 等。 */
        private String role;
        /** 文本内容。 */
        private String text;
        /** 内容类型。 */
        private String contentType;
        /** 子类型。 */
        private String subType;
        /** 附件或文件 URL。 */
        private String file;
        /** 来源智能体标识。 */
        private String agent;

        public String getRole() {
            return role;
        }

        public void setRole(String role) {
            this.role = role;
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

        public String getContentType() {
            return contentType;
        }

        public void setContentType(String contentType) {
            this.contentType = contentType;
        }

        public String getFile() {
            return file;
        }

        public void setFile(String file) {
            this.file = file;
        }

        public String getAgent() {
            return agent;
        }

        public void setAgent(String agent) {
            this.agent = agent;
        }

        public String getSubType() {
            return subType;
        }

        public void setSubType(String subType) {
            this.subType = subType;
        }

        @Override
        public String toString() {
            return "MessageContext{" +
                    "role='" + role + '\'' +
                    ", text='" + text + '\'' +
                    ", contentType='" + contentType + '\'' +
                    ", subType='" + subType + '\'' +
                    ", file='" + file + '\'' +
                    ", agent='" + agent + '\'' +
                    '}';
        }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Input extends UserInput {
    }

    @Override
    public String toString() {
        return "IntentionModelRequest{" +
                "sessionId='" + sessionId + '\'' +
                ", deviceType='" + deviceType + '\'' +
                ", context=" + context +
                ", input=" + input +
                '}';
    }
}
