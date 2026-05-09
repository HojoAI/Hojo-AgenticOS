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

import java.util.List;
import java.util.Map;

/**
 * Dify 聊天请求 DTO
 *
 * @author wangjian
 * @since 2026-01-22
 */
public class DifyChatRequest {

    /**
     * 输入参数
     */
    private Map<String, Object> inputs;

    /**
     * 查询内容
     */
    private String query;

    /**
     * 响应模式，固定为 streaming
     */
    private String response_mode;

    /**
     * 会话 ID
     */
    private String conversation_id;

    /**
     * 用户标识
     */
    private String user;

    /**
     * 文件列表（可选）
     */
    private List<DifyFile> files;

    public Map<String, Object> getInputs() {
        return inputs;
    }

    public void setInputs(Map<String, Object> inputs) {
        this.inputs = inputs;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public String getResponse_mode() {
        return response_mode;
    }

    public void setResponse_mode(String response_mode) {
        this.response_mode = response_mode;
    }

    public String getConversation_id() {
        return conversation_id;
    }

    public void setConversation_id(String conversation_id) {
        this.conversation_id = conversation_id;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public List<DifyFile> getFiles() {
        return files;
    }

    public void setFiles(List<DifyFile> files) {
        this.files = files;
    }
}

