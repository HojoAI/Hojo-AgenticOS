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
package com.hojoai.webmvc.omniasst.music.service.netease.dto;

/**
 * 生成授权URL响应
 * 
 * @author shengxianfei
 * @since 2026-01-17
 */
public class GenerateAuthUrlResponse {
    
    /**
     * 授权页URL
     */
    private String authUrl;
    
    /**
     * state参数（用于回调验证）
     */
    private String state;
    
    public String getAuthUrl() {
        return authUrl;
    }
    
    public void setAuthUrl(String authUrl) {
        this.authUrl = authUrl;
    }
    
    public String getState() {
        return state;
    }
    
    public void setState(String state) {
        this.state = state;
    }
}

