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
package com.hojoai.omniasst.sysagent.service.speech.impl;

import com.hojoai.omniasst.sysagent.service.speech.TTSService;
import com.hojoai.omniasst.sysagent.service.speech.bean.TTSRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

/**
 * The service implements class for tts.
 *
 * @author kevanwong
 * @version 1.0, 2025/11/15
 * @since hojoai-websocket-omniasst-sysagent 1.0.0
 */
@Service
public class TTSServiceImpl implements TTSService {

    private static final Logger logger = LoggerFactory.getLogger(TTSServiceImpl.class);

    /** TTS 网关要求的鉴权请求头名称。 */
    private static final String HEADER_NAME_API_KEY = "API-Key";

    @Value("${hojoai.tts.api-key}")
    private String ttsApiKey;

    /** 文本转语音 HTTP 接口地址。 */
    private static final String TTS_URL = "https://daily-www.hojoai.com/tts/text2speech";

    /**
     * 使用 WebClient 以流式方式拉取 TTS 音频分片。
     *
     * @param request 合成参数（文本、语种、音色等）
     * @return 音频 {@link DataBuffer} 流；出错时记录日志并向下游传递错误
     */
    @Override
    public Flux<DataBuffer> tts(TTSRequest request) {
        return WebClient.builder() // Web Client builder
                .baseUrl(TTS_URL) // Base URL
                .build() // Web Client
                .post() // POST
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE) // Header Content-Type
                .header(HEADER_NAME_API_KEY, ttsApiKey) // Header API-Key
                .bodyValue(request) // Request body
                .retrieve() // Execute request
                .bodyToFlux(DataBuffer.class) // Stream DataBuffer
                .doOnError(error -> logger.error("tts data buffer error", error)); // Log error
    }
}
