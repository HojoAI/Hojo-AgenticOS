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
package com.hojoai.omniasst.common;

import com.hojoai.common.response.ResponseStatus;

/**
 * The common enum for the extra inner response status.
 *
 * @author kevanwong
 * @version 1.0, 2020/05/12
 * @since hojoai-gateway-omniasst 1.0.0
 */
public enum ResponseStatusExtra implements ResponseStatus {

    GATEWAY_ERROR("GATEWAY_ERROR", "网关错误"),

    ILLEGAL_USER_TOKEN("ILLEGAL_USER_TOKEN", "非法用户TOKEN");

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
