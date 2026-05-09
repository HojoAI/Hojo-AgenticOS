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

import com.hojoai.omniasst.focus.dto.val.MeetingCreateOrUpdateVal;
import com.hojoai.omniasst.focus.dto.val.SysScheduleVal;
import com.hojoai.omniasst.focus.repository.po.RuleMeetingPO;
import com.hojoai.omniasst.focus.repository.po.ScheduleMeetingPO;
import com.hojoai.omniasst.focus.repository.po.SchedulePO;
import com.hojoai.omniasst.focus.repository.po.ScheduleRulePO;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Optional;

/**
 * 手动创建会议日程时构建 RuleMeetingPO、ScheduleMeetingPO 的公共逻辑
 *
 * @author My
 * @version 1.0 2026/03/05
 * @since hojoai-webmvc-omniasst-focus 1.0.0
 */
public final class RuleMeetingAssembler {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");

    private RuleMeetingAssembler() {
    }

    /**
     * 根据 MeetingVal 与规则 PO 构建 RuleMeetingPO
     *
     * @param id     主键（由调用方生成，如 Snowflake）
     * @param data   会议数据
     * @param rulePO 规则 PO
     * @return RuleMeetingPO
     */
        public static RuleMeetingPO buildRuleMeetingPO(long id, MeetingCreateOrUpdateVal data, ScheduleRulePO rulePO) {
        RuleMeetingPO po = new RuleMeetingPO();
        po.setId(id);
        po.setRuleId(rulePO.getId());

        po.setStartDate(rulePO.getStartDate());
        po.setEndDate(rulePO.getEndDate());
        po.setStartTime(rulePO.getStartTime());
        po.setEndTime(rulePO.getEndTime());
        po.setParticipant(nullToEmpty(data.getParticipant()));
        po.setMeetingId(nullToEmpty(data.getMeetingId()));
        po.setMeetingTitle(nullToEmpty(data.getMeetingTitle()));
        po.setMeetingUrl(nullToEmpty(data.getMeetingUrl()));
        return po;
    }

    /**
     * 根据 SysScheduleVal 与规则 PO 构建 RuleMeetingPO（系统同步时使用）
     */
    public static RuleMeetingPO buildRuleMeetingPO(long id, SysScheduleVal val, ScheduleRulePO rulePO) {
        RuleMeetingPO po = new RuleMeetingPO();
        po.setId(id);
        po.setRuleId(rulePO.getId());
        LocalDateTime defaultStart = rulePO.getRuleStartTime();
        LocalDateTime defaultEnd = rulePO.getRuleEndTime();
        LocalDateTime startLdt = null;
        LocalDateTime endLdt = null;
        if (val != null && val.getScheduleInfo() != null) {
            startLdt = ScheduleRuleAssembler.parseScheduleDateTime(val.getScheduleInfo().getStartTime());
            endLdt = ScheduleRuleAssembler.parseScheduleDateTime(val.getScheduleInfo().getEndTime());
        }
        if (startLdt == null) {
            startLdt = defaultStart;
        }
        if (endLdt == null) {
            endLdt = defaultEnd;
        }
        po.setStartDate(startLdt != null ? startLdt.toLocalDate() : null);
        po.setEndDate(endLdt != null ? endLdt.toLocalDate() : null);
        po.setStartTime(startLdt != null ? startLdt.toLocalTime() : null);
        po.setEndTime(endLdt != null ? endLdt.toLocalTime() : null);
        if (val != null && val.getBasicInfo() != null) {
            po.setMeetingTitle(nullToEmpty(val.getBasicInfo().getTitle()));
            po.setMeetingUrl(nullToEmpty(val.getBasicInfo().getUrl()));
        }
        if (val != null && val.getAttendees() != null && !val.getAttendees().isEmpty()) {
            String participant = val.getAttendees().stream()
                    .map(a -> a.getEmail() != null ? a.getEmail() : a.getName())
                    .filter(s -> s != null && !s.isEmpty())
                    .reduce((a, b) -> a + "," + b).orElse("");
            po.setParticipant(participant);
        }
        return po;
    }

    /**
     * 根据日程 PO 与规则会议 PO 构建 ScheduleMeetingPO
     *
     * @param id        主键（由调用方生成，如 Snowflake）
     * @param schedulePO 日程 PO
     * @param rule       规则会议 PO
     * @return ScheduleMeetingPO
     */
    public static ScheduleMeetingPO buildScheduleMeetingPO(long id, SchedulePO schedulePO, RuleMeetingPO rule) {
        ScheduleMeetingPO po = new ScheduleMeetingPO();
        po.setId(id);
        po.setScheduleId(schedulePO.getId());
        po.setStartTime(schedulePO.getStartTime());
        po.setEndTime(schedulePO.getEndTime());
        po.setParticipant(rule.getParticipant());
        po.setMeetingId(rule.getMeetingId());
        po.setMeetingTitle(rule.getMeetingTitle());
        po.setMeetingUrl(rule.getMeetingUrl());
        return po;
    }

    private static String nullToEmpty(String s) {
        return s != null ? s : "";
    }

    private static LocalDateTime dateToLocalDateTime(Date date) {
        return Optional.ofNullable(date)
                .map(d -> d.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime())
                .orElse(null);
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
}
