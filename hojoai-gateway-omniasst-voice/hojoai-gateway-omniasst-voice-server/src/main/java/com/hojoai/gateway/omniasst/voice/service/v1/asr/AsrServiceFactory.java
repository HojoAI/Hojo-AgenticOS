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
package com.hojoai.gateway.omniasst.voice.service.v1.asr;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ASR v1 平台实现注册表，由各类 {@link AsrService} 在启动时 {@link #registerService}，WebSocket 按平台码解析。
 */
@Component
public class AsrServiceFactory {
    
    private static final Logger logger = LoggerFactory.getLogger(AsrServiceFactory.class);
    
    private final Map<Integer, AsrService> serviceMap = new ConcurrentHashMap<>();

    /**
     * @param platform {@link AsrPlatform} 等定义的整型码
     * @param service  对应实现
     */
    public void registerService(Integer platform, AsrService service) {
        serviceMap.put(platform, service);
        logger.info("Registered ASR service for platform: {}", platform);
    }

    /**
     * @param platform 平台码；未命中时回退平台 {@code 0}
     * @return 非 null（若平台 0 也未注册则可能为 null，调用方需防护）
     */
    public AsrService getService(Integer platform) {
        AsrService service = serviceMap.get(platform);
        if (service == null) {
            logger.warn("No ASR service found for platform: {}, using default", platform);
            service = serviceMap.get(0);
        }
        return service;
    }
}
