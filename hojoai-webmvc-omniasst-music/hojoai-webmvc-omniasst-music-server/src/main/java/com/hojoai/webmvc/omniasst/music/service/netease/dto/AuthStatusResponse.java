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
 * 授权状态响应
 * 
 * @author shengxianfei
 * @since 2026-01-17
 */
public class AuthStatusResponse {
    
    /**
     * 是否已授权
     */
    private Boolean authorized;
    
    /**
     * openId
     */
    private String openId;
    
    /**
     * unionId
     */
    private String unionId;
    
    public Boolean getAuthorized() {
        return authorized;
    }
    
    public void setAuthorized(Boolean authorized) {
        this.authorized = authorized;
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
}

