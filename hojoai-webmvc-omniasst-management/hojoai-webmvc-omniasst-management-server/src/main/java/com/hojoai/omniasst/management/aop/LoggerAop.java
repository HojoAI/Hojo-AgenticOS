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
package com.hojoai.omniasst.management.aop;

import com.hojoai.common.jackson.JacksonUtil;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

/**
 * Controller 层请求日志切面：按配置记录入参、出参与耗时。
 */
@Aspect
@Component
public class LoggerAop {

    private static final Logger logger = LoggerFactory.getLogger(LoggerAop.class);

    @Resource
    private ControllerLogConfig config;

    /**
     * 定义切点：拦截所有 Controller 层的方法
     */
    @Pointcut("execution(* com.hojoai..controller..*Controller.*(..))")
    public void controllerPointcut() {
    }

    /**
     * 环绕通知：记录方法执行时间、输入参数、输出参数
     */
    @Around("controllerPointcut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        // 检查是否启用日志监控
        if (!config.isEnabled()) {
            return joinPoint.proceed();
        }

        long startTime = System.currentTimeMillis();

        // 获取请求信息
        HttpServletRequest request = getHttpServletRequest();
        String requestURI = request != null ? request.getRequestURI() : "N/A";
        String requestMethod = request != null ? request.getMethod() : "N/A";
        String clientIp = config.isLogClientIp() ? getClientIp(request) : "N/A";

        // 获取方法信息
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        String fullMethodName = className + "." + methodName;

        // 获取请求参数
        Object[] args = joinPoint.getArgs();
        String requestParams = config.isLogRequestParams() ? formatRequestParams(args) : "[disabled]";

        // 记录请求开始
        logger.info("[REQUEST START] {} {} - {}", requestMethod, requestURI, fullMethodName);
        if (config.isLogClientIp()) {
            logger.info("[REQUEST] Client IP: {}", clientIp);
        }
        if (config.isLogRequestParams()) {
            logger.info("[REQUEST] Params: {}", requestParams);
        }
        logger.info("[REQUEST] Start Time: {}", startTime);

        Object result = null;
        Throwable exception = null;

        try {
            // 执行目标方法
            result = joinPoint.proceed();
            return result;
        } catch (Throwable e) {
            exception = e;
            throw e;
        } finally {
            long endTime = System.currentTimeMillis();
            long executionTime = endTime - startTime;

            // 格式化返回结果
            String response = config.isLogResponse() ? formatResponse(result) : "[disabled]";

            // 记录请求结束
            if (exception != null) {
                logger.error("[ERROR] Exception occurred: {}", exception.getMessage());
            }
            if (config.isLogResponse()) {
                logger.info("[RESPONSE] Result: {}", response);
            }
            logger.info("[RESPONSE] Execution Time: {} ms, End Time: {}", executionTime, endTime);

            // 标记慢接口
            if (executionTime > config.getSlowApiThreshold()) {
                logger.warn("[SLOW API] {} {} - Execution time: {} ms (threshold: {} ms)", 
                           requestMethod, requestURI, executionTime, config.getSlowApiThreshold());
            }
        }
    }

    /**
     * 格式化请求参数
     */
    private String formatRequestParams(Object[] args) {
        if (args == null || args.length == 0) {
            return "[]";
        }

        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < args.length; i++) {
            if (i > 0) {
                sb.append(", ");
            }

            Object arg = args[i];
            
            // 特殊处理文件上传对象
            if (arg instanceof MultipartFile) {
                MultipartFile file = (MultipartFile) arg;
                sb.append(String.format("MultipartFile(name=%s, size=%d bytes)", 
                    file.getOriginalFilename(), file.getSize()));
            } else if (arg instanceof HttpServletRequest || arg instanceof jakarta.servlet.http.HttpServletResponse) {
                // 跳过 Request 和 Response 对象
                sb.append(arg.getClass().getSimpleName());
            } else {
                try {
                    String json = JacksonUtil.bean2Json(arg);
                    sb.append(truncate(json, config.getLogThreshold()));
                } catch (Exception e) {
                    sb.append(arg != null ? arg.toString() : "null");
                }
            }
        }
        sb.append("]");
        return sb.toString();
    }

    /**
     * 格式化返回结果
     */
    private String formatResponse(Object result) {
        if (result == null) {
            return "null";
        }

        try {
            String json = JacksonUtil.bean2Json(result);
            return truncate(json, config.getLogThreshold());
        } catch (Exception e) {
            return result.toString();
        }
    }

    /**
     * 截断超长字符串
     */
    private String truncate(String str, int maxLength) {
        if (str == null) {
            return "null";
        }
        if (str.length() <= maxLength) {
            return str;
        }
        return str.substring(0, maxLength) + "... (truncated, total length: " + str.length() + ")";
    }

    /**
     * 获取 HttpServletRequest
     */
    private HttpServletRequest getHttpServletRequest() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            return attributes != null ? attributes.getRequest() : null;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 获取客户端真实 IP
     */
    private String getClientIp(HttpServletRequest request) {
        if (request == null) {
            return "N/A";
        }

        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }

        // 处理多个IP的情况（取第一个）
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }

        return ip != null ? ip : "N/A";
    }
}
