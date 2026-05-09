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

import com.hojoai.gateway.omniasst.voice.api.dto.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 下游语音识别服务 Feign 客户端（{@code ai.asr.host}）。
 * 请求头 {@code API-Key} 由 {@link com.hojoai.gateway.omniasst.voice.configure.FeignApiKeyConfig} 注入。
 */
@FeignClient(name = "speech-asr", url = "${ai.asr.host}")
public interface AsrAPI {

    /**
     * 流式/分包 ASR（实时识别、语种检测等）。
     *
     * @param asrRequest 含 vid、package_data、package_end 等
     * @return 识别结果
     */
    @PostMapping(value = "/asr_stream")
    ASRResponse asr(@RequestBody ASRRequest asrRequest);

    /**
     * 音频翻译（在 ASR 服务内完成翻译链路）。
     *
     * @param request 源语种、目标语种、音频包等
     * @return 翻译文本与结束标志
     */
    @PostMapping(value = "/asr_trans")
    AsrTransResponse asrTrans(@RequestBody AsrTransRequest request);

    /**
     * 语音活动检测。
     *
     * @param vadRequest 音频与 VAD 参数
     * @return VAD 结果
     */
    @PostMapping(value = "/vad")
    VADResponse vad(@RequestBody VADRequest vadRequest);

}

