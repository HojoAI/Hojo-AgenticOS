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

import java.util.List;

/**
 * 调用 management PA 推送接口的请求体。
 */
public class PAPushRequestDTO {

    /** 接收用户 ID 列表 */
    private List<String> userIdList;

    /** 推送业务类型，如 translation */
    private String type;

    /** 是否广播 */
    private Boolean isBroadcast;

    /** 动作标识，如 action.pa.message */
    private String action;

    /** 推送标题 */
    private String title;

    /** 推送正文摘要 */
    private String content;

    /** 会话侧展示所需结构 */
    private ConversationInfo conversationInfo;

    public List<String> getUserIdList() {
        return userIdList;
    }

    public void setUserIdList(List<String> userIdList) {
        this.userIdList = userIdList;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Boolean getBroadcast() {
        return isBroadcast;
    }

    public void setBroadcast(Boolean broadcast) {
        isBroadcast = broadcast;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public ConversationInfo getConversationInfo() {
        return conversationInfo;
    }

    public void setConversationInfo(ConversationInfo conversationInfo) {
        this.conversationInfo = conversationInfo;
    }
}
