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
import com.hojoai.gateway.omniasst.voice.service.voice.VoiceCloneService;
import com.hojoai.gateway.omniasst.voice.service.voice.dto.VoiceCloneResponse;
import jakarta.annotation.Resource;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 声音克隆相关 HTTP 接口。
 * 依赖 {@link VoiceCloneService}，可按提供商路由至不同 TTS 厂商。
 */
@RestController
@RequestMapping("/v1/voice")
public class VoiceController {

    @Resource
    private VoiceCloneService voiceCloneService;

    /**
     * 上传参考音频进行声音克隆注册/训练。
     *
     * @param file     参考音频文件
     * @param fileType 可选，文件类型提示
     * @param provider 可选，指定克隆服务提供商
     * @return 克隆结果（如音色 ID 等）
     */
    @PostMapping(value = "/clone", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Response<VoiceCloneResponse> cloneVoice(
            @RequestPart("file") MultipartFile file,
            @RequestParam(value = "fileType", required = false) String fileType,
            @RequestParam(value = "provider", required = false) String provider) {
        return Response.newInstance(ResponseStatusBasic.OK, voiceCloneService.cloneVoice(file, fileType, provider));
    }

}
