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
package com.hojoai.omniasst.management.basic;

import com.hojoai.common.response.Response;
import com.hojoai.common.response.ResponseStatusBasic;
import com.hojoai.omniasst.management.common.BusinessException;
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
 * @since hojoai-webmvc-omniasst-management 1.0.0
 */
@ControllerAdvice("com.hojoai.omniasst.management.controller")
public class IExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(IExceptionHandler.class);

    /**
     * 处理业务异常
     *
     * @param e 业务异常
     * @return Response
     */
    @ExceptionHandler(value = BusinessException.class)
    @ResponseBody
    public Response<?> businessExceptionHandler(BusinessException e) {
        Response<?> response = Response.newInstance(e.getResponseStatus().getResCode(), e.getResMessage());

        logger.error("business exception handler on the controller: {}", e.getResMessage(), e);
        ILoggerHandler.leave(response);
        return response;
    }

    @ExceptionHandler(value = Exception.class)
    @ResponseBody
    public Response<?> exceptionHandler(Exception e) {
        Response<?> response = Response.newInstance(ResponseStatusBasic.GENERAL_ERROR);

        logger.error("exception handler on the controller", e);
        ILoggerHandler.leave(response);
        return response;
    }

    @ExceptionHandler(value = IllegalArgumentException.class)
    @ResponseBody
    public Response<?> illegalArgumentExceptionHandler(IllegalArgumentException e) {
        Response<?> response = Response.newInstance(ResponseStatusBasic.ILLEGAL_ARGUMENT);

        logger.error("exception handler on the controller", e);
        ILoggerHandler.leave(response);
        return response;
    }

    @ExceptionHandler(value = UnsupportedOperationException.class)
    @ResponseBody
    public Response<?> unsupportedOperationExceptionHandler(UnsupportedOperationException e) {
        Response<?> response = Response.newInstance(ResponseStatusBasic.UNSUPPORTED_OPERATION);

        logger.error("exception handler on the controller", e);
        ILoggerHandler.leave(response);
        return response;
    }
}
