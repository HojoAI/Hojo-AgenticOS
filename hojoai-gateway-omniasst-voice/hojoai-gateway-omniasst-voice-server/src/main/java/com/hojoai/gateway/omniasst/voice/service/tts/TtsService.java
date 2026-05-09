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

import com.hojoai.gateway.omniasst.voice.service.tts.dto.TtsRequest;
import com.hojoai.gateway.omniasst.voice.service.tts.dto.TtsResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import reactor.core.publisher.Flux;

/**
 * 网关侧 TTS 聚合接口：对接下游 {@link com.hojoai.gateway.omniasst.voice.api.TtsAPI} 并输出文件、OSS 或流。
 */
public interface TtsService {

    /**
     * @param request 文本与音色等
     * @return 含本地 WAV 文件的响应
     */
    TtsResponse convertToSpeechFile(TtsRequest request);

    /**
     * @param request 合成参数
     * @return 含 OSS URL 的响应
     */
    TtsResponse convertToSpeechOss(TtsRequest request);

    /**
     * @param request 合成参数
     * @return SSE 流式 PCM
     */
    SseEmitter convertToSpeechStream(TtsRequest request);

    /**
     * @param request 合成参数
     * @return 响应式 PCM 分块
     */
    Flux<byte[]> convertToSpeechStreamReactive(TtsRequest request);

    /**
     * @param request 合成参数
     * @return {@code application/octet-stream} 响应体
     */
    ResponseEntity<StreamingResponseBody> convertToPcmStream(TtsRequest request);
}

