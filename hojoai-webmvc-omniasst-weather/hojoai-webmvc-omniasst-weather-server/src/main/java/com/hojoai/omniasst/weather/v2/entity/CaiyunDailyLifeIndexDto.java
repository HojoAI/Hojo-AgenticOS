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

import java.util.List;

/**
 * 逐日生活指数 ultraviolet[], carWashing[], dressing[], comfort[], coldRisk[]
 *
 * @author wangjian
 * @since 2026-02-02
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class CaiyunDailyLifeIndexDto {

    private List<CaiyunDailyLifeIndexItemDto> ultraviolet;
    private List<CaiyunDailyLifeIndexItemDto> carWashing;
    private List<CaiyunDailyLifeIndexItemDto> dressing;
    private List<CaiyunDailyLifeIndexItemDto> comfort;
    private List<CaiyunDailyLifeIndexItemDto> coldRisk;

    public List<CaiyunDailyLifeIndexItemDto> getUltraviolet() {
        return ultraviolet;
    }

    public void setUltraviolet(List<CaiyunDailyLifeIndexItemDto> ultraviolet) {
        this.ultraviolet = ultraviolet;
    }

    public List<CaiyunDailyLifeIndexItemDto> getCarWashing() {
        return carWashing;
    }

    public void setCarWashing(List<CaiyunDailyLifeIndexItemDto> carWashing) {
        this.carWashing = carWashing;
    }

    public List<CaiyunDailyLifeIndexItemDto> getDressing() {
        return dressing;
    }

    public void setDressing(List<CaiyunDailyLifeIndexItemDto> dressing) {
        this.dressing = dressing;
    }

    public List<CaiyunDailyLifeIndexItemDto> getComfort() {
        return comfort;
    }

    public void setComfort(List<CaiyunDailyLifeIndexItemDto> comfort) {
        this.comfort = comfort;
    }

    public List<CaiyunDailyLifeIndexItemDto> getColdRisk() {
        return coldRisk;
    }

    public void setColdRisk(List<CaiyunDailyLifeIndexItemDto> coldRisk) {
        this.coldRisk = coldRisk;
    }
}
