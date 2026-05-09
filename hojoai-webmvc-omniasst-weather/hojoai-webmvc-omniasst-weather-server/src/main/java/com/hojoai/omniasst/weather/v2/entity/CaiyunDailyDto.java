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
 * 彩云天气 v2.6 天级别预报 result.daily
 *
 * @author wangjian
 * @since 2026-02-02
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class CaiyunDailyDto {

    /** 该 daily 子资源状态，与彩云文档一致 */
    private String status;

    /** 日出日落 */
    private List<CaiyunAstroDto> astro;

    /** 全天温度 [date, max, min, avg] */
    private List<CaiyunDailyValueDto> temperature;

    /** 白天段（08h–20h）温度 */
    @JsonProperty("temperature_08h_20h")
    private List<CaiyunDailyValueDto> temperature08h20h;

    /** 夜间段（20h–次日 32h）温度 */
    @JsonProperty("temperature_20h_32h")
    private List<CaiyunDailyValueDto> temperature20h32h;

    /** 全天降水 */
    private List<CaiyunDailyPrecipitationDto> precipitation;

    /** 白天段降水量 */
    @JsonProperty("precipitation_08h_20h")
    private List<CaiyunDailyPrecipitationDto> precipitation08h20h;

    /** 夜间段降水量 */
    @JsonProperty("precipitation_20h_32h")
    private List<CaiyunDailyPrecipitationDto> precipitation20h32h;

    /** 全天风速 */
    private List<CaiyunDailyWindDto> wind;

    /** 白天段风速 */
    @JsonProperty("wind_08h_20h")
    private List<CaiyunDailyWindDto> wind08h20h;

    /** 夜间段风速 */
    @JsonProperty("wind_20h_32h")
    private List<CaiyunDailyWindDto> wind20h32h;

    /** 相对湿度（按日） */
    private List<CaiyunDailyValueDto> humidity;
    /** 云量（按日） */
    private List<CaiyunDailyValueDto> cloudrate;
    /** 气压（按日） */
    private List<CaiyunDailyValueDto> pressure;
    /** 能见度（按日） */
    private List<CaiyunDailyValueDto> visibility;
    /** 向下短波辐射通量（按日） */
    private List<CaiyunDailyValueDto> dswrf;

    /** 全天主要天气现象 */
    private List<CaiyunDailySkyconDto> skycon;

    /** 白天段主要天气现象 */
    @JsonProperty("skycon_08h_20h")
    private List<CaiyunDailySkyconDto> skycon08h20h;

    /** 夜间段主要天气现象 */
    @JsonProperty("skycon_20h_32h")
    private List<CaiyunDailySkyconDto> skycon20h32h;

    /** 逐日空气质量（AQI、PM2.5 等） */
    @JsonProperty("air_quality")
    private CaiyunDailyAirQualityDto airQuality;

    /** 逐日生活指数（旧版结构） */
    @JsonProperty("life_index")
    private CaiyunDailyLifeIndexDto lifeIndex;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<CaiyunAstroDto> getAstro() {
        return astro;
    }

    public void setAstro(List<CaiyunAstroDto> astro) {
        this.astro = astro;
    }

    public List<CaiyunDailyValueDto> getTemperature() {
        return temperature;
    }

    public void setTemperature(List<CaiyunDailyValueDto> temperature) {
        this.temperature = temperature;
    }

    public List<CaiyunDailyValueDto> getTemperature08h20h() {
        return temperature08h20h;
    }

    public void setTemperature08h20h(List<CaiyunDailyValueDto> temperature08h20h) {
        this.temperature08h20h = temperature08h20h;
    }

    public List<CaiyunDailyValueDto> getTemperature20h32h() {
        return temperature20h32h;
    }

    public void setTemperature20h32h(List<CaiyunDailyValueDto> temperature20h32h) {
        this.temperature20h32h = temperature20h32h;
    }

    public List<CaiyunDailyPrecipitationDto> getPrecipitation() {
        return precipitation;
    }

    public void setPrecipitation(List<CaiyunDailyPrecipitationDto> precipitation) {
        this.precipitation = precipitation;
    }

    public List<CaiyunDailyPrecipitationDto> getPrecipitation08h20h() {
        return precipitation08h20h;
    }

    public void setPrecipitation08h20h(List<CaiyunDailyPrecipitationDto> precipitation08h20h) {
        this.precipitation08h20h = precipitation08h20h;
    }

    public List<CaiyunDailyPrecipitationDto> getPrecipitation20h32h() {
        return precipitation20h32h;
    }

    public void setPrecipitation20h32h(List<CaiyunDailyPrecipitationDto> precipitation20h32h) {
        this.precipitation20h32h = precipitation20h32h;
    }

    public List<CaiyunDailyWindDto> getWind() {
        return wind;
    }

    public void setWind(List<CaiyunDailyWindDto> wind) {
        this.wind = wind;
    }

    public List<CaiyunDailyWindDto> getWind08h20h() {
        return wind08h20h;
    }

    public void setWind08h20h(List<CaiyunDailyWindDto> wind08h20h) {
        this.wind08h20h = wind08h20h;
    }

    public List<CaiyunDailyWindDto> getWind20h32h() {
        return wind20h32h;
    }

    public void setWind20h32h(List<CaiyunDailyWindDto> wind20h32h) {
        this.wind20h32h = wind20h32h;
    }

    public List<CaiyunDailyValueDto> getHumidity() {
        return humidity;
    }

    public void setHumidity(List<CaiyunDailyValueDto> humidity) {
        this.humidity = humidity;
    }

    public List<CaiyunDailyValueDto> getCloudrate() {
        return cloudrate;
    }

    public void setCloudrate(List<CaiyunDailyValueDto> cloudrate) {
        this.cloudrate = cloudrate;
    }

    public List<CaiyunDailyValueDto> getPressure() {
        return pressure;
    }

    public void setPressure(List<CaiyunDailyValueDto> pressure) {
        this.pressure = pressure;
    }

    public List<CaiyunDailyValueDto> getVisibility() {
        return visibility;
    }

    public void setVisibility(List<CaiyunDailyValueDto> visibility) {
        this.visibility = visibility;
    }

    public List<CaiyunDailyValueDto> getDswrf() {
        return dswrf;
    }

    public void setDswrf(List<CaiyunDailyValueDto> dswrf) {
        this.dswrf = dswrf;
    }

    public List<CaiyunDailySkyconDto> getSkycon() {
        return skycon;
    }

    public void setSkycon(List<CaiyunDailySkyconDto> skycon) {
        this.skycon = skycon;
    }

    public List<CaiyunDailySkyconDto> getSkycon08h20h() {
        return skycon08h20h;
    }

    public void setSkycon08h20h(List<CaiyunDailySkyconDto> skycon08h20h) {
        this.skycon08h20h = skycon08h20h;
    }

    public List<CaiyunDailySkyconDto> getSkycon20h32h() {
        return skycon20h32h;
    }

    public void setSkycon20h32h(List<CaiyunDailySkyconDto> skycon20h32h) {
        this.skycon20h32h = skycon20h32h;
    }

    public CaiyunDailyAirQualityDto getAirQuality() {
        return airQuality;
    }

    public void setAirQuality(CaiyunDailyAirQualityDto airQuality) {
        this.airQuality = airQuality;
    }

    public CaiyunDailyLifeIndexDto getLifeIndex() {
        return lifeIndex;
    }

    public void setLifeIndex(CaiyunDailyLifeIndexDto lifeIndex) {
        this.lifeIndex = lifeIndex;
    }
}
