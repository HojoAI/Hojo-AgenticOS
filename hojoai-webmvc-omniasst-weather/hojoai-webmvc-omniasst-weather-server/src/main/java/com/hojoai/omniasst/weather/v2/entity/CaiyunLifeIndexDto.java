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
 * 生活指数（实时：ultraviolet, comfort 等）
 *
 * @author wangjian
 * @since 2026-02-02
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class CaiyunLifeIndexDto {

    private CaiyunLifeIndexItemDto ultraviolet;
    private CaiyunLifeIndexItemDto comfort;
    private CaiyunLifeIndexItemDto carWashing;
    private CaiyunLifeIndexItemDto dressing;
    private CaiyunLifeIndexItemDto coldRisk;

    public CaiyunLifeIndexItemDto getUltraviolet() {
        return ultraviolet;
    }

    public void setUltraviolet(CaiyunLifeIndexItemDto ultraviolet) {
        this.ultraviolet = ultraviolet;
    }

    public CaiyunLifeIndexItemDto getComfort() {
        return comfort;
    }

    public void setComfort(CaiyunLifeIndexItemDto comfort) {
        this.comfort = comfort;
    }

    public CaiyunLifeIndexItemDto getCarWashing() {
        return carWashing;
    }

    public void setCarWashing(CaiyunLifeIndexItemDto carWashing) {
        this.carWashing = carWashing;
    }

    public CaiyunLifeIndexItemDto getDressing() {
        return dressing;
    }

    public void setDressing(CaiyunLifeIndexItemDto dressing) {
        this.dressing = dressing;
    }

    public CaiyunLifeIndexItemDto getColdRisk() {
        return coldRisk;
    }

    public void setColdRisk(CaiyunLifeIndexItemDto coldRisk) {
        this.coldRisk = coldRisk;
    }
}
