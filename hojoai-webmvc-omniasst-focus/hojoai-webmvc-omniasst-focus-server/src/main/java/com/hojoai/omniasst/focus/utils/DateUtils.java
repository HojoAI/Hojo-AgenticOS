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
package com.hojoai.omniasst.focus.utils;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;

/**
 * date util
 *
 * @author My
 * @version 1.0 2024/12/29
 * @since hojoai-webmvc-omniasst.focus 1.0.0
 */
public class DateUtils {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final String[] WEEKDAYS = {"星期一", "星期二", "星期三", "星期四", "星期五", "星期六", "星期日"};

    public static Long localDateTimeToLong(LocalDateTime localDateTime) {
        return localDateTime.atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli();
    }

    /**
     * 获取当前时间并格式化为 "yyyy-MM-dd HH:mm 星期几" 格式
     *
     * @return 格式化后的时间字符串，如：2026-01-16 14:30 星期四
     */
    public static String getCurrentDateTimeWithWeekday() {
        LocalDateTime now = LocalDateTime.now();
        String dateTimeStr = now.format(DATE_TIME_FORMATTER);
        String weekdayStr = getWeekdayName(now.getDayOfWeek().getValue());
        return dateTimeStr + " " + weekdayStr;
    }

    /**
     * 获取本周和下周的日期字符串
     * 从本周一开始，到下周日结束，共14天
     *
     * @return 本周和下周的日期字符串，格式如：2025-12-29 本周星期一\n2026-01-01 本周星期四（今天）\n...
     */
    public static String getCurrentAndNextWeekDates() {
        LocalDate today = LocalDate.now();
        // 获取本周一
        LocalDate thisMonday = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        // 获取下周一
        LocalDate nextMonday = thisMonday.plusWeeks(1);
        
        StringBuilder result = new StringBuilder();
        
        // 生成14天的日期（本周7天 + 下周7天）
        for (int i = 0; i < 14; i++) {
            LocalDate date = thisMonday.plusDays(i);
            String dateStr = date.format(DATE_FORMATTER);
            String weekdayStr = getWeekdayName(date.getDayOfWeek().getValue());
            
            // 判断是本周还是下周
            String weekPrefix = date.isBefore(nextMonday) ? "本周" : "下周";
            
            // 判断与今天的关系
            String daySuffix = getDaySuffix(date, today);
            
            result.append(dateStr).append(" ").append(weekPrefix).append(weekdayStr).append(daySuffix);
            
            // 如果不是最后一天，添加换行符
            if (i < 13) {
                result.append("\n");
            }
        }
        
        return result.toString();
    }

    /**
     * 根据星期几的数字获取对应的中文名称
     *
     * @param dayOfWeekValue 星期几的数字值（1-7，1表示星期一，7表示星期日）
     * @return 星期的中文名称
     */
    private static String getWeekdayName(int dayOfWeekValue) {
        // dayOfWeekValue: 1=星期一, 2=星期二, ..., 7=星期日
        return WEEKDAYS[dayOfWeekValue - 1];
    }

    /**
     * 获取日期与今天的关系后缀
     *
     * @param date  要判断的日期
     * @param today 今天的日期
     * @return 后缀字符串，如"（今天）"、"（明天）"等，如果没有特殊关系则返回空字符串
     */
    private static String getDaySuffix(LocalDate date, LocalDate today) {
        long daysDiff = java.time.temporal.ChronoUnit.DAYS.between(today, date);

        return switch ((int) daysDiff) {
            case 0 -> "（今天）";
            case 1 -> "（明天）";
            case 2 -> "（后天）";
            case 3 -> "（大后天）";
            default -> "";
        };
    }
}

