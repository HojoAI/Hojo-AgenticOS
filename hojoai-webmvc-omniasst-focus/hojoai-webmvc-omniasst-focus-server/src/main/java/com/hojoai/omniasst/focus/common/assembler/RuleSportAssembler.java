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

import com.hojoai.omniasst.focus.dto.val.ScheduleInfo;
import com.hojoai.omniasst.focus.dto.val.SportCreateOrUpdateVal;
import com.hojoai.omniasst.focus.dto.val.SysScheduleVal;
import com.hojoai.omniasst.focus.repository.po.RuleSportPO;
import com.hojoai.omniasst.focus.repository.po.ScheduleRulePO;
import com.hojoai.omniasst.focus.repository.po.ScheduleSportPO;
import com.hojoai.omniasst.focus.repository.po.SchedulePO;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * 手动创建运动日程时构建 RuleSportPO、ScheduleSportPO 的公共逻辑
 *
 * @author My
 * @version 1.0 2026/03/05
 * @since hojoai-webmvc-omniasst-focus 1.0.0
 */
public final class RuleSportAssembler {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");

    private RuleSportAssembler() {
    }

    /**
     * 根据 SportVal 与规则 PO 构建 RuleSportPO
     *
     * @param id     主键（由调用方生成，如 Snowflake）
     * @param data   运动数据
     * @param rulePO 规则 PO
     * @return RuleSportPO
     */
    public static RuleSportPO buildRuleSportPO(long id, SportCreateOrUpdateVal data, ScheduleRulePO rulePO) {
        RuleSportPO po = new RuleSportPO();
        po.setId(id);
        po.setRuleId(rulePO.getId());

        po.setStartDate(rulePO.getStartDate());
        po.setEndDate(rulePO.getEndDate());
        po.setStartTime(rulePO.getStartTime());
        po.setEndTime(rulePO.getEndTime());
        po.setHeartRate(data.getHeartRate() != null ? data.getHeartRate() : "");
        return po;
    }

    /**
     * 根据 SysScheduleVal 与规则 PO 构建 RuleSportPO（系统同步时使用）
     */
    public static RuleSportPO buildRuleSportPO(long id, SysScheduleVal val, ScheduleRulePO rulePO) {
        RuleSportPO po = new RuleSportPO();
        po.setId(id);
        po.setRuleId(rulePO.getId());
        LocalDateTime defaultStart = rulePO.getRuleStartTime();
        LocalDateTime defaultEnd = rulePO.getRuleEndTime();
        LocalDateTime startLdt = null;
        LocalDateTime endLdt = null;
        if (val != null && val.getScheduleInfo() != null) {
            ScheduleInfo si = val.getScheduleInfo();
            startLdt = ScheduleRuleAssembler.parseScheduleDateTime(si.getStartTime());
            endLdt = ScheduleRuleAssembler.parseScheduleDateTime(si.getEndTime());
        }
        if (startLdt == null) {
            startLdt = defaultStart;
        }
        if (endLdt == null) {
            endLdt = defaultEnd;
        }
        po.setStartDate(startLdt != null ? startLdt.toLocalDate() : (rulePO.getStartDate() != null ? rulePO.getStartDate() : LocalDate.now()));
        po.setEndDate(endLdt != null ? endLdt.toLocalDate() : (rulePO.getEndDate() != null ? rulePO.getEndDate() : LocalDate.now()));
        po.setStartTime(startLdt != null ? startLdt.toLocalTime() : (rulePO.getStartTime() != null ? rulePO.getStartTime() : LocalTime.of(0, 0)));
        po.setEndTime(endLdt != null ? endLdt.toLocalTime() : (rulePO.getEndTime() != null ? rulePO.getEndTime() : LocalTime.of(23, 59)));
        po.setHeartRate("");
        return po;
    }

    /**
     * 根据日程 PO 与规则运动 PO 构建 ScheduleSportPO
     *
     * @param id         主键（由调用方生成，如 Snowflake）
     * @param schedulePO 日程 PO
     * @param rule       规则运动 PO
     * @return ScheduleSportPO
     */
    public static ScheduleSportPO buildScheduleSportPO(long id, SchedulePO schedulePO, RuleSportPO rule) {
        ScheduleSportPO po = new ScheduleSportPO();
        po.setId(id);
        po.setScheduleId(schedulePO.getId());
        po.setStartTime(schedulePO.getStartTime());
        po.setEndTime(schedulePO.getEndTime());
        po.setHeartRate(rule.getHeartRate() != null ? rule.getHeartRate() : "");
        return po;
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
