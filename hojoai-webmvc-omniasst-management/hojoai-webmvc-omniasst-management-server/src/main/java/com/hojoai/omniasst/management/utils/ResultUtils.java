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
package com.hojoai.omniasst.management.utils;

import com.hojoai.common.response.Response;
import com.hojoai.common.response.ResponseStatus;
import com.hojoai.common.response.ResponseStatusBasic;
import com.hojoai.omniasst.management.common.ResponseStatusExtra;

/**
 * The utils class for result.
 *
 * @author zhangxuepeng
 * @version 1.0, 2025/12/10
 * @since hojoai-webmvc-omniasst-management 1.0.0
 */
public class ResultUtils {

    /**
     * 成功
     *
     * @param data 返回数据
     * @param <T> 数据类型
     * @return Response
     */
    public static <T> Response<T> Ok(T data) {
        return Response.newInstance(ResponseStatusBasic.OK, data);
    }

    /**
     * 成功（无数据）
     *
     * @return Response
     */
    public static Response Ok() {
        return Response.newInstance(ResponseStatusBasic.OK);
    }

    /**
     * 失败（使用 ResponseStatusBasic）
     *
     * @param responseStatusBasic 响应状态
     * @return Response
     */
    public static Response Error(ResponseStatusBasic responseStatusBasic) {
        return Response.newInstance(responseStatusBasic);
    }

    /**
     * 失败（使用 ResponseStatusBasic）
     *
     * @param responseStatusBasic 响应状态
     * @param data 返回数据
     * @param <T> 数据类型
     * @return Response
     */
    public static <T> Response<T> Error(ResponseStatusBasic responseStatusBasic, T data) {
        return Response.newInstance(responseStatusBasic, data);
    }

    /**
     * 失败（使用 ResponseStatusExtra）
     *
     * @param responseStatusExtra 响应状态
     * @return Response
     */
    public static Response Error(ResponseStatusExtra responseStatusExtra) {
        return Response.newInstance(responseStatusExtra);
    }

    /**
     * 失败（使用 ResponseStatusExtra）
     *
     * @param responseStatusExtra 响应状态
     * @param data 返回数据
     * @param <T> 数据类型
     * @return Response
     */
    public static <T> Response<T> Error(ResponseStatusExtra responseStatusExtra, T data) {
        return Response.newInstance(responseStatusExtra, data);
    }

    /**
     * 失败
     *
     * @param responseStatus 响应状态
     * @return Response
     */
    public static Response Error(ResponseStatus responseStatus) {
        return Response.newInstance(responseStatus);
    }

    /**
     * 失败
     *
     * @param responseStatus 响应状态
     * @param data 返回数据
     * @param <T> 数据类型
     * @return Response
     */
    public static <T> Response<T> Error(ResponseStatus responseStatus, T data) {
        return Response.newInstance(responseStatus, data);
    }

    /**
     * 失败（使用通用错误）
     *
     * @param message 错误消息
     * @return Response
     */
    public static Response Error(String message) {
        return Response.newInstance(ResponseStatusBasic.GENERAL_ERROR.getResCode(), message);
    }

    /**
     * 失败（使用通用错误）
     *
     * @param message 错误消息
     * @param data 返回数据
     * @param <T> 数据类型
     * @return Response
     */
    public static <T> Response<T> Error(String message, T data) {
        return Response.newInstance(ResponseStatusBasic.GENERAL_ERROR.getResCode(), message, data);
    }

    /**
     * 失败（自定义状态码和消息）
     *
     * @param resCode 响应码
     * @param resMessage 响应消息
     * @return Response
     */
    public static Response Error(String resCode, String resMessage) {
        return Response.newInstance(resCode, resMessage);
    }

    /**
     * 失败（自定义状态码和消息）
     *
     * @param resCode 响应码
     * @param resMessage 响应消息
     * @param data 返回数据
     * @param <T> 数据类型
     * @return Response
     */
    public static <T> Response<T> Error(String resCode, String resMessage, T data) {
        return Response.newInstance(resCode, resMessage, data);
    }
}
