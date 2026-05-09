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
package com.hojoai.central.configure;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.config.EnableWebFlux;
import org.springframework.web.reactive.config.WebFluxConfigurationSupport;

/**
 * The web flux configuration support class.
 *
 * @author kevanwong
 * @version 1.0, 2020/05/12
 * @since hojoai-gateway-central 1.0.0
 *
 * <p>职责：启用 Spring WebFlux 并继承 {@link WebFluxConfigurationSupport}，采用框架默认的响应式 Web 配置。
 * <p>关键依赖：{@link EnableWebFlux}、{@link WebFluxConfigurationSupport}。
 * <p>使用场景：网关等基于 WebFlux 的应用需要统一入口配置时使用；可在子类中覆盖以定制格式转换器等。
 */
@EnableWebFlux
@Configuration
public class IWebFluxConfigurationSupport extends WebFluxConfigurationSupport {

}
