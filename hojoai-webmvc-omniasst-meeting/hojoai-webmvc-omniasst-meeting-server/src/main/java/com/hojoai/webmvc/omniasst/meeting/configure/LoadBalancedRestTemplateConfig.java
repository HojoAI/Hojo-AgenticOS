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
package com.hojoai.webmvc.omniasst.meeting.configure;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * 用于通过服务名调用 management 等需 {@link org.springframework.web.client.RestTemplate} 的接口（如部分 multipart）。
 */
@Configuration
public class LoadBalancedRestTemplateConfig {

    public static final String BEAN_NAME = "loadBalancedRestTemplate";

    @Bean
    @LoadBalanced
    @Qualifier(BEAN_NAME)
    public RestTemplate loadBalancedRestTemplate() {
        return new RestTemplate();
    }
}
