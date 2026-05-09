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
package com.hojoai.omniasst.weather.entity;

import java.util.Map;

/**
 * SA智能体请求DTO
 * 
 * @author wangjian
 * @since 2026-01-22
 */
public class SAAgentRequest {
    
    /**
     * 用户ID（必须）
     */
    private String userId;
    
    /**
     * 查询内容（必须）
     */
    private String query;
    
    /**
     * 会话ID（可选，用于多轮对话）
     */
    private String conversationId;
    
    /**
     * 输入参数（可选，用于传递额外参数，如 location）
     */
    private Map<String, Object> inputs;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public String getConversationId() {
        return conversationId;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }

    public Map<String, Object> getInputs() {
        return inputs;
    }

    public void setInputs(Map<String, Object> inputs) {
        this.inputs = inputs;
    }
}

