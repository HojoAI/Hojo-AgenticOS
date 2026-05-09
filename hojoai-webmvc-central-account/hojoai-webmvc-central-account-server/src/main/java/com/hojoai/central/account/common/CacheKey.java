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

/**
 * Redis 等业务缓存键前缀与分隔符（验证码限流、验证码存储等）。
 */
public class CacheKey {

    /** 键片段分隔符 */
    public static final String DELIMITER = ":";

    /** 手机号 + 类型维度的验证码内容 */
    public static final String CAPTCHA_BY_MOBILE = "central-account:captcha_by_mobile:";

    /** 日维度发送次数（历史拼写 minitue 保持不变以兼容已有 Redis 键） */
    public static final String CAPTCHA_COUNT_BY_MOBILE_DAY = "central-account:captcha_count_by_mobile_day:";
    public static final String CAPTCHA_COUNT_BY_MOBILE_HOUR = "central-account:captcha_count_by_mobile_hour:";
    public static final String CAPTCHA_COUNT_BY_MOBILE_MINITUE = "central-account:captcha_count_by_mobile_minitue:";
}
