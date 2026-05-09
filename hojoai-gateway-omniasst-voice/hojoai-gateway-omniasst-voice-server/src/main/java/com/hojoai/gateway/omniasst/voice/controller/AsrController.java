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
import com.hojoai.gateway.omniasst.voice.api.AsrAPI;
import com.hojoai.gateway.omniasst.voice.api.dto.ASRRequest;
import com.hojoai.gateway.omniasst.voice.api.dto.ASRResponse;
import com.hojoai.gateway.omniasst.voice.constants.VoiceServiceErrorCode;
import com.hojoai.gateway.omniasst.voice.service.alqw.AlqwSpeechFlashService;
import com.hojoai.gateway.omniasst.voice.service.asr.AsrService;
import com.hojoai.gateway.omniasst.voice.service.asr.dto.AsrFileOssRequest;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;

/**
 * 语音识别（ASR）HTTP 接口。
 * 依赖 {@link AsrService}、{@link AlqwSpeechFlashService} 与 {@link AsrAPI}，提供文件/流式识别及实时 ASR。
 */
@Slf4j
@RestController
@RequestMapping("/v1/asr")
public class AsrController {

    @Resource
    private AsrService asrService;

    @Resource
    private AlqwSpeechFlashService alqwSpeechFlashService;

    @Resource
    private AsrAPI asrAPI;

    /**
     * 根据 OSS 音频地址进行语音识别，返回完整文本。
     *
     * @param request 含 OSS 路径等参数的识别请求
     * @return 识别文本
     */
    @PostMapping
    public Response<String> asrFile(@RequestBody AsrFileOssRequest request) {
        return Response.newInstance(ResponseStatusBasic.OK, alqwSpeechFlashService.convertToText(request));
    }

    /**
     * 流式语音识别（Server-Sent Events），按片段推送识别文本。
     *
     * @param request 含 OSS 路径等参数的识别请求
     * @return SSE 流；异常时推送 event 为 error 的事件
     */
    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> asrFileStream(@RequestBody AsrFileOssRequest request) {
        return asrService.convertToTextStreamReactive(request)
                .map(text -> ServerSentEvent.<String>builder().data(text).build())
                .onErrorResume(e -> Flux.just(ServerSentEvent.<String>builder()
                        .event("error").data("Error: " + e.getMessage()).build()));
    }

    /**
     * 上传音频文件进行语音识别。
     *
     * @param file 用户上传的音频文件
     * @return 识别文本；校验或处理失败时返回对应业务错误码
     */
    @PostMapping(value = "/file", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Response<String> asrFileUpload(@RequestParam("file") MultipartFile file) {
        try {
            // 将文件格式、大小等校验与转换委托给服务层，此处仅映射常见异常为统一错误码
            String text = asrService.convertToTextFromFile(file);
            return Response.newInstance(ResponseStatusBasic.OK, text);
        } catch (IllegalArgumentException e) {
            String errorMsg = e.getMessage();
            if (errorMsg != null && errorMsg.contains("文件大小超过限制")) {
                return Response.newInstance(ResponseStatusBasic.GENERAL_ERROR, VoiceServiceErrorCode.FILE_TOO_LARGE.getCode() + ": " + VoiceServiceErrorCode.FILE_TOO_LARGE.getMessage());
            }
            if (errorMsg != null && errorMsg.contains("文件格式不正确")) {
                return Response.newInstance(ResponseStatusBasic.GENERAL_ERROR, VoiceServiceErrorCode.INVALID_FILE_FORMAT.getCode() + ": " + VoiceServiceErrorCode.INVALID_FILE_FORMAT.getMessage());
            }
            return Response.newInstance(ResponseStatusBasic.ILLEGAL_ARGUMENT, errorMsg);
        } catch (Exception e) {
            String errorMsg = e.getMessage();
            log.error("[asrFile接口] ASR文件转换失败:", e);
            if (errorMsg != null && (errorMsg.contains("转换") || errorMsg.contains("conversion"))) {
                return Response.newInstance(ResponseStatusBasic.GENERAL_ERROR, VoiceServiceErrorCode.FILE_CONVERSION_ERROR.getCode() + ": " + VoiceServiceErrorCode.FILE_CONVERSION_ERROR.getMessage());
            }
            return Response.newInstance(ResponseStatusBasic.GENERAL_ERROR, VoiceServiceErrorCode.ASR_PROCESSING_ERROR.getCode() + ": " + (errorMsg != null ? errorMsg : VoiceServiceErrorCode.ASR_PROCESSING_ERROR.getMessage()));
        }
    }

    /**
     * 实时 ASR 识别接口
     * 纯算法封装，用于流式语音识别场景
     * 适用于 WebSocket 等实时场景的后端接口调用
     *
     * @param request ASR 请求参数
     * @return ASR 识别结果
     */
    @PostMapping("/realtime")
    public Response<ASRResponse> asrRealtime(@RequestBody ASRRequest request) {
        log.info("[ASR收到请求]，vid: {}", request.getVid());
        ASRResponse asrResponse = asrAPI.asr(request);
        log.info("[ASR发送响应]，vid: {}, text: {}", request.getVid(), JacksonUtil.bean2Json(asrResponse.getText()));
        return Response.newInstance(ResponseStatusBasic.OK, asrResponse);
    }

}

