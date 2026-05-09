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

import com.hojoai.omniasst.focus.common.enums.ScheduleStatusEnum;
import com.hojoai.omniasst.focus.common.enums.TagEnum;
import com.hojoai.omniasst.focus.repository.po.ScheduleRulePO;
import com.hojoai.omniasst.focus.repository.po.SchedulePO;

import java.time.LocalDateTime;

/**
 * 根据规则构建 SchedulePO 的公共逻辑（与 focus_schedule_2 表一致）
 *
 * @author My
 * @version 1.0 2026/03/05
 * @since hojoai-webmvc-omniasst-focus 1.0.0
 */
public final class ScheduleAssembler {

    private ScheduleAssembler() {
    }

    /**
     * 根据规则 PO 及起止时间构建 SchedulePO
     *
     * @param scheduleId 日程主键（由调用方生成，如 Snowflake）
     * @param rulePO     规则 PO
     * @param startTime  开始时间
     * @param endTime    结束时间
     * @param isRepeat   是否重复
     * @return SchedulePO
     */
    public static SchedulePO buildSchedulePO(Long scheduleId, ScheduleRulePO rulePO, LocalDateTime startTime, LocalDateTime endTime, Boolean isRepeat) {
        SchedulePO po = new SchedulePO();
        po.setId(scheduleId);
        po.setUserId(rulePO.getUserId());
        po.setRuleId(rulePO.getId());
        po.setSysId(rulePO.getSysId());
        po.setIsSysSync(rulePO.getIsSysSync());
        po.setConversationId(rulePO.getConversationId());
        po.setTitle(rulePO.getTitle());
        po.setRemark(rulePO.getRemark());
        po.setType(rulePO.getType());
        if (Boolean.TRUE.equals(isRepeat)) {
            po.setStartTime(startTime);
            po.setEndTime(endTime);
        } else {
            po.setStartTime(rulePO.getStartDate().atTime(rulePO.getStartTime()));
            po.setEndTime(rulePO.getEndDate().atTime(rulePO.getEndTime()));
        }
        po.setTimeZone(rulePO.getTimeZone());
        po.setIcon(rulePO.getIcon());
        po.setIsRemind(rulePO.getIsRemind() != null ? rulePO.getIsRemind() : false);
        po.setStatus(ScheduleStatusEnum.PENDING.getCode());
        po.setIsConflict(false);
        po.setConflictCount(0);
        po.setTag(TagEnum.NORMAL.getCode());
        po.setIsCreate(rulePO.getIsCreate());
        // 是否开启 AI 建议：从规则的 is_create_sug 同步到日程
        po.setIsAiSuggestion(rulePO.getIsCreateSug() != null && rulePO.getIsCreateSug() == 1);

        LocalDateTime localDateTime = LocalDateTime.now();
        po.setCreateTime(localDateTime);
        po.setUpdateTime(localDateTime);
        return po;
    }

    /**
     * 根据更新后的规则 PO 更新 SchedulePO 的可同步字段（保留 id、ruleId、startTime、endTime 等）
     */
    public static void updateSchedulePOFromRule(SchedulePO schedule, ScheduleRulePO rulePO) {
        if (schedule == null || rulePO == null) {
            return;
        }
        schedule.setSysId(rulePO.getSysId());
        schedule.setConversationId(rulePO.getConversationId());
        schedule.setTitle(rulePO.getTitle());
        schedule.setRemark(rulePO.getRemark());
        schedule.setType(rulePO.getType());
        schedule.setIcon(rulePO.getIcon());
        schedule.setIsRemind(rulePO.getIsRemind() != null ? rulePO.getIsRemind() : false);
        schedule.setIsCreate(rulePO.getIsCreate());
        schedule.setIsAiSuggestion(rulePO.getIsCreateSug() != null && rulePO.getIsCreateSug() == 1);
    }

    /**
     * 根据规则 PO 更新 SchedulePO，并用日程的日期 + 规则的时间更新 startTime/endTime（用于编辑“全部”或“当前及以后”）
     */
    public static void updateSchedulePOFromRuleWithTime(SchedulePO schedule, ScheduleRulePO rulePO) {
        if (schedule == null || rulePO == null) {
            return;
        }
        updateSchedulePOFromRule(schedule, rulePO);
        if (rulePO.getStartTime() != null && schedule.getStartTime() != null) {
            schedule.setStartTime(schedule.getStartTime().toLocalDate().atTime(rulePO.getStartTime()));
        }
        if (rulePO.getEndTime() != null && schedule.getEndTime() != null) {
            schedule.setEndTime(schedule.getEndTime().toLocalDate().atTime(rulePO.getEndTime()));
        }
    }

}
