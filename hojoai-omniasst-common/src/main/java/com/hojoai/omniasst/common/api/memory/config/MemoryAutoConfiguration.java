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
package com.hojoai.omniasst.common.api.memory.config;

import com.hojoai.omniasst.common.api.memory.client.MemoryApiClient;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

/**
 * Memory API 自动配置：在 classpath 存在 OpenFeign 且未关闭 {@code hojoai.memory.api.enabled} 时生效。
 * <p>
 * 注册 {@link MemoryApiProperties}、扫描 {@code MemoryApiClient} Feign 接口，并扫描 {@code com.hojoai.omniasst.common.utils}
 * 包（含 {@link com.hojoai.omniasst.common.utils.SpringContextHolder}）。{@link com.hojoai.omniasst.common.utils.MemoryUtils} 为静态工具，无需声明为 Bean。
 * </p>
 */
@AutoConfiguration
@ConditionalOnClass(name = "org.springframework.cloud.openfeign.FeignClient")
@ConditionalOnProperty(
        prefix = "hojoai.memory.api",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true
)
@EnableConfigurationProperties(MemoryApiProperties.class)
@EnableFeignClients(basePackageClasses = MemoryApiClient.class)
@ComponentScan(basePackages = "com.hojoai.omniasst.common.utils")
public class MemoryAutoConfiguration {
    // MemoryApiClient 会由 @EnableFeignClients 自动创建和注册
    // MemoryUtils 为纯静态工具类，无需在此注册 Bean
}
