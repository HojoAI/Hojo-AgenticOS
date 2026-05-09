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
package com.hojoai.omniasst.focus.common.assembler;

import com.hojoai.omniasst.focus.dto.request.ManualModifyScheduleRequestDTO;
import com.hojoai.omniasst.focus.dto.request.ManualScheduleRequestDTO;
import com.hojoai.omniasst.focus.dto.val.BasicInfo;
import com.hojoai.omniasst.focus.dto.val.RecurrenceInfo;
import com.hojoai.omniasst.focus.dto.val.ScheduleInfo;
import com.hojoai.omniasst.focus.dto.val.SysScheduleVal;
import com.hojoai.omniasst.focus.repository.po.ScheduleRulePO;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 手动创建日程时构建 ScheduleRulePO 的公共逻辑
 *
 * @author My
 * @version 1.0 2026/03/03
 * @since hojoai-webmvc-omniasst-focus 1.0.0
 */
public final class ScheduleRuleAssembler {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");

    private ScheduleRuleAssembler() {
    }

    /**
     * 根据入参构建并赋值 ScheduleRulePO（与 focus_schedule_rule 表一致）
     * startDate/endDate 格式 yyyy-MM-dd，startTime/endTime 格式 HH:mm:ss
     */
    public static ScheduleRulePO buildBaseScheduleRulePO(String userId, ManualScheduleRequestDTO requestDTO) {
        if (requestDTO == null) {
            ScheduleRulePO po = new ScheduleRulePO();
            if (userId != null) {
                po.setUserId(userId);
            }
            return po;
        }
        ScheduleRulePO po = new ScheduleRulePO();
        po.setUserId(userId);
        po.setTitle(requestDTO.getTitle());
        po.setType(requestDTO.getType());
        po.setIcon(requestDTO.getIcon());
        LocalDateTime start = parseDateTime(requestDTO.getStartDate(), requestDTO.getStartTime());
        LocalDateTime end = parseDateTime(requestDTO.getEndDate(), requestDTO.getEndTime());
        if (start != null) {
            po.setStartDate(start.toLocalDate());
            po.setStartTime(start.toLocalTime());
        }
        if (end != null) {
            po.setEndDate(end.toLocalDate());
            po.setEndTime(end.toLocalTime());
        }
        po.setRuleStartTime(start);
        // po.setRuleEndTime(end);
        po.setIsRepeat(Optional.ofNullable(requestDTO.getIsRepeat()).orElse(Boolean.FALSE));
        po.setRepeatTime(repeatTimeListToString(requestDTO.getRepeatTime()));
        po.setRepeatUnit(requestDTO.getRepeatUnit());
        po.setIsRemind(Optional.ofNullable(requestDTO.getIsRemind()).orElse(Boolean.FALSE));
        po.setRemindTime(requestDTO.getRemindTime());
        po.setRemindUnit(requestDTO.getRemindUnit());
        po.setIsCreateSug(requestDTO.getIsCreateAiSuggestion() == null ? null : (requestDTO.getIsCreateAiSuggestion() ? 1 : 0));
        po.setParticipant(requestDTO.getParticipant());
        po.setRemark(requestDTO.getRemark());
        po.setIsCreate(Boolean.TRUE);
        LocalDateTime localDateTime = LocalDateTime.now();
        po.setCreateTime(localDateTime);
        po.setUpdateTime(localDateTime);
        return po;
    }

    /**
     * 根据 SysScheduleVal 构建并赋值 ScheduleRulePO（系统同步日程规则）
     */
    public static ScheduleRulePO buildBaseScheduleRulePO(String userId, SysScheduleVal sysScheduleVal) {
        ScheduleRulePO po = new ScheduleRulePO();
        po.setId(Long.valueOf(sysScheduleVal.getCloudEventId()));
        if (userId != null) {
            po.setUserId(userId);
        }
        po.setSysId(sysScheduleVal.getLocalSystemId());
        po.setIsSysSync(1);
        po.setIsCreate(Boolean.TRUE);
        if (sysScheduleVal.getVersion() != null) {
            po.setVersion(sysScheduleVal.getVersion().intValue());
        }
        BasicInfo basicInfo = sysScheduleVal.getBasicInfo();
        if (basicInfo != null) {
            po.setTitle(basicInfo.getTitle());
            po.setRemark(basicInfo.getDescription());
        }
        ScheduleInfo scheduleInfo = sysScheduleVal.getScheduleInfo();
        if (scheduleInfo != null) {
            LocalDateTime ruleStart = parseDateTimeFlexible(scheduleInfo.getStartTime());
            LocalDateTime ruleEnd = parseDateTimeFlexible(scheduleInfo.getEndTime());
            if (ruleStart != null) {
                po.setRuleStartTime(ruleStart);
                po.setStartDate(ruleStart.toLocalDate());
                po.setStartTime(ruleStart.toLocalTime());
            }
            if (ruleEnd != null) {
                // po.setRuleEndTime(ruleEnd);
                po.setEndDate(ruleEnd.toLocalDate());
                po.setEndTime(ruleEnd.toLocalTime());
            }
            if (Objects.nonNull(scheduleInfo.getIanaTimezone())) {
                po.setTimeZone(scheduleInfo.getIanaTimezone());
            }
        }
        RecurrenceInfo recurrence = sysScheduleVal.getRecurrence();
        if (recurrence != null) {
            Integer frequency = recurrence.getFrequency();
            if (frequency == null || frequency == 0) {
                po.setIsRepeat(Boolean.FALSE);
            } else {
                po.setIsRepeat(Boolean.TRUE);
                po.setRepeatInterval(recurrence.getInterval());
                po.setRepeatUnit(mapFrequencyToRepeatUnit(recurrence.getFrequency()));
                if (frequency == 2) {
                    if (CollectionUtils.isNotEmpty(recurrence.getDaysOfWeek())) {
                        po.setRepeatTime(recurrence.getDaysOfWeek().stream()
                                .map(String::valueOf)
                                .collect(Collectors.joining(",")));
                    }
                }
                if (scheduleInfo != null) {
                    LocalDateTime ruleStart = parseDateTimeFlexible(scheduleInfo.getStartTime());
                    LocalDateTime ruleEnd = parseDateTimeFlexible(scheduleInfo.getEndTime());
                    if (frequency == 1) {
                        if (ruleStart != null) {
                            po.setStartTime(ruleStart.toLocalTime());
                        }
                        if (ruleEnd != null) {
                            po.setEndTime(ruleEnd.toLocalTime());
                        }
                    } else if (frequency == 3) {
                        if (ruleStart != null) {
                            po.setStartTime(ruleStart.toLocalTime());
                            po.setRepeatTime(String.valueOf(ruleStart.getDayOfMonth()));
                        }
                        if (ruleEnd != null) {
                            po.setEndTime(ruleEnd.toLocalTime());
                        }
                    }
                }
            }
        } else {
            po.setIsRepeat(Boolean.FALSE);
        }
        List<Integer> reminders = sysScheduleVal.getReminders();
        if (CollectionUtils.isNotEmpty(reminders)) {
            po.setIsRemind(Boolean.TRUE);
            po.setRemindTime(reminders.getLast());
        } else {
            po.setIsRemind(Boolean.FALSE);
        }
        if (CollectionUtils.isNotEmpty(sysScheduleVal.getAttendees())) {
            String participant = sysScheduleVal.getAttendees().stream()
                    .map(a -> a.getEmail() != null ? a.getEmail() : a.getName())
                    .filter(StringUtils::hasText)
                    .collect(Collectors.joining(","));
            po.setParticipant(participant);
        }
        po.setIsCreate(Boolean.TRUE);
        po.setIsCreateSug(1);

        LocalDateTime localDateTime = LocalDateTime.now();
        po.setCreateTime(localDateTime);
        po.setUpdateTime(localDateTime);

        return po;
    }

    /**
     * 将 source 中的业务字段复制到 target，保留 target 的 id、createTime 不变
     */
    public static void copyRuleFieldsFrom(ScheduleRulePO target, ScheduleRulePO source) {
        if (source == null || target == null) {
            return;
        }
        target.setUserId(source.getUserId());
        target.setConversationId(source.getConversationId());
        target.setSysId(source.getSysId());
        target.setTitle(source.getTitle());
        target.setType(source.getType());
        target.setIcon(source.getIcon());
        target.setStartDate(source.getStartDate());
        target.setEndDate(source.getEndDate());
        target.setStartTime(source.getStartTime());
        target.setEndTime(source.getEndTime());
        target.setIsSysSync(source.getIsSysSync());
        target.setIsFixedDate(source.getIsFixedDate());
        target.setIsRepeat(source.getIsRepeat());
        target.setRepeatTime(source.getRepeatTime());
        target.setRepeatUnit(source.getRepeatUnit());
        target.setRepeatInterval(source.getRepeatInterval());
        target.setIsRemind(source.getIsRemind());
        target.setRemindTime(source.getRemindTime());
        target.setRemindUnit(source.getRemindUnit());
        target.setRuleStartTime(source.getRuleStartTime());
        target.setRuleEndTime(source.getRuleEndTime());
        target.setIsCreateSug(source.getIsCreateSug());
        target.setParticipant(source.getParticipant());
        target.setLocationId(source.getLocationId());
        target.setRemark(source.getRemark());
        target.setIsCreate(source.getIsCreate());
        target.setVersion(source.getVersion());
    }

    /** 偏移量无冒号格式：2026-03-23T14:00:00.000+0800、2026-03-23T14:00:00+0800 */
    public static final DateTimeFormatter OFFSET_NO_COLON =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss[.SSS]XX");

    /**
     * 解析日程时间字符串（供 RuleXxxAssembler 等复用）
     * 支持格式：ISO 8601 UTC（yyyy-MM-ddTHH:mm:ssZ、yyyy-MM-ddTHH:mm:ss.SSSZ）、
     * 带偏移量含冒号（yyyy-MM-ddTHH:mm:ss+HH:mm）、
     * 带偏移量无冒号（yyyy-MM-ddTHH:mm:ss[.SSS]+HHmm）、
     * yyyy-MM-ddTHH:mm:ss、yyyy-MM-dd HH:mm:ss、yyyy-MM-dd HH:mm、yyyy-MM-dd
     */
    public static LocalDateTime parseScheduleDateTime(String dateTimeStr) {
        return parseDateTimeFlexible(dateTimeStr);
    }

    private static LocalDateTime parseDateTimeFlexible(String dateTimeStr) {
        if (!StringUtils.hasText(dateTimeStr)) {
            return null;
        }
        String s = dateTimeStr.trim();
        // ISO 8601 UTC/偏移量格式：2026-03-23T10:30:00Z、2026-03-23T10:30:00.000Z、2026-03-23T10:30:00+08:00
        if (s.contains("T")) {
            try {
                return OffsetDateTime.parse(s, DateTimeFormatter.ISO_OFFSET_DATE_TIME)
                        // .withOffsetSameInstant(ZoneOffset.UTC)
                        .toLocalDateTime();
            } catch (Exception ignored) {
            }
            // 偏移量无冒号格式：2026-03-23T14:00:00.000+0800
            try {
                return OffsetDateTime.parse(s, OFFSET_NO_COLON)
                        // .withOffsetSameInstant(ZoneOffset.UTC)
                        .toLocalDateTime();
            } catch (Exception ignored) {
            }
            // ISO 8601 本地日期时间：2026-03-23T10:30:00
            try {
                return LocalDateTime.parse(s, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            } catch (Exception ignored) {
            }
        }
        // yyyy-MM-dd HH:mm:ss
        try {
            return LocalDateTime.parse(s, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        } catch (Exception ignored) {
        }
        // yyyy-MM-dd HH:mm
        try {
            return LocalDateTime.parse(s, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        } catch (Exception ignored) {
        }
        // yyyy-MM-dd
        try {
            return LocalDate.parse(s, DateTimeFormatter.ISO_LOCAL_DATE).atStartOfDay();
        } catch (Exception ignored) {
        }
        return null;
    }

    /** 频率枚举常见值：1-日 2-周 3-月 等，映射到 PO 的 repeatUnit */
    private static String mapFrequencyToRepeatUnit(Integer frequency) {
        if (frequency == null) {
            return null;
        }
        switch (frequency) {
            case 1:
                return "D";
            case 2:
                return "W";
            case 3:
                return "M";
            case 4:
                return "Y";
            default:
                return "D";
        }
    }

    private static String repeatTimeListToString(List<String> repeatTime) {
        if (CollectionUtils.isEmpty(repeatTime)) {
            return "1";
        }
        return String.join(",", repeatTime);
    }

    /**
     * 将日期字符串（yyyy-MM-dd）与时间字符串（HH:mm:ss）解析为 LocalDateTime
     */
    private static LocalDateTime parseDateTime(String dateStr, String timeStr) {
        if (!StringUtils.hasText(dateStr) || !StringUtils.hasText(timeStr)) {
            return null;
        }
        try {
            LocalDate date = LocalDate.parse(dateStr.trim(), DATE_FORMATTER);
            LocalTime time = LocalTime.parse(timeStr.trim(), TIME_FORMATTER);
            return LocalDateTime.of(date, time);
        } catch (Exception ignored) {
            return null;
        }
    }

    /**
     * 供外部调用：将请求中的日期+时间字符串解析为 LocalDateTime（yyyy-MM-dd + HH:mm:ss）
     */
    public static LocalDateTime parseRequestDateTime(String dateStr, String timeStr) {
        return parseDateTime(dateStr, timeStr);
    }

    public static ScheduleRulePO buildBaseScheduleRulePO(ScheduleRulePO po, ManualModifyScheduleRequestDTO requestDTO) {
        po.setTitle(requestDTO.getTitle());
        po.setType(requestDTO.getType());
        po.setIcon(requestDTO.getIcon());
        LocalDateTime start = parseDateTime(requestDTO.getStartDate(), requestDTO.getStartTime());
        LocalDateTime end = parseDateTime(requestDTO.getEndDate(), requestDTO.getEndTime());
        if (start != null) {
            po.setStartDate(start.toLocalDate());
            po.setStartTime(start.toLocalTime());
        }
        if (end != null) {
            po.setEndDate(end.toLocalDate());
            po.setEndTime(end.toLocalTime());
        }
        po.setRuleStartTime(start);
        // po.setRuleEndTime(end);
        po.setIsRepeat(Optional.ofNullable(requestDTO.getIsRepeat()).orElse(Boolean.FALSE));
        po.setRepeatTime(repeatTimeListToString(requestDTO.getRepeatTime()));
        po.setRepeatUnit(requestDTO.getRepeatUnit());
        po.setIsRemind(Optional.ofNullable(requestDTO.getIsRemind()).orElse(Boolean.FALSE));
        po.setRemindTime(requestDTO.getRemindTime());
        po.setRemindUnit(requestDTO.getRemindUnit());
        po.setIsCreateSug(requestDTO.getIsCreateAiSuggestion() == null ? null : (requestDTO.getIsCreateAiSuggestion() ? 1 : 0));
        po.setParticipant(requestDTO.getParticipant());
        po.setRemark(requestDTO.getRemark());
        po.setIsCreate(Boolean.TRUE);
        LocalDateTime localDateTime = LocalDateTime.now();
        po.setCreateTime(localDateTime);
        po.setUpdateTime(localDateTime);
        return po;
    }

}
