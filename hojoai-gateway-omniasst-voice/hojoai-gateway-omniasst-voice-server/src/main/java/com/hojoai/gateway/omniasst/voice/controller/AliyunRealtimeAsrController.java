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
package com.hojoai.gateway.omniasst.voice.controller;

import com.hojoai.common.jackson.JacksonUtil;
import com.hojoai.common.response.Response;
import com.hojoai.common.response.ResponseStatusBasic;
import com.hojoai.gateway.omniasst.voice.api.dto.ASRRequest;
import com.hojoai.gateway.omniasst.voice.api.dto.ASRResponse;
import com.hojoai.gateway.omniasst.voice.service.aliyunrealtime.AliyunRealtimeAsrService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 阿里云实时语音识别 HTTP 接口。
 * 依赖 {@link AliyunRealtimeAsrService}，与通用实时 ASR 入参出参保持一致，便于切换实现。
 */
@Slf4j
@RestController
@RequestMapping("/v1/asr/aliyun")
public class AliyunRealtimeAsrController {

    @Resource
    private AliyunRealtimeAsrService aliyunRealtimeAsrService;

    /**
     * 阿里云实时 ASR 识别接口
     * 与 /realtime 入参、回参一致，对接阿里云智能语音交互 Java SDK（SpeechTranscriber），
     * 支持按 vid 流式发送多包音频（package_data 为 base64 编码的 PCM 16k 数据），实时识别。
     *
     * @param request ASR 请求参数（vid、package_data、package_end、vad_end 等）
     * @return ASR 识别结果（rtn、is_end、vid、text 等）
     */
    @PostMapping("/realtime")
    public Response<ASRResponse> asrRealtimeAliyun(@RequestBody ASRRequest request) {
        log.info("[AliyunRealtimeASR收到请求]，vid: {}", request != null ? request.getVid() : null);
        ASRResponse asrResponse = aliyunRealtimeAsrService.asr(request);
        log.info("[AliyunRealtimeASR发送响应]，vid: {}, text: {}", asrResponse != null ? asrResponse.getVid() : null, JacksonUtil.bean2Json(asrResponse != null ? asrResponse.getText() : null));
        return Response.newInstance(ResponseStatusBasic.OK, asrResponse);
    }

    /**
     * 阿里云实时 ASR：从本地 WAV 文件模拟实时发送并识别，用于本地测试。
     * WAV 需为 16kHz、16bit、单声道 PCM；路径为服务端本地路径。
     *
     * @param wavFilePath 本地 WAV 文件路径（必填）
     * @param vid         会话 id（可选，不传则自动生成）
     * @return ASR 识别结果，与 /realtime/aliyun 一致
     */
    @PostMapping("/wav")
    public Response<ASRResponse> asrRealtimeAliyunFromWav(
            @RequestParam("wavFilePath") String wavFilePath,
            @RequestParam(value = "vid", required = false) String vid) {
        log.info("[AliyunRealtimeASR/wav] 收到请求，wavFilePath: {}, vid: {}", wavFilePath, vid);
        ASRResponse asrResponse = vid != null && !vid.isEmpty()
                ? aliyunRealtimeAsrService.asrFromLocalWav(wavFilePath, vid)
                : aliyunRealtimeAsrService.asrFromLocalWav(wavFilePath);
        log.info("[AliyunRealtimeASR/wav] 发送响应，vid: {}, text: {}", asrResponse != null ? asrResponse.getVid() : null, JacksonUtil.bean2Json(asrResponse != null ? asrResponse.getText() : null));
        return Response.newInstance(ResponseStatusBasic.OK, asrResponse);
    }
}
