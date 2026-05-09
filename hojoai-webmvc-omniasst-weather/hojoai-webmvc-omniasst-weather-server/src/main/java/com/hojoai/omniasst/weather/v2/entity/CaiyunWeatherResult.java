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
 * 彩云天气综合接口 result 节点：仅解析实时 + 逐日，不解析逐时/分钟/预警。
 * 请求时未传 hourlysteps，API 不返回逐时；若 API 仍返回 hourly/minutely，会被本类忽略。
 *
 * @author wangjian
 * @since 2026-02-02
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class CaiyunWeatherResult {

    /**
     * 实时天气
     */
    private CaiyunRealtimeDto realtime;

    /**
     * 逐日预报（天级别）
     */
    private CaiyunDailyDto daily;

    /**
     * 主要数据索引
     */
    private Integer primary;

    @JsonProperty("forecast_keypoint")
    private String forecastKeypoint;

    public CaiyunRealtimeDto getRealtime() {
        return realtime;
    }

    public void setRealtime(CaiyunRealtimeDto realtime) {
        this.realtime = realtime;
    }

    public CaiyunDailyDto getDaily() {
        return daily;
    }

    public void setDaily(CaiyunDailyDto daily) {
        this.daily = daily;
    }

    public Integer getPrimary() {
        return primary;
    }

    public void setPrimary(Integer primary) {
        this.primary = primary;
    }

    public String getForecastKeypoint() {
        return forecastKeypoint;
    }

    public void setForecastKeypoint(String forecastKeypoint) {
        this.forecastKeypoint = forecastKeypoint;
    }
}
