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
 * 降水（实时：local + nearest）
 *
 * @author wangjian
 * @since 2026-02-02
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class CaiyunPrecipitationDto {

    @JsonProperty("local")
    private CaiyunPrecipitationLocalDto local;

    @JsonProperty("nearest")
    private CaiyunPrecipitationNearestDto nearest;

    public CaiyunPrecipitationLocalDto getLocal() {
        return local;
    }

    public void setLocal(CaiyunPrecipitationLocalDto local) {
        this.local = local;
    }

    public CaiyunPrecipitationNearestDto getNearest() {
        return nearest;
    }

    public void setNearest(CaiyunPrecipitationNearestDto nearest) {
        this.nearest = nearest;
    }
}
