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
package com.hojoai.gateway.omniasst.voice.configure;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/**
 * HTTP 客户端与 JSON 工具 Bean 定义。
 * 为 Feign 之外的同步 HTTP 调用提供带超时配置的 {@link RestTemplate}。
 */
@Configuration
public class RestTemplateConfig {
    /**
     * 连接超时 10s、读取超时 30s 的 {@link RestTemplate}。
     *
     * @return RestTemplate 实例
     */
    @Bean
    public RestTemplate restTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(10000);
        factory.setReadTimeout(30000);
        return new RestTemplate(factory);
    }

    /**
     * 默认配置的 Jackson {@link ObjectMapper}。
     *
     * @return ObjectMapper 实例
     */
    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
}
