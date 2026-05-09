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
package com.hojoai.gateway.omniasst.voice.service.tts.impl;

import com.hojoai.gateway.omniasst.voice.constants.TtsPlatform;
import com.hojoai.gateway.omniasst.voice.service.tts.DoubaoTtsService;
import com.hojoai.gateway.omniasst.voice.service.tts.TtsQueueConsumer;
import com.hojoai.gateway.omniasst.voice.service.tts.TtsRequestQueueManager.TtsRequestWrapper;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * 豆包平台 TTS 队列消费者占位实现（待接入 {@link DoubaoTtsService} 与 WebSocket 回推）。
 */
@Component
public class DoubaoTtsQueueConsumer implements TtsQueueConsumer {
    private static final Logger logger = LoggerFactory.getLogger(DoubaoTtsQueueConsumer.class);
    
    @Resource
    private DoubaoTtsService doubaoTtsService;

    /**
     * {@inheritDoc}
     */
    @Override
    public void consume(String sessionId, TtsRequestWrapper wrapper) {
        logger.info("Doubao TTS queue consumer processing request for session: {}", sessionId);
        // TODO: 实现豆包平台的 TTS 消费逻辑
        // 1. 从 wrapper 中获取 request 和 session
        // 2. 调用 doubaoTtsService.synthesize 生成音频
        // 3. 将音频包通过 session 发送出去
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer getPlatform() {
        return TtsPlatform.DOUBAO.getCode();
    }
}
