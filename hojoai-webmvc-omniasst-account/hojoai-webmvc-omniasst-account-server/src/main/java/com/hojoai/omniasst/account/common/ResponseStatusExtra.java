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
package com.hojoai.omniasst.account.common;

import com.hojoai.common.response.ResponseStatus;

/**
 * The common enum for the extra inner response status.
 *
 * @author kevanwong
 * @version 1.0, 2017/10/21
 * @since hojoai-webmvc-omniasst-account 1.0.0
 */
public enum ResponseStatusExtra implements ResponseStatus {

    /** 占位示例，业务勿依赖 */
    EXAMPLE("EXAMPLE", "示例"),

    /** Google idToken 校验失败 */
    INVALID_GOOGLE_TOKEN("INVALID_GOOGLE_TOKEN", "Google Token verification failed"),

    /** Apple identityToken 校验失败 */
    INVALID_APPLE_TOKEN("INVALID_APPLE_TOKEN", "Apple Token verification failed"),

    /** 邮箱验证码错误 */
    INVALID_OTP("INVALID_OTP", "Invalid verification code"),

    /** 邮箱验证码已过期或不存在 */
    OTP_EXPIRED("OTP_EXPIRED", "Verification code expired"),

    /** 发送验证码或 Magic Link 触发限流 */
    OTP_RATE_LIMITED("OTP_RATE_LIMITED", "Please wait before requesting a new code"),

    /** OTP 尝试次数超限 */
    OTP_ATTEMPTS_EXCEEDED("OTP_ATTEMPTS_EXCEEDED", "Too many attempts, please request a new code"),

    /** Magic Link 已过期或 token 无效（部分接口） */
    MAGIC_LINK_EXPIRED("MAGIC_LINK_EXPIRED", "Login link expired"),

    /** Magic Link 已被使用 */
    MAGIC_LINK_USED("MAGIC_LINK_USED", "Login link already used"),

    /** 账号已禁用 */
    ACCOUNT_DISABLED("ACCOUNT_DISABLED", "Account is disabled"),

    /** 账号已删除 */
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
