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

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * The common class for the inner response.
 *
 * @author kevanwong
 * @version 1.0, 2017/10/21
 * @since hojoai-common 1.0.0
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Response<T> {

    /** 业务或协议层约定的响应状态码 */
    private String resCode;
    /** 响应说明，便于排查问题或展示给用户 */
    private String resMessage;
    /** 业务数据载体，可为 null；序列化时 null 字段可按 {@link JsonInclude} 策略省略 */
    private T resContent;

    private Response() {
    }

    private Response(String resCode, String resMessage) {
        this.resCode = resCode;
        this.resMessage = resMessage;
    }

    private Response(String resCode, String resMessage, T resContent) {
        this.resCode = resCode;
        this.resMessage = resMessage;
        this.resContent = resContent;
    }

    /**
     * 创建仅含默认状态的响应实例（具体码与文案由构造逻辑决定，字段初始为 null）。
     *
     * @param <T> 载荷类型
     * @return 新的 {@link Response} 实例
     */
    public static <T> Response<T> newInstance() {
        return new Response<>();
    }

    /**
     * 创建带状态码与说明、无业务载荷的响应。
     *
     * @param resCode    响应状态码
     * @param resMessage 响应说明
     * @param <T>        载荷类型
     * @return 新的 {@link Response} 实例
     */
    public static <T> Response<T> newInstance(String resCode, String resMessage) {
        return new Response<>(resCode, resMessage);
    }

    /**
     * 创建带状态码、说明与业务载荷的响应。
     *
     * @param resCode    响应状态码
     * @param resMessage 响应说明
     * @param resContent 业务数据，可为 null
     * @param <T>        载荷类型
     * @return 新的 {@link Response} 实例
     */
    public static <T> Response<T> newInstance(String resCode, String resMessage, T resContent) {
        return new Response<>(resCode, resMessage, resContent);
    }

    /**
     * 根据 {@link ResponseStatus} 创建无载荷响应。
     *
     * @param responseStatus 状态枚举或实现，不可为 null
     * @param <T>            载荷类型
     * @return 新的 {@link Response} 实例
     * @throws NullPointerException 当 {@code responseStatus} 为 null 时
     */
    public static <T> Response<T> newInstance(ResponseStatus responseStatus) {
        return new Response<>(responseStatus.getResCode(), responseStatus.getResMessage());
    }

    /**
     * 根据 {@link ResponseStatus} 创建带载荷响应。
     *
     * @param responseStatus 状态枚举或实现，不可为 null
     * @param resContent     业务数据，可为 null
     * @param <T>            载荷类型
     * @return 新的 {@link Response} 实例
     * @throws NullPointerException 当 {@code responseStatus} 为 null 时
     */
    public static <T> Response<T> newInstance(ResponseStatus responseStatus, T resContent) {
        return new Response<>(responseStatus.getResCode(), responseStatus.getResMessage(), resContent);
    }

    /**
     * 获取响应状态码。
     *
     * @return 状态码，可能为 null（若未设置）
     */
    public String getResCode() {
        return resCode;
    }

    /**
     * 设置响应状态码。
     *
     * @param resCode 状态码
     */
    public void setResCode(String resCode) {
        this.resCode = resCode;
    }

    /**
     * 获取响应说明。
     *
     * @return 说明文案，可能为 null
     */
    public String getResMessage() {
        return resMessage;
    }

    /**
     * 设置响应说明。
     *
     * @param resMessage 说明文案
     */
    public void setResMessage(String resMessage) {
        this.resMessage = resMessage;
    }

    /**
     * 获取业务载荷。
     *
     * @return 载荷对象，可能为 null
     */
    public T getResContent() {
        return resContent;
    }

    /**
     * 设置业务载荷。
     *
     * @param resContent 载荷对象，可为 null
     */
    public void setResContent(T resContent) {
        this.resContent = resContent;
    }
}
