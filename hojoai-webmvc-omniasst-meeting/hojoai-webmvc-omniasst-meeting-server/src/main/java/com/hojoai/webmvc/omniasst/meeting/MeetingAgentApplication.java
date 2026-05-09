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
package com.hojoai.webmvc.omniasst.meeting;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * 会议服务启动类：启用 Nacos 发现、Feign（本模块 API 包）、异步任务。
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients("com.hojoai.webmvc.omniasst.meeting")
@EnableAsync
public class MeetingAgentApplication {
    /**
     * @param args 命令行参数（如 {@code --spring.profiles.active=dev}）
     */
    public static void main(String[] args) {

        SpringApplication.run(MeetingAgentApplication.class, args);
    }
}

