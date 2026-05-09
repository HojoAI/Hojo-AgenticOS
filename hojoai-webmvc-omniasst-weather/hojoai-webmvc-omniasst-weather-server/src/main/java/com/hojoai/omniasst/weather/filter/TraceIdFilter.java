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
package com.hojoai.omniasst.weather.filter;

import com.hojoai.omniasst.weather.util.TraceIdUtil;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * TraceId过滤器
 * 在请求入口处生成或获取traceId，并设置到MDC中
 * 
 * @author HojoAI
 * @since 2026-01-28
 */
@Component
@Order(1)
public class TraceIdFilter implements Filter {
    
    /**
     * HTTP请求头中traceId的key
     */
    private static final String TRACE_ID_HEADER = "X-Trace-Id";

    /**
     * 从请求头 {@value #TRACE_ID_HEADER} 读取或生成 traceId，写入 MDC 与响应头，请求结束后清理 MDC。
     *
     * @param request  Servlet 请求
     * @param response Servlet 响应
     * @param chain    过滤器链
     * @throws IOException      链执行 IO 异常
     * @throws ServletException 链执行 Servlet 异常
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
