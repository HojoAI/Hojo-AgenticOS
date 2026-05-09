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

import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

/**
 * Injects {@code X-Powered-By} on every HTTP response (gateway routes, WebSocket upgrade, and local controllers).
 *
 * @author zhaoyuliang
 * @since hojoai-gateway-omniasst 1.0.0
 */
@Component
public class BrandingResponseHeaderWebFilter implements WebFilter, Ordered {

    private static final String HEADER_X_POWERED_BY = "X-Powered-By";
    private static final String X_POWERED_BY_VALUE = "Hojo-Agentic-OS";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        exchange.getResponse().getHeaders().set(HEADER_X_POWERED_BY, X_POWERED_BY_VALUE);
        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
