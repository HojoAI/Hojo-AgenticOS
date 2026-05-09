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
package com.hojoai.omniasst.gatewayfilter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Objects;

/**
 * The global filter class for access log.
 *
 * @author kevanwong
 * @version 1.0, 2025/08/13
 * @since hojoai-gateway-omniasst 1.0.0
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class AccessLogGlobalFilter implements GlobalFilter {

    private static final Logger logger = LoggerFactory.getLogger(AccessLogGlobalFilter.class);

    /** HTTP 请求头：User-Agent */
    private static final String HEADER_USER_AGENT = "User-Agent";
    /** HTTP 请求头：Referer */
    private static final String HEADER_REFERER = "Referer";
    /** HTTP 请求头：经代理时的客户端 IP 链（取首个为原始客户端） */
    private static final String HEADER_X_FORWARDED_FOR = "X-Forwarded-For";
    /** HTTP 请求头：代理注入的真实客户端 IP */
    private static final String HEADER_X_REAL_IP = "X-Real-IP";
    /** HTTP 请求头：协议升级（用于识别 WebSocket） */
    private static final String HEADER_UPGRADE = "Upgrade";

    /** 无法解析客户端 IP 时的占位值 */
    private static final String DEFAULT_CLIENT_IP = "unknown";
    /** 缺失请求头时的日志占位符 */
    private static final String DEFAULT_EMPTY_STRING = "-";

    /** 在 Exchange 属性中存放请求开始时间（毫秒）的键，供耗时统计使用 */
    private static final String ATTRIBUTE_START_TIME = "requestStartTime";

    /**
     * 在请求进入时记录开始时间，在响应完成后输出一条访问日志（含耗时、状态码、客户端 IP、请求类型等）。
     *
     * @param exchange 当前请求/响应上下文
     * @param chain    后续过滤器链
     * @return 异步完成信号
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // Record the start time
        exchange.getAttributes().put(ATTRIBUTE_START_TIME, System.currentTimeMillis());
        
        return chain.filter(exchange).doFinally(signal -> {
            Objects.requireNonNull(signal, "signal");
            accessLog(exchange);
        });
    }

    private static void accessLog(ServerWebExchange exchange) {
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();

        // Calculate request duration
        Long startTime = exchange.getAttribute(ATTRIBUTE_START_TIME);
        long duration = startTime != null ? System.currentTimeMillis() - startTime : -1;

        // Extract request information
        String method = request.getMethod().name();
        String path = request.getURI().getRawQuery() != null ? (request.getURI().getRawPath() + "?" + request.getURI().getRawQuery()) : request.getURI().getRawPath();
        String userAgent = request.getHeaders().getFirst(HEADER_USER_AGENT) != null ? request.getHeaders().getFirst(HEADER_USER_AGENT) : DEFAULT_EMPTY_STRING;
        String referer = request.getHeaders().getFirst(HEADER_REFERER) != null ? request.getHeaders().getFirst(HEADER_REFERER) : DEFAULT_EMPTY_STRING;
        String clientIP = resolveClientIP(request) != null ? resolveClientIP(request) : DEFAULT_CLIENT_IP;

        // Extract response information
        String status = response.getStatusCode() != null ? String.valueOf(response.getStatusCode().value()) : DEFAULT_EMPTY_STRING;
        String contentLength = response.getHeaders().getContentLength() >= 0 ? String.valueOf(response.getHeaders().getContentLength()) : DEFAULT_EMPTY_STRING;

        // Determine request type
        String requestType = determineRequestType(request);

        logger.info("access: {} {} {} {} {} {}ms [{}] \"{}\" \"{}\"", 
                clientIP, method, path, status, contentLength, duration, requestType, referer, userAgent);
    }

    /**
     * Determine the type of request based on headers and other characteristics
     *
     * @param request the server http request
     * @return request type (HTTP, SSE, or WebSocket)
     */
    private static String determineRequestType(ServerHttpRequest request) {
        // Check for WebSocket upgrade
        String upgrade = request.getHeaders().getFirst(HEADER_UPGRADE);
        if ("websocket".equalsIgnoreCase(upgrade)) {
            return "WebSocket";
        }

        // Check for SSE (Server-Sent Events)
        String accept = request.getHeaders().getFirst("Accept");
        if (accept != null && accept.contains("text/event-stream")) {
            return "SSE";
        }

        // Default to HTTP
        return "HTTP";
    }

    /**
     * 解析真实客户端 IP：优先 {@code X-Forwarded-For} 第一个地址，其次 {@code X-Real-IP}，最后使用连接的远端地址。
     *
     * @param request 当前请求
     * @return 解析得到的 IP；若均不可用则可能为 {@code null}
     */
    private static String resolveClientIP(ServerHttpRequest request) {
        // Try X-Forwarded-For header first (first IP is the original client)
        String forwardedFor = request.getHeaders().getFirst(HEADER_X_FORWARDED_FOR);
        if (forwardedFor != null && !forwardedFor.trim().isEmpty()) {
            String firstIP = forwardedFor.split(",", 2)[0].trim();
            if (!firstIP.isEmpty()) {
                return firstIP;
            }
        }

        // Fallback to X-Real-IP header
        String realIP = request.getHeaders().getFirst(HEADER_X_REAL_IP);
        if (realIP != null && !realIP.trim().isEmpty()) {
            return realIP.trim();
        }

        // Last resort: use remote address from connection
        return request.getRemoteAddress() != null ? request.getRemoteAddress().getAddress().getHostAddress() : null;
    }
}
