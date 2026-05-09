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
package com.hojoai.omniasst.management.common;

import com.hojoai.common.response.ResponseStatus;

/**
 * 自定义异常类
 *
 */
public class BusinessException extends RuntimeException {

    /**
     * 响应状态
     */
    private final ResponseStatus responseStatus;

    /**
     * 自定义错误消息（可选，如果提供则覆盖 ResponseStatus 的默认消息）
     */
    private final String customMessage;

    public BusinessException(ResponseStatus responseStatus) {
        super(responseStatus.getResMessage());
        this.responseStatus = responseStatus;
        this.customMessage = null;
    }

    public BusinessException(ResponseStatus responseStatus, String message) {
        super(message);
        this.responseStatus = responseStatus;
        this.customMessage = message;
    }

    public BusinessException(String resCode, String resMessage) {
        super(resMessage);
        this.responseStatus = new ResponseStatus() {
            @Override
            public String getResCode() {
                return resCode;
            }

            @Override
            public void setResCode(String resCode) {
                // 不可变
            }

            @Override
            public String getResMessage() {
                return resMessage;
            }

            @Override
            public void setResMessage(String resMessage) {
                // 不可变
            }
        };
        this.customMessage = resMessage;
    }

    public ResponseStatus getResponseStatus() {
        return responseStatus;
    }

    public String getResCode() {
        return responseStatus.getResCode();
    }

    public String getResMessage() {
        return customMessage != null ? customMessage : responseStatus.getResMessage();
    }
}
