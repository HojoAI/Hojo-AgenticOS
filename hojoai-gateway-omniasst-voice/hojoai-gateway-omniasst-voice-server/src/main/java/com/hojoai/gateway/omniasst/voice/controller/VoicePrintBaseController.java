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
import com.hojoai.gateway.omniasst.voice.api.dto.VoicePrintRequest;
import com.hojoai.gateway.omniasst.voice.api.dto.VoicePrintResponse;
import com.hojoai.gateway.omniasst.voice.service.voiceprint.VoicePrintService;
import jakarta.annotation.Resource;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 声纹识别基础 HTTP 接口（JSON Base64 与文件上传）。
 * 与 {@link VoiceprintController} 同属 /v1/voiceprint 路径下不同资源操作。
 */
@RestController
@RequestMapping("/v1/voiceprint")
public class VoicePrintBaseController {

    @Resource
    private VoicePrintService voicePrintService;

    /**
     * 声纹识别（JSON）
     * 接收Base64编码的PCM音频数据，返回声纹向量
     */
    @PostMapping("/recognize")
    public Response<VoicePrintResponse> recognize(@RequestBody VoicePrintRequest request) {
        VoicePrintResponse response = voicePrintService.recognizeFromRequest(request);
        return Response.newInstance(ResponseStatusBasic.OK, response);
    }

    /**
     * 通过 multipart 上传音频文件进行声纹识别。
     *
     * @param audioFile 音频文件
     * @return 声纹向量等识别结果；参数非法或识别失败时返回错误码与信息
     */
    @PostMapping(value = "/recognize/file", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Response<VoicePrintResponse> recognizeFromFile(@RequestParam("file") MultipartFile audioFile) {
        try {
            VoicePrintResponse response = voicePrintService.recognizeFromFile(audioFile);
            return Response.newInstance(ResponseStatusBasic.OK, response);
        } catch (IllegalArgumentException e) {
            return Response.newInstance(ResponseStatusBasic.ILLEGAL_ARGUMENT.getResCode(), e.getMessage());
        } catch (Exception e) {
            return Response.newInstance(ResponseStatusBasic.GENERAL_ERROR.getResCode(), "声纹识别失败: " + e.getMessage());
        }
    }

}

