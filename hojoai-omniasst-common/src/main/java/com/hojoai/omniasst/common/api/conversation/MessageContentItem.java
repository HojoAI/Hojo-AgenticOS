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

import java.util.Map;

/**
 * 单条多模态消息内容项，可组合为 {@link ConversationAddMessageRequest} 的 content 列表。
 */
public class MessageContentItem {
    /**
     * 内容类型：MessageType获取
     */
    private String type;

    /**
     * 文本内容，可选
     */
    private String text;

    /**
     * URL 或资源地址，可选
     */
    private String url;

    /**
     * Agent类型：AgentType获取
     */
    private String agent;

    /**
     * 上下文角色：ContextRole获取
     */
    private String role;

    /**
     * 子类型：SubType获取
     */
    private String subType;

    /** 业务自定义扩展载荷 */
    private Object extra;

    /** 结构化元数据，如打点、溯源信息 */
    private Map<String, Object> metadata;

    /** 内容块 ID，用于与更新扩展字段等接口关联 */
    private String contentId;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }

    public String getAgent() {
        return agent;
    }

    public void setAgent(String agent) {
        this.agent = agent;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getSubType() {
        return subType;
    }

    public void setSubType(String subType) {
        this.subType = subType;
    }

    public Object getExtra() {
        return extra;
    }

    public void setExtra(Object extra) {
        this.extra = extra;
    }

    public String getContentId() {
        return contentId;
    }

    public void setContentId(String contentId) {
        this.contentId = contentId;
    }
}
