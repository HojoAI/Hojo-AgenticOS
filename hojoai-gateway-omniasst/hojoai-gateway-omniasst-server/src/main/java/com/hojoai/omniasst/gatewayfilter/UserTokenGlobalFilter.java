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

import com.hojoai.common.jackson.JacksonUtil;
import com.hojoai.common.response.Response;
import com.hojoai.common.response.ResponseStatusBasic;
import com.hojoai.omniasst.common.ResponseStatusExtra;
import com.hojoai.omniasst.configure.IConfiguration;
import io.micrometer.common.util.StringUtils;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.Set;

/**
 * The global filter class for user token.
 *
 * @author kevanwong
 * @version 1.0, 2025/11/07
 * @since hojoai-gateway-omniasst 1.0.0
 */
@Order(Ordered.HIGHEST_PRECEDENCE)
public class UserTokenGlobalFilter implements GlobalFilter {

    private static final Logger logger = LoggerFactory.getLogger(UserTokenGlobalFilter.class);

    /**
     * 需要走用户 Token 校验的路由（路径 + HTTP 方法）。未出现在此表中的请求将直接放行，不校验 Token。
     */
    private static final Map<String, Set<HttpMethod>> USER_TOKEN_REQUIRED_ROUTES = Map.of(
            // System Agent
            "/omniasst-sysagent", Set.of(HttpMethod.GET),
            // Mobile Captchas
            "/omniasst-account/mobile-captchas", Set.of(HttpMethod.POST),
            // User Registration
            "/omniasst-account/users", Set.of(HttpMethod.POST),
            // Token Generation
            "/omniasst-account/tokens", Set.of(HttpMethod.POST));

    /** 调用账户服务校验 Token 时使用的业务类型标识 */
    private static final int BUSINESS_TYPE = 1;

    @Resource
    private IConfiguration iConfiguration;

    @Resource
    private RestTemplate restTemplate;

    /**
     * 对 {@link #USER_TOKEN_REQUIRED_ROUTES} 中配置的路径与方法进行用户 Token 校验：请求头需携带
     * {@code X-User-Id}、{@code X-User-Token}、{@code X-Platform-Type}，并通过账户服务接口校验有效性。
     * 未在表中的路由直接放行；校验失败返回业务错误码；调用账户服务异常时返回网关错误。
     *
     * @param exchange 当前请求/响应上下文
     * @param chain    后续过滤器链
     * @return 异步完成信号
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        try {
            // 非配置表内路由：不校验 Token，直接向后传递
            if (!isUserTokenRequired(exchange)) {
                return chain.filter(exchange);
            }

            // 表内路由：校验 Token，失败则短路返回 JSON 错误体
            if (!isUserTokenValid(exchange)) {
                return makeResponse(exchange, Response.newInstance(ResponseStatusExtra.ILLEGAL_USER_TOKEN));
            }
        } catch (Exception e) {
            logger.error("user token global filter error", e);
            return makeResponse(exchange, Response.newInstance(ResponseStatusExtra.GATEWAY_ERROR));
        }
        return chain.filter(exchange);
    }

    /**
     * 判断当前请求是否属于需要执行用户 Token 校验的路由（路径与方法均命中 {@link #USER_TOKEN_REQUIRED_ROUTES}）。
     *
     * @param exchange 当前请求上下文
     * @return 为 {@code true} 时表示需要校验 Token；为 {@code false} 时放行
     */
    private static boolean isUserTokenRequired(ServerWebExchange exchange) {
        String path = exchange.getRequest().getURI().getPath();
        HttpMethod method = exchange.getRequest().getMethod();

        Set<HttpMethod> httpMethods = USER_TOKEN_REQUIRED_ROUTES.get(path);
        return httpMethods != null && httpMethods.contains(method);
    }

    /**
     * 从请求头读取用户标识与 Token，请求账户服务校验；HTTP 非 200 时抛出异常由上层转为网关错误。
     *
     * @param exchange 当前请求上下文
     * @return Token 合法且账户服务返回成功码时为 {@code true}，否则为 {@code false}
     * @throws UnsupportedOperationException 当账户服务响应状态非 200 时
     */
    private boolean isUserTokenValid(ServerWebExchange exchange) {
        String userId = exchange.getRequest().getHeaders().getFirst("X-User-Id");
        String userToken = exchange.getRequest().getHeaders().getFirst("X-User-Token");
        String platformType = exchange.getRequest().getHeaders().getFirst("X-Platform-Type");

        if (userId == null || StringUtils.isBlank(userId)) {
            return false;
        }
        if (userToken == null || StringUtils.isBlank(userToken)) {
            return false;
        }
        if (platformType == null || StringUtils.isBlank(platformType)) {
            return false;
        }

        String url = iConfiguration.getCentralAccountBaseUrl() + "/tokens" + "?userId=" + userId + "&businessType=" + BUSINESS_TYPE + "&platformType=" + platformType + "&userToken=" + userToken;
        ResponseEntity<Response<?>> responseEntity = restTemplate.exchange(url, HttpMethod.GET, null, new ParameterizedTypeReference<>() {
        });
        if (HttpStatus.OK != responseEntity.getStatusCode()) {
            throw new UnsupportedOperationException("query user token failed, status code: " + responseEntity.getStatusCode());
        }

        return responseEntity.getBody() != null && ResponseStatusBasic.OK.getResCode().equals(responseEntity.getBody().getResCode());
    }

    /**
     * 将业务响应序列化为 JSON 写入响应体，HTTP 状态固定为 200，Content-Type 为 {@code application/json}。
     *
     * @param exchange 当前响应上下文
     * @param response 业务响应对象
     * @return 异步完成信号
     */
    private Mono<Void> makeResponse(ServerWebExchange exchange, Response<Object> response) {
        ServerHttpResponse serverHttpResponse = exchange.getResponse();
        serverHttpResponse.setStatusCode(HttpStatus.OK);
        serverHttpResponse.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        String jsonResponse = JacksonUtil.bean2Json(response);
        return serverHttpResponse.writeWith(Mono.just(serverHttpResponse.bufferFactory().wrap(jsonResponse.getBytes())));
    }
}
