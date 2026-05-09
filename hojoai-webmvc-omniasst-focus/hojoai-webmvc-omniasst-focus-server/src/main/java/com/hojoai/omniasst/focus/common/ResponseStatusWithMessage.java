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
package com.hojoai.omniasst.focus.common;

import com.hojoai.common.response.ResponseStatus;

/**
 * 可携带自定义消息的 ResponseStatus，用于在接口中返回具体错误原因（便于排查）。
 *
 * @since hojoai-webmvc-omniasst.focus 1.0.0
 */
public class ResponseStatusWithMessage implements ResponseStatus {

    private final String resCode;
    private final String resMessage;

    public ResponseStatusWithMessage(String resCode, String resMessage) {
        this.resCode = resCode;
        this.resMessage = resMessage != null ? resMessage : "";
    }

    @Override
    public String getResCode() {
        return resCode;
    }

    @Override
    public void setResCode(String resCode) {
        throw new UnsupportedOperationException("immutable");
    }

    @Override
    public String getResMessage() {
        return resMessage;
    }

    @Override
    public void setResMessage(String resMessage) {
        throw new UnsupportedOperationException("immutable");
    }
}
