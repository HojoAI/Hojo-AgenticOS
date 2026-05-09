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
import com.hojoai.gateway.omniasst.voice.api.AsrAPI;
import com.hojoai.gateway.omniasst.voice.api.dto.VADRequest;
import com.hojoai.gateway.omniasst.voice.api.dto.VADResponse;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 语音活动检测（VAD）HTTP 接口。
 * 通过 {@link AsrAPI} 转发至底层算法服务。
 */
@RestController
@RequestMapping("/v1/vad")
public class VadController {

    @Resource
    private AsrAPI asrAPI;

    /**
     * 对请求中的音频进行 VAD 检测。
     *
     * @param request VAD 请求（含音频等）
     * @return VAD 检测结果
     */
    @PostMapping
    public Response<VADResponse> vad(@RequestBody VADRequest request) {
        VADResponse vadResponse = asrAPI.vad(request);
        return Response.newInstance(ResponseStatusBasic.OK, vadResponse);
    }
}

