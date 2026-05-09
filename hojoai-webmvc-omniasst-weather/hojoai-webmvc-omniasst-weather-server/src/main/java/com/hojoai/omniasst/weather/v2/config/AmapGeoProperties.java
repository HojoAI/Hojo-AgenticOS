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
package com.hojoai.omniasst.weather.v2.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 高德地图地理编码 API 配置（根据城市/地址获取经纬度）
 * 文档：https://lbs.amap.com/api/webservice/guide/api/georegeo
 * 前缀：amap.geo
 *
 * @author wangjian
 * @since 2026-02-02
 */
@Component
@ConfigurationProperties(prefix = "amap.geo")
public class AmapGeoProperties {

    /**
     * API 基址
     */
    private String baseUrl = "https://restapi.amap.com";

    /**
     * Web 服务 Key（高德控制台申请）
     */
    private String key;

    /**
     * 请求超时秒
     */
    private int timeoutSeconds = 10;

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public int getTimeoutSeconds() {
        return timeoutSeconds;
    }

    public void setTimeoutSeconds(int timeoutSeconds) {
        this.timeoutSeconds = timeoutSeconds;
    }
}
