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
package com.hojoai.central.account.service.user.bean;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * 发送手机验证码响应（当前业务成功时多依赖统一 Response 状态，本对象预留扩展字段）。
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserMobileCaptchaResponse {

    /** 手机号（若接口需要回显） */
    private String userMobile;
    /** 验证码类型 */
    private String mobileCaptchaType;

    public String getUserMobile() {
        return userMobile;
    }

    public void setUserMobile(String userMobile) {
        this.userMobile = userMobile;
    }

    public String getMobileCaptchaType() {
        return mobileCaptchaType;
    }

    public void setMobileCaptchaType(String mobileCaptchaType) {
        this.mobileCaptchaType = mobileCaptchaType;
    }
}
