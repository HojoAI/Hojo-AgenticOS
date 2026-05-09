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
package com.hojoai.gateway.omniasst.voice.api;

import com.hojoai.gateway.omniasst.voice.api.dto.TTSRequest;
import com.hojoai.gateway.omniasst.voice.api.dto.VoicePrintRequest;
import com.hojoai.gateway.omniasst.voice.api.dto.VoicePrintResponse;
import feign.Response;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 下游语音合成与声纹算法服务 Feign 客户端（{@code ai.tts.host}）。
 */
@FeignClient(name = "speech-tts", url = "${ai.tts.host}")
public interface TtsAPI {

    /**
     * 文本转语音，返回二进制流（通常为 PCM），由调用方读取 {@link Response#body()}。
     *
     * @param ttsRequest 文本、语言、音色等
     * @return Feign 原始响应（含 HTTP 状态与流）
     */
    @PostMapping(value = "/text2speech")
    Response tts(@RequestBody TTSRequest ttsRequest);

    /**
     * 声纹特征提取（向量维度等与下游约定一致）。
     *
     * @param request Base64 PCM 等音频载荷
     * @return 声纹向量及 rtn/msg
     */
    @PostMapping(value = "/voiceprint")
    VoicePrintResponse recognize(@RequestBody VoicePrintRequest request);

}