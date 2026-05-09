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
import com.hojoai.gateway.omniasst.voice.api.AutoFlowModelAPI;
import com.hojoai.gateway.omniasst.voice.api.dto.AsrTransRequest;
import com.hojoai.gateway.omniasst.voice.api.dto.AsrTransResponse;
import com.hojoai.gateway.omniasst.voice.api.dto.TranslateModelRequest;
import com.hojoai.gateway.omniasst.voice.api.dto.TranslateModelResponse;
import com.hojoai.gateway.omniasst.voice.service.translate.TranslateService;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.*;

/**
 * 翻译相关 HTTP 接口。
 * 依赖 Azure 翻译服务与 AutoFlow 模型 API，提供音频翻译与文本翻译。
 */
@RestController
@RequestMapping("/v1")
public class TranslateController {

    @Resource
    private AsrAPI asrAPI;

    @Resource
    private AutoFlowModelAPI autoFlowModelAPI;

    @Resource
    @Qualifier("azureTranslateService")
    private TranslateService azureTranslateService;

    /**
     * 音频翻译接口(ASR + 翻译)
     * 当前使用Azure Speech Service实现
     * 基于音频的实时翻译，适用于流式场景
     *
     * @param request 音频翻译请求参数
     * @return 翻译结果
     */
    @PostMapping("/translate")
    public Response<AsrTransResponse> translate(@RequestBody AsrTransRequest request) {
        AsrTransResponse asrTransResponse = azureTranslateService.translate(request);
        return Response.newInstance(ResponseStatusBasic.OK, asrTransResponse);
    }

    /**
     * 文本翻译接口（NLU 模型）
     * 纯文本翻译，适用于兜底场景
     *
     * @param authorization 授权信息
     * @param request 文本翻译请求参数
     * @return 翻译结果
     */
    @PostMapping("/nlu/translate")
    public Response<TranslateModelResponse> nluTranslate(
            @RequestHeader("Authorization") String authorization,
            @RequestBody TranslateModelRequest request) {
        TranslateModelResponse translateModelResponse = autoFlowModelAPI.translate(authorization, request);
        return Response.newInstance(ResponseStatusBasic.OK, translateModelResponse);
    }
}

