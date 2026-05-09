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
package com.hojoai.omniasst.management.llm.client;

import com.hojoai.omniasst.management.llm.bean.GenerateTitleRequest;
import com.hojoai.omniasst.management.llm.bean.GenerateTitleResponse;
import feign.Request;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Feign client for LLM title generation API.
 *
 * @author zhangxuepeng
 * @version 1.0, 2025/12/24
 * @since hojoai-webmvc-omniasst-management 1.0.0
 */
@FeignClient(
        name = "titleGenerationModel",
        url = "${llm.model.title-generation.url:}"
)
public interface TitleGenerationModelClient {

    /**
     * Generate title and abstract from translation context.
     *
     * @param request the request containing source/target language and translation context
     * @return the response containing generated title and abstract
     */
    @PostMapping(value = "/v1/generateTitle", consumes = "application/json", produces = "application/json")
    GenerateTitleResponse generateTitle(@RequestBody GenerateTitleRequest request);

    /**
     * Feign client configuration for timeout settings.
     */
    class Configuration {
        public Request.Options options() {
            // Connect timeout: 10s, Read timeout: 30s
            return new Request.Options(10000, 30000);
        }
    }
}

