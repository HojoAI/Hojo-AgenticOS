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
package com.hojoai.gateway.omniasst.voice.api;

import com.hojoai.gateway.omniasst.voice.api.dto.TranslateModelRequest;
import com.hojoai.gateway.omniasst.voice.api.dto.TranslateModelResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

/**
 * AutoFlow NLU 模型服务：纯文本翻译等能力（URL 由 {@code autoflow.model.nlu.url} 配置）。
 */
@FeignClient(name = "nlu-model-api", url = "${autoflow.model.nlu.url}")
public interface AutoFlowModelAPI {

    /**
     * 文本翻译（需携带调用方 Authorization）。
     *
     * @param authorization   Bearer 等令牌
     * @param translateModelRequest 源/目标语言与待译文本
     * @return 模型翻译结果
     */
    @PostMapping(value = "/translate")
    TranslateModelResponse translate(@RequestHeader("Authorization") String authorization, @RequestBody TranslateModelRequest translateModelRequest);
}

