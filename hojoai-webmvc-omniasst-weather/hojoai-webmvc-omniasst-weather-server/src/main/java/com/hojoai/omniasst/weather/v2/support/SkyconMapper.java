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
package com.hojoai.omniasst.weather.v2.support;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * skycon → 中文 / icon 码映射（彩云天气现象代码）
 *
 * @author wangjian
 * @since 2026-02-02
 */
@Component
public class SkyconMapper {

    /** skycon → 中文描述 */
    private static final Map<String, String> TO_ZH = new HashMap<>();
    /** skycon → 卡片 icon（d01 风格，如 WeatherOverview） */
    private static final Map<String, String> TO_ICON_D = new HashMap<>();
    /** skycon → 卡片 icon（数字风格，如 WeatherDailyFallback 的 104） */
    private static final Map<String, String> TO_ICON_NUM = new HashMap<>();

    static {
        TO_ZH.put("CLEAR_DAY", "晴");
        TO_ZH.put("CLEAR_NIGHT", "晴");
        TO_ZH.put("PARTLY_CLOUDY_DAY", "多云");
        TO_ZH.put("PARTLY_CLOUDY_NIGHT", "多云");
        TO_ZH.put("CLOUDY", "阴");
        TO_ZH.put("LIGHT_RAIN", "小雨");
        TO_ZH.put("MODERATE_RAIN", "中雨");
        TO_ZH.put("HEAVY_RAIN", "大雨");
        TO_ZH.put("STORM_RAIN", "暴雨");
        TO_ZH.put("LIGHT_SNOW", "小雪");
        TO_ZH.put("MODERATE_SNOW", "中雪");
        TO_ZH.put("HEAVY_SNOW", "大雪");
        TO_ZH.put("STORM_SNOW", "暴雪");
        TO_ZH.put("LIGHT_HAZE", "轻度雾霾");
        TO_ZH.put("MODERATE_HAZE", "中度雾霾");
        TO_ZH.put("HEAVY_HAZE", "重度雾霾");
        TO_ZH.put("DUST", "浮尘");
        TO_ZH.put("SAND", "沙尘");
        TO_ZH.put("FOG", "雾");
        TO_ZH.put("WIND", "大风");

        TO_ICON_D.put("CLEAR_DAY", "d00");
        TO_ICON_D.put("CLEAR_NIGHT", "n00");
        TO_ICON_D.put("PARTLY_CLOUDY_DAY", "d01");
        TO_ICON_D.put("PARTLY_CLOUDY_NIGHT", "n01");
        TO_ICON_D.put("CLOUDY", "d02");
        TO_ICON_D.put("LIGHT_RAIN", "d03");
        TO_ICON_D.put("MODERATE_RAIN", "d04");
        TO_ICON_D.put("HEAVY_RAIN", "d05");
        TO_ICON_D.put("STORM_RAIN", "d06");
        TO_ICON_D.put("LIGHT_SNOW", "d07");
        TO_ICON_D.put("MODERATE_SNOW", "d08");
        TO_ICON_D.put("HEAVY_SNOW", "d09");
        TO_ICON_D.put("STORM_SNOW", "d10");
        TO_ICON_D.put("LIGHT_HAZE", "d11");
        TO_ICON_D.put("MODERATE_HAZE", "d12");
        TO_ICON_D.put("HEAVY_HAZE", "d13");
        TO_ICON_D.put("DUST", "d14");
        TO_ICON_D.put("SAND", "d15");
        TO_ICON_D.put("FOG", "d16");
        TO_ICON_D.put("WIND", "d17");

        TO_ICON_NUM.put("CLEAR_DAY", "100");
        TO_ICON_NUM.put("CLEAR_NIGHT", "150");
        TO_ICON_NUM.put("PARTLY_CLOUDY_DAY", "101");
        TO_ICON_NUM.put("PARTLY_CLOUDY_NIGHT", "151");
        TO_ICON_NUM.put("CLOUDY", "104");
        TO_ICON_NUM.put("LIGHT_RAIN", "305");
        TO_ICON_NUM.put("MODERATE_RAIN", "306");
        TO_ICON_NUM.put("HEAVY_RAIN", "307");
        TO_ICON_NUM.put("STORM_RAIN", "308");
        TO_ICON_NUM.put("LIGHT_SNOW", "400");
        TO_ICON_NUM.put("MODERATE_SNOW", "401");
        TO_ICON_NUM.put("HEAVY_SNOW", "402");
        TO_ICON_NUM.put("STORM_SNOW", "403");
        TO_ICON_NUM.put("LIGHT_HAZE", "501");
        TO_ICON_NUM.put("MODERATE_HAZE", "502");
        TO_ICON_NUM.put("HEAVY_HAZE", "503");
        TO_ICON_NUM.put("DUST", "504");
        TO_ICON_NUM.put("SAND", "505");
        TO_ICON_NUM.put("FOG", "509");
        TO_ICON_NUM.put("WIND", "900");
    }

    public String toChinese(String skycon) {
        if (skycon == null || skycon.isBlank()) return "未知";
        return TO_ZH.getOrDefault(skycon, "未知");
    }

    /** 用于 WeatherOverview 等 d01 风格 icon */
    public String toIconD(String skycon) {
        if (skycon == null || skycon.isBlank()) return "d01";
        return TO_ICON_D.getOrDefault(skycon, "d01");
    }

    /** 用于 WeatherDailyFallback 等数字风格 icon */
    public String toIconNum(String skycon) {
        if (skycon == null || skycon.isBlank()) return "104";
        return TO_ICON_NUM.getOrDefault(skycon, "104");
    }

    /** 用于 WeatherBehaviorAdvice 等：晴间多云、晴、多云等展示文案 */
    public String toConditionDisplay(String skycon) {
        if (skycon == null || skycon.isBlank()) return "未知";
        if (skycon.contains("PARTLY_CLOUDY")) return "晴间多云";
        return TO_ZH.getOrDefault(skycon, "未知");
    }

    /** 用于 WeatherBehaviorAdvice 卡片 icon：snake_case 如 partly_cloudy、clear */
    public String toIconSnake(String skycon) {
        if (skycon == null || skycon.isBlank()) return "cloudy";
        switch (skycon) {
            case "CLEAR_DAY":
            case "CLEAR_NIGHT":
                return "clear";
            case "PARTLY_CLOUDY_DAY":
            case "PARTLY_CLOUDY_NIGHT":
                return "partly_cloudy";
            case "CLOUDY":
                return "cloudy";
            case "LIGHT_RAIN":
                return "light_rain";
            case "MODERATE_RAIN":
                return "moderate_rain";
            case "HEAVY_RAIN":
                return "heavy_rain";
            case "STORM_RAIN":
                return "storm_rain";
            case "LIGHT_SNOW":
                return "light_snow";
            case "MODERATE_SNOW":
                return "moderate_snow";
            case "HEAVY_SNOW":
                return "heavy_snow";
            case "STORM_SNOW":
                return "storm_snow";
            case "LIGHT_HAZE":
            case "MODERATE_HAZE":
            case "HEAVY_HAZE":
                return "haze";
            case "FOG":
                return "fog";
            case "WIND":
                return "wind";
            default:
                return "cloudy";
        }
    }
}
