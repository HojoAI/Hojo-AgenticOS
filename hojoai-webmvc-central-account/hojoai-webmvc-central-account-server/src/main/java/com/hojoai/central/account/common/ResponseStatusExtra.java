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
package com.hojoai.central.account.common;

import com.hojoai.common.response.ResponseStatus;

/**
 * The common enum for the extra inner response status.
 *
 * @author kevanwong
 * @version 1.0, 2017/10/21
 * @since hojoai-webmvc-central-account 1.0.0
 */
public enum ResponseStatusExtra implements ResponseStatus {

    // user info
    USER_INFO_NOT_REGISTERED("USER_INFO_NOT_REGISTERED", "用户未注册"),

    USER_INFO_ALREADY_REGISTERED("USER_INFO_ALREADY_REGISTERED", "用户已注册"),

    // user mobile
    ILLEGAL_USER_MOBILE("ILLEGAL_USER_MOBILE", "非法用户手机"),

    USER_MOBILE_NOT_REGISTERED("USER_MOBILE_NOT_REGISTERED", "用户手机未注册"),

    USER_MOBILE_ALREADY_REGISTERED("USER_MOBILE_ALREADY_REGISTERED", "用户手机已注册"),

    // user token
    ILLEGAL_USER_TOKEN("ILLEGAL_USER_TOKEN", "非法用户TOKEN"),

    // mobile captcha
    MOBILE_CAPTCHA_SENDING_FAILED("MOBILE_CAPTCHA_SENDING_FAILED", "手机验证码发送失败"),

    MOBILE_CAPTCHA_COUNT_LIMIT("MOBILE_CAPTCHA_COUNT_LIMIT", "手机验证码次数上限"),

    MOBILE_CAPTCHA_IS_INVALID("MOBILE_CAPTCHA_IS_INVALID", "手机验证码无效"),

    OLD_MOBILE_CAPTCHA_IS_INVALID("OLD_MOBILE_CAPTCHA_IS_INVALID", "旧手机验证码无效"),

    NEW_MOBILE_CAPTCHA_IS_INVALID("NEW_MOBILE_CAPTCHA_IS_INVALID", "新手机验证码无效"),

    // oversea auth
    ACCOUNT_DISABLED("ACCOUNT_DISABLED", "Account is disabled"),

    ACCOUNT_DELETED("ACCOUNT_DELETED", "Account has been deleted");

    private String resCode;
    private String resMessage;

    ResponseStatusExtra(String resCode, String resMessage) {
        this.resCode = resCode;
        this.resMessage = resMessage;
    }

    @Override
    public String getResCode() {
        return resCode;
    }

    @Override
    public void setResCode(String resCode) {
        this.resCode = resCode;
    }

    @Override
    public String getResMessage() {
        return resMessage;
    }

    @Override
    public void setResMessage(String resMessage) {
        this.resMessage = resMessage;
    }
}
