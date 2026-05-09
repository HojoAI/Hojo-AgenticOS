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
 * 彩云天气 v3 生活指数 API 配置（丰富版生活指数）
 * 文档：https://docs.caiyunapp.com/weather-api/v3/meteorology/lifeindex.html
 *
 * @author wangjian
 * @since 2026-02-03
 */
@Component
@ConfigurationProperties(prefix = "caiyun.lifeindex")
public class CaiyunLifeIndexV3Properties {

    /**
     * v3 生活指数 API 基址，如 https://singer.caiyunhub.com
     */
    private String baseUrl = "https://singer.caiyunhub.com";

    /**
     * Token（与天气接口可共用）
     */
    private String token;

    /**
     * 请求天数 [1, 15]，默认 15
     */
    private int days = 15;

    /**
     * 请求的指数类型，逗号分隔。6=洗车指数，10=穿衣指数
     */
    private String fields = "6,10";

    /**
     * 连接超时秒
     */
    private int connectTimeoutSeconds = 10;

    /**
     * 读取超时秒
     */
    private int readTimeoutSeconds = 30;

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public int getDays() {
        return days;
    }

    public void setDays(int days) {
        this.days = days;
    }

    public String getFields() {
        return fields;
    }

    public void setFields(String fields) {
        this.fields = fields;
    }

    public int getConnectTimeoutSeconds() {
        return connectTimeoutSeconds;
    }

    public void setConnectTimeoutSeconds(int connectTimeoutSeconds) {
        this.connectTimeoutSeconds = connectTimeoutSeconds;
    }

    public int getReadTimeoutSeconds() {
        return readTimeoutSeconds;
    }

    public void setReadTimeoutSeconds(int readTimeoutSeconds) {
        this.readTimeoutSeconds = readTimeoutSeconds;
    }
}
