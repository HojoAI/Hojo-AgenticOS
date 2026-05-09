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
package com.hojoai.webmvc.omniasst.translate.service.llm.client;

import com.hojoai.webmvc.omniasst.translate.service.llm.bean.GenerateTitleRequest;
import com.hojoai.webmvc.omniasst.translate.service.llm.bean.GenerateTitleResponse;
import feign.Request;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 标题与摘要生成模型 Feign 客户端，配置项 {@code llm.model.title-generation.url}。
 */
@FeignClient(
        name = "titleGenerationModel",
        url = "${llm.model.title-generation.url:}"
)
public interface TitleGenerationModelClient {
    /**
     * 根据多轮翻译上下文生成会话标题与摘要。
     *
     * @param request 源/目标语言与对话轮次
     * @return 模型返回结构体
     */
    @PostMapping(value = "/v1/generateTitle", consumes = "application/json", produces = "application/json")
    GenerateTitleResponse generateTitle(@RequestBody GenerateTitleRequest request);

    /**
     * Feign 超时：连接 10s、读取 30s。
     */
    class Configuration {
        public Request.Options options() {
            // Connect timeout: 10s, Read timeout: 30s
            return new Request.Options(10000, 30000);
        }
    }
}

