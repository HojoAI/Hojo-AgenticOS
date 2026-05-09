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

/**
 * 按 TTS 平台划分的队列消费者，由 {@link TtsQueueConsumerFactory} 按 session 与 platform 缓存实例。
 */
public interface TtsQueueConsumer {

    /**
     * 处理队列中的一条 TTS 请求（发送合成、回写 WebSocket 等）。
     *
     * @param sessionId WebSocket 会话 ID
     * @param wrapper   含 {@link com.hojoai.gateway.omniasst.voice.websocket.bean.dto.TtsWebSocketRequest} 与 {@link org.springframework.web.socket.WebSocketSession}
     */
    void consume(String sessionId, TtsRequestWrapper wrapper);

    /**
     * @return 与 {@link com.hojoai.gateway.omniasst.voice.constants.TtsPlatform} 一致的整型码
     */
    Integer getPlatform();
    
    /**
     * 清理指定 session 的资源（如长连接等）
     * @param sessionId session ID
     */
    default void cleanup(String sessionId) {
        // 默认实现为空，子类可以覆盖
    }
}
