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
package com.hojoai.webmvc.omniasst.music.configure;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * Netease Cloud Music API Configuration
 * 网易云音乐API配置
 * 
 * @author shengxianfei
 * @since 2026-01-13
 */
@Configuration
public class NeteaseCloudMusicConfig {
    
    /** 网易云开放平台 HTTP 根地址 */
    @Value("${netease.cloud.music.api.base-url}")
    private String baseUrl;
    
    /** 调用开放平台的超时时间（毫秒，与配置项含义一致） */
    @Value("${netease.cloud.music.api.timeout}")
    private Integer timeout;
    
    /**
     * @return 开放平台 base URL
     */
    public String getBaseUrl() {
        return baseUrl;
    }
    
    /**
     * @param baseUrl 开放平台 base URL
     */
    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }
    
    /**
     * @return 请求超时时间
     */
    public Integer getTimeout() {
        return timeout;
    }
    
    /**
     * @param timeout 请求超时时间
     */
    public void setTimeout(Integer timeout) {
        this.timeout = timeout;
    }
}

