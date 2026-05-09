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

import com.hojoai.gateway.omniasst.voice.websocket.bean.dto.TtsWebSocketRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 按 WebSocket 会话维护阻塞队列与“是否已启动消费”标记，与 {@link TtsQueueConsumerService} 配合做串行 TTS。
 */
@Component
public class TtsRequestQueueManager {
    private static final Logger logger = LoggerFactory.getLogger(TtsRequestQueueManager.class);
    
    private final ConcurrentHashMap<String, BlockingQueue<TtsRequestWrapper>> sessionQueues = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, AtomicBoolean> sessionConsumingFlags = new ConcurrentHashMap<>();
    
    /**
     * 将请求加入队列
     * @param request TTS 请求
     * @param session WebSocket 会话
     * @return 如果这是该 session 的第一个请求（需要启动消费者），返回 true；否则返回 false
     */
    public boolean enqueueRequest(TtsWebSocketRequest request, WebSocketSession session) {
        String sessionId = session.getId();
        BlockingQueue<TtsRequestWrapper> queue = sessionQueues.computeIfAbsent(
            sessionId, 
            k -> new LinkedBlockingQueue<>()
        );
        
        AtomicBoolean consumingFlag = sessionConsumingFlags.computeIfAbsent(sessionId, k -> new AtomicBoolean(false));
        
        boolean needStartConsuming = consumingFlag.compareAndSet(false, true);
        
        TtsRequestWrapper wrapper = new TtsRequestWrapper(request, session);
        try {
            queue.put(wrapper);
            logger.debug("TTS request enqueued for session: {}", sessionId);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("Failed to enqueue TTS request for session: {}", sessionId, e);
        }
        
        return needStartConsuming;
    }

    /**
     * @param sessionId WebSocket 会话 ID
     * @return 该会话的队列，若不存在则为 {@code null}
     */
    public BlockingQueue<TtsRequestWrapper> getQueue(String sessionId) {
        return sessionQueues.get(sessionId);
    }

    /**
     * 移除会话队列与消费标记（通常在消费者退出时调用）。
     *
     * @param sessionId 会话 ID
     */
    public void removeQueue(String sessionId) {
        sessionQueues.remove(sessionId);
        sessionConsumingFlags.remove(sessionId);
        logger.debug("TTS queue removed for session: {}", sessionId);
    }

    /** 队列元素：一次 TTS 请求及其所属 WebSocket，供消费者回写 */
    public static class TtsRequestWrapper {
        private final TtsWebSocketRequest request;
        private final WebSocketSession session;

        /**
         * @param request TTS 文本与平台等参数
         * @param session 客户端会话
         */
        public TtsRequestWrapper(TtsWebSocketRequest request, WebSocketSession session) {
            this.request = request;
            this.session = session;
        }

        /**
         * @return TTS 请求体
         */
        public TtsWebSocketRequest getRequest() {
            return request;
        }

        /**
         * @return WebSocket 会话
         */
        public WebSocketSession getSession() {
            return session;
        }
    }
}
