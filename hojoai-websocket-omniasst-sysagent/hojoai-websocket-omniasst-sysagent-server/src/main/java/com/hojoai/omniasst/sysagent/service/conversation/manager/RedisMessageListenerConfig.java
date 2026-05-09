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
package com.hojoai.omniasst.sysagent.service.conversation.manager;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hojoai.omniasst.sysagent.service.conversation.bean.StopMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

/**
 * 对话「停止生成」的 Redis Pub/Sub 配置：订阅固定频道并将消息交给 {@link SseEmitterManager} 关闭 SSE。
 */
@Configuration
public class RedisMessageListenerConfig {
    private static final Logger logger = LoggerFactory.getLogger(RedisMessageListenerConfig.class);

    /** 停止会话时发布消息的 Redis 频道名（与发布端约定一致）。 */
    public static final String STOP_CHANNEL = "Omniasst:Conversation:Stop";

    /**
     * 注册 Redis 监听容器并订阅 {@link #STOP_CHANNEL}。
     *
     * @param connectionFactory   Redis 连接
     * @param stopMessageListener 停止消息监听器
     * @return 已启动订阅的容器
     */
    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer(
            RedisConnectionFactory connectionFactory,
            StopMessageReceiver stopMessageListener) {
        
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        
        // 订阅停止频道
        container.addMessageListener(stopMessageListener, new PatternTopic(STOP_CHANNEL));
        
        logger.info("Redis message listener container initialized, subscribed to: {}", STOP_CHANNEL);
        return container;
    }

    /**
     * 构造停止消息监听器 Bean。
     *
     * @param emitterManager 用于查找并完成对应 SSE 连接
     * @return {@link StopMessageReceiver}
     */
    @Bean
    public StopMessageReceiver stopMessageReceiver(SseEmitterManager emitterManager) {
        return new StopMessageReceiver(emitterManager);
    }

    /**
     * 解析 Redis 消息体为 {@link StopMessage} 并驱动 {@link SseEmitterManager#stopConversation}。
     */
    public static class StopMessageReceiver implements MessageListener {
        private static final Logger logger = LoggerFactory.getLogger(StopMessageReceiver.class);
        private final ObjectMapper objectMapper = new ObjectMapper();
        private final SseEmitterManager emitterManager;

        public StopMessageReceiver(SseEmitterManager emitterManager) {
            this.emitterManager = emitterManager;
        }

        /**
         * 收到 Pub/Sub 消息后反序列化并尝试停止对应 SSE。
         *
         * @param message Redis 消息体（JSON）
         * @param pattern 订阅模式字节（可为 null）
         */
        @Override
        public void onMessage(Message message, byte[] pattern) {
            try {
                String messageBody = new String(message.getBody());
                StopMessage stopMessage = objectMapper.readValue(messageBody, StopMessage.class);
                logger.info("Received stop message: conversationId={}, messageId={}", 
                        stopMessage.getConversationId(), stopMessage.getMessageId());
                
                if (emitterManager.hasEmitter(stopMessage.getConversationId(), stopMessage.getMessageId())) {
                    logger.info("Found active emitter for stop message: {}", stopMessage);
                    boolean success = emitterManager.stopConversation(stopMessage.getConversationId(), stopMessage.getMessageId());
                    if (success) {
                        logger.info("Successfully stopped conversation: conversationId={}, messageId={}", 
                                stopMessage.getConversationId(), stopMessage.getMessageId());
                    } else {
                        logger.warn("Failed to stop conversation: conversationId={}, messageId={}", 
                                stopMessage.getConversationId(), stopMessage.getMessageId());
                    }
                } else {
                    logger.info("No active emitter found for stop message: conversationId={}, messageId={}, ignoring", 
                            stopMessage.getConversationId(), stopMessage.getMessageId());
                }
            } catch (Exception e) {
                logger.error("Failed to process stop message: {}", new String(message.getBody()), e);
            }
        }
    }
}
