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
 * 生成授权URL请求
 * 
 * @author shengxianfei
 * @since 2026-01-17
 */
public class GenerateAuthUrlRequest {
    
    /**
     * 用户ID
     */
    private String userId;
    
    /**
     * 客户端类型（web/android）
     */
    private String clientType = "web";
    
    /**
     * 打开接入方App的schema协议（唤端必传，当clientType为android时需要提供）
     */
    private String schemaUrl;
    
    /**
     * Hojo重定向URL（可选，用于授权成功后的前端跳转）
     */
    private String hojoRedirectUrl;
    
    public String getUserId() {
        return userId;
    }
    
    public void setUserId(String userId) {
        this.userId = userId;
    }
    
    public String getClientType() {
        return clientType;
    }
    
    public void setClientType(String clientType) {
        this.clientType = clientType;
    }
    
    public String getSchemaUrl() {
        return schemaUrl;
    }
    
    public void setSchemaUrl(String schemaUrl) {
        this.schemaUrl = schemaUrl;
    }
    
    public String getHojoRedirectUrl() {
        return hojoRedirectUrl;
    }
    
    public void setHojoRedirectUrl(String hojoRedirectUrl) {
        this.hojoRedirectUrl = hojoRedirectUrl;
    }
}

