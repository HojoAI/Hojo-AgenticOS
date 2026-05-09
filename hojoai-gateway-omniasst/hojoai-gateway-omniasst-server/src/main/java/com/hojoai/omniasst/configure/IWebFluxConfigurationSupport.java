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
package com.hojoai.omniasst.configure;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.config.EnableWebFlux;
import org.springframework.web.reactive.config.WebFluxConfigurationSupport;

/**
 * The web flux configuration support class.
 *
 * @author kevanwong
 * @version 1.0, 2020/05/12
 * @since hojoai-gateway-omniasst 1.0.0
 */
@EnableWebFlux
@Configuration
public class IWebFluxConfigurationSupport extends WebFluxConfigurationSupport {

    @Value("${rest-template.connect-timeout}")
    private Integer restTemplateConnectTimeout;

    @Value("${rest-template.read-timeout}")
    private Integer restTemplateReadTimeout;

    @Bean
    public RestTemplate restTemplate() {
        HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
        requestFactory.setConnectTimeout(restTemplateConnectTimeout);
        requestFactory.setReadTimeout(restTemplateReadTimeout);

        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setRequestFactory(requestFactory);
        return restTemplate;
    }
}
