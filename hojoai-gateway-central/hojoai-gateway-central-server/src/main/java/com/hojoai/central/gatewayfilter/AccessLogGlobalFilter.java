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
package com.hojoai.central.gatewayfilter;

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

/**
 * The gateway global filter class for access log.
 *
 * @author kevanwong
 * @version 1.0, 2025/08/13
 * @since hojoai-gateway-central 1.0.0
 *
 * <p>职责：在请求完成响应后记录单行访问日志（客户端 IP、方法、路径、状态码、内容长度、Referer、UA）。
 * <p>关键依赖：Spring Cloud Gateway {@link GlobalFilter}、{@code doFinally} 保证在响应结束后落日志。
 * <p>使用场景：运维审计、流量分析与问题排查；需在代理/网关后正确解析 {@code X-Forwarded-For} 等头时使用。
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class AccessLogGlobalFilter implements GlobalFilter {

    private static final Logger logger = LoggerFactory.getLogger(AccessLogGlobalFilter.class);

    /** HTTP 请求头名称：User-Agent，用于记录客户端标识。 */
    private static final String HEADER_USER_AGENT = "User-Agent";
    /** HTTP 请求头名称：Referer，用于记录来源页。 */
    private static final String HEADER_REFERER = "Referer";
    /** HTTP 请求头名称：X-Forwarded-For，代理链中的客户端 IP（取第一段为原始客户端）。 */
    private static final String HEADER_X_FORWARDED_FOR = "X-Forwarded-For";
    /** HTTP 请求头名称：X-Real-IP，部分代理传递的客户端真实 IP。 */
    private static final String HEADER_X_REAL_IP = "X-Real-IP";

    /** 无法解析客户端 IP 时的占位值。 */
    private static final String DEFAULT_CLIENT_IP = "unknown";
    /** 请求头或响应字段缺失时的占位显示。 */
    private static final String DEFAULT_EMPTY_STRING = "-";

    /**
     * 先放行请求经过下游过滤器与路由，在响应阶段 {@code doFinally} 中输出访问日志。
     *
     * @param exchange 当前请求与响应的交换上下文
     * @param chain    网关过滤器链
     * @return 与下游链路串联的异步完成信号；日志在请求处理结束后写入
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        return chain.filter(exchange).doFinally(signalType -> accessLog(exchange));
    }

    private static void accessLog(ServerWebExchange exchange) {
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();

        // 1. 汇总请求侧：方法、路径（含 QueryString）、UA、Referer、解析后的客户端 IP
        // Extract request information
        String method = request.getMethod().name();
        String path = request.getURI().getRawQuery() != null ? (request.getURI().getRawPath() + "?" + request.getURI().getRawQuery()) : request.getURI().getRawPath();
        String userAgent = request.getHeaders().getFirst(HEADER_USER_AGENT) != null ? request.getHeaders().getFirst(HEADER_USER_AGENT) : DEFAULT_EMPTY_STRING;
        String referer = request.getHeaders().getFirst(HEADER_REFERER) != null ? request.getHeaders().getFirst(HEADER_REFERER) : DEFAULT_EMPTY_STRING;
        String clientIP = resolveClientIP(request) != null ? resolveClientIP(request) : DEFAULT_CLIENT_IP;

        // 2. 汇总响应侧：HTTP 状态码、Content-Length（若可用）
        // Extract response information
        String status = response.getStatusCode() != null ? String.valueOf(response.getStatusCode().value()) : DEFAULT_EMPTY_STRING;
        String contentLength = response.getHeaders().getContentLength() >= 0 ? String.valueOf(response.getHeaders().getContentLength()) : DEFAULT_EMPTY_STRING;

        // 3. 单行结构化访问日志，便于日志采集与检索
        logger.info("access: {} {} {} {} {} \"{}\" \"{}\"", clientIP, method, path, status, contentLength, referer, userAgent);
    }

    /**
     * 按优先级从代理相关头解析客户端 IP，否则回退到直连远端地址。
     *
     * @param request 当前响应式请求
     * @return 解析到的客户端 IP；无法解析时返回 {@code null}
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
