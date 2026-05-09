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
package com.hojoai.webmvc.omniasst.music.service.dify.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashMap;
import java.util.Map;

/**
 * Dify请求DTO
 * 
 * @author shengxianfei
 * @since 2026-01-20
 */
public class DifyRequest {
    
    /**
     * 输入参数，包含userID和Query
     */
    private Map<String, String> inputs;
    
    /**
     * 用户标识
     */
    private String user;
    
    /**
     * 响应模式，固定为streaming
     */
    @JsonProperty("response_mode")
    private String responseMode = "streaming";
    
    public DifyRequest() {
        this.inputs = new HashMap<>();
    }
    
    public DifyRequest(String userId, String query) {
        this.inputs = new HashMap<>();
        this.inputs.put("userID", userId);
        this.inputs.put("Query", query);  // 注意：Dify workflow要求Query首字母大写
        this.user = userId;
        this.responseMode = "streaming";
    }
    
    public Map<String, String> getInputs() {
        return inputs;
    }
    
    public void setInputs(Map<String, String> inputs) {
        this.inputs = inputs;
    }
    
    public String getUser() {
        return user;
    }
    
    public void setUser(String user) {
        this.user = user;
    }
    
    public String getResponseMode() {
        return responseMode;
    }
    
    public void setResponseMode(String responseMode) {
        this.responseMode = responseMode;
    }
}

