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
package com.hojoai.template.basic;

import com.hojoai.template.context.RequestContext;
import com.hojoai.common.response.Response;
import com.hojoai.common.response.ResponseStatusBasic;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 模板模块全局异常处理。仅扫描 {@code com.hojoai.template.controller} 包下控制器抛出的异常，
 * 映射为 {@link Response} 统一结构并写错误日志；在记录日志时尽量附带请求耗时并清理 {@link RequestContext}。
 */
@ControllerAdvice("com.hojoai.template.controller")
@Slf4j
public class ExceptionHandler {

    @Resource
    private LoggerHandler loggerHandler;

    /**
     * 处理通用异常
     *
     * @param e 异常对象
     * @return 统一响应格式
     */
    @org.springframework.web.bind.annotation.ExceptionHandler(value = Exception.class)
    @ResponseBody
    public Response<?> exceptionHandler(Exception e) {
        Response<?> response = Response.newInstance(ResponseStatusBasic.GENERAL_ERROR);
        return handleException(e, response, "General exception occurred");
    }

    /**
     * 处理非法参数异常
     *
     * @param e 异常对象
     * @return 统一响应格式
     */
    @org.springframework.web.bind.annotation.ExceptionHandler(value = IllegalArgumentException.class)
    @ResponseBody
    public Response<?> illegalArgumentExceptionHandler(IllegalArgumentException e) {
        Response<?> response = Response.newInstance(ResponseStatusBasic.ILLEGAL_ARGUMENT);
        return handleException(e, response, "Illegal argument exception occurred");
    }

    /**
     * 处理不支持的操作异常
     *
     * @param e 异常对象
     * @return 统一响应格式
     */
    @org.springframework.web.bind.annotation.ExceptionHandler(value = UnsupportedOperationException.class)
    @ResponseBody
    public Response<?> unsupportedOperationExceptionHandler(UnsupportedOperationException e) {
        Response<?> response = Response.newInstance(ResponseStatusBasic.UNSUPPORTED_OPERATION);
        return handleException(e, response, "Unsupported operation exception occurred");
    }

    /**
     * 统一处理异常的核心方法
     * 消除代码重复，提高可维护性
     *
     * @param e 异常对象
     * @param response 响应对象
     * @param logMessage 日志消息
     * @return 统一响应格式
     */
    private Response<?> handleException(Exception e, Response<?> response, String logMessage) {
        // 记录异常日志
        log.error(logMessage, e);

        try {
            // 从RequestContext获取耗时信息
            RequestContext context = RequestContext.get();
            long costTime = 0L;
            if (context != null) {
                costTime = context.getCostTime();
            }
            
            // 记录异常响应日志和耗时
            loggerHandler.leave(response, costTime);
        } finally {
            // 确保清理ThreadLocal，防止内存泄漏
            RequestContext.clear();
        }
        
        return response;
    }
}
