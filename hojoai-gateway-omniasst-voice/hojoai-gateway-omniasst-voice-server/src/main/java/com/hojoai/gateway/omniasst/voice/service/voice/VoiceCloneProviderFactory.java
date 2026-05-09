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
package com.hojoai.gateway.omniasst.voice.service.voice;

import com.hojoai.gateway.omniasst.voice.constants.VoiceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 声音克隆提供商注册表，由各 {@link VoiceCloneProvider} 在 {@link jakarta.annotation.PostConstruct} 中 {@link #registerProvider}。
 */
@Component
public class VoiceCloneProviderFactory {
    
    private static final Logger logger = LoggerFactory.getLogger(VoiceCloneProviderFactory.class);
    
    private final Map<String, VoiceCloneProvider> providerMap = new ConcurrentHashMap<>();

    /**
     * @param providerName 不区分大小写存储
     * @param provider     实现 Bean
     */
    public void registerProvider(String providerName, VoiceCloneProvider provider) {
        providerMap.put(providerName.toLowerCase(), provider);
        logger.info("Registered voice clone provider: {}", providerName);
    }

    /**
     * @param providerName 为空时使用默认 {@link VoiceProvider#HOJO}
     * @return 已注册实现，未找到时返回 {@code null}
     */
    public VoiceCloneProvider getProvider(String providerName) {
        if (providerName == null || providerName.isEmpty()) {
            logger.warn("Provider name is empty, using default '{}'", VoiceProvider.HOJO.getCode());
            providerName = VoiceProvider.HOJO.getCode();
        }
        VoiceCloneProvider provider = providerMap.get(providerName.toLowerCase());
        if (provider == null) {
            logger.warn("No voice clone provider found for: {}", providerName);
        }
        return provider;
    }
}
