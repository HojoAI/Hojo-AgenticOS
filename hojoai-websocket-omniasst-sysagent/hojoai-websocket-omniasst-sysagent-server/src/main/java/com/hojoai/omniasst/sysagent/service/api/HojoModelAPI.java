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

import com.hojoai.omniasst.sysagent.configure.HojoModelFeignConfig;
import com.hojoai.omniasst.sysagent.service.conversation.bean.IntentionModelRequest;
import com.hojoai.omniasst.sysagent.service.conversation.bean.IntentionModelResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 大模型/意图识别远程调用客户端，直连配置的 {@code hojoai.model.api.url}，超时由 {@link HojoModelFeignConfig} 注入。
 */
@FeignClient(name = "hojoai-model-api", url = "${hojoai.model.api.url}", configuration = HojoModelFeignConfig.class)
public interface HojoModelAPI {

    /**
     * 调用意图模型（SA）完成分类或结构化理解。
     *
     * @param request 用户输入及上下文等
     * @return 模型侧 {@link IntentionModelResponse}，含意图与参数
     */
    @PostMapping("/v2/callSA")
    ResponseEntity<IntentionModelResponse> callIntentionModel(@RequestBody IntentionModelRequest request);
}
