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
import com.hojoai.omniasst.weather.v2.entity.CaiyunAirQualityDto;
import com.hojoai.omniasst.weather.v2.entity.CaiyunAqiDto;
import com.hojoai.omniasst.weather.v2.entity.CaiyunAqiDescriptionDto;
import com.hojoai.omniasst.weather.v2.entity.CaiyunDailyAirQualityDto;
import com.hojoai.omniasst.weather.v2.entity.CaiyunDailyAqiItemDto;
import com.hojoai.omniasst.weather.v2.entity.CaiyunDailyDto;
import com.hojoai.omniasst.weather.v2.entity.CaiyunRealtimeDto;
import com.hojoai.omniasst.weather.v2.entity.CaiyunWeatherResult;
import com.hojoai.omniasst.weather.v2.slot.WeatherCardType;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * WeatherAirQuality 卡片 data 映射（某地空气质量：name、suggestion、airQuality、airQualityText）
 *
 * @author wangjian
 * @since 2026-02-02
 */
@Component
public class WeatherAirQualityCardHandler implements WeatherCardHandler {

    @Override
    public WeatherCardType getCardType() {
        return WeatherCardType.WeatherAirQuality;
    }

    @Override
    public Map<String, Object> buildCardData(CardContext context) {
        Map<String, Object> data = new LinkedHashMap<>();
        CaiyunWeatherResult result = context.getCaiyunResult();
        CaiyunRealtimeDto realtime = result != null ? result.getRealtime() : null;

        data.put("name", context.getDisplayName());

        LocalDate targetDate = getTargetDate(context);
        String dateLabel = formatDateLabel(targetDate);
        if(StringUtils.isNotBlank(dateLabel)){
            data.put("name", context.getDisplayName() + "·" + dateLabel);
        }

        CaiyunAirQualityDto aq = realtime != null ? realtime.getAirQuality() : null;
        if (aq == null) {
            data.put("suggestion", "暂无空气质量数据。");
            data.put("airQuality", "—");
            data.put("airQualityText", "—");
            return data;
        }

        String airQuality = "—";
        CaiyunAqiDto aqi = aq.getAqi();
        if (aqi != null && aqi.getChn() != null) {
            airQuality = String.valueOf(aqi.getChn());
        }

        String airQualityText = "—";
        CaiyunAqiDescriptionDto description = aq.getDescription();
        if (description != null && description.getChn() != null && !description.getChn().isBlank()) {
            airQualityText = description.getChn();
        }

        String suggestion = suggestionByQuality(airQualityText);
        data.put("suggestion", suggestion);
        data.put("airQuality", airQuality);
        data.put("airQualityText", airQualityText);

        return data;
    }

    @Override
    public Optional<String> buildBubbleText(CardContext context) {
        List<LocalDate> parsedDates = context.getParsedDates();
        if (parsedDates == null || parsedDates.isEmpty()) {
            parsedDates = List.of(LocalDate.now());
        }
        if (parsedDates.size() == 1) {
            // 单日：保持现有逻辑
            String name = context.getDisplayName();
            LocalDate targetDate = parsedDates.get(0);
            String dateLabel = formatDateLabel(targetDate);
            String airQuality = getAirQualityValue(context);
            String airQualityText = getAirQualityText(context);
            String suggestion = getSuggestion(context);
            String bubble = String.format("%s%s空气质量AQI指数%s，%s。%s", name, dateLabel, airQuality, airQualityText, suggestion);
            return Optional.of(bubble);
        }
        // 多日：按日期循环获取逐日空气质量，增量拼接
        String name = context.getDisplayName();
        StringBuilder sb = new StringBuilder();
        sb.append(name).append("多日空气质量：");
        for (int i = 0; i < parsedDates.size(); i++) {
            if (i > 0) sb.append(" ");
            LocalDate date = parsedDates.get(i);
            String dateLabel = formatDateLabel(date);
            String aqiValue = getDailyAirQualityValue(context, date);
            String aqiLevel = getDailyAirQualityLevelText(context, date);
            String daySuggestion = getDailySuggestionByLevel(aqiLevel);
            sb.append(dateLabel).append("AQI指数").append(aqiValue).append("，").append(aqiLevel).append("。").append(daySuggestion);
        }
        return Optional.of(sb.toString());
    }

    private String getAirQualityValue(CardContext context) {
        CaiyunWeatherResult result = context.getCaiyunResult();
        CaiyunRealtimeDto realtime = result != null ? result.getRealtime() : null;
        CaiyunAirQualityDto aq = realtime != null ? realtime.getAirQuality() : null;
        if (aq == null || aq.getAqi() == null || aq.getAqi().getChn() == null) return "—";
        return String.valueOf(aq.getAqi().getChn());
    }

    private String getAirQualityText(CardContext context) {
        CaiyunWeatherResult result = context.getCaiyunResult();
        CaiyunRealtimeDto realtime = result != null ? result.getRealtime() : null;
        CaiyunAirQualityDto aq = realtime != null ? realtime.getAirQuality() : null;
        if (aq == null || aq.getDescription() == null || aq.getDescription().getChn() == null || aq.getDescription().getChn().isBlank()) {
            return "—";
        }
        return aq.getDescription().getChn();
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

    private String getSuggestion(CardContext context) {
        CaiyunWeatherResult result = context.getCaiyunResult();
        CaiyunRealtimeDto realtime = result != null ? result.getRealtime() : null;
        CaiyunAirQualityDto aq = realtime != null ? realtime.getAirQuality() : null;
        if (aq == null) return "暂无空气质量数据。";
        CaiyunAqiDescriptionDto description = aq.getDescription();
        String airQualityText = "—";
        if (description != null && description.getChn() != null && !description.getChn().isBlank()) {
            airQualityText = description.getChn();
        }
        return suggestionByQuality(airQualityText);
    }

    /** 从逐日数据中按日期取 AQI 数值（优先 avg.chn） */
    private String getDailyAirQualityValue(CardContext context, LocalDate date) {
        CaiyunDailyAqiItemDto item = findDailyAqiItemByDate(context, date);
        if (item == null) return "—";
        CaiyunAqiDto aqi = item.getAvg() != null ? item.getAvg() : item.getMax();
        if (aqi == null || aqi.getChn() == null) return "—";
        return String.valueOf(aqi.getChn());
    }

    /** 根据逐日 AQI 数值推导等级文案（优/良/轻度污染等） */
    private String getDailyAirQualityLevelText(CardContext context, LocalDate date) {
        CaiyunDailyAqiItemDto item = findDailyAqiItemByDate(context, date);
        if (item == null) return "—";
        CaiyunAqiDto aqi = item.getAvg() != null ? item.getAvg() : item.getMax();
        if (aqi == null || aqi.getChn() == null) return "—";
        return aqiToLevelText(aqi.getChn());
    }

    private static String getDailySuggestionByLevel(String airQualityLevelText) {
        return suggestionByQuality(airQualityLevelText);
    }

    private CaiyunDailyAqiItemDto findDailyAqiItemByDate(CardContext context, LocalDate date) {
        CaiyunWeatherResult result = context.getCaiyunResult();
        CaiyunDailyDto daily = result != null ? result.getDaily() : null;
        CaiyunDailyAirQualityDto dailyAq = daily != null ? daily.getAirQuality() : null;
        List<CaiyunDailyAqiItemDto> aqiList = dailyAq != null ? dailyAq.getAqi() : null;
        if (aqiList == null || aqiList.isEmpty()) return null;
        for (CaiyunDailyAqiItemDto item : aqiList) {
            if (item == null || item.getDate() == null) continue;
            LocalDate itemDate = parseApiDate(item.getDate());
            if (date.equals(itemDate)) return item;
        }
        return null;
    }

    private static LocalDate parseApiDate(String dateStr) {
        if (dateStr == null || dateStr.isBlank()) return LocalDate.now();
        try {
            String dateOnly = dateStr.contains("T") ? dateStr.substring(0, dateStr.indexOf('T')) : dateStr;
            return LocalDate.parse(dateOnly, DateTimeFormatter.ISO_LOCAL_DATE);
        } catch (DateTimeParseException e) {
            return LocalDate.now();
        }
    }

    /** 根据 AQI 数值返回等级描述（与 realtime description 语义一致） */
    private static String aqiToLevelText(int aqi) {
        if (aqi <= 50) return "优";
        if (aqi <= 100) return "良";
        if (aqi <= 150) return "轻度污染";
        if (aqi <= 200) return "中度污染";
        if (aqi <= 300) return "重度污染";
        return "严重污染";
    }

    /** 根据空气质量等级（优/良等）返回建议文案 */
    private static String suggestionByQuality(String airQualityText) {
        if (airQualityText == null || airQualityText.isBlank() || "—".equals(airQualityText)) {
            return "暂无空气质量建议。";
        }
        if (airQualityText.contains("优")) {
            return "空气质量令人满意，基本无空气污染，各类人群可正常活动。";
        }
        if (airQualityText.contains("良")) {
            return "空气质量可接受，某些污染物对极少数敏感人群有较弱影响，可正常活动。";
        }
        if (airQualityText.contains("轻度污染")) {
            return "易感人群应减少户外活动。";
        }
        if (airQualityText.contains("中度污染")) {
            return "儿童、老人及心脏病、呼吸系统疾病患者应减少长时间、高强度的户外锻炼。";
        }
        if (airQualityText.contains("重度污染") || airQualityText.contains("严重污染")) {
            return "儿童、老人及病人应停留在室内，一般人群避免户外活动。";
        }
        return "请关注空气质量变化。";
    }
}
