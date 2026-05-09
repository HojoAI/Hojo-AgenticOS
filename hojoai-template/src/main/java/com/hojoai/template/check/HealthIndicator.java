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
package com.hojoai.template.check;

import org.springframework.boot.actuate.health.Health;
import org.springframework.stereotype.Component;

/**
 * Actuator 自定义健康检查示例。通过 {@link #check()} 返回错误码，非 0 时上报为 DOWN，供编排与监控探测。
 */
@Component
public class HealthIndicator implements org.springframework.boot.actuate.health.HealthIndicator {

    /**
     * 执行自定义探测逻辑并映射为 Spring Boot {@link Health}。
     *
     * @return {@code UP} 表示检查通过；{@code DOWN} 时附带 Error Code 明细
     */
    @Override
    public Health health() {
        // perform some specific health check
        int errorCode = check();
        if (errorCode != 0) {
            return Health.down().withDetail("Error Code", errorCode).build();
        }
        return Health.up().build();
    }

    /**
     * 具体健康检查实现占位：返回 0 表示正常，其它值为业务定义的错误码。
     *
     * @return 0 表示健康；非 0 表示不健康
     */
    private int check() {
        return 0;
    }
}
