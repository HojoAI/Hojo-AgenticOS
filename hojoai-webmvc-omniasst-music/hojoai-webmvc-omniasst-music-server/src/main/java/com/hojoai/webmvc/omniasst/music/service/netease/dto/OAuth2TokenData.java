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

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * OAuth2 Token数据
 * 用于接收网易云code兑换token接口的响应
 * 
 * @author shengxianfei
 * @since 2026-01-17
 */
public class OAuth2TokenData {
    
    /**
     * 访问令牌
     */
    @JsonProperty("accessToken")
    private String accessToken;
    
    /**
     * 刷新令牌
     */
    @JsonProperty("refreshToken")
    private String refreshToken;
    
    /**
     * 开放平台用户唯一标识
     */
    @JsonProperty("openId")
    private String openId;
    
    /**
     * 开发者账号下用户唯一标识
     */
    @JsonProperty("unionId")
    private String unionId;
    
    /**
     * 访问令牌有效期（秒）
     */
    @JsonProperty("expireIn")
    private Long expireIn;
    
    public String getAccessToken() {
        return accessToken;
    }
    
    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }
    
    public String getRefreshToken() {
        return refreshToken;
    }
    
    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
    
    public String getOpenId() {
        return openId;
    }
    
    public void setOpenId(String openId) {
        this.openId = openId;
    }
    
    public String getUnionId() {
        return unionId;
    }
    
    public void setUnionId(String unionId) {
        this.unionId = unionId;
    }
    
    public Long getExpireIn() {
        return expireIn;
    }
    
    public void setExpireIn(Long expireIn) {
        this.expireIn = expireIn;
    }
}

