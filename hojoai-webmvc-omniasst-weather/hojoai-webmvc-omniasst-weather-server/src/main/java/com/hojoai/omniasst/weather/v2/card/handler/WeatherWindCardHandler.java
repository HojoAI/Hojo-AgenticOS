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
import com.hojoai.omniasst.weather.v2.entity.CaiyunRealtimeDto;
import com.hojoai.omniasst.weather.v2.entity.CaiyunWeatherResult;
import com.hojoai.omniasst.weather.v2.entity.CaiyunWindDto;
import com.hojoai.omniasst.weather.v2.slot.WeatherCardType;
import com.hojoai.omniasst.weather.v2.support.WindFormatter;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * WeatherWind 卡片 data 映射（某地某天风力：name、date、suggestion、wind、windLevel）
 *
 * @author wangjian
 * @since 2026-02-02
 */
@Component
public class WeatherWindCardHandler implements WeatherCardHandler {

    private final WindFormatter windFormatter;

    public WeatherWindCardHandler(WindFormatter windFormatter) {
        this.windFormatter = windFormatter;
    }

    @Override
    public WeatherCardType getCardType() {
        return WeatherCardType.WeatherWind;
    }

    @Override
    public Map<String, Object> buildCardData(CardContext context) {
        Map<String, Object> data = new LinkedHashMap<>();
        CaiyunWeatherResult result = context.getCaiyunResult();
        CaiyunRealtimeDto realtime = result != null ? result.getRealtime() : null;
        CaiyunWindDto wind = realtime != null ? realtime.getWind() : null;

        String name = context.getDisplayName();
        data.put("name", name);

        LocalDate targetDate = getTargetDate(context);
        String dateLabel = formatDateLabel(targetDate);
        data.put("date", dateLabel);

        String windDir = windFormatter.formatDirection(wind);
        String windLevel = windFormatter.formatWindLevel(wind);
        int level = parseWindLevel(windLevel);
        String suggestionBody = buildSuggestionBody(level);
        String suggestion = dateLabel + suggestionBody;
        data.put("suggestion", suggestion);
        data.put("wind", windDir);
        data.put("windLevel", windLevel);

        return data;
    }

    @Override
    public Optional<String> buildBubbleText(CardContext context) {
        String name = context.getDisplayName();
        LocalDate targetDate = getTargetDate(context);
        String dateLabel = formatDateLabel(targetDate);
        String windDir = getWindDirection(context);
        String windLevelStr = getWindLevelStr(context);
        int level = getWindLevel(context);
        String suggestionBody = buildSuggestionBody(level);
        // 与 WeatherDailyFallback 一致：地名 + 日期 + 关键数据 + 建议
        String bubble = String.format("%s%s风力%s级，%s。%s", name, dateLabel, windLevelStr, windDir, suggestionBody);
        return Optional.of(bubble);
    }

    private static LocalDate getTargetDate(CardContext context) {
        List<LocalDate> parsed = context.getParsedDates();
        if (parsed != null && !parsed.isEmpty()) {
            return parsed.get(0);
        }
        return LocalDate.now();
    }

    private String getWindDirection(CardContext context) {
        CaiyunWeatherResult result = context.getCaiyunResult();
        CaiyunRealtimeDto realtime = result != null ? result.getRealtime() : null;
        CaiyunWindDto wind = realtime != null ? realtime.getWind() : null;
        return windFormatter.formatDirection(wind);
    }

    private String getWindLevelStr(CardContext context) {
        CaiyunWeatherResult result = context.getCaiyunResult();
        CaiyunRealtimeDto realtime = result != null ? result.getRealtime() : null;
        CaiyunWindDto wind = realtime != null ? realtime.getWind() : null;
        String s = windFormatter.formatWindLevel(wind);
        return (s != null && !"—".equals(s)) ? s : "0";
    }

    private static String formatDateLabel(LocalDate date) {
        if (date == null) return "今天";
        LocalDate today = LocalDate.now();
        if (date.equals(today)) return "今天";
        if (date.equals(today.plusDays(1))) return "明天";
        if (date.equals(today.plusDays(2))) return "后天";
        return date.getMonthValue() + "月" + date.getDayOfMonth() + "日";
    }

    /**
     * 按蒲福风级给出更丰满的出行与穿衣建议。
     */
    private static String buildSuggestionBody(int windLevel) {
        if (windLevel >= 8) {
            return "风力很大，建议尽量减少外出；若需出门请穿防风外套、注意固定好围巾与帽子。";
        }
        if (windLevel >= 6) {
            return "强风天气，体感偏冷，建议穿夹克、风衣等防风衣物，户外活动注意安全，老人与儿童尽量少在风口停留。";
        }
        if (windLevel >= 4) {
            return "风力较大，早晚体感偏凉，建议穿着夹克、薄毛衣等，外出可备一件防风外套，适宜正常出行。";
        }
        if (windLevel >= 2) {
            return "风力适中，体感舒适，适宜户外活动与出行，可穿长袖或薄外套以备早晚温差。";
        }
        if (windLevel >= 1) {
            return "轻风，天气宜人，适宜散步、骑行等户外活动，可正常出行。";
        }
        return "静风或微风，天气平稳，非常适宜户外活动与出行。";
    }

    private int getWindLevel(CardContext context) {
        CaiyunWeatherResult result = context.getCaiyunResult();
        CaiyunRealtimeDto realtime = result != null ? result.getRealtime() : null;
        CaiyunWindDto wind = realtime != null ? realtime.getWind() : null;
        String levelStr = windFormatter.formatWindLevel(wind);
        return parseWindLevel(levelStr);
    }

    private static int parseWindLevel(String windLevelStr) {
        if (windLevelStr == null || "—".equals(windLevelStr)) return 0;
        try {
            return Integer.parseInt(windLevelStr.trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
