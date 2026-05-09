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
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;

/**
 * TTS 队列服务门面
 * 统一封装队列管理和消费者管理的功能，对外提供简洁的接口
 */
@Service
public class TtsQueueService {
    private static final Logger logger = LoggerFactory.getLogger(TtsQueueService.class);
    
    @Resource
    private TtsRequestQueueManager queueManager;
    
    @Resource
    private TtsQueueConsumerService consumerService;
    
    /**
     * 将 TTS 请求加入队列，如果需要则自动启动消费者
     * @param request TTS 请求
     * @param session WebSocket 会话
     */
    public void enqueueRequest(TtsWebSocketRequest request, WebSocketSession session) {
        boolean needStartConsuming = queueManager.enqueueRequest(request, session);
        if (needStartConsuming) {
            String sessionId = session.getId();
            consumerService.startConsuming(sessionId);
            logger.info("Started TTS consumer for session: {}", sessionId);
        }
    }
    
    /**
     * 停止指定 session 的消费并清理资源
     * @param sessionId session ID
     */
    public void stopConsuming(String sessionId) {
        consumerService.stopConsuming(sessionId);
    }
}
