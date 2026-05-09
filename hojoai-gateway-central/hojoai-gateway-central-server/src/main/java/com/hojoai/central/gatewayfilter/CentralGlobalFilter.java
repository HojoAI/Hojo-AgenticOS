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
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * The gateway global filter class for central.
 *
 * @author kevanwong
 * @version 1.0, 2020/05/12
 * @since hojoai-gateway-central 1.0.0
 *
 * <p>职责：在中央网关链路最前序执行通用逻辑（当前为示例日志），再交给下游 {@code GatewayFilter} 链。
 * <p>关键依赖：Spring Cloud Gateway {@link GlobalFilter}、Reactor {@link reactor.core.publisher.Mono}。
 * <p>使用场景：需要对全站请求做统一前置处理且要求最高优先级时使用。
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CentralGlobalFilter implements GlobalFilter {

    private static final Logger logger = LoggerFactory.getLogger(CentralGlobalFilter.class);

    /**
     * 执行本过滤器逻辑：先运行自定义步骤，再将交换上下文交给后续过滤器链。
     *
     * @param exchange 当前请求与响应的交换上下文
     * @param chain    网关过滤器链，用于继续转发请求
     * @return 与下游链路串联的异步完成信号
     * @throws UnsupportedOperationException 当自定义步骤抛出异常并经包装后抛出时
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        try {
            // 示例步骤：可替换为鉴权、埋点等实际业务
            executeExample();
        } catch (Exception e) {
            // 记录原始异常并包装抛出，避免静默失败，便于上层以错误信号结束 Mono
            logger.error("central gateway filter exception", e);
            throw new UnsupportedOperationException("central gateway filter exception", e);
        }

        return chain.filter(exchange);
    }

    /** 示例：记录中央网关过滤器执行日志，可扩展为实际业务逻辑。 */
    private void executeExample() {
        logger.info("execute central gateway filter");
    }
}
