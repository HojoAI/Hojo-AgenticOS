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
package com.hojoai.central;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * The spring application class.
 *
 * @author kevanwong
 * @version 1.0, 2020/05/12
 * @since hojoai-gateway-central 1.0.0
 *
 * <p>职责：中央网关应用的启动入口，负责引导 Spring Boot 上下文并启用服务发现客户端。
 * <p>关键依赖：{@link SpringBootApplication}、{@link EnableDiscoveryClient}（与 Nacos 等注册中心集成）。
 * <p>使用场景：以独立进程部署 API 网关，将请求路由至下游微服务。
 */
@SpringBootApplication
@EnableDiscoveryClient
public class ISpringApplication {

    /**
     * 启动 Spring 应用上下文，加载网关、配置与注册发现等自动配置。
     *
     * @param args 命令行参数，透传至 Spring Boot
     */
    public static void main(String[] args) {
        SpringApplication.run(ISpringApplication.class, args);
    }
}
