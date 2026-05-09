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

import com.hojoai.common.jackson.JacksonUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Objects;

/**
 * The logger handler class.
 *
 * @author kevanwong
 * @version 1.0, 2017/10/21
 * @since hojoai-webmvc-omniasst-account 1.0.0
 */
public class ILoggerHandler {

    private static final Logger logger = LoggerFactory.getLogger(ILoggerHandler.class);

    private static final int LOG_THRESHOLD = 1000;

    public static void enter(Object object) {
        HttpServletRequest httpServletRequest = getHttpServletRequest();
        String requestURI = httpServletRequest.getRequestURI();
        String requestMethod = httpServletRequest.getMethod();

        String jsonMessage = JacksonUtil.bean2Json(object);
        String message = jsonMessage.length() > LOG_THRESHOLD ? jsonMessage.substring(0, LOG_THRESHOLD) : jsonMessage;

        logger.info("controller enter on uri: {}, method: {}, message: {}", requestURI, requestMethod, message);
    }

    public static void leave(Object object) {
        HttpServletRequest httpServletRequest = getHttpServletRequest();
        String requestURI = httpServletRequest.getRequestURI();
        String requestMethod = httpServletRequest.getMethod();

        String jsonMessage = JacksonUtil.bean2Json(object);
        String message = jsonMessage.length() > LOG_THRESHOLD ? jsonMessage.substring(0, LOG_THRESHOLD) : jsonMessage;

        logger.info("controller leave on uri: {}, method: {}, message: {}", requestURI, requestMethod, message);
    }

    private static HttpServletRequest getHttpServletRequest() {
        return ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes())).getRequest();
    }
}
