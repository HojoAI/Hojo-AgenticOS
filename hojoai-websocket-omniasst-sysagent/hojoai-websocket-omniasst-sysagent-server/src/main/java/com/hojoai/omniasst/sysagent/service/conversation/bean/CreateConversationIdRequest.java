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

/**
 * 创建对话ID请求实体类
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CreateConversationIdRequest {

    /** 客户端已有会话 ID 时可传入；为空则由 management 分配。 */
    private String conversationId;
    /** 用户 ID。 */
    private String userId;
    /** 是否耳机模式。 */
    private Boolean headphoneMode;
    
    public String getConversationId() {
        return conversationId;
    }
    
    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }
    
    public String getUserId() {
        return userId;
    }
    
    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Boolean getHeadphoneMode() {
        return headphoneMode;
    }

    public void setHeadphoneMode(Boolean headphoneMode) {
        this.headphoneMode = headphoneMode;
    }
}