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

import com.hojoai.omniasst.focus.dto.val.FlightVal;
import com.hojoai.omniasst.focus.dto.val.SysScheduleVal;
import com.hojoai.omniasst.focus.repository.po.RuleFlightPO;
import com.hojoai.omniasst.focus.repository.po.ScheduleFlightPO;
import com.hojoai.omniasst.focus.repository.po.ScheduleRulePO;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Optional;

/**
 * 手动创建航班日程时构建 RuleFlightPO、ScheduleFlightPO 的公共逻辑
 *
 * @author My
 * @version 1.0 2026/03/05
 * @since hojoai-webmvc-omniasst-focus 1.0.0
 */
public final class RuleFlightAssembler {

    private RuleFlightAssembler() {
    }

    /**
     * 根据 FlightVal 与规则 PO 构建 RuleFlightPO
     *
     * @param id     主键（由调用方生成，如 Snowflake）
     * @param data   航班数据
     * @param rulePO 规则 PO
     * @return RuleFlightPO
     */
    public static RuleFlightPO buildRuleFlightPO(long id, FlightVal data, ScheduleRulePO rulePO) {
        RuleFlightPO po = new RuleFlightPO();
        po.setId(id);
        po.setRuleId(rulePO.getId());

        LocalDateTime defaultStart = rulePO.getRuleStartTime();
        LocalDateTime defaultEnd = rulePO.getRuleEndTime();

        po.setFlightNo(nullToEmpty(data.getFlightNo()));
        po.setFlightType(nullToEmpty(data.getFlightType()));
        po.setSeatNo(nullToEmpty(data.getSeatNo()));
        po.setBoardingGate(nullToEmpty(data.getBoardingGate()));
        po.setCategory(data.getCategory());
        po.setCompany(nullToEmpty(data.getCompany()));
        po.setDepTimezone(nullToEmpty(data.getDepTimezone()));
        po.setArrTimezone(nullToEmpty(data.getArrTimezone()));
        po.setDepCode(nullToEmpty(data.getDepCode()));
        po.setArrCode(nullToEmpty(data.getArrCode()));
        po.setDepAirport(nullToEmpty(data.getDepAirport()));
        po.setArrAirport(nullToEmpty(data.getArrAirport()));
        po.setDepCity(nullToEmpty(data.getDepCity()));
        po.setArrCity(nullToEmpty(data.getArrCity()));
        po.setDepTerminal(nullToEmpty(data.getDepTerminal()));
        po.setArrTerminal(nullToEmpty(data.getArrTerminal()));

        LocalDateTime depPlan = data.getDepPlanTime() != null ? dateToLocalDateTime(data.getDepPlanTime()) : defaultStart;
        LocalDateTime arrPlan = data.getArrPlanTime() != null ? dateToLocalDateTime(data.getArrPlanTime()) : defaultEnd;
        po.setDepPlanTime(depPlan);
        po.setArrPlanTime(arrPlan);
        po.setDepReadyTime(data.getDepReadyTime() != null ? dateToLocalDateTime(data.getDepReadyTime()) : (data.getDepPlanTime() != null ? dateToLocalDateTime(data.getDepPlanTime()) : defaultStart));
        po.setArrReadyTime(data.getArrReadyTime() != null ? dateToLocalDateTime(data.getArrReadyTime()) : (data.getArrPlanTime() != null ? dateToLocalDateTime(data.getArrPlanTime()) : defaultEnd));
        po.setDepTime(data.getDepTime() != null ? dateToLocalDateTime(data.getDepTime()) : (data.getDepPlanTime() != null ? dateToLocalDateTime(data.getDepPlanTime()) : defaultStart));
        po.setArrTime(data.getArrTime() != null ? dateToLocalDateTime(data.getArrTime()) : (data.getArrPlanTime() != null ? dateToLocalDateTime(data.getArrPlanTime()) : defaultEnd));

        po.setState(nullToEmpty(data.getState()));
        po.setStopFlag(data.getStopFlag() != null ? data.getStopFlag() : "0");
        po.setLegFlag(data.getLegFlag() != null ? String.valueOf(data.getLegFlag()) : "0");
        po.setOntimeRate(nullToEmpty(data.getOntimeRate()));
        po.setDuration(data.getDuration() != null ? data.getDuration() : 0);
        return po;
    }

    /**
     * 根据 SysScheduleVal 与规则 PO 构建 RuleFlightPO（系统同步时使用，仅填充规则与时间）
     */
    public static RuleFlightPO buildRuleFlightPO(long id, SysScheduleVal val, ScheduleRulePO rulePO) {
        RuleFlightPO po = new RuleFlightPO();
        po.setId(id);
        po.setRuleId(rulePO.getId());
        LocalDateTime defaultStart = rulePO.getRuleStartTime();
        LocalDateTime defaultEnd = rulePO.getRuleEndTime();
        if (val != null && val.getScheduleInfo() != null) {
            po.setDepPlanTime(ScheduleRuleAssembler.parseScheduleDateTime(val.getScheduleInfo().getStartTime()));
            po.setArrPlanTime(ScheduleRuleAssembler.parseScheduleDateTime(val.getScheduleInfo().getEndTime()));
        }
        if (po.getDepPlanTime() == null) {
            po.setDepPlanTime(defaultStart);
        }
        if (po.getArrPlanTime() == null) {
            po.setArrPlanTime(defaultEnd);
        }
        po.setDepReadyTime(po.getDepPlanTime());
        po.setArrReadyTime(po.getArrPlanTime());
        po.setDepTime(po.getDepPlanTime());
        po.setArrTime(po.getArrPlanTime());
        po.setFlightNo("");
        po.setSeatNo("");
        po.setBoardingGate("");
        po.setCompany("");
        po.setDepTimezone("");
        po.setArrTimezone("");
        po.setDepCode("");
        po.setArrCode("");
        po.setDepAirport("");
        po.setArrAirport("");
        po.setDepCity("");
        po.setArrCity("");
        po.setDepTerminal("");
        po.setArrTerminal("");
        po.setState("");
        po.setStopFlag("0");
        po.setLegFlag("0");
        po.setOntimeRate("");
        po.setDuration(0);
        return po;
    }

    /**
     * 根据日程 ID 与规则航班 PO 构建 ScheduleFlightPO
     *
     * @param id   主键（由调用方生成，如 Snowflake）
     * @param scheduleId 日程 ID
     * @param rule 规则航班 PO
     * @return ScheduleFlightPO
     */
    public static ScheduleFlightPO buildScheduleFlightPO(long id, long scheduleId, RuleFlightPO rule) {
        ScheduleFlightPO po = new ScheduleFlightPO();
        po.setId(id);
        po.setScheduleId(scheduleId);
        po.setFlightNo(rule.getFlightNo());
        po.setFlightType(rule.getFlightType());
        po.setSeatNo(rule.getSeatNo());
        po.setBoardingGate(rule.getBoardingGate());
        po.setCategory(rule.getCategory());
        po.setCompany(rule.getCompany());
        po.setDepTimezone(rule.getDepTimezone());
        po.setArrTimezone(rule.getArrTimezone());
        po.setDepCode(rule.getDepCode());
        po.setArrCode(rule.getArrCode());
        po.setDepAirport(rule.getDepAirport());
        po.setArrAirport(rule.getArrAirport());
        po.setDepCity(rule.getDepCity());
        po.setArrCity(rule.getArrCity());
        po.setDepTerminal(rule.getDepTerminal());
        po.setArrTerminal(rule.getArrTerminal());
        po.setDepPlanTime(rule.getDepPlanTime());
        po.setArrPlanTime(rule.getArrPlanTime());
        po.setDepReadyTime(rule.getDepReadyTime());
        po.setArrReadyTime(rule.getArrReadyTime());
        po.setDepTime(rule.getDepTime());
        po.setArrTime(rule.getArrTime());
        po.setState(rule.getState());
        po.setStopFlag(rule.getStopFlag());
        po.setLegFlag(rule.getLegFlag());
        po.setOntimeRate(rule.getOntimeRate());
        po.setDuration(rule.getDuration());
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
}
