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
package com.hojoai.omniasst.weather.v2.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * 彩云天气 v2.6 综合接口响应根对象
 *
 * @author wangjian
 * @since 2026-02-02
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class CaiyunWeatherResponse {

    /**
     * 返回状态 ok / failed
     */
    private String status;

    /** 彩云 API 版本号 */
    @JsonProperty("api_version")
    private String apiVersion;

    /** 彩云 API 状态字段，与 HTTP 层区分 */
    @JsonProperty("api_status")
    private String apiStatus;

    /**
     * 返回语言
     */
    private String lang;

    /**
     * 单位 metric 等
     */
    private String unit;

    /**
     * 时区偏移（秒）
     */
    private Integer tzshift;

    /**
     * 时区 如 Asia/Shanghai
     */
    private String timezone;

    /** 服务端时间戳（单位以彩云文档为准） */
    @JsonProperty("server_time")
    private Long serverTime;

    /**
     * 地理位置 [纬度, 经度]
     */
    private List<Double> location;

    /**
     * 综合结果（本应用仅使用 result.realtime、result.daily，不请求/不解析逐时）
     */
    private CaiyunWeatherResult result;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getApiVersion() {
        return apiVersion;
    }

    public void setApiVersion(String apiVersion) {
        this.apiVersion = apiVersion;
    }

    public String getApiStatus() {
        return apiStatus;
    }

    public void setApiStatus(String apiStatus) {
        this.apiStatus = apiStatus;
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

    public Integer getTzshift() {
        return tzshift;
    }

    public void setTzshift(Integer tzshift) {
        this.tzshift = tzshift;
    }

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    public Long getServerTime() {
        return serverTime;
    }

    public void setServerTime(Long serverTime) {
        this.serverTime = serverTime;
    }

    public List<Double> getLocation() {
        return location;
    }

    public void setLocation(List<Double> location) {
        this.location = location;
    }

    public CaiyunWeatherResult getResult() {
        return result;
    }

    public void setResult(CaiyunWeatherResult result) {
        this.result = result;
    }
}
