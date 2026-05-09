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
import com.hojoai.gateway.omniasst.voice.service.tts.TtsQueueConsumer;
import com.hojoai.gateway.omniasst.voice.service.tts.TtsRequestQueueManager.TtsRequestWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Hojo 默认 TTS 队列消费者占位实现（待对接 Feign TTS 与下行推送）。
 */
@Component
public class HojoTtsQueueConsumer implements TtsQueueConsumer {
    private static final Logger logger = LoggerFactory.getLogger(HojoTtsQueueConsumer.class);

    /**
     * {@inheritDoc}
     */
    @Override
    public void consume(String sessionId, TtsRequestWrapper wrapper) {
        logger.info("Hojo TTS queue consumer processing request for session: {}", sessionId);
        // TODO: 实现 Hojo 平台的 TTS 消费逻辑
        // 1. 从 wrapper 中获取 request 和 session
        // 2. 调用 TTS 服务生成音频
        // 3. 将音频包通过 session 发送出去
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer getPlatform() {
        return TtsPlatform.HOJO.getCode();
    }
}
