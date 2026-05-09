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

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * 发送 Magic Link 成功后的响应：含供客户端轮询的 loginToken。
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MagicLinkSendResponse {

    /** 用于 {@code /magic/status} 轮询的 token */
    private String loginToken;

    public String getLoginToken() {
        return loginToken;
    }

    public void setLoginToken(String loginToken) {
        this.loginToken = loginToken;
    }
}
