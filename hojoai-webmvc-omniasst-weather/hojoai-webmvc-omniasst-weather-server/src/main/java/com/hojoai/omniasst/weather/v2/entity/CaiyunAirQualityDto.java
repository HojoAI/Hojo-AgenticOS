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

/**
 * 空气质量（实时）
 *
 * @author wangjian
 * @since 2026-02-02
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class CaiyunAirQualityDto {

    private Integer pm25;
    private Integer pm10;
    private Integer o3;
    private Integer so2;
    private Integer no2;
    private Double co;
    private CaiyunAqiDto aqi;
    private CaiyunAqiDescriptionDto description;

    public Integer getPm25() {
        return pm25;
    }

    public void setPm25(Integer pm25) {
        this.pm25 = pm25;
    }

    public Integer getPm10() {
        return pm10;
    }

    public void setPm10(Integer pm10) {
        this.pm10 = pm10;
    }

    public Integer getO3() {
        return o3;
    }

    public void setO3(Integer o3) {
        this.o3 = o3;
    }

    public Integer getSo2() {
        return so2;
    }

    public void setSo2(Integer so2) {
        this.so2 = so2;
    }

    public Integer getNo2() {
        return no2;
    }

    public void setNo2(Integer no2) {
        this.no2 = no2;
    }

    public Double getCo() {
        return co;
    }

    public void setCo(Double co) {
        this.co = co;
    }

    public CaiyunAqiDto getAqi() {
        return aqi;
    }

    public void setAqi(CaiyunAqiDto aqi) {
        this.aqi = aqi;
    }

    public CaiyunAqiDescriptionDto getDescription() {
        return description;
    }

    public void setDescription(CaiyunAqiDescriptionDto description) {
        this.description = description;
    }
}
