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

import com.hojoai.omniasst.focus.dto.val.TrainVal;
import com.hojoai.omniasst.focus.dto.val.SysScheduleVal;
import com.hojoai.omniasst.focus.repository.po.RuleTrainPO;
import com.hojoai.omniasst.focus.repository.po.ScheduleRulePO;
import com.hojoai.omniasst.focus.repository.po.ScheduleTrainPO;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Optional;

/**
 * 手动创建火车日程时构建 RuleTrainPO、ScheduleTrainPO 的公共逻辑
 *
 * @author My
 * @version 1.0 2026/03/05
 * @since hojoai-webmvc-omniasst-focus 1.0.0
 */
public final class RuleTrainAssembler {

    private RuleTrainAssembler() {
    }

    /**
     * 根据 TrainVal 与规则 PO 构建 RuleTrainPO
     *
     * @param id     主键（由调用方生成，如 Snowflake）
     * @param data   火车数据
     * @param rulePO 规则 PO
     * @return RuleTrainPO
     */
    public static RuleTrainPO buildRuleTrainPO(long id, TrainVal data, ScheduleRulePO rulePO) {
        RuleTrainPO po = new RuleTrainPO();
        po.setId(id);
        po.setRuleId(rulePO.getId());
        po.setTrainNo(data.getTrainNo() != null ? data.getTrainNo() : "");
        po.setSeatNo(data.getSeatNo() != null ? data.getSeatNo() : "");
        po.setTicketGate(data.getTicketGate() != null ? data.getTicketGate() : "");
        LocalDateTime defaultStart = rulePO.getRuleStartTime();
        LocalDateTime defaultEnd = rulePO.getRuleEndTime();
        po.setDepPlanTime(data.getDepPlanTime() != null ? dateToLocalDateTime(data.getDepPlanTime()) : defaultStart);
        po.setArrPlanTime(data.getArrPlanTime() != null ? dateToLocalDateTime(data.getArrPlanTime()) : defaultEnd);
        po.setDepStan(data.getDepStan() != null ? data.getDepStan() : "");
        po.setArrStan(data.getArrStan() != null ? data.getArrStan() : "");
        po.setDepCode(data.getDepCode() != null ? data.getDepCode() : "");
        po.setArrCode(data.getArrCode() != null ? data.getArrCode() : "");
        po.setDuration(data.getDuration() != null ? data.getDuration() : 0);
        return po;
    }

    /**
     * 根据 SysScheduleVal 与规则 PO 构建 RuleTrainPO（系统同步时使用，仅填充规则与时间）
     */
    public static RuleTrainPO buildRuleTrainPO(long id, SysScheduleVal val, ScheduleRulePO rulePO) {
        RuleTrainPO po = new RuleTrainPO();
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
        po.setTrainNo("");
        po.setSeatNo("");
        po.setTicketGate("");
        po.setDepStan("");
        po.setArrStan("");
        po.setDepCode("");
        po.setArrCode("");
        po.setDuration(0);
        return po;
    }

    /**
     * 根据日程 ID 与规则火车 PO 构建 ScheduleTrainPO
     *
     * @param id         主键（由调用方生成，如 Snowflake）
     * @param scheduleId 日程 ID
     * @param rule       规则火车 PO
     * @return ScheduleTrainPO
     */
    public static ScheduleTrainPO buildScheduleTrainPO(long id, long scheduleId, RuleTrainPO rule) {
        ScheduleTrainPO po = new ScheduleTrainPO();
        po.setId(id);
        po.setScheduleId(scheduleId);
        po.setTrainNo(rule.getTrainNo());
        po.setSeatNo(rule.getSeatNo());
        po.setTicketGate(rule.getTicketGate());
        po.setDepPlanTime(rule.getDepPlanTime());
        po.setArrPlanTime(rule.getArrPlanTime());
        po.setDepStan(rule.getDepStan());
        po.setArrStan(rule.getArrStan());
        po.setDepCode(rule.getDepCode());
        po.setArrCode(rule.getArrCode());
        po.setDuration(rule.getDuration());
        return po;
    }

    private static LocalDateTime dateToLocalDateTime(Date date) {
        return Optional.ofNullable(date)
                .map(d -> d.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime())
                .orElse(null);
    }
}
