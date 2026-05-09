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
package com.hojoai.gateway.omniasst.voice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 语音网关服务启动类：启用服务发现、本包 Feign 客户端、异步与定时任务（如 WebSocket 心跳检测）。
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients("com.hojoai.gateway.omniasst.voice")
@EnableAsync
@EnableScheduling
public class VoiceServiceApplication {

    /**
     * @param args 标准 Spring Boot 启动参数
     */
    public static void main(String[] args) {
        SpringApplication.run(VoiceServiceApplication.class, args);
    }
}

