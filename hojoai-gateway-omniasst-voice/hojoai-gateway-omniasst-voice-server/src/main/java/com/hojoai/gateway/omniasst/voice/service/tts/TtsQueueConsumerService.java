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

import com.hojoai.gateway.omniasst.voice.service.tts.TtsRequestQueueManager.TtsRequestWrapper;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 为每个 session 启动单线程消费者，从 {@link TtsRequestQueueManager} 取任务并委派 {@link TtsQueueConsumerFactory}。
 */
@Service
public class TtsQueueConsumerService {
    private static final Logger logger = LoggerFactory.getLogger(TtsQueueConsumerService.class);
    
    @Resource
    private TtsRequestQueueManager queueManager;
    
    @Resource
    private TtsQueueConsumerFactory consumerFactory;
    
    private final ConcurrentHashMap<String, ExecutorService> sessionExecutors = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Integer> sessionPlatformMap = new ConcurrentHashMap<>();

    /**
     * Bean 就绪日志。
     */
    @PostConstruct
    public void init() {
        logger.info("TtsQueueConsumerService initialized");
    }

    /**
     * 关闭所有会话级线程池。
     */
    @PreDestroy
    public void destroy() {
        logger.info("Shutting down TtsQueueConsumerService...");
        sessionExecutors.values().forEach(executor -> {
            executor.shutdownNow();
        });
        sessionExecutors.clear();
        logger.info("TtsQueueConsumerService shutdown completed");
    }

    /**
     * 若队列存在则提交后台循环：take → 按 platform 选 {@link TtsQueueConsumer} → {@link TtsQueueConsumer#consume}，直至会话关闭或中断。
     *
     * @param sessionId 会话 ID（须与 {@link TtsRequestQueueManager#enqueueRequest} 使用的一致）
     */
    public void startConsuming(String sessionId) {
        BlockingQueue<TtsRequestWrapper> queue = queueManager.getQueue(sessionId);
        if (queue == null) {
            logger.warn("Queue not found for session: {}", sessionId);
            return;
        }
        
        ExecutorService executor = sessionExecutors.computeIfAbsent(sessionId, k -> {
            ExecutorService newExecutor = Executors.newSingleThreadExecutor(r -> {
                Thread t = new Thread(r, "TTS-Consumer-" + sessionId);
                t.setDaemon(true);
                return t;
            });
            logger.info("Created consumer executor for session: {}", sessionId);
            return newExecutor;
        });
        
        executor.submit(() -> {
            logger.info("Starting TTS queue consumer for session: {}", sessionId);
            
            try {
                while (true) {
                    TtsRequestWrapper wrapper = queue.take();
                    WebSocketSession session = wrapper.getSession();
                    
                    if (!session.isOpen()) {
                        logger.info("Session closed, stopping consumer for session: {}", sessionId);
                        break;
                    }
                    
                    try {
                        Integer platform = wrapper.getRequest().getPlatform();
                        if (platform == null) {
                            platform = 0;
                        }
                        sessionPlatformMap.put(sessionId, platform);
                        TtsQueueConsumer consumer = consumerFactory.getConsumer(platform, sessionId);
                        logger.debug("Processing TTS request for session: {}, platform: {}", sessionId, platform);
                        consumer.consume(sessionId, wrapper);
                    } catch (Exception e) {
                        logger.error("Error consuming TTS request for session: {}", sessionId, e);
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                logger.info("TTS consumer interrupted for session: {}", sessionId);
            } catch (Exception e) {
                logger.error("Unexpected error in TTS consumer for session: {}", sessionId, e);
            } finally {
                sessionExecutors.remove(sessionId);
                queueManager.removeQueue(sessionId);
                Integer platform = sessionPlatformMap.remove(sessionId);
                if (platform != null) {
                    consumerFactory.removeSession(platform, sessionId);
                }
                logger.info("TTS consumer stopped for session: {}", sessionId);
            }
        });
    }

    /**
     * 中断消费者线程、移除队列，并通知工厂 {@link TtsQueueConsumerFactory#removeSession} 做清理。
     *
     * @param sessionId 会话 ID
     */
    public void stopConsuming(String sessionId) {
        ExecutorService executor = sessionExecutors.remove(sessionId);
        if (executor != null) {
            executor.shutdownNow();
            logger.info("Stopped consumer executor for session: {}", sessionId);
        }
        queueManager.removeQueue(sessionId);
        Integer platform = sessionPlatformMap.remove(sessionId);
        if (platform != null) {
            consumerFactory.removeSession(platform, sessionId);
        }
    }
}
