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
package com.hojoai.gateway.omniasst.voice.service.tts;

import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 按 {@code platform + sessionId} 缓存 {@link TtsQueueConsumer} 实例，并从 Spring 容器解析具体实现 Bean。
 */
@Component
public class TtsQueueConsumerFactory {
    private static final Logger logger = LoggerFactory.getLogger(TtsQueueConsumerFactory.class);
    
    private final Map<String, TtsQueueConsumer> sessionConsumerMap = new ConcurrentHashMap<>();
    
    @Resource
    private ApplicationContext applicationContext;

    /**
     * 获取或创建与会话绑定的消费者（同平台同会话复用同一实例）。
     *
     * @param platform  平台码，null 视为 0
     * @param sessionId 会话 ID
     * @return 匹配的 {@link TtsQueueConsumer}
     * @throws IllegalStateException 未找到对应平台的 Bean
     */
    public TtsQueueConsumer getConsumer(Integer platform, String sessionId) {
        if (platform == null) {
            platform = 0;
        }
        
        String key = platform + "_" + sessionId;
        TtsQueueConsumer consumer = sessionConsumerMap.get(key);
        
        if (consumer == null) {
            consumer = createConsumerByPlatform(platform);
            sessionConsumerMap.put(key, consumer);
            logger.debug("Created and cached consumer for session: {}, platform: {}", sessionId, platform);
        }
        
        return consumer;
    }
    
    private TtsQueueConsumer createConsumerByPlatform(Integer platform) {
        Map<String, TtsQueueConsumer> consumers = applicationContext.getBeansOfType(TtsQueueConsumer.class);
        for (TtsQueueConsumer consumer : consumers.values()) {
            if (consumer.getPlatform().equals(platform)) {
                return consumer;
            }
        }
        
        logger.error("No TTS queue consumer found for platform: {}, using default platform 0", platform);
        throw new IllegalStateException("No TTS queue consumer available for platform: " + platform);
    }

    /**
     * 移除缓存并调用消费者的 {@link TtsQueueConsumer#cleanup(String)}（如关闭长连接）。
     *
     * @param platform  平台码
     * @param sessionId 会话 ID
     */
    public void removeSession(Integer platform, String sessionId) {
        if (platform == null) {
            platform = 0;
        }
        String key = platform + "_" + sessionId;
        TtsQueueConsumer removed = sessionConsumerMap.remove(key);
        if (removed != null) {
            // 调用清理方法，释放资源（如长连接等）
            try {
                removed.cleanup(sessionId);
            } catch (Exception e) {
                logger.error("Error cleaning up consumer for session: {}, platform: {}", sessionId, platform, e);
            }
            logger.debug("Removed consumer cache for session: {}, platform: {}", sessionId, platform);
        }
    }
}
