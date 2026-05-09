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
package com.hojoai.omniasst.weather.v2.card.handler;

import com.hojoai.omniasst.weather.v2.card.CardContext;
import com.hojoai.omniasst.weather.v2.entity.CaiyunAqiDto;
import com.hojoai.omniasst.weather.v2.entity.CaiyunAirQualityDto;
import com.hojoai.omniasst.weather.v2.entity.CaiyunDailyDto;
import com.hojoai.omniasst.weather.v2.entity.CaiyunDailyPrecipitationDto;
import com.hojoai.omniasst.weather.v2.entity.CaiyunRealtimeDto;
import com.hojoai.omniasst.weather.v2.entity.CaiyunWeatherResult;
import com.hojoai.omniasst.weather.v2.slot.WeatherCardType;
import com.hojoai.omniasst.weather.v2.support.SkyconMapper;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * WeatherBehaviorAdvice 卡片：行为建议（洗车、遛狗、穿短袖等）。
 * 档位仅两档：适合 / 不适合（按降雨概率与空气质量综合判定）。
 * suggestion 只显示「适合」或「不适合」；description 只显示「天气晴好、无降水，凉爽，空气质量佳。」；bubbleText = suggestion + description。
 *
 * @author wangjian
 * @since 2026-02-02
 */
@Component
public class WeatherBehaviorAdviceCardHandler implements WeatherCardHandler {

    private final SkyconMapper skyconMapper;

    /** 降雨概率：<0.2 适合，>=0.2 不适合 */
    private static final double RAIN_SUITABLE_MAX = 0.2;

    public WeatherBehaviorAdviceCardHandler(SkyconMapper skyconMapper) {
        this.skyconMapper = skyconMapper;
    }

    /** 按降雨概率、空气质量综合得出是否适合（两档）：无雨或低概率 + 优/良 为适合 */
    private boolean computeSuitable(CardContext context) {
        CaiyunWeatherResult result = context.getCaiyunResult();
        CaiyunRealtimeDto realtime = result != null ? result.getRealtime() : null;
        CaiyunDailyDto daily = result != null ? result.getDaily() : null;

        double rainProb = getRainProbability(daily);
        boolean rainOk = rainProb < RAIN_SUITABLE_MAX;

        boolean aqiOk = isAirQualitySuitable(realtime);

        return rainOk && aqiOk;
    }

    /** 空气质量适合：优、良为适合，无数据视为适合 */
    private boolean isAirQualitySuitable(CaiyunRealtimeDto realtime) {
        if (realtime == null || realtime.getAirQuality() == null || realtime.getAirQuality().getDescription() == null) {
            return true;
        }
        String chn = realtime.getAirQuality().getDescription().getChn();
        if (chn == null || chn.isBlank()) return true;
        return chn.contains("优") || chn.contains("良");
    }

    @Override
    public WeatherCardType getCardType() {
        return WeatherCardType.WeatherBehaviorAdvice;
    }

    /** 今日降雨概率 0～1，无数据视为 0 */
    private double getRainProbability(CaiyunDailyDto daily) {
        if (daily == null || daily.getPrecipitation() == null || daily.getPrecipitation().isEmpty()) return 0.0;
        CaiyunDailyPrecipitationDto p = daily.getPrecipitation().get(0);
        return p.getProbability() != null ? p.getProbability() : 0.0;
    }

    /**
     * description 仅四段：天气晴好、无降水，凉爽，空气质量佳。（不显示其他）
     */
    private String buildDescriptionOnly(CaiyunRealtimeDto realtime, CaiyunDailyDto daily) {
        String weatherDesc = "天气晴好";
        if (realtime != null && realtime.getSkycon() != null && !realtime.getSkycon().isBlank()) {
            String zh = skyconMapper.toChinese(realtime.getSkycon());
            if ("晴".equals(zh) || "多云".equals(zh)) weatherDesc = "天气晴好";
            else weatherDesc = "天气" + zh;
        }
        String rainDesc = hasPrecipitation(daily) ? "有降水" : "无降水";
        String comfortDesc = "凉爽";
        if (realtime != null && realtime.getLifeIndex() != null && realtime.getLifeIndex().getComfort() != null) {
            String c = realtime.getLifeIndex().getComfort().getDesc();
            if (c != null && !c.isBlank()) comfortDesc = c;
        }
        String aqiDesc = "空气质量佳";
        if (realtime != null && realtime.getAirQuality() != null && realtime.getAirQuality().getDescription() != null
                && realtime.getAirQuality().getDescription().getChn() != null) {
            String chn = realtime.getAirQuality().getDescription().getChn();
            if (chn.contains("优")) aqiDesc = "空气质量优";
            else if (chn.contains("良")) aqiDesc = "空气质量佳";
            else if (!chn.isBlank()) aqiDesc = "空气质量" + chn;
        }
        return weatherDesc + "、" + rainDesc + "，" + comfortDesc + "，" + aqiDesc + "。";
    }

    private boolean hasPrecipitation(CaiyunDailyDto daily) {
        if (daily == null || daily.getPrecipitation() == null || daily.getPrecipitation().isEmpty()) return false;
        CaiyunDailyPrecipitationDto p = daily.getPrecipitation().get(0);
        return p.getProbability() != null && p.getProbability() > 0;
    }

    @Override
    public Map<String, Object> buildCardData(CardContext context) {
        Map<String, Object> data = new LinkedHashMap<>();
        CaiyunWeatherResult result = context.getCaiyunResult();
        CaiyunRealtimeDto realtime = result != null ? result.getRealtime() : null;
        CaiyunDailyDto daily = result != null ? result.getDaily() : null;

        data.put("name", context.getDisplayName());

        boolean suitable = computeSuitable(context);
        String suggestion = suitable ? "适合" : "不适合";
        data.put("suggestion", suggestion);

        String description = buildDescriptionOnly(realtime, daily);
        data.put("description", description);

        String skycon = realtime != null ? realtime.getSkycon() : null;
        data.put("condition", skyconMapper.toConditionDisplay(skycon));
        data.put("icon", skycon != null && !skycon.isBlank() ? skycon.toUpperCase() : "");

        List<Map<String, String>> details = buildDetails(realtime, daily);
        data.put("details", details);

        return data;
    }

    @Override
    public Optional<String> buildBubbleText(CardContext context) {
        CaiyunWeatherResult result = context.getCaiyunResult();
        CaiyunRealtimeDto realtime = result != null ? result.getRealtime() : null;
        CaiyunDailyDto daily = result != null ? result.getDaily() : null;
        boolean suitable = computeSuitable(context);
        String suggestion = suitable ? "适合" : "不适合";
        String description = buildDescriptionOnly(realtime, daily);
        return Optional.of(suggestion + "。" + description);
    }

    private List<Map<String, String>> buildDetails(CaiyunRealtimeDto realtime, CaiyunDailyDto daily) {
        List<Map<String, String>> details = new ArrayList<>();

        String rainProb = "0%";
        if (daily != null && daily.getPrecipitation() != null && !daily.getPrecipitation().isEmpty()) {
            CaiyunDailyPrecipitationDto p = daily.getPrecipitation().get(0);
            if (p.getProbability() != null) {
                rainProb = (int) Math.round(p.getProbability() * 100) + "%";
            }
        }
        details.add(Map.of("name", "降雨概率", "value", rainProb));

        String aqiStr = "—";
        if (realtime != null && realtime.getAirQuality() != null) {
            CaiyunAirQualityDto aq = realtime.getAirQuality();
            CaiyunAqiDto aqi = aq.getAqi();
            if (aqi != null && aqi.getChn() != null) {
                aqiStr = "AQI: " + aqi.getChn();
            }
        }
        details.add(Map.of("name", "空气质量", "value", aqiStr));

        return details;
    }
}
