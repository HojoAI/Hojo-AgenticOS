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

import com.hojoai.common.jackson.JacksonUtil;
import com.hojoai.template.configure.LoggerProperties;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * 日志处理器
 * 负责统一处理Controller的入参和出参日志记录
 * 
 * <p>功能特性：</p>
 * <ul>
 *   <li>支持配置化的日志消息长度阈值</li>
 *   <li>异常安全，避免日志记录影响业务流程</li>
 *   <li>自动获取HTTP请求信息（URI、Method）</li>
 *   <li>支持JSON序列化，便于日志分析</li>
 * </ul>
 * 
 */
@Component
public class LoggerHandler {

    private static final Logger logger = LoggerFactory.getLogger(LoggerHandler.class);
    
    @Resource
    private LoggerProperties loggerProperties;

    /**
     * 记录Controller方法入参
     * 
     * @param object 入参对象（数组形式）
     */
    public void enter(Object object) {
        try {
            HttpServletRequest httpServletRequest = getHttpServletRequest();
            if (httpServletRequest == null) {
                logger.warn("Cannot get HttpServletRequest, skip enter log");
                return;
            }
            
            String requestURI = httpServletRequest.getRequestURI();
            String requestMethod = httpServletRequest.getMethod();

            String message = formatMessage(object);

            logger.info("Controller Enter >>> uri: {}, method: {}, params: {}", 
                    requestURI, requestMethod, message);
        } catch (Exception e) {
            // 日志记录失败不应该影响业务流程
            logger.error("Failed to log enter message", e);
        }
    }

    /**
     * 记录Controller方法出参（兼容方法，默认耗时为0）
     * 
     * @param object 出参对象
     */
    public void leave(Object object) {
        leave(object, 0L);
    }

    /**
     * 记录Controller方法出参和耗时
     * 
     * @param object 出参对象
     * @param costTime 方法执行耗时（毫秒）
     */
    public void leave(Object object, long costTime) {
        try {
            HttpServletRequest httpServletRequest = getHttpServletRequest();
            if (httpServletRequest == null) {
                logger.warn("Cannot get HttpServletRequest, skip leave log");
                return;
            }
            
            String requestURI = httpServletRequest.getRequestURI();
            String requestMethod = httpServletRequest.getMethod();

            String message = formatMessage(object);

            logger.info("Controller Leave <<< uri: {}, method: {}, costTime: {}ms, result: {}", 
                    requestURI, requestMethod, costTime, message);
        } catch (Exception e) {
            // 日志记录失败不应该影响业务流程
            logger.error("Failed to log leave message", e);
        }
    }

    /**
     * 格式化消息内容
     * 将对象序列化为JSON，如果超过阈值则截断
     * 
     * @param object 待格式化的对象
     * @return 格式化后的消息字符串
     */
    private String formatMessage(Object object) {
        try {
            String jsonMessage = JacksonUtil.bean2Json(object);
            int threshold = loggerProperties.getMessageThreshold();
            
            if (jsonMessage.length() > threshold) {
                return jsonMessage.substring(0, threshold) + "...(truncated)";
            }
            return jsonMessage;
        } catch (Exception e) {
            logger.debug("Failed to serialize object to JSON", e);
            return String.valueOf(object);
        }
    }

    /**
     * 获取当前HTTP请求对象
     * 
     * @return HttpServletRequest对象，如果获取失败返回null
     */
    private HttpServletRequest getHttpServletRequest() {
        try {
            ServletRequestAttributes attributes = 
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            return attributes != null ? attributes.getRequest() : null;
        } catch (Exception e) {
            logger.debug("Failed to get HttpServletRequest", e);
            return null;
        }
    }
}
