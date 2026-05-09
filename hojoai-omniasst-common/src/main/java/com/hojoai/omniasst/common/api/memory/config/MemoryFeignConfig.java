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

import feign.Logger;
import feign.Request;
import feign.RequestInterceptor;
import feign.Retryer;
import feign.codec.ErrorDecoder;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;

/**
 * Memory API 专用 Feign 配置类，仅绑定到 {@link com.hojoai.omniasst.common.api.memory.client.MemoryApiClient}。
 * <p>从 {@link MemoryApiProperties} 读取超时、日志及多租户请求头；与业务中其他 FeignClient 的配置相互隔离。</p>
 */
public class MemoryFeignConfig {

    @Autowired(required = false)
    private MemoryApiProperties properties;

    /** 未配置组织 ID 时占位用，仅用于满足非空分支逻辑，实际生产环境应配置真实租户 ID */
    private static final String DEFAULT_HEADER_X_ORGANIZATION_ID = "default_organization_id";
    /** 未配置空间 ID 时占位用 */
    private static final String DEFAULT_HEADER_X_SPACE_ID = "default_space_id";
    /** 未配置 Hash Key 时占位用 */
    private static final String DEFAULT_HEADER_X_HASH_KEY = "default_hash_key";

    /**
     * 配置 Feign 请求选项（连接与读取超时）。
     *
     * @return Feign 请求选项；properties 缺失时使用 5s/10s 默认超时
     */
    @Bean
    public Request.Options feignOptions() {
        if (properties != null) {
            return new Request.Options(
                    properties.getConnectTimeout(),
                    java.util.concurrent.TimeUnit.MILLISECONDS,
                    properties.getReadTimeout(),
                    java.util.concurrent.TimeUnit.MILLISECONDS,
                    true
            );
        }
        return new Request.Options(
                5000,
                java.util.concurrent.TimeUnit.MILLISECONDS,
                10000,
                java.util.concurrent.TimeUnit.MILLISECONDS,
                true
        );
    }

    /**
     * 配置 Feign 日志级别（受 {@code hojoai.memory.api.log-enabled} 与 {@code log-level} 控制）。
     *
     * @return Feign 日志级别枚举
     */
    @Bean
    public Logger.Level feignLoggerLevel() {
        if (properties != null && properties.getLogEnabled()) {
            return switch (properties.getLogLevel().toUpperCase()) {
                case "NONE" -> Logger.Level.NONE;
                case "BASIC" -> Logger.Level.BASIC;
                case "HEADERS" -> Logger.Level.HEADERS;
                case "FULL" -> Logger.Level.FULL;
                default -> Logger.Level.BASIC;
            };
        }
        return Logger.Level.BASIC;
    }

    /**
     * 配置重试策略：关闭 Feign 默认重试，避免记忆接口幂等边界下重复提交。
     *
     * @return 不重试的 {@link Retryer} 实例
     */
    @Bean
    public Retryer feignRetryer() {
        return Retryer.NEVER_RETRY;
    }

    /**
     * 注册 Memory API 专用错误解码器（当前委托默认解码，便于后续扩展）。
     *
     * @return 错误解码器 Bean
     */
    @Bean
    public ErrorDecoder errorDecoder() {
        return new MemoryApiErrorDecoder();
    }

    /**
     * 请求拦截器：在已配置的前提下为每个请求附加多租户头（X-Organization-Id、X-Space-Id、X-Hash-Key）。
     *
     * @return 租户头拦截器
     */
    @Bean
    public RequestInterceptor tenantHeaderInterceptor() {
        return requestTemplate -> {
            if (properties != null) {
                // 添加组织ID
                if (StringUtils.isNotBlank(properties.getOrganizationId())) {
                    String orgId = StringUtils.isBlank(properties.getOrganizationId()) ? DEFAULT_HEADER_X_ORGANIZATION_ID : properties.getOrganizationId();
                    requestTemplate.header("X-Organization-Id", orgId);
                }
                // 添加空间ID
                if (StringUtils.isNotBlank(properties.getSpaceId())) {
                    String spaceId = StringUtils.isBlank(properties.getSpaceId()) ? DEFAULT_HEADER_X_SPACE_ID : properties.getSpaceId();
                    requestTemplate.header("X-Space-Id", spaceId);
                }
                // 添加Hash Key
                if (StringUtils.isNotBlank(properties.getHashKey())) {
                    String hashKey = StringUtils.isBlank(properties.getHashKey()) ? DEFAULT_HEADER_X_HASH_KEY : properties.getHashKey();
                    requestTemplate.header("X-Hash-Key", hashKey);
                }
            }
        };
    }

    /**
     * Memory API 的 Feign 错误解码器；将 HTTP 错误映射为异常，便于调用方统一处理。
     */
    public static class MemoryApiErrorDecoder implements ErrorDecoder {
        private final ErrorDecoder defaultDecoder = new Default();

        /**
         * 将非 2xx 或协议层错误转换为可抛出异常。
         *
         * @param methodKey  Feign 方法标识
         * @param response   原始 HTTP 响应
         * @return 供 Feign 抛出的异常，不为 null
         */
        @Override
        public Exception decode(String methodKey, feign.Response response) {
            return defaultDecoder.decode(methodKey, response);
        }
    }
}
