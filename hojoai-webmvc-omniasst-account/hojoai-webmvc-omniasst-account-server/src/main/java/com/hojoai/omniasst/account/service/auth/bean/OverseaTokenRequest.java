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
package com.hojoai.omniasst.account.service.auth.bean;

/**
 * 调用 central-account 创建海外用户会话时的请求体。
 */
public class OverseaTokenRequest {

    /** 登录提供方：google / apple / email */
    private String provider;
    /** 第三方用户唯一标识（邮箱登录时可为邮箱） */
    private String providerUserId;
    /** 用户邮箱 */
    private String email;
    /** 昵称 */
    private String nickname;
    /** 头像 URL */
    private String avatar;
    /** 端侧平台类型 */
    private Integer platformType;

    public String getProvider() { return provider; }
    public void setProvider(String provider) { this.provider = provider; }
    public String getProviderUserId() { return providerUserId; }
    public void setProviderUserId(String providerUserId) { this.providerUserId = providerUserId; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }
    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }
    public Integer getPlatformType() { return platformType; }
    public void setPlatformType(Integer platformType) { this.platformType = platformType; }
}
