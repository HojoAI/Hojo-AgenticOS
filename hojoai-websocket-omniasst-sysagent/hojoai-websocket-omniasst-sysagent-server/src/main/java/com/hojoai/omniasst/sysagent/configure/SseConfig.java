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
package com.hojoai.omniasst.sysagent.configure;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * SSE 相关线程池配置：提供带命名线程的调度器，供心跳等定时任务使用。
 */
@Configuration
public class SseConfig {
    /**
     * SSE 心跳/调度专用线程池，核心数以 CPU 核数为参考。
     *
     * @return 随容器销毁时 {@code shutdownNow} 的调度器
     */
    @Bean(destroyMethod = "shutdownNow")
    @Primary
    public ScheduledExecutorService sseHeartbeatScheduler() {
        int corePoolSize = Runtime.getRuntime().availableProcessors();
        return Executors.newScheduledThreadPool(
                corePoolSize,
                r -> new Thread(r, "sse-heartbeat-thread-" + System.nanoTime())
        );
    }
}
