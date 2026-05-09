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
 * 常用输入校验正则（手机号、邮箱、密码强度），供业务侧 {@link java.util.regex.Pattern} 使用。
 */
public class RegexUtil {

    /** 中国大陆手机号（含可选国家码） */
    public static final String MOBILE = "^(\\+86|86)?0?1[3-9][0-9]{9}$";

    /** 简单邮箱格式 */
    public static final String EMAIL = "^[a-zA-Z0-9_-]+@[a-zA-Z0-9_-]+(\\.[a-zA-Z0-9_-]+)+$";

    /** 密码：6–20 位，需包含字母、数字与特殊字符中至少两类 */
    public static final String PASSWORD = "^(?![0-9]+$)(?![a-zA-Z]+$)(?!([^(0-9a-zA-Z)]|[\\\\(\\\\)])+$)([^(0-9a-zA-Z)]|[\\\\(\\\\)]|[a-zA-Z]|[0-9]){6,20}$";
}
