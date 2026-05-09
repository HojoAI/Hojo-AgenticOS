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
package com.hojoai.gateway.omniasst.voice.service.asr;

import com.hojoai.gateway.omniasst.voice.service.asr.dto.AsrFileOssRequest;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.springframework.web.socket.WebSocketSession;
import reactor.core.publisher.Flux;

import java.nio.ByteBuffer;

public interface AsrService {

    void configureSession(WebSocketSession session, String asrLanguage);

    void handleBinaryMessage(WebSocketSession session, ByteBuffer payload);

    void handleConnectionClosed(WebSocketSession session);

    String convertToText(AsrFileOssRequest request);

    SseEmitter convertToTextStream(AsrFileOssRequest request);

    Flux<String> convertToTextStreamReactive(AsrFileOssRequest request);

    String convertToTextFromFile(org.springframework.web.multipart.MultipartFile file) throws Exception;
}

