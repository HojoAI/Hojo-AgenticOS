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
package com.hojoai.omniasst.common.api.memory.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Memory API 配置属性
 * 前缀为 hojoai.memory.api
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "hojoai.memory.api")
public class MemoryApiProperties {

    /**
     * Memory API 服务地址（由各业务服务配置 hojoai.memory.api.url；未配置时与 Feign 占位默认一致）
     */
    private String url = "http://localhost:8001";

    /**
     * 连接超时时间（毫秒）
     */
    private Integer connectTimeout = 5000;

    /**
     * 读取超时时间（毫秒）
     */
    private Integer readTimeout = 10000;

    /**
     * 是否启用日志
     */
    private Boolean logEnabled = true;

    /**
     * 日志级别：NONE, BASIC, HEADERS, FULL
     */
    private String logLevel = "BASIC";

    /**
     * 租户组织ID（用于多租户场景）
     */
    private String organizationId;

    /**
     * 租户空间ID（用于多租户场景）
     */
    private String spaceId;

    /**
     * 租户Hash Key（用于多租户场景）
     */
    private String hashKey;
}
