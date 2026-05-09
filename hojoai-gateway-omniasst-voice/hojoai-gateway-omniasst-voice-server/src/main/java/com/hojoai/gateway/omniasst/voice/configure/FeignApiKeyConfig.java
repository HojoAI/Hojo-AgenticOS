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

import feign.RequestInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Feign 调用下游语音服务时注入 API-Key 请求头。
 * 按 Feign 客户端名称（speech-asr、speech-tts、speech-asr-trans）选择对应配置项。
 */
@Configuration
public class FeignApiKeyConfig {
    
    @Value("${ai.asr.api-key:}")
    private String asrApiKey;
    
    @Value("${ai.tts.api-key:}")
    private String ttsApiKey;
    
    @Value("${ai.asr-trans.api-key:${ai.asr.api-key:}}")
    private String asrTransApiKey;
    
    /**
     * 为匹配的 Feign 目标统一添加 {@code API-Key} 头。
     *
     * @return Feign 请求拦截器
     */
    @Bean
    public RequestInterceptor apiKeyInterceptor() {
        return template -> {
            String feignClientName = template.feignTarget().name();
            if ("speech-asr".equals(feignClientName)) {
                template.header("API-Key", asrApiKey);
            } else if ("speech-tts".equals(feignClientName)) {
                template.header("API-Key", ttsApiKey);
            } else if ("speech-asr-trans".equals(feignClientName)) {
                template.header("API-Key", asrTransApiKey);
            }
        };
    }
}

