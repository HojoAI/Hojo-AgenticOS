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
import com.hojoai.omniasst.weather.v2.entity.CaiyunDailyValueDto;
import com.hojoai.omniasst.weather.v2.entity.CaiyunRealtimeDto;
import com.hojoai.omniasst.weather.v2.entity.CaiyunWeatherResult;
import com.hojoai.omniasst.weather.v2.slot.WeatherCardType;
import com.hojoai.omniasst.weather.v2.support.SkyconMapper;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * WeatherDailyFallback 卡片 data 映射（当日温度 + 穿衣建议）
 *
 * @author wangjian
 * @since 2026-02-02
 */
@Component
public class WeatherDailyFallbackCardHandler implements WeatherCardHandler {

    private final SkyconMapper skyconMapper;

    public WeatherDailyFallbackCardHandler(SkyconMapper skyconMapper) {
        this.skyconMapper = skyconMapper;
    }

    /**
     * 根据当前温度与天气现象，按规则生成穿衣建议（避免固定或不当表述）。
     * 规则：先按温度区间给出基础建议，再按天气类型追加或微调。
     */
    private String generateDressingTip(Double temperature, String skycon) {
        int temp = temperature != null ? (int) Math.round(temperature) : 20;
        boolean isRain = skycon != null && skycon.contains("RAIN");
        boolean isHeavyRain = "HEAVY_RAIN".equals(skycon) || "STORM_RAIN".equals(skycon);
        boolean isSnow = skycon != null && skycon.contains("SNOW");
        boolean isHazeOrFog = skycon != null && (skycon.contains("HAZE") || "FOG".equals(skycon) || "DUST".equals(skycon) || "SAND".equals(skycon));
        boolean isClearSunny = "CLEAR_DAY".equals(skycon);
        boolean isWindy = "WIND".equals(skycon);

        String base;
        if (temp < -5) {
            base = "天气严寒，建议穿羽绒服、厚棉衣，注意保暖";
        } else if (temp < 5) {
            base = "天气寒冷，建议厚外套搭配毛衣";
        } else if (temp < 15) {
            base = "早晚较凉，建议薄外套或长袖";
        } else if (temp < 25) {
            base = "体感舒适，适宜长袖或短袖";
        } else if (temp < 32) {
            base = "天气较热，适宜短袖，注意防晒";
        } else {
            base = "天气炎热，注意防暑，适宜轻薄透气衣物";
        }

        if (isSnow) {
            return "有降雪，注意防寒保暖，外出穿防滑鞋";
        }
        if (isHeavyRain) {
            return base + "；有强降水，出门请带伞并注意防风防雨";
        }
        if (isRain) {
            return base + "；有降水，出门请带伞";
        }
        if (isHazeOrFog) {
            return base + "；空气质量较差，外出可佩戴口罩";
        }
        if (isWindy) {
            return base + "；风力较大，可加一件防风外套";
        }
        if (isClearSunny && temp >= 25) {
            return base + "，户外注意防晒";
        }
        return base;
    }

    /**
     * 根据日期得到展示用日标签：今天 / 明天 / 后天 / x月x日
     */
    private static String formatDateLabel(LocalDate date) {
        if (date == null) return "今天";
        LocalDate today = LocalDate.now();
        if (date.equals(today)) return "今天";
        if (date.equals(today.plusDays(1))) return "明天";
        if (date.equals(today.plusDays(2))) return "后天";
        return date.getMonthValue() + "月" + date.getDayOfMonth() + "日";
    }

    /**
     * 解析目标日期：优先从 context 解析出的日期取首日，否则从当日温度数据的 date 解析，否则今天。
     */
    private static LocalDate resolveTargetDate(CardContext context, CaiyunDailyDto daily) {
        List<LocalDate> parsed = context != null ? context.getParsedDates() : null;
        if (parsed != null && !parsed.isEmpty()) {
            return parsed.get(0);
        }
        if (daily != null && daily.getTemperature() != null && !daily.getTemperature().isEmpty()) {
            String dateStr = daily.getTemperature().get(0).getDate();
            if (dateStr != null && !dateStr.isBlank()) {
                try {
                    return LocalDate.parse(dateStr.substring(0, Math.min(10, dateStr.length())), DateTimeFormatter.ISO_LOCAL_DATE);
                } catch (DateTimeParseException ignored) {
                }
            }
        }
        return LocalDate.now();
    }

    @Override
    public WeatherCardType getCardType() {
        return WeatherCardType.WeatherDailyFallback;
    }

    @Override
    public Map<String, Object> buildCardData(CardContext context) {
        Map<String, Object> data = new LinkedHashMap<>();
        CaiyunWeatherResult result = context.getCaiyunResult();
        CaiyunRealtimeDto realtime = result != null ? result.getRealtime() : null;
        CaiyunDailyDto daily = result != null ? result.getDaily() : null;

        data.put("name", context.getDisplayName());
        String skycon = realtime != null ? realtime.getSkycon() : null;
        data.put("condition", skyconMapper.toChinese(skycon));
        data.put("icon", skycon != null && !skycon.isBlank() ? skycon.toUpperCase() : "");

        if (realtime != null && realtime.getTemperature() != null) {
            data.put("currentTemp", (int) Math.round(realtime.getTemperature()) + "°");
        } else {
            data.put("currentTemp", "—");
        }
        Double max = null, min = null;
        if (daily != null && daily.getTemperature() != null && !daily.getTemperature().isEmpty()) {
            CaiyunDailyValueDto t = daily.getTemperature().get(0);
            max = t.getMax();
            min = t.getMin();
        }
        data.put("maxTemp", max != null ? "最高 " + (int) Math.round(max) + "°" : "—");
        data.put("minTemp", min != null ? "最低 " + (int) Math.round(min) + "°" : "—");

        Double temp = realtime != null ? realtime.getTemperature() : null;
        String dressingTip = generateDressingTip(temp, skycon);
        data.put("dressingTip", dressingTip);
        return data;
    }

    @Override
    public Optional<String> buildBubbleText(CardContext context) {
        CaiyunWeatherResult result = context.getCaiyunResult();
        CaiyunRealtimeDto realtime = result != null ? result.getRealtime() : null;
        CaiyunDailyDto daily = result != null ? result.getDaily() : null;

        String name = context.getDisplayName();
        String condition = skyconMapper.toChinese(realtime != null ? realtime.getSkycon() : null);
        if (condition == null || condition.isBlank()) condition = "未知";

        int currentTemp = realtime != null && realtime.getTemperature() != null
                ? (int) Math.round(realtime.getTemperature()) : 0;
        Double max = null, min = null;
        if (daily != null && daily.getTemperature() != null && !daily.getTemperature().isEmpty()) {
            CaiyunDailyValueDto t = daily.getTemperature().get(0);
            max = t.getMax();
            min = t.getMin();
        }
        int maxTemp = max != null ? (int) Math.round(max) : currentTemp;
        int minTemp = min != null ? (int) Math.round(min) : currentTemp;

        Double temp = realtime != null ? realtime.getTemperature() : null;
        String skyconForDressing = realtime != null ? realtime.getSkycon() : null;
        String dressingTip = generateDressingTip(temp, skyconForDressing);

        LocalDate targetDate = resolveTargetDate(context, daily);
        String dateLabel = formatDateLabel(targetDate);

        String bubble = String.format("%s当前温度%d度，%s。%s最高温度%d度，最低%d度。穿衣建议：%s。",
                name, currentTemp, condition, dateLabel, maxTemp, minTemp, dressingTip);
        return Optional.of(bubble);
    }
}
