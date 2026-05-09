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
package com.hojoai.central.common;

import com.hojoai.common.response.ResponseStatus;

/**
 * The common enum for the extra inner response status.
 *
 * @author kevanwong
 * @version 1.0, 2020/05/12
 * @since hojoai-gateway-central 1.0.0
 *
 * <p>职责：为网关或公共模块提供扩展的业务响应状态码，与 {@link ResponseStatus} 对齐供统一封装。
 * <p>使用场景：错误码、提示文案需要与公共响应体规范一致时在此枚举中扩展。
 */
public enum ResponseStatusExtra implements ResponseStatus {

    /** 示例枚举项：占位，用于演示扩展方式；实际业务请替换为真实状态码与文案。 */
    EXAMPLE("EXAMPLE", "示例");

    /** 对外响应码，建议与接口文档约定一致。 */
    private String resCode;
    /** 对外提示信息，可与 {@link #resCode} 组合展示。 */
    private String resMessage;

    ResponseStatusExtra(String resCode, String resMessage) {
        this.resCode = resCode;
        this.resMessage = resMessage;
    }

    /**
     * 返回当前枚举项对应的响应码。
     *
     * @return 响应码字符串
     */
    @Override
    public String getResCode() {
        return resCode;
    }

    /**
     * 设置响应码（一般较少在运行期修改枚举语义，保留以符合 {@link ResponseStatus} 契约）。
     *
     * @param resCode 响应码
     */
    @Override
    public void setResCode(String resCode) {
        this.resCode = resCode;
    }

    /**
     * 返回当前枚举项对应的提示信息。
     *
     * @return 提示文案
     */
    @Override
    public String getResMessage() {
        return resMessage;
    }

    /**
     * 设置提示信息。
     *
     * @param resMessage 提示文案
     */
    @Override
    public void setResMessage(String resMessage) {
        this.resMessage = resMessage;
    }
}
