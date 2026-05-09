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
package com.hojoai.omniasst.sysagent.check;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

/**
 * The health indicator class.
 *
 * @author kevanwong
 * @version 1.0, 2020/05/12
 * @since hojoai-websocket-omniasst-sysagent 1.0.0
 */
@Component
public class IHealthIndicator implements HealthIndicator {

    /**
     * Actuator 健康检查入口：{@code check()==0} 视为 UP，否则 DOWN 并附带错误码。
     *
     * @return {@link Health#up()} 或 {@link Health#down()}
     */
    @Override
    public Health health() {
        int errorCode = check(); // perform some specific health check
        if (errorCode != 0) {
            return Health.down().withDetail("Error Code", errorCode).build();
        }
        return Health.up().build();
    }

    private int check() {
        return 0;
    }
}
