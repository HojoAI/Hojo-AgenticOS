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
package com.hojoai.omniasst.focus.basic;

import com.hojoai.common.response.Response;
import com.hojoai.common.response.ResponseStatusBasic;
import com.hojoai.omniasst.focus.common.ResponseStatusWithMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 控制器层全局异常处理：将未捕获异常转为统一 {@link Response}，并记录日志。
 * <p>{@link IllegalArgumentException} 映射为非法参数错误码；{@link UnsupportedOperationException} 映射为不支持的操作。</p>
 */
@ControllerAdvice("com.hojoai.omniasst.focus.controller")
public class IExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(IExceptionHandler.class);

    /**
     * 处理通用异常，返回 GENERAL_ERROR 与异常信息（若有）。
     *
     * @param e 任意未单独声明的异常
     * @return 带错误码与消息的响应体
     */
    @ExceptionHandler(value = Exception.class)
    @ResponseBody
    public Response<?> exceptionHandler(Exception e) {
        logger.error("exception handler on the controller", e);
        String msg = e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
        Response<?> response = Response.newInstance(new ResponseStatusWithMessage(ResponseStatusBasic.GENERAL_ERROR.getResCode(), msg), null);
        ILoggerHandler.leave(response);
        return response;
    }

    /**
     * 参数或业务前置校验失败时使用。
     *
     * @param e 非法参数异常
     * @return ILLEGAL_ARGUMENT 响应
     */
    @ExceptionHandler(value = IllegalArgumentException.class)
    @ResponseBody
    public Response<?> illegalArgumentExceptionHandler(IllegalArgumentException e) {
        logger.error("exception handler on the controller", e);
        String msg = e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
        Response<?> response = Response.newInstance(new ResponseStatusWithMessage(ResponseStatusBasic.ILLEGAL_ARGUMENT.getResCode(), msg), null);
        ILoggerHandler.leave(response);
        return response;
    }

    /**
     * 当前接口或资源不支持所请求操作时抛出。
     *
     * @param e 不支持的操作异常
     * @return UNSUPPORTED_OPERATION 响应
     */
    @ExceptionHandler(value = UnsupportedOperationException.class)
    @ResponseBody
    public Response<?> unsupportedOperationExceptionHandler(UnsupportedOperationException e) {
        logger.error("exception handler on the controller", e);
        String msg = e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
        Response<?> response = Response.newInstance(new ResponseStatusWithMessage(ResponseStatusBasic.UNSUPPORTED_OPERATION.getResCode(), msg), null);
        ILoggerHandler.leave(response);
        return response;
    }
}
