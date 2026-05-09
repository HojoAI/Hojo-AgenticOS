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

import com.hojoai.omniasst.focus.dto.val.BasicInfo;
import com.hojoai.omniasst.focus.dto.val.RecurrenceInfo;
import com.hojoai.omniasst.focus.dto.val.ScheduleInfo;
import com.hojoai.omniasst.focus.dto.val.SysScheduleVal;
import com.hojoai.omniasst.focus.repository.po.ScheduleRulePO;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * SysScheduleVal 与 ScheduleRulePO 互转
 *
 * @author My
 * @version 1.0
 * @since hojoai-webmvc-omniasst.focus 1.0.0
 */
public final class SysScheduleValAssembler {

    private SysScheduleValAssembler() {
    }

    /**
     * 根据 ScheduleRulePO 构建 SysScheduleVal
     */
    public static SysScheduleVal buildScheduleVal(ScheduleRulePO rule) {
        if (rule == null) {
            return null;
        }
        SysScheduleVal val = new SysScheduleVal();
        val.setCloudEventId(String.valueOf(rule.getId()));
        val.setLocalSystemId(rule.getSysId());
        val.setVersion(rule.getVersion() != null ? rule.getVersion().longValue() : null);

        BasicInfo basicInfo = new BasicInfo();
        basicInfo.setTitle(rule.getTitle());
        basicInfo.setDescription(rule.getRemark());
        val.setBasicInfo(basicInfo);

        ScheduleInfo scheduleInfo = new ScheduleInfo();
        ZoneId zoneId = StringUtils.hasText(rule.getTimeZone())
                ? ZoneId.of(rule.getTimeZone()) : ZoneId.of("UTC");
        if (rule.getStartDate() != null && rule.getStartTime() != null) {
            scheduleInfo.setStartTime(LocalDateTime.of(rule.getStartDate(), rule.getStartTime())
                    .atZone(zoneId).toOffsetDateTime().format(ScheduleRuleAssembler.OFFSET_NO_COLON));
        }
        if (rule.getEndDate() != null && rule.getEndTime() != null) {
            scheduleInfo.setEndTime(LocalDateTime.of(rule.getEndDate(), rule.getEndTime())
                    .atZone(zoneId).toOffsetDateTime().format(ScheduleRuleAssembler.OFFSET_NO_COLON));
        }
        if (Objects.nonNull(rule.getTimeZone())) {
            scheduleInfo.setIanaTimezone(rule.getTimeZone());
        }
        val.setScheduleInfo(scheduleInfo);

        if (Boolean.TRUE.equals(rule.getIsRepeat())) {
            RecurrenceInfo recurrence = new RecurrenceInfo();
            recurrence.setInterval(rule.getRepeatInterval());
            recurrence.setFrequency(repeatUnitToFrequency(rule.getRepeatUnit()));
            if (StringUtils.hasText(rule.getRepeatTime())) {
                List<Integer> daysOfWeek = new ArrayList<>();
                for (String s : rule.getRepeatTime().split("[,，\\s]+")) {
                    String t = s.trim();
                    if (!t.isEmpty()) {
                        try {
                            daysOfWeek.add(Integer.parseInt(t));
                        } catch (NumberFormatException ignored) {
                        }
                    }
                }
                recurrence.setDaysOfWeek(daysOfWeek);
            }
            val.setRecurrence(recurrence);
        }

        if (rule.getRemindTime() != null) {
            val.setReminders(Collections.singletonList(rule.getRemindTime()));
        }
        return val;
    }

    private static Integer repeatUnitToFrequency(String repeatUnit) {
        if (!StringUtils.hasText(repeatUnit)) {
            return null;
        }
        switch (repeatUnit.trim().toUpperCase()) {
            case "D":
                return 1;
            case "W":
                return 2;
            case "M":
                return 3;
            case "Y":
                return 4;
            default:
                return 1;
        }
    }
}
