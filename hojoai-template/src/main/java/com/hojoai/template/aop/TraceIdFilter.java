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
package com.hojoai.template.aop;

import com.hojoai.template.utils.TraceIdUtil;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 请求级 TraceId 过滤器。从请求头读取或生成 traceId，写入 MDC 并在响应头回传，便于日志关联；
 * 请求结束时清理 MDC，避免线程池复用导致串链。
 */
@Component
@Order(1)
public class TraceIdFilter implements Filter {
    
    /**
     * HTTP 请求头与响应头中传递 traceId 的头部名称。
     */
    private static final String TRACE_ID_HEADER = "X-Trace-Id";
    
    /**
     * 为当前请求建立 trace 上下文并继续过滤器链；在 {@code finally} 中清理 MDC。
     *
     * @param request  Servlet 请求
     * @param response Servlet 响应
     * @param chain    过滤器链
     * @throws IOException      IO 异常
     * @throws ServletException Servlet 容器异常
     */
    @Override
    public void doFilter(jakarta.servlet.ServletRequest request, 
                        jakarta.servlet.ServletResponse response, 
                        FilterChain chain) 
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        try {
            // 优先从请求头获取traceId，如果没有则生成新的
            String traceId = httpRequest.getHeader(TRACE_ID_HEADER);
            if (traceId == null || traceId.isEmpty()) {
                traceId = TraceIdUtil.generateTraceId();
            }
            
            // 设置traceId到MDC
            TraceIdUtil.setTraceId(traceId);
            
            // 可选：将traceId添加到响应头，方便前端追踪
            httpResponse.setHeader(TRACE_ID_HEADER, traceId);
            
            // 继续执行过滤器链
            chain.doFilter(request, response);
            
        } finally {
            // 请求结束后清除MDC，避免内存泄漏
            TraceIdUtil.clear();
        }
    }
}
