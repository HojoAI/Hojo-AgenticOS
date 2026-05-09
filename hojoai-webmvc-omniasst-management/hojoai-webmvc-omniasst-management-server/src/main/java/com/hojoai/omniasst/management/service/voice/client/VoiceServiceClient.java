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
package com.hojoai.omniasst.management.service.voice.client;

import com.hojoai.common.response.Response;
import com.hojoai.omniasst.management.service.voice.bean.VoicePrintResponse;
import com.hojoai.omniasst.management.service.voice.bean.VoiceCloneResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

/**
 * 调用音色服务（克隆、声纹文件识别等）的 Feign 客户端。
 */
@FeignClient(name = "hojoai-webmvc-omniasst-voice", path = "/omniasst-voice")
public interface VoiceServiceClient {

    @PostMapping(value = "/v1/voice/clone", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    Response<VoiceCloneResponse> cloneVoice(
            @RequestPart("file") MultipartFile file,
            @RequestPart("fileType") String fileType,
            @RequestPart("provider") String provider
    );

    @PostMapping(value = "/v1/voiceprint/recognize/file", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    Response<VoicePrintResponse> recognizeFromFile(@RequestPart("file") MultipartFile file);

}
