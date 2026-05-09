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
package com.hojoai.omniasst.weather.v2.slot;

/**
 * 天气卡片类型（与模型返回、前端卡片 name 一致）
 *
 * @author wangjian
 * @since 2026-02-02
 */
public enum WeatherCardType {
    /** 询问某地当天的温度时 */
    WeatherDailyFallback,
    /** 询问某地几天的天气时 */
    WeatherInfoMultiDay,
    /** 询问天气相关的某天的行为建议时 */
    WeatherBehaviorAdvice,
    /** 询问某地某天的天气状况时 */
    WeatherOverview,
    /** 询问某地某天的降雨情况时 */
    WeatherRain,
    /** 询问某地某天的风力时 */
    WeatherWind,
    /** 询问某地的空气质量时 */
    WeatherAirQuality
}
