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
package com.hojoai.central.account.basic;

import com.hojoai.common.response.Response;
import com.hojoai.common.response.ResponseStatusBasic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * The exception advice class.
 *
 * @author kevanwong
 * @version 1.0, 2017/10/21
 * @since hojoai-webmvc-central-account 1.0.0
 */
@ControllerAdvice("com.hojoai.central.account.controller")
public class IExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(IExceptionHandler.class);

    /**
     * 兜底异常：映射为通用错误响应并记录日志。
     *
     * @param e 任意未单独处理的异常
     * @return 带 GENERAL_ERROR 的响应体
     */
    @ExceptionHandler(value = Exception.class)
    @ResponseBody
    public Response<?> exceptionHandler(Exception e) {
        Response<?> response = Response.newInstance(ResponseStatusBasic.GENERAL_ERROR);

        logger.error("exception handler on the controller", e);
        ILoggerHandler.leave(response);
        return response;
    }

    /**
     * 非法参数：通常为入参校验失败。
     *
     * @param e 非法参数异常
     * @return 带 ILLEGAL_ARGUMENT 的响应体
     */
    @ExceptionHandler(value = IllegalArgumentException.class)
    @ResponseBody
    public Response<?> illegalArgumentExceptionHandler(IllegalArgumentException e) {
        Response<?> response = Response.newInstance(ResponseStatusBasic.ILLEGAL_ARGUMENT);

        logger.error("exception handler on the controller", e);
        ILoggerHandler.leave(response);
        return response;
    }

    /**
     * 不支持的操作：例如依赖服务明确抛出时。
     *
     * @param e 不支持的操作异常
     * @return 带 UNSUPPORTED_OPERATION 的响应体
     */
    @ExceptionHandler(value = UnsupportedOperationException.class)
    @ResponseBody
    public Response<?> unsupportedOperationExceptionHandler(UnsupportedOperationException e) {
        Response<?> response = Response.newInstance(ResponseStatusBasic.UNSUPPORTED_OPERATION);

        logger.error("exception handler on the controller", e);
        ILoggerHandler.leave(response);
        return response;
    }
}
