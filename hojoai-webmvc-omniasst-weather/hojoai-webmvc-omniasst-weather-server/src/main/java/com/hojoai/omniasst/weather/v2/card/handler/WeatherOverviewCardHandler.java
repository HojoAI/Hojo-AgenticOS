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
import com.hojoai.omniasst.weather.v2.entity.CaiyunDailyDto;
import com.hojoai.omniasst.weather.v2.entity.CaiyunDailyPrecipitationDto;
import com.hojoai.omniasst.weather.v2.entity.CaiyunDailyValueDto;
import com.hojoai.omniasst.weather.v2.entity.CaiyunRealtimeDto;
import com.hojoai.omniasst.weather.v2.entity.CaiyunWeatherResult;
import com.hojoai.omniasst.weather.v2.slot.WeatherCardType;
import com.hojoai.omniasst.weather.v2.support.SkyconMapper;
import com.hojoai.omniasst.weather.v2.support.WindFormatter;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * WeatherOverview 卡片 data 映射
 *
 * @author wangjian
 * @since 2026-02-02
 */
@Component
public class WeatherOverviewCardHandler implements WeatherCardHandler {

    private final SkyconMapper skyconMapper;
    private final WindFormatter windFormatter;

    public WeatherOverviewCardHandler(SkyconMapper skyconMapper, WindFormatter windFormatter) {
        this.skyconMapper = skyconMapper;
        this.windFormatter = windFormatter;
    }

    @Override
    public WeatherCardType getCardType() {
        return WeatherCardType.WeatherOverview;
    }

    @Override
    public Map<String, Object> buildCardData(CardContext context) {
        Map<String, Object> data = new LinkedHashMap<>();
        CaiyunWeatherResult result = context.getCaiyunResult();
        CaiyunRealtimeDto realtime = result != null ? result.getRealtime() : null;
        CaiyunDailyDto daily = result != null ? result.getDaily() : null;

        String name = context.getDisplayName();
        data.put("name", name);

        Double max = null, min = null;
        if (daily != null && daily.getTemperature() != null && !daily.getTemperature().isEmpty()) {
            CaiyunDailyValueDto t = daily.getTemperature().get(0);
            max = t.getMax();
            min = t.getMin();
        }
        data.put("maxTemp", max != null ? "最高 " + formatTempValue(max) : "—");
        data.put("minTemp", min != null ? "最低 " + formatTempValue(min) : "—");

        String skycon = realtime != null ? realtime.getSkycon() : null;
        data.put("condition", skyconMapper.toConditionDisplay(skycon));
        data.put("icon", skycon != null && !skycon.isBlank() ? skycon.toUpperCase() : "");

        data.put("currentTemp", realtime != null && realtime.getTemperature() != null
                ? formatTempValue(realtime.getTemperature()) : "—");

        formatDetails(data, realtime, daily);
        return data;
    }

    @Override
    public Optional<String> buildBubbleText(CardContext context) {
        CaiyunWeatherResult result = context.getCaiyunResult();
        CaiyunDailyDto daily = result != null ? result.getDaily() : null;

        String name = context.getDisplayName();
        LocalDate targetDate = getTargetDate(context);
        String dateLabel = formatDateLabel(targetDate);

        int maxTemp = 0, minTemp = 0;
        if (daily != null && daily.getTemperature() != null && !daily.getTemperature().isEmpty()) {
            CaiyunDailyValueDto t = daily.getTemperature().get(0);
            maxTemp = t.getMax() != null ? (int) Math.round(t.getMax()) : 0;
            minTemp = t.getMin() != null ? (int) Math.round(t.getMin()) : 0;
        }
        String bubble = String.format("%s%s最高温度%d度，最低%d度。", name, dateLabel, maxTemp, minTemp);
        return Optional.of(bubble);
    }

    private static LocalDate getTargetDate(CardContext context) {
        List<LocalDate> parsed = context.getParsedDates();
        if (parsed != null && !parsed.isEmpty()) {
            return parsed.get(0);
        }
        return LocalDate.now();
    }

    private static String formatDateLabel(LocalDate date) {
        if (date == null) return "今天";
        LocalDate today = LocalDate.now();
        if (date.equals(today)) return "今天";
        if (date.equals(today.plusDays(1))) return "明天";
        if (date.equals(today.plusDays(2))) return "后天";
        return date.getMonthValue() + "月" + date.getDayOfMonth() + "日";
    }

    private void formatDetails(Map<String, Object> data, CaiyunRealtimeDto realtime, CaiyunDailyDto daily) {
        List<Map<String, String>> details = new ArrayList<>();
        // 降雨概率
        String rainProb = "0%";
        if (daily != null && daily.getPrecipitation() != null && !daily.getPrecipitation().isEmpty()) {
            CaiyunDailyPrecipitationDto p = daily.getPrecipitation().get(0);
            if (p.getProbability() != null) {
                rainProb = (int) Math.round(p.getProbability()) + "%";
            }
        }
        details.add(Map.of("name", "降雨概率", "value", rainProb));
        // 空气质量
        String aqiStr = "—";
        if (realtime != null && realtime.getAirQuality() != null) {
            CaiyunAqiDto aqi = realtime.getAirQuality().getAqi();
            if (aqi != null && aqi.getChn() != null) {
                aqiStr = "AQI: " + aqi.getChn();
            }
        }
        details.add(Map.of("name", "空气质量", "value", aqiStr));
        // 风力
        String windStr = windFormatter.format(realtime != null ? realtime.getWind() : null);
        details.add(Map.of("name", "风力", "value", windStr));
        // 紫外线指数
        String uv = "—";
        if (realtime != null && realtime.getLifeIndex() != null && realtime.getLifeIndex().getUltraviolet() != null) {
            String idx = realtime.getLifeIndex().getUltraviolet().getIndex();
            if (idx != null && !idx.isBlank()) uv = idx;
        }
        details.add(Map.of("name", "紫外线指数", "value", uv));
        data.put("details", details);
    }

    private static String formatTempValue(double t) {
        return (int) Math.round(t) + "°";
    }
}
