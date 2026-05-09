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
package com.hojoai.common.response;

/**
 * The common enum for the basic inner response status.
 *
 * @author kevanwong
 * @version 1.0, 2017/10/21
 * @since hojoai-common 1.0.0
 */
public enum ResponseStatusBasic implements ResponseStatus {

    /** 请求处理成功 */
    OK("OK", "请求成功"),

    /** 未分类的业务或系统错误 */
    GENERAL_ERROR("GENERAL_ERROR", "通用错误"),

    /** 入参不合法或校验失败 */
    ILLEGAL_ARGUMENT("ILLEGAL_ARGUMENT", "非法参数"),

    /** 当前操作或接口不被支持 */
    UNSUPPORTED_OPERATION("UNSUPPORTED_OPERATION", "不支持的操作");

    /** 响应状态码，用于与客户端或上游约定 */
    private String resCode;
    /** 响应说明文案，面向调用方展示或日志 */
    private String resMessage;

    ResponseStatusBasic(String resCode, String resMessage) {
        this.resCode = resCode;
        this.resMessage = resMessage;
    }

    /**
     * 获取响应状态码。
     *
     * @return 状态码字符串，不为 null
     */
    @Override
    public String getResCode() {
        return resCode;
    }

    /**
     * 设置响应状态码。
     *
     * @param resCode 状态码
     */
    @Override
    public void setResCode(String resCode) {
        this.resCode = resCode;
    }

    /**
     * 获取响应说明信息。
     *
     * @return 说明文案，可能为空字符串
     */
    @Override
    public String getResMessage() {
        return resMessage;
    }

    /**
     * 设置响应说明信息。
     *
     * @param resMessage 说明文案
     */
    @Override
    public void setResMessage(String resMessage) {
        this.resMessage = resMessage;
    }
}
