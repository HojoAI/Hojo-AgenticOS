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

import java.io.Serializable;

/**
 * 用户Token数据
 * 用于存储到Redis中
 * 
 * @author shengxianfei
 * @since 2026-01-17
 */
public class UserTokenData implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 用户ID
     */
    private String userId;
    
    /**
     * 访问令牌
     */
    private String accessToken;
    
    /**
     * 刷新令牌
     */
    private String refreshToken;
    
    /**
     * 开放平台用户唯一标识
     */
    private String openId;
    
    /**
     * 开发者账号下用户唯一标识
     */
    private String unionId;
    
    /**
     * accessToken过期时间（时间戳）
     */
    private Long accessTokenExpireTime;
    
    /**
     * refreshToken过期时间（时间戳）
     */
    private Long refreshTokenExpireTime;
    
    /**
     * 创建时间
     */
    private Long createTime;
    
    public String getUserId() {
        return userId;
    }
    
    public void setUserId(String userId) {
        this.userId = userId;
    }
    
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
    
    public Long getAccessTokenExpireTime() {
        return accessTokenExpireTime;
    }
    
    public void setAccessTokenExpireTime(Long accessTokenExpireTime) {
        this.accessTokenExpireTime = accessTokenExpireTime;
    }
    
    public Long getRefreshTokenExpireTime() {
        return refreshTokenExpireTime;
    }
    
    public void setRefreshTokenExpireTime(Long refreshTokenExpireTime) {
        this.refreshTokenExpireTime = refreshTokenExpireTime;
    }
    
    public Long getCreateTime() {
        return createTime;
    }
    
    public void setCreateTime(Long createTime) {
        this.createTime = createTime;
    }
}

