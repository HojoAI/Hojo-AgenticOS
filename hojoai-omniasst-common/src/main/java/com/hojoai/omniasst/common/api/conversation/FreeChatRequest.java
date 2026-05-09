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

import java.util.List;

/**
 * 自由对话请求体：按会话维度提交多段内容，支持文本与媒体 URL 混合。
 */
public class FreeChatRequest {
    
    /** 会话 ID */
    private String conversationId;

    /** 有序内容片段列表 */
    private List<ContentItem> contents;

    /** 请求级扩展字段 */
    private Object extra;

    /**
     * 单段内容：根据 type 选择填充 text 或 url。
     */
    public static class ContentItem {

        /** 内容类型：text / image / pdf / audio / video 等 */
        private String type;

        /** 非文本类型时的资源地址（如 OSS URL） */
        private String url;

        /** type 为 text 时的正文 */
        private String text;

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }
    }

    public String getConversationId() {
        return conversationId;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }

    public Object getExtra() {
        return extra;
    }

    public void setExtra(Object extra) {
        this.extra = extra;
    }

    public List<ContentItem> getContents() {
        return contents;
    }

    public void setContents(List<ContentItem> contents) {
        this.contents = contents;
    }
}