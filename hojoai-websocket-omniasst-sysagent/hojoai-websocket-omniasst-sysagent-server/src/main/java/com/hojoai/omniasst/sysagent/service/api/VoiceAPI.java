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
package com.hojoai.omniasst.sysagent.service.api;

import com.hojoai.common.response.Response;
import com.hojoai.omniasst.sysagent.service.speech.bean.ASRFileToTextRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 语音能力远程调用客户端，通过 OpenFeign 访问 {@code hojoai-webmvc-omniasst-voice} 服务。
 * <p>用于文件/URL 转写等与 omniasst-voice 对齐的 HTTP 接口。</p>
 */
@FeignClient(name = "hojoai-webmvc-omniasst-voice")
public interface VoiceAPI {

    /**
     * 调用语音服务将音频文件（OSS 地址）转为文本。
     *
     * @param request 转写请求，需包含音频 OSS URL 等参数
     * @return 统一响应包装，成功时 {@code resContent} 为识别文本
     */
    @PostMapping("/omniasst-voice/v1/asr")
    Response<String> asr(@RequestBody ASRFileToTextRequest request);
}