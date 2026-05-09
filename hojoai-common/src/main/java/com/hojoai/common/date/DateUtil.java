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
package com.hojoai.common.date;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * The common class for the date util.
 *
 * @author kevanwong
 * @version 1.0, 2017/10/21
 * @since hojoai-common 1.0.0
 */
public class DateUtil {

    /** 日期格式：仅年份，如 yyyy */
    private static final String PATTERN_AS_YEAR_ZH = "yyyy";
    /** 日期格式：年-月，如 yyyy-MM */
    private static final String PATTERN_AS_MONTH_ZH = "yyyy-MM";
    /** 日期格式：年月日，如 yyyy-MM-dd */
    private static final String PATTERN_AS_DAY_ZH = "yyyy-MM-dd";
    /** 日期格式：到秒，如 yyyy-MM-dd HH:mm:ss */
    private static final String PATTERN_AS_SECOND_ZH = "yyyy-MM-dd HH:mm:ss";
    /** 日期格式：到毫秒，如 yyyy-MM-dd HH:mm:ss.SSS */
    private static final String PATTERN_AS_MILLISECOND_ZH = "yyyy-MM-dd HH:mm:ss.SSS";

    /* Date Format */
    /**
     * 将日期格式化为「年」精度字符串（{@code yyyy}）。
     *
     * @param date 待格式化的日期，不可为 null
     * @return 格式化结果
     * @throws UnsupportedOperationException 当格式化失败时
     */
    public static String formatAsYear(Date date) {
        return formatAsPattern(date, PATTERN_AS_YEAR_ZH);
    }

    /**
     * 将日期格式化为「年-月」精度字符串（{@code yyyy-MM}）。
     *
     * @param date 待格式化的日期，不可为 null
     * @return 格式化结果
     * @throws UnsupportedOperationException 当格式化失败时
     */
    public static String formatAsMonth(Date date) {
        return formatAsPattern(date, PATTERN_AS_MONTH_ZH);
    }

    /**
     * 将日期格式化为「年月日」精度字符串（{@code yyyy-MM-dd}）。
     *
     * @param date 待格式化的日期，不可为 null
     * @return 格式化结果
     * @throws UnsupportedOperationException 当格式化失败时
     */
    public static String formatAsDay(Date date) {
        return formatAsPattern(date, PATTERN_AS_DAY_ZH);
    }

    /**
     * 将日期格式化为「到秒」字符串（{@code yyyy-MM-dd HH:mm:ss}）。
     *
     * @param date 待格式化的日期，不可为 null
     * @return 格式化结果
     * @throws UnsupportedOperationException 当格式化失败时
     */
    public static String formatAsSecond(Date date) {
        return formatAsPattern(date, PATTERN_AS_SECOND_ZH);
    }

    /**
     * 将日期格式化为「到毫秒」字符串（{@code yyyy-MM-dd HH:mm:ss.SSS}）。
     *
     * @param date 待格式化的日期，不可为 null
     * @return 格式化结果
     * @throws UnsupportedOperationException 当格式化失败时
     */
    public static String formatAsMillisecond(Date date) {
        return formatAsPattern(date, PATTERN_AS_MILLISECOND_ZH);
    }

    /**
     * 按自定义 {@link SimpleDateFormat} 模式将日期格式化为字符串。
     *
     * @param date    待格式化的日期，不可为 null
     * @param pattern 格式模式，需符合 {@link SimpleDateFormat} 约定
     * @return 格式化结果
     * @throws UnsupportedOperationException 当模式非法或格式化失败时
     */
    public static String formatAsPattern(Date date, String pattern) {
        try {
            return new SimpleDateFormat(pattern).format(date);
        } catch (Exception e) {
            throw new UnsupportedOperationException("format as pattern exception", e);
        }
    }

    /* Date Parse */
    /**
     * 按「年」精度解析日期字符串（{@code yyyy}）。
     *
     * @param date 日期字符串
     * @return 解析得到的 {@link Date}
     * @throws UnsupportedOperationException 当字符串与模式不匹配或解析失败时
     */
    public static Date parseAsYear(String date) {
        return parseAsPattern(date, PATTERN_AS_YEAR_ZH);
    }

    /**
     * 按「年-月」精度解析日期字符串（{@code yyyy-MM}）。
     *
     * @param date 日期字符串
     * @return 解析得到的 {@link Date}
     * @throws UnsupportedOperationException 当字符串与模式不匹配或解析失败时
     */
    public static Date parseAsMonth(String date) {
        return parseAsPattern(date, PATTERN_AS_MONTH_ZH);
    }

    /**
     * 按「年月日」精度解析日期字符串（{@code yyyy-MM-dd}）。
     *
     * @param date 日期字符串
     * @return 解析得到的 {@link Date}
     * @throws UnsupportedOperationException 当字符串与模式不匹配或解析失败时
     */
    public static Date parseAsDay(String date) {
        return parseAsPattern(date, PATTERN_AS_DAY_ZH);
    }

    /**
     * 按「到秒」格式解析日期字符串（{@code yyyy-MM-dd HH:mm:ss}）。
     *
     * @param date 日期字符串
     * @return 解析得到的 {@link Date}
     * @throws UnsupportedOperationException 当字符串与模式不匹配或解析失败时
     */
    public static Date parseAsSecond(String date) {
        return parseAsPattern(date, PATTERN_AS_SECOND_ZH);
    }

    /**
     * 按「到毫秒」格式解析日期字符串（{@code yyyy-MM-dd HH:mm:ss.SSS}）。
     *
     * @param date 日期字符串
     * @return 解析得到的 {@link Date}
     * @throws UnsupportedOperationException 当字符串与模式不匹配或解析失败时
     */
    public static Date parseAsMillisecond(String date) {
        return parseAsPattern(date, PATTERN_AS_MILLISECOND_ZH);
    }

    /**
     * 按自定义 {@link SimpleDateFormat} 模式解析日期字符串。
     *
     * @param date    日期字符串
     * @param pattern 格式模式
     * @return 解析得到的 {@link Date}
     * @throws UnsupportedOperationException 当解析失败时
     */
    public static Date parseAsPattern(String date, String pattern) {
        try {
            return new SimpleDateFormat(pattern).parse(date);
        } catch (Exception e) {
            throw new UnsupportedOperationException("parse as pattern exception", e);
        }
    }

    /* Date Increase */
    /**
     * 在基准日期上增加指定年数。
     *
     * @param date  基准日期，不可为 null
     * @param value 增加的年数；为负时表示减少
     * @return 计算后的日期
     * @throws UnsupportedOperationException 当日历计算异常时
     */
    public static Date increaseAsYear(Date date, int value) {
        return increaseAsField(date, Calendar.YEAR, value);
    }

    /**
     * 在基准日期上增加指定月数。
     *
     * @param date  基准日期，不可为 null
     * @param value 增加的月数；为负时表示减少
     * @return 计算后的日期
     * @throws UnsupportedOperationException 当日历计算异常时
     */
    public static Date increaseAsMonth(Date date, int value) {
        return increaseAsField(date, Calendar.MONTH, value);
    }

    /**
     * 在基准日期上增加指定「年中周」数。
     *
     * @param date  基准日期，不可为 null
     * @param value 周数增量；为负时表示减少
     * @return 计算后的日期
     * @throws UnsupportedOperationException 当日历计算异常时
     */
    public static Date increaseAsWeekOfYear(Date date, int value) {
        return increaseAsField(date, Calendar.WEEK_OF_YEAR, value);
    }

    /**
     * 在基准日期上增加指定「月中周」数。
     *
     * @param date  基准日期，不可为 null
     * @param value 周数增量；为负时表示减少
     * @return 计算后的日期
     * @throws UnsupportedOperationException 当日历计算异常时
     */
    public static Date increaseAsWeekOfMonth(Date date, int value) {
        return increaseAsField(date, Calendar.WEEK_OF_MONTH, value);
    }

    /**
     * 在基准日期上增加指定「月中日」数。
     *
     * @param date  基准日期，不可为 null
     * @param value 天数增量；为负时表示减少
     * @return 计算后的日期
     * @throws UnsupportedOperationException 当日历计算异常时
     */
    public static Date increaseAsDayOfMonth(Date date, int value) {
        return increaseAsField(date, Calendar.DAY_OF_MONTH, value);
    }

    /**
     * 在基准日期上增加指定「年中日」数。
     *
     * @param date  基准日期，不可为 null
     * @param value 天数增量；为负时表示减少
     * @return 计算后的日期
     * @throws UnsupportedOperationException 当日历计算异常时
     */
    public static Date increaseAsDayOfYear(Date date, int value) {
        return increaseAsField(date, Calendar.DAY_OF_YEAR, value);
    }

    /**
     * 在基准日期上增加指定「周中日」数。
     *
     * @param date  基准日期，不可为 null
     * @param value 天数增量；为负时表示减少
     * @return 计算后的日期
     * @throws UnsupportedOperationException 当日历计算异常时
     */
    public static Date increaseAsDayOfWeek(Date date, int value) {
        return increaseAsField(date, Calendar.DAY_OF_WEEK, value);
    }

    /**
     * 在基准日期上增加指定「月中第几周的工作日偏移」相关字段（{@link Calendar#DAY_OF_WEEK_IN_MONTH}）。
     *
     * @param date  基准日期，不可为 null
     * @param value 增量；为负时表示减少
     * @return 计算后的日期
     * @throws UnsupportedOperationException 当日历计算异常时
     */
    public static Date increaseAsDayOfWeekInMonth(Date date, int value) {
        return increaseAsField(date, Calendar.DAY_OF_WEEK_IN_MONTH, value);
    }

    /**
     * 在基准日期上增加上午/下午字段（{@link Calendar#AM_PM}）。
     *
     * @param date  基准日期，不可为 null
     * @param value 增量；为负时表示减少
     * @return 计算后的日期
     * @throws UnsupportedOperationException 当日历计算异常时
     */
    public static Date increaseAsAmPm(Date date, int value) {
        return increaseAsField(date, Calendar.AM_PM, value);
    }

    /**
     * 在基准日期上增加 12 小时制「小时」字段（{@link Calendar#HOUR}）。
     *
     * @param date  基准日期，不可为 null
     * @param value 增量；为负时表示减少
     * @return 计算后的日期
     * @throws UnsupportedOperationException 当日历计算异常时
     */
    public static Date increaseAsHour(Date date, int value) {
        return increaseAsField(date, Calendar.HOUR, value);
    }

    /**
     * 在基准日期上增加 24 小时制小时数（{@link Calendar#HOUR_OF_DAY}）。
     *
     * @param date  基准日期，不可为 null
     * @param value 增量；为负时表示减少
     * @return 计算后的日期
     * @throws UnsupportedOperationException 当日历计算异常时
     */
    public static Date increaseAsHourOfDay(Date date, int value) {
        return increaseAsField(date, Calendar.HOUR_OF_DAY, value);
    }

    /**
     * 在基准日期上增加分钟数。
     *
     * @param date  基准日期，不可为 null
     * @param value 增量；为负时表示减少
     * @return 计算后的日期
     * @throws UnsupportedOperationException 当日历计算异常时
     */
    public static Date increaseAsMinute(Date date, int value) {
        return increaseAsField(date, Calendar.MINUTE, value);
    }

    /**
     * 在基准日期上增加秒数。
     *
     * @param date  基准日期，不可为 null
     * @param value 增量；为负时表示减少
     * @return 计算后的日期
     * @throws UnsupportedOperationException 当日历计算异常时
     */
    public static Date increaseAsSecond(Date date, int value) {
        return increaseAsField(date, Calendar.SECOND, value);
    }

    /**
     * 在基准日期上增加毫秒数。
     *
     * @param date  基准日期，不可为 null
     * @param value 增量；为负时表示减少
     * @return 计算后的日期
     * @throws UnsupportedOperationException 当日历计算异常时
     */
    public static Date increaseAsMillisecond(Date date, int value) {
        return increaseAsField(date, Calendar.MILLISECOND, value);
    }

    /**
     * 在基准日期上按 {@link Calendar} 指定字段增加 {@code value}。
     *
     * @param date  基准日期，不可为 null
     * @param field {@link Calendar} 字段常量，如 {@link Calendar#DATE}
     * @param value 增量；为负时表示减少
     * @return 计算后的日期
     * @throws UnsupportedOperationException 当日历计算异常时
     */
    @SuppressWarnings("MagicConstant")
    public static Date increaseAsField(Date date, int field, int value) {
        try {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            calendar.add(field, value);

            return calendar.getTime();
        } catch (Exception e) {
            throw new UnsupportedOperationException("increase as field exception", e);
        }
    }

    /* Date Decrease */
    /**
     * 在基准日期上减少指定年数（等价于按年字段增加负值）。
     *
     * @param date  基准日期，不可为 null
     * @param value 减少的年数，应为非负；内部实现为对字段做减法
     * @return 计算后的日期
     * @throws UnsupportedOperationException 当日历计算异常时
     */
    public static Date decreaseAsYear(Date date, int value) {
        return decreaseAsField(date, Calendar.YEAR, value);
    }

    /**
     * 在基准日期上减少指定月数。
     *
     * @param date  基准日期，不可为 null
     * @param value 减少的月数，应为非负
     * @return 计算后的日期
     * @throws UnsupportedOperationException 当日历计算异常时
     */
    public static Date decreaseAsMonth(Date date, int value) {
        return decreaseAsField(date, Calendar.MONTH, value);
    }

    /**
     * 在基准日期上减少指定「年中周」数。
     *
     * @param date  基准日期，不可为 null
     * @param value 减少的周数，应为非负
     * @return 计算后的日期
     * @throws UnsupportedOperationException 当日历计算异常时
     */
    public static Date decreaseAsWeekOfYear(Date date, int value) {
        return decreaseAsField(date, Calendar.WEEK_OF_YEAR, value);
    }

    /**
     * 在基准日期上减少指定「月中周」数。
     *
     * @param date  基准日期，不可为 null
     * @param value 减少的周数，应为非负
     * @return 计算后的日期
     * @throws UnsupportedOperationException 当日历计算异常时
     */
    public static Date decreaseAsWeekOfMonth(Date date, int value) {
        return decreaseAsField(date, Calendar.WEEK_OF_MONTH, value);
    }

    /**
     * 在基准日期上减少指定「月中日」数。
     *
     * @param date  基准日期，不可为 null
     * @param value 减少的天数，应为非负
     * @return 计算后的日期
     * @throws UnsupportedOperationException 当日历计算异常时
     */
    public static Date decreaseAsDayOfMonth(Date date, int value) {
        return decreaseAsField(date, Calendar.DAY_OF_MONTH, value);
    }

    /**
     * 在基准日期上减少指定「年中日」数。
     *
     * @param date  基准日期，不可为 null
     * @param value 减少的天数，应为非负
     * @return 计算后的日期
     * @throws UnsupportedOperationException 当日历计算异常时
     */
    public static Date decreaseAsDayOfYear(Date date, int value) {
        return decreaseAsField(date, Calendar.DAY_OF_YEAR, value);
    }

    /**
     * 在基准日期上减少指定「周中日」数。
     *
     * @param date  基准日期，不可为 null
     * @param value 减少的天数，应为非负
     * @return 计算后的日期
     * @throws UnsupportedOperationException 当日历计算异常时
     */
    public static Date decreaseAsDayOfWeek(Date date, int value) {
        return decreaseAsField(date, Calendar.DAY_OF_WEEK, value);
    }

    /**
     * 在基准日期上减少 {@link Calendar#DAY_OF_WEEK_IN_MONTH} 对应增量。
     *
     * @param date  基准日期，不可为 null
     * @param value 减少量，应为非负
     * @return 计算后的日期
     * @throws UnsupportedOperationException 当日历计算异常时
     */
    public static Date decreaseAsDayOfWeekInMonth(Date date, int value) {
        return decreaseAsField(date, Calendar.DAY_OF_WEEK_IN_MONTH, value);
    }

    /**
     * 在基准日期上减少上午/下午字段对应增量。
     *
     * @param date  基准日期，不可为 null
     * @param value 减少量，应为非负
     * @return 计算后的日期
     * @throws UnsupportedOperationException 当日历计算异常时
     */
    public static Date decreaseAsAmPm(Date date, int value) {
        return decreaseAsField(date, Calendar.AM_PM, value);
    }

    /**
     * 在基准日期上减少 12 小时制小时数。
     *
     * @param date  基准日期，不可为 null
     * @param value 减少的小时数，应为非负
     * @return 计算后的日期
     * @throws UnsupportedOperationException 当日历计算异常时
     */
    public static Date decreaseAsHour(Date date, int value) {
        return decreaseAsField(date, Calendar.HOUR, value);
    }

    /**
     * 在基准日期上减少 24 小时制小时数。
     *
     * @param date  基准日期，不可为 null
     * @param value 减少的小时数，应为非负
     * @return 计算后的日期
     * @throws UnsupportedOperationException 当日历计算异常时
     */
    public static Date decreaseAsHourOfDay(Date date, int value) {
        return decreaseAsField(date, Calendar.HOUR_OF_DAY, value);
    }

    /**
     * 在基准日期上减少分钟数。
     *
     * @param date  基准日期，不可为 null
     * @param value 减少的分钟数，应为非负
     * @return 计算后的日期
     * @throws UnsupportedOperationException 当日历计算异常时
     */
    public static Date decreaseAsMinute(Date date, int value) {
        return decreaseAsField(date, Calendar.MINUTE, value);
    }

    /**
     * 在基准日期上减少秒数。
     *
     * @param date  基准日期，不可为 null
     * @param value 减少的秒数，应为非负
     * @return 计算后的日期
     * @throws UnsupportedOperationException 当日历计算异常时
     */
    public static Date decreaseAsSecond(Date date, int value) {
        return decreaseAsField(date, Calendar.SECOND, value);
    }

    /**
     * 在基准日期上减少毫秒数。
     *
     * @param date  基准日期，不可为 null
     * @param value 减少的毫秒数，应为非负
     * @return 计算后的日期
     * @throws UnsupportedOperationException 当日历计算异常时
     */
    public static Date decreaseAsMillisecond(Date date, int value) {
        return decreaseAsField(date, Calendar.MILLISECOND, value);
    }

    /**
     * 在基准日期上按 {@link Calendar} 指定字段减少 {@code value}（内部对字段做 {@code add(field, -value)}）。
     *
     * @param date  基准日期，不可为 null
     * @param field {@link Calendar} 字段常量
     * @param value 减少量，应为非负
     * @return 计算后的日期
     * @throws UnsupportedOperationException 当日历计算异常时
     */
    @SuppressWarnings("MagicConstant")
    public static Date decreaseAsField(Date date, int field, int value) {
        try {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            calendar.add(field, -value);

            return calendar.getTime();
        } catch (Exception e) {
            throw new UnsupportedOperationException("decrease as field exception", e);
        }
    }
}
