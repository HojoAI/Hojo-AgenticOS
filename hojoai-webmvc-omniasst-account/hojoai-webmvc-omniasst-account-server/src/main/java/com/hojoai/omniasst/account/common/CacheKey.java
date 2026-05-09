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

/**
 * 本服务在 Redis 中使用的 key 前缀，与海外邮箱 OTP、Magic Link 等业务对应。
 */
public class CacheKey {

    /** 业务分段分隔符 */
    public static final String DELIMITER = ":";

    /** 邮箱 OTP 存储：邮箱 → 验证码 */
    public static final String EMAIL_OTP = "omniasst-account:email_otp:";
    /** 发送 OTP / Magic Link 的限流键：邮箱维度 */
    public static final String EMAIL_OTP_RATE = "omniasst-account:email_otp_rate:";
    /** OTP 错误尝试次数 */
    public static final String EMAIL_OTP_ATTEMPT = "omniasst-account:email_otp_attempt:";

    /** Magic Link 状态：loginToken → JSON 状态 */
    public static final String MAGIC_LINK = "omniasst-account:magic:";
}
