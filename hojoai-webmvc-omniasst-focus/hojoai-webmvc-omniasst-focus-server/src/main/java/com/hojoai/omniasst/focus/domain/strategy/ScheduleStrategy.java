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
package com.hojoai.omniasst.focus.domain.strategy;

import com.hojoai.omniasst.focus.dto.request.ManualScheduleRequestDTO;
import com.hojoai.omniasst.focus.dto.val.SysScheduleVal;
import com.hojoai.omniasst.focus.repository.po.ScheduleRulePO;
import com.hojoai.omniasst.focus.repository.po.SchedulePO;

import java.util.List;

/**
 * 手动创建日程策略接口（按日程类型分发）
 * 由各 ManualCreateXxxStrategyImpl 实现
 *
 * @param <T> 各实现类创建的规则明细 PO 类型（如 RuleMeetingPO、RuleFlightPO 等）
 * @param <R> createSchedule 方法返回值类型
 * @author My
 * @version 1.0 2026/03/03
 * @since hojoai-webmvc-omniasst-focus 1.0.0
 */
public interface ScheduleStrategy<T, R> {

    /**
     * 支持的日程类型
     * 1-会议；2-运动；3-电影；4-航班；5-火车
     */
    Integer getType();

    /**
     * 执行创建日程规则明细（由 Service 层先创建 ScheduleRulePO 后传入）
     *
     * @param requestDTO 手动创建请求
     * @param rulePO    已创建并落库的日程规则
     * @return 本策略创建的规则明细 PO 列表（如 RuleMeetingPO、RuleFlightPO 等）
     */
    List<T> createScheduleRule(ManualScheduleRequestDTO requestDTO, ScheduleRulePO rulePO);

    /**
     * 根据系统同步日程值对象创建规则明细（系统同步时由 Service 层调用）
     *
     * @param scheduleRulePO 已创建并落库的日程规则
     * @param sysScheduleVal 系统同步的日程数据
     * @return 本策略创建的规则明细 PO 列表（如 RuleMeetingPO、RuleFlightPO 等）
     */
    List<T> createScheduleRule(ScheduleRulePO scheduleRulePO, SysScheduleVal sysScheduleVal);

    /**
     * 根据日程主表与规则明细创建日程（如写入 ScheduleXxx 表等）
     *
     * @param schedulePOList   日程主表 PO 列表（focus_schedule_2）
     * @param dataRulePOList   规则明细 PO 列表（如 RuleMeetingPO、RuleFlightPO 等）
     * @return 创建结果列表，类型由各实现类指定
     */
    List<R> createSchedule(List<SchedulePO> schedulePOList, List<?> dataRulePOList);

    /**
     * 根据日程ID查询该类型下的日程数据明细列表
     *
     * @param scheduleId 日程主表ID（focus_schedule_2.id）
     * @return 本策略对应的日程明细 PO 列表（如 ScheduleMeetingPO、ScheduleFlightPO 等）
     */
    List<R> getScheduleData(Long scheduleId);

    /**
     * 根据规则ID查询规则明细列表（如 RuleFlightPO、RuleMeetingPO 等）
     *
     * @param ruleId 日程规则 ID
     * @return 规则明细 PO 列表
     */
    List<T> getScheduleRuleData(Long ruleId);

    /**
     * 根据规则ID删除规则明细（如 RuleFlightPO、RuleMeetingPO 等）
     *
     * @param ruleId 日程规则 ID
     */
    void deleteScheduleRuleData(Long ruleId);

    /**
     * 根据日程ID列表删除日程明细（如 ScheduleFlightPO、ScheduleMeetingPO 等）
     *
     * @param scheduleIdList 日程 ID 列表
     */
    void deleteScheduleData(List<Long> scheduleIdList);

    /**
     * 更新规则明细（如 RuleFlightPO、RuleMeetingPO 等）；若不存在则新建。
     * 用于"修改全部"/"修改当前及以后"场景，保留主键不变地更新字段。
     *
     * @param requestDTO 手动修改请求（含最新数据）
     * @param rulePO     已更新并持久化的日程规则
     * @return 更新或新建后的规则明细 PO 列表
     */
    default List<T> updateScheduleRuleData(ManualScheduleRequestDTO requestDTO, ScheduleRulePO rulePO) {
        return createScheduleRule(requestDTO, rulePO);
    }

    /**
     * 更新日程明细（如 ScheduleFlightPO、ScheduleMeetingPO 等）；若不存在则新建，多余的删除。
     * 用于"修改全部"/"修改当前及以后"场景，复用 SchedulePO 的主键，仅更新明细字段。
     *
     * @param schedulePOList  已更新的日程主表 PO 列表
     * @param dataRulePOList  规则明细 PO 列表（来自 updateScheduleRuleData 的返回值）
     */
    default void updateScheduleData(List<SchedulePO> schedulePOList, List<?> dataRulePOList) {
    }

}
