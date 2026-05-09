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
import com.hojoai.omniasst.weather.v2.entity.CaiyunDailyDto;
import com.hojoai.omniasst.weather.v2.entity.CaiyunDailyPrecipitationDto;
import com.hojoai.omniasst.weather.v2.entity.CaiyunDailySkyconDto;
import com.hojoai.omniasst.weather.v2.entity.CaiyunDailyValueDto;
import com.hojoai.omniasst.weather.v2.entity.CaiyunWeatherResult;
import com.hojoai.omniasst.weather.v2.slot.WeatherCardType;
import com.hojoai.omniasst.weather.v2.support.SkyconMapper;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * WeatherInfoMultiDay 卡片 data 映射（某地多日天气：name、highlight、description、forecast、dressingTip）
 *
 * @author wangjian
 * @since 2026-02-02
 */
@Component
public class WeatherInfoMultiDayCardHandler implements WeatherCardHandler {

    private static final String[] WEEKDAY_ZH = {"周一", "周二", "周三", "周四", "周五", "周六", "周日"};

    private final SkyconMapper skyconMapper;

    public WeatherInfoMultiDayCardHandler(SkyconMapper skyconMapper) {
        this.skyconMapper = skyconMapper;
    }

    @Override
    public WeatherCardType getCardType() {
        return WeatherCardType.WeatherInfoMultiDay;
    }

    @Override
    public Map<String, Object> buildCardData(CardContext context) {
        Map<String, Object> data = new LinkedHashMap<>();
        CaiyunWeatherResult result = context.getCaiyunResult();
        CaiyunDailyDto daily = result != null ? result.getDaily() : null;

        data.put("name", context.getDisplayName());

        if (daily == null || daily.getTemperature() == null || daily.getTemperature().isEmpty()) {
            data.put("highlight", Map.of("temperature", "—", "description", "未来几天"));
            data.put("description", "未来几天");
            data.put("forecast", List.of());
            data.put("dressingTip", "");
            return data;
        }

        List<CaiyunDailyValueDto> temps = daily.getTemperature();
        List<CaiyunDailySkyconDto> skycons = daily.getSkycon() != null ? daily.getSkycon() : List.of();

        int daysCount = temps.size();
        String description = "未来" + daysCount + "天，最低温度";
        data.put("description", description);

        // 找出最低温及其对应日
        double minTempVal = Double.MAX_VALUE;
        String highlightTemp = "—";
        for (CaiyunDailyValueDto t : temps) {
            if (t.getMin() != null && t.getMin() < minTempVal) {
                minTempVal = t.getMin();
                highlightTemp = (int) Math.round(t.getMin()) + "°";
            }
        }
        if (minTempVal == Double.MAX_VALUE) highlightTemp = "—";
        Map<String, String> highlight = new LinkedHashMap<>();
        highlight.put("temperature", highlightTemp);
        highlight.put("description", "未来" + daysCount + "天·最低温度");
        data.put("highlight", highlight);

        List<Map<String, Object>> forecast = new ArrayList<>();
        LocalDate today = LocalDate.now();
        for (int i = 0; i < temps.size(); i++) {
            CaiyunDailyValueDto t = temps.get(i);
            Map<String, Object> item = new LinkedHashMap<>();
            LocalDate date = parseDate(t.getDate());
            item.put("day", formatDayLabel(date, today));
            item.put("lowTemp", t.getMin() != null ? (int) Math.round(t.getMin()) + "°" : "—");
            item.put("highTemp", t.getMax() != null ? (int) Math.round(t.getMax()) + "°" : "—");

            String skycon = null;
            if (i < skycons.size()) {
                CaiyunDailySkyconDto sc = skycons.get(i);
                skycon = sc != null ? sc.getValue() : null;
            }
            item.put("icon", skycon != null && !skycon.isBlank() ? skycon.toUpperCase() : "");
            forecast.add(item);
        }
        data.put("forecast", forecast);

        String dressingTip = buildDressingTip(temps, skycons, today);
        data.put("dressingTip", dressingTip);

        return data;
    }

    @Override
    public Optional<String> buildBubbleText(CardContext context) {
        CaiyunWeatherResult result = context.getCaiyunResult();
        CaiyunDailyDto daily = result != null ? result.getDaily() : null;
        if (daily == null || daily.getTemperature() == null || daily.getTemperature().isEmpty()) {
            return Optional.of(context.getDisplayName() + "未来几天暂无预报。");
        }

        List<CaiyunDailyValueDto> temps = daily.getTemperature();
        List<CaiyunDailySkyconDto> skycons = daily.getSkycon() != null ? daily.getSkycon() : List.of();
        List<CaiyunDailyPrecipitationDto> precips = daily.getPrecipitation() != null ? daily.getPrecipitation() : List.of();
        LocalDate today = LocalDate.now();

        String name = context.getDisplayName();
        int daysCount = temps.size();
        StringBuilder sb = new StringBuilder();
        sb.append(name).append("未来").append(daysCount).append("天的天气如下：");

        int coldestIdx = findColdestDayIndex(temps);
        for (int i = 0; i < temps.size(); i++) {
            CaiyunDailyValueDto t = temps.get(i);
            LocalDate date = parseDate(t.getDate());
            String dayLabel = formatDayLabel(date, today);
            String skycon = null;
            if (i < skycons.size()) {
                CaiyunDailySkyconDto sc = skycons.get(i);
                skycon = sc != null ? sc.getValue() : null;
            }
            String conditionZh = skyconMapper.toChinese(skycon);
            int low = t.getMin() != null ? (int) Math.round(t.getMin()) : 0;
            int high = t.getMax() != null ? (int) Math.round(t.getMax()) : 0;

            boolean hasRain = false;
            if (i < precips.size()) {
                CaiyunDailyPrecipitationDto p = precips.get(i);
                hasRain = p != null && p.getProbability() != null && p.getProbability() > 0;
            }
            boolean hasStorm = "STORM_RAIN".equals(skycon) || "STORM_SNOW".equals(skycon);

            if (i == coldestIdx) {
                sb.append(dayLabel).append("将迎来近期最低温").append(low).append("度，");
                if (hasStorm) {
                    sb.append("当天有雷雨，");
                } else if (hasRain) {
                    sb.append("当天有雨，");
                }
                sb.append("最高气温").append(high).append("度，体感稍凉，请注意保暖。");
            } else if (hasRain) {
                sb.append(dayLabel).append("有雨，气温").append(low).append("至").append(high).append("度。");
            } else if ("晴".equals(conditionZh)) {
                sb.append(dayLabel).append("天气转晴，气温").append(low).append("至").append(high).append("度。");
            } else {
                sb.append(dayLabel).append(conditionZh).append("，气温").append(low).append("至").append(high).append("度。");
            }
        }

        return Optional.of(sb.toString());
    }

    /**
     * 解析 API 日期字符串为 LocalDate（年月日），用于与“今天”对比。
     * 支持格式：2026-02-04T00:00+08:00（只取 T 前年月日）、2026-02-04（纯日期）。
     */
    private static LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.isBlank()) return LocalDate.now();
        try {
            String dateOnly = dateStr.contains("T") ? dateStr.substring(0, dateStr.indexOf('T')) : dateStr;
            return LocalDate.parse(dateOnly, DateTimeFormatter.ISO_LOCAL_DATE);
        } catch (Exception e) {
            return LocalDate.now();
        }
    }

    private static String formatDayLabel(LocalDate date, LocalDate today) {
        if (date == null) return "—";
        if (date.equals(today)) return "今天";
        if (date.equals(today.plusDays(1))) return "明天";
        if (date.equals(today.plusDays(2))) return "后天";
        DayOfWeek dow = date.getDayOfWeek();
        return WEEKDAY_ZH[dow.getValue() - 1];
    }

    private static int findColdestDayIndex(List<CaiyunDailyValueDto> temps) {
        if (temps == null || temps.isEmpty()) return 0;
        int minIdx = 0;
        double minVal = Double.MAX_VALUE;
        for (int i = 0; i < temps.size(); i++) {
            Double min = temps.get(i).getMin();
            if (min != null && min < minVal) {
                minVal = min;
                minIdx = i;
            }
        }
        return minIdx;
    }

    /** 根据最低温日生成穿衣提示 */
    private static String buildDressingTip(List<CaiyunDailyValueDto> temps, List<CaiyunDailySkyconDto> skycons, LocalDate today) {
        if (temps == null || temps.isEmpty()) return "";
        int minIdx = findColdestDayIndex(temps);
        CaiyunDailyValueDto coldest = temps.get(minIdx);
        if (coldest.getMin() == null) return "";
        String dateStr = coldest.getDate();
        LocalDate coldestDate = parseDate(dateStr);
        String dayLabel = formatDayLabel(coldestDate, today);
        int low = (int) Math.round(coldest.getMin());
        int high = coldest.getMax() != null ? (int) Math.round(coldest.getMax()) : low;
        String skycon = minIdx < skycons.size() && skycons.get(minIdx) != null ? skycons.get(minIdx).getValue() : null;
        boolean hasStorm = "STORM_RAIN".equals(skycon) || "STORM_SNOW".equals(skycon);
        boolean hasRain = skycon != null && (skycon.contains("RAIN") || skycon.contains("SNOW"));
        StringBuilder tip = new StringBuilder();
        tip.append(dayLabel).append("将迎来近期最低温").append(low).append("度，");
        if (hasStorm) {
            tip.append("当天有雷雨，");
        } else if (hasRain) {
            tip.append("当天有雨，");
        }
        tip.append("最高气温").append(high).append("度，体感稍凉，请注意保暖。");
        return tip.toString();
    }
}
