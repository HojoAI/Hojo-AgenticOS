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

import com.hojoai.common.response.Response;
import com.hojoai.common.response.ResponseStatusBasic;
import com.hojoai.gateway.omniasst.voice.service.tts.TtsService;
import com.hojoai.gateway.omniasst.voice.service.tts.dto.TtsRequest;
import com.hojoai.gateway.omniasst.voice.service.tts.dto.TtsResponse;
import jakarta.annotation.Resource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import reactor.core.publisher.Flux;

import java.io.File;

/**
 * 语音合成（TTS）HTTP 接口。
 * 依赖 {@link TtsService}，支持文件下载、OSS 地址、SSE 流式与 PCM 二进制流。
 */
@RestController
@RequestMapping("/v1/tts")
public class TtsController {

    @Resource
    private TtsService ttsService;

    /**
     * 合成语音并以下载 WAV 文件形式返回。
     *
     * @param request 合成参数（文本、音色等）
     * @return 带 Content-Disposition 的音频响应
     */
    @PostMapping("/file")
    public ResponseEntity<org.springframework.core.io.Resource> ttsFile(@RequestBody TtsRequest request) {
        TtsResponse ttsResponse = ttsService.convertToSpeechFile(request);
        File file = ttsResponse.getFile();
        org.springframework.core.io.Resource resource = new FileSystemResource(file);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("audio/wav"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getName() + "\"")
                .body(resource);
    }

    /**
     * 合成语音并上传 OSS，返回可访问 URL。
     *
     * @param request 合成参数
     * @return OSS 地址字符串
     */
    @PostMapping("/oss")
    public Response<String> ttsOss(@RequestBody TtsRequest request) {
        TtsResponse ttsResponse = ttsService.convertToSpeechOss(request);
        return Response.newInstance(ResponseStatusBasic.OK, ttsResponse.getOssUrl());
    }

    /**
     * 流式 TTS（SSE），按块推送音频二进制数据。
     *
     * @param request 合成参数
     * @return SSE 流；异常时推送 error 事件
     */
    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<byte[]>> ttsStream(@RequestBody TtsRequest request) {
        return ttsService.convertToSpeechStreamReactive(request)
                .map(chunk -> ServerSentEvent.<byte[]>builder().data(chunk).build())
                .onErrorResume(e -> Flux.just(ServerSentEvent.<byte[]>builder()
                        .event("error").data(("Error: " + e.getMessage()).getBytes()).build()));
    }

    /**
     * PCM 原始音频流式输出（HTTP 分块传输）。
     *
     * @param request 合成参数
     * @return {@code application/octet-stream} 流式响应体
     */
    @PostMapping(value = "/pcm/stream", produces = "application/octet-stream")
    public ResponseEntity<StreamingResponseBody> ttsPcmStream(@RequestBody TtsRequest request) {
        return ttsService.convertToPcmStream(request);
    }
}

