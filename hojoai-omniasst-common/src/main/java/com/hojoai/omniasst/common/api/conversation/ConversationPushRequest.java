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
 * 会话推送请求：向指定用户会话写入一条带标题的多模态消息。
 */
public class ConversationPushRequest {
    /** 接收用户 ID */
    private String userId;
    /** 目标会话 ID */
    private String conversationId;
    /** 消息内容块列表 */
    private List<MessageContentItem> content;
    /** 会话或消息标题 */
    private String title;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getConversationId() {
        return conversationId;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }

    public List<MessageContentItem> getContent() {
        return content;
    }

    public void setContent(List<MessageContentItem> content) {
        this.content = content;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
