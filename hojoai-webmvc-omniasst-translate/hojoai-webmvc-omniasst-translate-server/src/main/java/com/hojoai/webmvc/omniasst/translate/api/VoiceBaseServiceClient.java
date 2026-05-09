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
package com.hojoai.webmvc.omniasst.translate.api;

import com.hojoai.common.response.Response;
import com.hojoai.webmvc.omniasst.translate.api.dto.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

/**
 * 调用 voice 服务的 HTTP 能力：实时 ASR、VAD、翻译与 NLU 文本翻译（本模块主要走 WebSocket，此客户端供扩展或直连使用）。
 */
@FeignClient(name = "hojoai-webmvc-omniasst-voice", path = "/omniasst-voice")
public interface VoiceBaseServiceClient {
    
    /**
     * 实时 ASR 语音识别
     * 对应 voice 服务：AsrController.asrRealtime()
     */
    @PostMapping(value = "/v1/asr/realtime")
    Response<ASRResponse> asr(@RequestBody ASRRequest request);
    
    /**
     * VAD 语音活动检测
     * 对应 voice 服务：VadController.vad()
     */
    @PostMapping(value = "/v1/vad")
    Response<VADResponse> vad(@RequestBody VADRequest request);
    
    /**
     * 音频翻译（ASR + 翻译）
     * 对应 voice 服务：TranslateController.translate()
     */
    @PostMapping(value = "/v1/translate")
    Response<AsrTransResponse> translate(@RequestBody AsrTransRequest request);
    
    /**
     * 文本翻译（NLU 模型，兜底翻译）
     * 对应 voice 服务：TranslateController.nluTranslate()
     */
    @PostMapping(value = "/v1/nlu/translate")
    Response<TranslateModelResponse> nluTranslate(@RequestHeader("Authorization") String authorization, @RequestBody TranslateModelRequest request);
}

