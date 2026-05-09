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
package com.hojoai.omniasst.configure;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StreamUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Controller for .well-known endpoints
 *
 * @author system
 * @version 1.0
 */
@RestController
public class WellKnownController {

    private static final Logger log = LoggerFactory.getLogger(WellKnownController.class);

    /**
     * 返回 Android App Links 所需的 Digital Asset Links 声明（assetlinks.json）。
     * 从 classpath {@code static/.well-known/assetlinks.json} 读取；若文件不存在或读取失败则返回空数组 {@code []}，避免客户端解析失败。
     *
     * @return JSON 字符串及 {@code Content-Type: application/json}
     */
    @GetMapping(value = "/.well-known/assetlinks.json", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<String>> getAssetLinks() {
        try {
            Resource resource = new ClassPathResource("static/.well-known/assetlinks.json");
            if (resource.exists()) {
                String content = StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
                return Mono.just(ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .body(content));
            } else {
                return Mono.just(ResponseEntity.ok().body("[]"));
            }
        } catch (IOException e) {
            log.debug("Failed to read assetlinks.json from classpath", e);
            return Mono.just(ResponseEntity.ok().body("[]"));
        }
    }
}
