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
package com.hojoai.omniasst.focus.domain;

import com.hojoai.common.response.Response;
import com.hojoai.omniasst.focus.dto.request.ManualScheduleRequestDTO;
import com.hojoai.omniasst.focus.dto.request.SysScheduleRequestDTO;
import com.hojoai.omniasst.focus.dto.response.SysScheduleResponseDTO;
import com.hojoai.omniasst.focus.dto.val.LocationInfoVal;
import com.hojoai.omniasst.focus.dto.val.SysScheduleVal;
import com.hojoai.omniasst.focus.repository.po.SchedulePO;
import com.hojoai.omniasst.focus.repository.po.ScheduleRulePO;

import java.util.List;

/**
 * 日程规则领域服务（创建并落库 ScheduleRulePO 的统一逻辑）
 *
 * @author My
 * @version 1.0 2026/03/03
 * @since hojoai-webmvc-omniasst-focus 1.0.0
 */
public interface ScheduleRuleDomainService {

    /**
     * 根据请求构建并保存 ScheduleRulePO
     *
     * @param userId    用户ID
     * @param requestDTO 手动创建请求
     * @return 已持久化的 ScheduleRulePO
     */
    ScheduleRulePO createScheduleRule(String userId, ManualScheduleRequestDTO requestDTO);

    /**
     * 根据日程规则创建并落库 SchedulePO（focus_schedule_2）
     *
     * @param rulePO 已持久化的日程规则
     * @return 已持久化的 SchedulePO
     */
    List<SchedulePO> createSchedule(ScheduleRulePO rulePO);

    /**
     * 系统同步新增日历
     *
     * @param userId
     * @param addScheduleList
     */
    List<ScheduleRulePO> sysAddSchedule(String userId, List<SysScheduleVal> addScheduleList);

    /**
     * 移除重复日程
     *
     * @param userId
     * @param addScheduleList
     */
    void removeRepeatSchedule(String userId, List<SysScheduleVal> addScheduleList);

    /**
     * 系统同步更新日历
     *
     * @param userId
     * @param updateScheduleList
     */
    List<SchedulePO> sysUpdateSchedule(String userId, List<SysScheduleVal> updateScheduleList);

    /**
     * 系统同步删除日历
     *
     * @param userId
     * @param deleteScheduleList
     */
    void sysDelSchedule(String userId, List<SysScheduleVal> deleteScheduleList);

    /**
     * 获取系统同步日历
     *
     * @param userId
     * @param requestDTO
     * @return
     */
    Response<SysScheduleResponseDTO> getSysUpdateScheduleList(String userId, SysScheduleRequestDTO requestDTO);

    /**
     * 根据规则ID查询 ScheduleRulePO
     *
     * @param ruleId 规则ID
     * @return ScheduleRulePO，不存在则返回 null
     */
    ScheduleRulePO getScheduleRuleById(Long ruleId);

    /**
     * 更新日程规则
     *
     * @param rule 规则PO
     */
    void updateScheduleRule(ScheduleRulePO rule);

    /**
     * 批量更新日程规则
     *
     * @param rules 规则PO列表
     */
    void batchUpdateScheduleRules(List<ScheduleRulePO> rules);

    /**
     * 更新单条日程的位置：删除旧 ScheduleLocationPO，按 locationInfo 新建（为 null 则仅删除）
     *
     * @param scheduleId   日程ID
     * @param locationInfo 新位置信息，可为 null
     */
    void updateScheduleLocation(Long scheduleId, LocationInfoVal locationInfo);

    /**
     * 更新规则及其所有日程的位置：
     * 删除旧 RuleLocationPO 和批量 ScheduleLocationPO，按 locationInfo 重建（为 null 则仅删除）
     *
     * @param rulePO       已持久化的规则，locationId 字段会被同步更新
     * @param scheduleList 需要同步位置的日程列表
     * @param locationInfo 新位置信息，可为 null
     */
    void updateRuleAndScheduleLocations(ScheduleRulePO rulePO, List<SchedulePO> scheduleList, LocationInfoVal locationInfo);

    /**
     * 批量删除 ScheduleLocationPO
     *
     * @param scheduleIdList 日程ID列表
     */
    void deleteScheduleLocations(List<Long> scheduleIdList);

}
