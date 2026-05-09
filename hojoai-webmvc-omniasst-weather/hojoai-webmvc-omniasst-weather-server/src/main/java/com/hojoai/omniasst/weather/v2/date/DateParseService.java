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
package com.hojoai.omniasst.weather.v2.date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.lang.reflect.Method;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 基于 xk-time 的中文自然语言日期解析：从 query 中解析出具体日期或日期区间，
 * 单日直接返回，区间则返回区间内所有日期。
 * xk-time 内置中文时间 NLP（源自 Time-NLP），支持「明天」「下周三」「3月5日」等。
 *
 * @author wangjian
 * @since 2026-02-03
 */
@Service
public class DateParseService {

    private static final Logger logger = LoggerFactory.getLogger(DateParseService.class);

    private static final DateTimeFormatter DATE_ONLY = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private static final String XK_TIME_NLP_UTIL = "com.xkzhangsan.time.nlp.TimeNLPUtil";
    private static final String XK_TIME_NLP_UTIL_GITHUB = "com.github.xkzhangsan.time.nlp.TimeNLPUtil";

    /** 未来N天：匹配「未来3天」「未来 7 天」等，N 限制在 1～15；返回从明天起的 N 天 */
    private static final Pattern FUTURE_N_DAYS = Pattern.compile("未来\\s*(\\d+)\\s*天");

    /** 未来几天/这几天：匹配「未来几天」「这几天」「未来这几天」等，返回从今天起的 3 天 */
    private static final Pattern THESE_OR_FEW_DAYS = Pattern.compile("(未来\\s*)?(这几|几)\\s*天");

    /** 日期区间：匹配「3月1日到3月5日」「2月1日至2月7日」等，分组为 (左半部分, 到/至, 右半部分) */
    private static final Pattern RANGE_TO = Pattern.compile("(.+?)(到|至)(.+)");

    /** 「这几天」「未来几天」默认返回的天数（从今天起） */
    private static final int DEFAULT_FEW_DAYS = 3;

    /** 「下周」/「一周」整周：匹配「下周」且未指定星期几（非「下周一」～「下周日」），或匹配「一周」，返回下周一到下周日 */
    private static final Pattern NEXT_WEEK_FULL = Pattern.compile("(?:下周(?!一|二|三|四|五|六|日)|一周)");

    /** 「本周」「这周」：匹配「本周」「这周」等，解析为从今天起 3 天内日期 */
    private static final Pattern THIS_WEEK = Pattern.compile("本\\s*周|这\\s*周");

    public DateParseService() {
    }

    /** 「下周」整周：返回下周一到下周日共 7 天（ISO 周：周一为第一天） */
    private List<LocalDate> parseNextWeekFull(String query) {
        if (query == null || !NEXT_WEEK_FULL.matcher(query).find()) return List.of();
        LocalDate today = LocalDate.now();
        int dayOfWeek = today.getDayOfWeek().getValue(); // 1=Monday, 7=Sunday
        long daysToNextMonday = dayOfWeek == 1 ? 7 : (8 - dayOfWeek);
        LocalDate nextMonday = today.plusDays(daysToNextMonday);
        LocalDate nextSunday = nextMonday.plusDays(6);
        return expandRange(nextMonday, nextSunday);
    }

    /** 含「现在」时返回今天的日期（在其它解析无结果时使用） */
    private List<LocalDate> parseNowAsToday(String query) {
        if (query != null && query.contains("现在")) {
            return List.of(LocalDate.now());
        }
        return List.of();
    }

    /** 「未来N天」：返回从明天起的 N 天（不含今天） */
    private List<LocalDate> parseFutureNDays(String query) {
        if (query == null) return List.of();
        Matcher m = FUTURE_N_DAYS.matcher(query);
        if (!m.find()) return List.of();
        int n;
        try {
            n = Integer.parseInt(m.group(1));
        } catch (NumberFormatException e) {
            return List.of();
        }
        if (n < 1 || n > 15) return List.of();
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        List<LocalDate> result = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            result.add(tomorrow.plusDays(i));
        }
        return result;
    }

    /** 「未来几天」「这几天」：返回从今天起的 3 天（今天、明天、后天） */
    private List<LocalDate> parseTheseOrFewDays(String query) {
        if (query == null) return List.of();
        if (!THESE_OR_FEW_DAYS.matcher(query).find()) return List.of();
        LocalDate today = LocalDate.now();
        List<LocalDate> result = new ArrayList<>();
        for (int i = 0; i < DEFAULT_FEW_DAYS; i++) {
            result.add(today.plusDays(i));
        }
        return result;
    }

    /** 「本周」「这周」：正则匹配后返回从今天起 3 天内日期（今天、明天、后天），逻辑同 parseTheseOrFewDays */
    private List<LocalDate> parseThisWeekWithinThreeDays(String query) {
        if (query == null) return List.of();
        if (!THIS_WEEK.matcher(query).find()) return List.of();
        LocalDate today = LocalDate.now();
        List<LocalDate> result = new ArrayList<>();
        for (int i = 0; i < DEFAULT_FEW_DAYS; i++) {
            result.add(today.plusDays(i));
        }
        return result;
    }

    /**
     * 从 query 中解析日期：单日返回该日，区间返回区间内全部日期（含首尾）。
     * 直接支持中文，如「明天」「下周三」「北京明天天气」「3月1日到3月5日」。
     *
     * @param query 用户输入
     * @return 解析出的日期列表，无结果时返回空列表
     */
    public List<LocalDate> parseDates(String query) {
        if (query == null || query.isBlank()) {
            return List.of();
        }
        List<LocalDate> localDates = parseByXkTime(query);
        // 无结果或仅一个日期但 query 含「到/至」时，尝试按区间解析（如「3月1日到3月5日」）
        if (localDates.isEmpty() || (localDates.size() == 1 && (query.contains("到") || query.contains("至")))) {
            List<LocalDate> range = parseRangeFallback(query);
            if (!range.isEmpty()) localDates = range;
        }
        if (localDates.isEmpty()) {
            List<LocalDate> nowFallback = parseNowAsToday(query);
            if (!nowFallback.isEmpty()) return nowFallback;
            List<LocalDate> nextWeekFull = parseNextWeekFull(query);
            if (!nextWeekFull.isEmpty()) return nextWeekFull;
            List<LocalDate> futureN = parseFutureNDays(query);
            if (!futureN.isEmpty()) return futureN;
            List<LocalDate> theseDays = parseTheseOrFewDays(query);
            if (!theseDays.isEmpty()) return theseDays;
            // 未解析到任何日期时默认按「今天」处理，例如「获取空气质量信息」等无日期 query
            return List.of(LocalDate.now());
        }
        localDates = localDates.stream().distinct().sorted().collect(Collectors.toList());
        // 「下周」整周（未指定星期几）：xk-time 可能只返回周日等单日，统一展开为下周一～下周日
        if (localDates.size() == 1 && NEXT_WEEK_FULL.matcher(query).find()) {
            List<LocalDate> nextWeekFull = parseNextWeekFull(query);
            if (!nextWeekFull.isEmpty()) return nextWeekFull;
        }
        // query 带「本周」「这周」时，按 3 天内日期解析（今天、明天、后天）
        if (localDates.size() == 1 && THIS_WEEK.matcher(query).find()) {
            List<LocalDate> thisWeekDays = parseThisWeekWithinThreeDays(query);
            logger.info("v2 获取 thisWeekDays: query={}, thisWeekDays={}", query, thisWeekDays);
            if (!thisWeekDays.isEmpty()) return thisWeekDays;
        }
        if (localDates.size() == 1) {
            return localDates;
        }
        LocalDate start = localDates.get(0);
        LocalDate end = localDates.get(localDates.size() - 1);
        return expandRange(start, end);
    }

    /**
     * 使用 xk-time 解析：单日会返回一个 TimeNLP（getTime/getTimeNormFormat），区间可能返回多个 TimeNLP。
     * 注意 xk-time 的 TimeNLP 没有 endTime/end 字段，区间需依赖多条目或下方 parseRangeFallback。
     */
    private List<LocalDate> parseByXkTime(String query) {
        List<LocalDate> localDates = new ArrayList<>();
        try {
            Class<?> utilClass = null;
            try {
                utilClass = Class.forName(XK_TIME_NLP_UTIL);
            } catch (ClassNotFoundException e) {
                utilClass = Class.forName(XK_TIME_NLP_UTIL_GITHUB);
            }
            Method parseMethod = utilClass.getMethod("parse", String.class);
            Object result = parseMethod.invoke(null, query);
            if (result == null || !(result instanceof List)) {
                return localDates;
            }
            List<?> entries = (List<?>) result;
            for (Object entry : entries) {
                if (entry == null) continue;
                // xk-time TimeNLP: getTime() 为 Date，getTimeNormFormat() 为 "yyyy-MM-dd HH:mm:ss"，getTimeNorm() 为 "yyyy年MM月dd日..."
                String timeStr = getStringProp(entry, "TimeNormFormat", "time", "TimeNorm");
                if (timeStr != null) {
                    LocalDate start = parseTimeToLocalDate(timeStr);
                    if (start != null) localDates.add(start);
                }
                String endTime = getStringProp(entry, "endTime", "end");
                if (endTime != null && !endTime.isBlank()) {
                    LocalDate end = parseTimeToLocalDate(endTime);
                    if (end != null) localDates.add(end);
                }
            }
        } catch (Exception e) {
            logger.warn("xk-time 解析日期失败: query={}", query, e);
        }
        return localDates;
    }

    /**
     * 区间兜底：匹配「X到Y」「X至Y」，分别解析左右两部分，若得到两个日期则展开为区间内全部日期。
     */
    private List<LocalDate> parseRangeFallback(String query) {
        if (query == null || query.isBlank()) return List.of();
        Matcher m = RANGE_TO.matcher(query.trim());
        if (!m.matches()) return List.of();
        String left = m.group(1).trim();
        String right = m.group(3).trim();
        if (left.isEmpty() || right.isEmpty()) return List.of();
        List<LocalDate> fromLeft = parseByXkTime(left);
        List<LocalDate> fromRight = parseByXkTime(right);
        LocalDate start = fromLeft.isEmpty() ? null : fromLeft.stream().min(LocalDate::compareTo).orElse(null);
        LocalDate end = fromRight.isEmpty() ? null : fromRight.stream().max(LocalDate::compareTo).orElse(null);
        if (start == null || end == null) return List.of();
        return expandRange(start, end);
    }

    private static String getStringProp(Object obj, String... methodNames) {
        for (String name : methodNames) {
            try {
                String methodName = "get" + name.substring(0, 1).toUpperCase() + name.substring(1);
                Method m = obj.getClass().getMethod(methodName);
                Object v = m.invoke(obj);
                if (v instanceof String) return (String) v;
                if (v instanceof Date) return ((Date) v).toInstant().atZone(ZoneId.systemDefault()).toLocalDate().toString();
                if (v instanceof Long) return Instant.ofEpochMilli((Long) v).atZone(ZoneId.systemDefault()).toLocalDate().toString();
            } catch (Exception ignored) {
            }
        }
        return null;
    }

    /** 中文日期片段：年、月、日 */
    private static final Pattern CN_DATE_PART = Pattern.compile("(\\d{4})年(\\d{1,2})月(\\d{1,2})日");

    /**
     * 将时间字符串解析为 LocalDate。支持：
     * yyyy-MM-dd、yyyy-MM-dd HH:mm:ss、yyyy年M月d日。
     */
    private LocalDate parseTimeToLocalDate(String timeStr) {
        if (timeStr == null || timeStr.isBlank()) return null;
        String s = timeStr.trim();
        try {
            if (s.length() >= 10) {
                if (s.contains("年") && s.contains("月")) {
                    Matcher matcher = CN_DATE_PART.matcher(s);
                    if (matcher.find()) {
                        int y = Integer.parseInt(matcher.group(1));
                        int m = Integer.parseInt(matcher.group(2));
                        int d = Integer.parseInt(matcher.group(3));
                        return LocalDate.of(y, m, d);
                    }
                }
                String prefix = s.substring(0, 10);
                if (prefix.indexOf('-') >= 0) {
                    return LocalDate.parse(prefix, DATE_ONLY);
                }
            }
        } catch (DateTimeParseException e) {
            logger.debug("解析时间字符串失败: {}", timeStr, e);
        } catch (Exception e) {
            logger.debug("解析时间字符串异常: {}", timeStr, e);
        }
        return null;
    }

    private List<LocalDate> expandRange(LocalDate start, LocalDate end) {
        if (start == null || end == null || end.isBefore(start)) {
            return start != null ? List.of(start) : List.of();
        }
        List<LocalDate> result = new ArrayList<>();
        for (LocalDate d = start; !d.isAfter(end); d = d.plusDays(1)) {
            result.add(d);
        }
        return result;
    }
}
