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
package com.hojoai.template.common;

import com.hojoai.common.response.ResponseStatus;

/**
 * 模板工程扩展的响应状态枚举，实现 {@link ResponseStatus}，与公共包状态码并列使用。
 */
public enum ResponseStatusExtra implements ResponseStatus {

    /** 示例状态：演示自定义业务码与文案 */
    EXAMPLE("EXAMPLE", "示例");

    /** 业务响应码，如 EXAMPLE */
    private String resCode;
    /** 面向调用方或日志的人类可读说明 */
    private String resMessage;

    ResponseStatusExtra(String resCode, String resMessage) {
        this.resCode = resCode;
        this.resMessage = resMessage;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getResCode() {
        return resCode;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setResCode(String resCode) {
        this.resCode = resCode;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getResMessage() {
        return resMessage;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setResMessage(String resMessage) {
        this.resMessage = resMessage;
    }
}
