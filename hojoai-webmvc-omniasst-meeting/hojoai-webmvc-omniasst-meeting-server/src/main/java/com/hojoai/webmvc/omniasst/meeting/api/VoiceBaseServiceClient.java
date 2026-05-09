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
package com.hojoai.webmvc.omniasst.meeting.api;

import com.hojoai.common.response.Response;
import com.hojoai.webmvc.omniasst.meeting.api.dto.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Voice 基础服务客户端
 * 通过 Feign 调用 voice 服务提供的纯算法接口
 * 使用 Nacos 服务发现，服务名：hojoai-webmvc-omniasst-voice
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
     * 声纹识别
     * 对应 voice 服务：VoiceprintBaseController.recognize()
     */
    @PostMapping(value = "/v1/voiceprint/recognize")
    Response<VoiceprintResponse> recognize(@RequestBody VoiceprintRequest request);
}

