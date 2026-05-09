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
 * OAuth2回调请求
 * 用于接收网易云授权回调的参数
 * 
 * @author shengxianfei
 * @since 2026-01-17
 */
public class OAuth2CallbackRequest {
    
    /**
     * 授权码
     */
    private String code;
    
    /**
     * 状态参数（含用户ID等信息）
     */
    private String state;
    
    /**
     * Hojo重定向URL（可选，用于授权成功后的前端跳转）
     */
    private String hojoRedirectUrl;
    
    public String getCode() {
        return code;
    }
    
    public void setCode(String code) {
        this.code = code;
    }
    
    public String getState() {
        return state;
    }
    
    public void setState(String state) {
        this.state = state;
    }
    
    public String getHojoRedirectUrl() {
        return hojoRedirectUrl;
    }
    
    public void setHojoRedirectUrl(String hojoRedirectUrl) {
        this.hojoRedirectUrl = hojoRedirectUrl;
    }
}

