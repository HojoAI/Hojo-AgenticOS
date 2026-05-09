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
package com.hojoai.central.check;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

/**
 * The health indicator class.
 *
 * @author kevanwong
 * @version 1.0, 2020/05/12
 * @since hojoai-gateway-central 1.0.0
 *
 * <p>职责：向 Spring Boot Actuator 提供本服务的存活/就绪类健康状态，供编排与负载均衡探测。
 * <p>关键依赖：{@link HealthIndicator}、{@link Health}。
 * <p>使用场景：Kubernetes liveness/readiness、注册中心或监控拉取 {@code /actuator/health} 时使用。
 */
@Component
public class IHealthIndicator implements HealthIndicator {

    /**
     * 执行健康检查：非零错误码表示不健康，详情中带 {@code Error Code}。
     *
     * @return {@link Health#up()} 表示通过；{@link Health#down()} 表示未通过并附带错误码
     */
    @Override
    public Health health() {
        // 可替换为对数据库、下游 RPC、缓存等的真实探测
        int errorCode = check(); // perform some specific health check
        if (errorCode != 0) {
            return Health.down().withDetail("Error Code", errorCode).build();
        }
        return Health.up().build();
    }

    /** 占位检查逻辑：返回 0 表示健康；扩展时返回非 0 表示失败原因编码。 */
    private int check() {
        return 0;
    }
}
