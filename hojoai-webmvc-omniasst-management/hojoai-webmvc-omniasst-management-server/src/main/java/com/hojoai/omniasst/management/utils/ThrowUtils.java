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

import com.hojoai.common.response.ResponseStatus;
import com.hojoai.omniasst.management.common.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The utils class for Exception.
 *
 * @author zhangxuepeng
 * @version 1.0, 2025/12/10
 * @since hojoai-webmvc-omniasst-management 1.0.0
 */
public class ThrowUtils {

    private static final Logger logger = LoggerFactory.getLogger(ThrowUtils.class);

    /**
     * 条件成立则抛异常
     *
     * @param condition 条件
     * @param runtimeException 运行时异常
     */
    public static void ThrowIf(boolean condition, RuntimeException runtimeException) {
        if (condition) {
            throw runtimeException;
        }
    }

    /**
     * 条件成立则抛异常
     *
     * @param condition 条件
     * @param responseStatus 响应状态
     */
    public static void ThrowIf(boolean condition, ResponseStatus responseStatus) {
        if (condition) {
            BusinessException exception = new BusinessException(responseStatus);
            throw exception;
        }
    }

    /**
     * 条件成立则抛异常
     *
     * @param condition 条件
     * @param responseStatus 响应状态
     * @param message 自定义错误消息
     */
    public static void ThrowIf(boolean condition, ResponseStatus responseStatus, String message) {
        if (condition) {
            BusinessException exception = new BusinessException(responseStatus, message);
            throw exception;
        }
    }

    /**
     * 条件成立则抛异常
     *
     * @param condition 条件
     * @param resCode 响应码
     * @param resMessage 响应消息
     */
    public static void ThrowIf(boolean condition, String resCode, String resMessage) {
        if (condition) {
            BusinessException exception = new BusinessException(resCode, resMessage);
            throw exception;
        }
    }
}
