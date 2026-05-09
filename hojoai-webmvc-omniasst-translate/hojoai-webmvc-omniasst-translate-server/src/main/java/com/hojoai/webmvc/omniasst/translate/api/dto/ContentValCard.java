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
package com.hojoai.webmvc.omniasst.translate.api.dto;

/**
 * 推送中「翻译回顾卡片」组件结构，序列化后作为 {@link ContentVal#getText()} 载荷。
 */
public class ContentValCard {

    /** 组件名称 */
    private String name = "ConversationReviewCard";

    /** 组件类型 */
    private String type = "component";

    /** 卡片数据 */
    private CardData data;

    /** 卡片内展示字段 */
    public static class CardData {

        /** 翻译会话 chatId */
        private String chatId;

        /** 标题 */
        private String title;

        /** 副标题/摘要 */
        private String subtitle;

        public String getSubtitle() {
            return subtitle;
        }

        public void setSubtitle(String subtitle) {
            this.subtitle = subtitle;
        }

        public String getChatId() {
            return chatId;
        }

        public void setChatId(String chatId) {
            this.chatId = chatId;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public CardData getData() {
        return data;
    }

    public void setData(CardData data) {
        this.data = data;
    }
}
