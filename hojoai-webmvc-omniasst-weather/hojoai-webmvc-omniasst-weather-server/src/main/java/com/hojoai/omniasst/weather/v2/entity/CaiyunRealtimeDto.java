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

/**
 * 彩云天气 v2.6 实时天气 result.realtime
 *
 * @author wangjian
 * @since 2026-02-02
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class CaiyunRealtimeDto {

    private String status;

    /** 地表2米气温 ℃ */
    private Double temperature;

    @JsonProperty("apparent_temperature")
    private Double apparentTemperature;

    /** 地面气压 Pa */
    private Double pressure;

    /** 相对湿度 0-1 */
    private Double humidity;

    /** 总云量 0.0-1.0 */
    private Double cloudrate;

    /** 天气现象 skycon */
    private String skycon;

    /** 水平能见度 km */
    private Double visibility;

    /** 向下短波辐射 W/m² */
    private Double dswrf;

    private CaiyunWindDto wind;

    private CaiyunPrecipitationDto precipitation;

    @JsonProperty("air_quality")
    private CaiyunAirQualityDto airQuality;

    @JsonProperty("life_index")
    private CaiyunLifeIndexDto lifeIndex;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Double getTemperature() {
        return temperature;
    }

    public void setTemperature(Double temperature) {
        this.temperature = temperature;
    }

    public Double getApparentTemperature() {
        return apparentTemperature;
    }

    public void setApparentTemperature(Double apparentTemperature) {
        this.apparentTemperature = apparentTemperature;
    }

    public Double getPressure() {
        return pressure;
    }

    public void setPressure(Double pressure) {
        this.pressure = pressure;
    }

    public Double getHumidity() {
        return humidity;
    }

    public void setHumidity(Double humidity) {
        this.humidity = humidity;
    }

    public Double getCloudrate() {
        return cloudrate;
    }

    public void setCloudrate(Double cloudrate) {
        this.cloudrate = cloudrate;
    }

    public String getSkycon() {
        return skycon;
    }

    public void setSkycon(String skycon) {
        this.skycon = skycon;
    }

    public Double getVisibility() {
        return visibility;
    }

    public void setVisibility(Double visibility) {
        this.visibility = visibility;
    }

    public Double getDswrf() {
        return dswrf;
    }

    public void setDswrf(Double dswrf) {
        this.dswrf = dswrf;
    }

    public CaiyunWindDto getWind() {
        return wind;
    }

    public void setWind(CaiyunWindDto wind) {
        this.wind = wind;
    }

    public CaiyunPrecipitationDto getPrecipitation() {
        return precipitation;
    }

    public void setPrecipitation(CaiyunPrecipitationDto precipitation) {
        this.precipitation = precipitation;
    }

    public CaiyunAirQualityDto getAirQuality() {
        return airQuality;
    }

    public void setAirQuality(CaiyunAirQualityDto airQuality) {
        this.airQuality = airQuality;
    }

    public CaiyunLifeIndexDto getLifeIndex() {
        return lifeIndex;
    }

    public void setLifeIndex(CaiyunLifeIndexDto lifeIndex) {
        this.lifeIndex = lifeIndex;
    }
}
