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
 * 彩云天气 v2.6 API 配置
 *
 * @author wangjian
 * @since 2026-02-02
 */
@Component
@ConfigurationProperties(prefix = "caiyun.weather")
public class CaiyunWeatherProperties {

    /**
     * API 基址，如 https://api.caiyunapp.com
     */
    private String baseUrl = "https://api.caiyunapp.com";

    /**
     * API 版本路径，如 v2.6
     */
    private String apiVersion = "v2.6";

    /**
     * Token（放在 URL 路径中，与 appKey 二选一）
     */
    private String token;

    /**
     * 默认返回语言 zh_CN / en
     */
    private String lang = "zh_CN";

    /**
     * 单位 metric / imperial
     */
    private String unit = "metric";

    /**
     * 综合接口默认逐日天数 [1, 15]
     */
    private int dailysteps = 15;

    /**
     * 综合接口逐小时数（仅实时+逐日时为 0，不请求小时数据）
     */
    private int hourlysteps = 0;

    /**
     * 是否请求预警数据（仅实时+逐日时为 false）
     */
    private boolean alert = false;

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

    public String getApiVersion() {
        return apiVersion;
    }

    public void setApiVersion(String apiVersion) {
        this.apiVersion = apiVersion;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getLang() {
        return lang;
    }

    public void setLang(String lang) {
        this.lang = lang;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public int getDailysteps() {
        return dailysteps;
    }

    public void setDailysteps(int dailysteps) {
        this.dailysteps = dailysteps;
    }

    public int getHourlysteps() {
        return hourlysteps;
    }

    public void setHourlysteps(int hourlysteps) {
        this.hourlysteps = hourlysteps;
    }

    public boolean isAlert() {
        return alert;
    }

    public void setAlert(boolean alert) {
        this.alert = alert;
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
