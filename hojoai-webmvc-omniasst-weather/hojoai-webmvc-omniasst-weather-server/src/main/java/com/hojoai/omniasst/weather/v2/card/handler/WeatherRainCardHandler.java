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
import com.hojoai.omniasst.weather.v2.entity.CaiyunPrecipitationDto;
import com.hojoai.omniasst.weather.v2.entity.CaiyunPrecipitationLocalDto;
import com.hojoai.omniasst.weather.v2.entity.CaiyunRealtimeDto;
import com.hojoai.omniasst.weather.v2.entity.CaiyunWeatherResult;
import com.hojoai.omniasst.weather.v2.slot.WeatherCardType;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * WeatherRain 卡片 data 映射（某地某天降雨：location、date、percent、condition、chartData、suggestion）
 *
 * @author wangjian
 * @since 2026-02-02
 */
@Component
public class WeatherRainCardHandler implements WeatherCardHandler {

    private static final String[] CHART_X = {"00:00", "03:00", "06:00", "09:00", "12:00", "15:00", "18:00", "21:00"};

    @Override
    public WeatherCardType getCardType() {
        return WeatherCardType.WeatherRain;
    }

    @Override
    public Map<String, Object> buildCardData(CardContext context) {
        Map<String, Object> data = new LinkedHashMap<>();
        CaiyunWeatherResult result = context.getCaiyunResult();
        CaiyunRealtimeDto realtime = result != null ? result.getRealtime() : null;
        CaiyunDailyDto daily = result != null ? result.getDaily() : null;

        String location = context.getDisplayName();
        data.put("location", location);

        LocalDate targetDate = getTargetDate(context);
        String dateLabel = formatDateLabel(targetDate);
        data.put("date", dateLabel);

        int percent = 0;
        CaiyunDailyPrecipitationDto precip = null;
        if (daily != null && daily.getPrecipitation() != null && !daily.getPrecipitation().isEmpty()) {
            precip = daily.getPrecipitation().get(0);
            if (precip.getProbability() != null) {
                percent = (int) Math.round(precip.getProbability() * 100);
                percent = Math.min(100, Math.max(0, percent));
            }
        }
        data.put("percent", percent);

        String condition = conditionFromPercent(percent, realtime);
        data.put("condition", condition);

        Map<String, Object> chartData = buildChartData(percent);
        data.put("chartData", chartData);

        String suggestionDesc = buildSuggestionDescription(location, dateLabel, percent);
        Map<String, Object> suggestion = new LinkedHashMap<>();
        suggestion.put("title", "出行建议:");
        suggestion.put("description", suggestionDesc);
        data.put("suggestion", suggestion);

        return data;
    }

    @Override
    public Optional<String> buildBubbleText(CardContext context) {
        String location = context.getDisplayName();
        LocalDate targetDate = getTargetDate(context);
        String dateLabel = formatDateLabel(targetDate);
        int percent = getRainPercent(context);
        String suggestionDesc = buildSuggestionDescription(location, dateLabel, percent);
        return Optional.of(suggestionDesc);
    }

    /** 取目标日期：优先 context 解析出的单日，否则今天 */
    private static LocalDate getTargetDate(CardContext context) {
        List<LocalDate> parsed = context.getParsedDates();
        if (parsed != null && !parsed.isEmpty()) {
            return parsed.get(0);
        }
        return LocalDate.now();
    }

    /** 今天/明天/后天 或 M月d日 */
    private static String formatDateLabel(LocalDate date) {
        if (date == null) return "今天";
        LocalDate today = LocalDate.now();
        if (date.equals(today)) return "今天";
        if (date.equals(today.plusDays(1))) return "明天";
        if (date.equals(today.plusDays(2))) return "后天";
        return date.getMonthValue() + "月" + date.getDayOfMonth() + "日";
    }

    /** 根据概率和实时强度得到降雨状况文案 */
    private static String conditionFromPercent(int percent, CaiyunRealtimeDto realtime) {
        if (percent <= 0) return "无降雨";
        double intensity = 0;
        if (realtime != null && realtime.getPrecipitation() != null) {
            CaiyunPrecipitationDto p = realtime.getPrecipitation();
            CaiyunPrecipitationLocalDto local = p != null ? p.getLocal() : null;
            if (local != null && local.getIntensity() != null) {
                intensity = local.getIntensity();
            }
        }
        if (percent >= 70 || intensity >= 2.5) return "有强降雨";
        if (percent >= 40 || intensity >= 0.5) return "有降雨";
        return "有小雨";
    }

    /** 根据当日降雨概率生成模拟逐时 chartData（无逐时 API 时用概率分布近似） */
    private static Map<String, Object> buildChartData(int percent) {
        Map<String, Object> chart = new LinkedHashMap<>();
        List<String> x = List.of(CHART_X);
        chart.put("x", x);

        int[] y = syntheticHourlyFromPercent(percent);
        List<Integer> yList = new ArrayList<>();
        for (int v : y) yList.add(v);
        chart.put("y", yList);

        int maxVal = 0;
        for (int v : y) if (v > maxVal) maxVal = v;
        List<String> highlight = new ArrayList<>();
        for (int i = 0; i < y.length; i++) {
            if (maxVal > 0 && y[i] == maxVal) highlight.add(CHART_X[i]);
        }
        chart.put("highlight", highlight);

        return chart;
    }

    /** 用概率生成 8 个点的模拟值（中间偏高，形成单峰） */
    private static int[] syntheticHourlyFromPercent(int percent) {
        if (percent <= 0) return new int[]{0, 0, 0, 0, 0, 0, 0, 0};
        // 单峰：索引 3、4 最高，向两边递减
        double[] weights = {0.3, 0.5, 0.7, 1.0, 0.95, 0.75, 0.55, 0.35};
        int[] y = new int[8];
        for (int i = 0; i < 8; i++) {
            y[i] = (int) Math.round(percent * weights[i]);
            y[i] = Math.min(100, Math.max(0, y[i]));
        }
        return y;
    }

    private static String buildSuggestionDescription(String location, String dateLabel, int percent) {
        if (percent <= 0) {
            return String.format("%s%s没有降雨，天气晴朗，可放心出行。", location, dateLabel);
        }
        return String.format("%s%s将有持续降雨，请务必携带雨具，并注意交通安全。", location, dateLabel);
    }

    private static int getRainPercent(CardContext context) {
        CaiyunWeatherResult result = context.getCaiyunResult();
        CaiyunDailyDto daily = result != null ? result.getDaily() : null;
        if (daily == null || daily.getPrecipitation() == null || daily.getPrecipitation().isEmpty()) {
            return 0;
        }
        CaiyunDailyPrecipitationDto p = daily.getPrecipitation().get(0);
        if (p.getProbability() == null) return 0;
        int percent = (int) Math.round(p.getProbability() * 100);
        return Math.min(100, Math.max(0, percent));
    }
}
