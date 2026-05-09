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
package com.hojoai.omniasst.focus.domain.strategy.impl;

import com.hojoai.omniasst.focus.common.assembler.RuleMeetingAssembler;
import com.hojoai.omniasst.focus.common.enums.TypeEnum;
import com.hojoai.omniasst.focus.domain.ManualCreateScheduleDataParser;
import com.hojoai.omniasst.focus.domain.strategy.ScheduleStrategy;
import com.hojoai.omniasst.focus.dto.request.ManualScheduleRequestDTO;
import com.hojoai.omniasst.focus.dto.val.MeetingCreateOrUpdateVal;
import com.hojoai.omniasst.focus.dto.val.SysScheduleVal;
import com.hojoai.omniasst.focus.repository.mapper.RuleMeetingMapper;
import com.hojoai.omniasst.focus.repository.mapper.ScheduleMeetingMapper;
import com.hojoai.omniasst.focus.repository.po.RuleMeetingPO;
import com.hojoai.omniasst.focus.repository.po.ScheduleMeetingPO;
import com.hojoai.omniasst.focus.repository.po.SchedulePO;
import com.hojoai.omniasst.focus.repository.po.ScheduleRulePO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;
import xyz.downgoon.snowflake.Snowflake;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * 手动创建会议日程策略实现
 *
 * @author My
 * @version 1.0 2026/03/03
 * @since hojoai-webmvc-omniasst-focus 1.0.0
 */
@Component
public class MeetingStrategyImpl implements ScheduleStrategy<RuleMeetingPO, ScheduleMeetingPO> {

    @Resource
    private RuleMeetingMapper ruleMeetingMapper;

    @Resource
    private ScheduleMeetingMapper scheduleMeetingMapper;

    @Resource
    private Snowflake snowflake;

    @Override
    public Integer getType() {
        return TypeEnum.MEETING.getCode();
    }

    @Override
    public List<RuleMeetingPO> createScheduleRule(ManualScheduleRequestDTO requestDTO, ScheduleRulePO rulePO) {
        MeetingCreateOrUpdateVal data = ManualCreateScheduleDataParser.parseData(requestDTO.getData(), MeetingCreateOrUpdateVal.class);
        return List.of(createScheduleWithData(requestDTO, data, rulePO));
    }

    private RuleMeetingPO createScheduleWithData(ManualScheduleRequestDTO requestDTO, MeetingCreateOrUpdateVal data, ScheduleRulePO rulePO) {
        RuleMeetingPO po = RuleMeetingAssembler.buildRuleMeetingPO(snowflake.nextId(), data, rulePO);
        ruleMeetingMapper.insert(po);
        return po;
    }

    @Override
    public List<RuleMeetingPO> createScheduleRule(ScheduleRulePO scheduleRulePO, SysScheduleVal sysScheduleVal) {
        if (sysScheduleVal == null) {
            return List.of();
        }
        RuleMeetingPO po = RuleMeetingAssembler.buildRuleMeetingPO(snowflake.nextId(), sysScheduleVal, scheduleRulePO);
        ruleMeetingMapper.insert(po);
        return List.of(po);
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<ScheduleMeetingPO> createSchedule(List<SchedulePO> schedulePOList, List<?> dataRulePOList) {
        if (schedulePOList == null || dataRulePOList == null) {
            return List.of();
        }
        List<RuleMeetingPO> ruleList = (List<RuleMeetingPO>) dataRulePOList;
        List<ScheduleMeetingPO> result = new ArrayList<>();

        schedulePOList.forEach(schedulePO -> {
            List<ScheduleMeetingPO> scheduleMeetingPOList = ruleList.stream()
                    .map(ruleMeetingPO -> RuleMeetingAssembler.buildScheduleMeetingPO(snowflake.nextId(), schedulePO, ruleMeetingPO))
                    .toList();

            result.addAll(scheduleMeetingPOList);
        });

        scheduleMeetingMapper.insert(result);

        return result;
    }

    @Override
    public List<ScheduleMeetingPO> getScheduleData(Long scheduleId) {
        if (scheduleId == null) {
            return List.of();
        }
        return scheduleMeetingMapper.selectList(
                new LambdaQueryWrapper<ScheduleMeetingPO>().eq(ScheduleMeetingPO::getScheduleId, scheduleId));
    }

    @Override
    public List<RuleMeetingPO> getScheduleRuleData(Long ruleId) {
        if (ruleId == null) {
            return List.of();
        }
        return ruleMeetingMapper.selectList(
                new LambdaQueryWrapper<RuleMeetingPO>().eq(RuleMeetingPO::getRuleId, ruleId));
    }

    @Override
    public void deleteScheduleRuleData(Long ruleId) {
        if (ruleId == null) {
            return;
        }
        ruleMeetingMapper.delete(
                new LambdaQueryWrapper<RuleMeetingPO>().eq(RuleMeetingPO::getRuleId, ruleId));
    }

    @Override
    public void deleteScheduleData(List<Long> scheduleIdList) {
        if (CollectionUtils.isEmpty(scheduleIdList)) {
            return;
        }
        scheduleMeetingMapper.delete(
                new LambdaQueryWrapper<ScheduleMeetingPO>().in(ScheduleMeetingPO::getScheduleId, scheduleIdList));
    }

    @Override
    public List<RuleMeetingPO> updateScheduleRuleData(ManualScheduleRequestDTO requestDTO, ScheduleRulePO rulePO) {
        MeetingCreateOrUpdateVal data = ManualCreateScheduleDataParser.parseData(requestDTO.getData(), MeetingCreateOrUpdateVal.class);
        if (data == null) {
            return List.of();
        }
        List<RuleMeetingPO> existing = ruleMeetingMapper.selectList(
                new LambdaQueryWrapper<RuleMeetingPO>().eq(RuleMeetingPO::getRuleId, rulePO.getId()));
        if (CollectionUtils.isNotEmpty(existing)) {
            RuleMeetingPO updated = RuleMeetingAssembler.buildRuleMeetingPO(existing.get(0).getId(), data, rulePO);
            ruleMeetingMapper.updateById(updated);
            return List.of(updated);
        } else {
            RuleMeetingPO po = RuleMeetingAssembler.buildRuleMeetingPO(snowflake.nextId(), data, rulePO);
            ruleMeetingMapper.insert(po);
            return List.of(po);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void updateScheduleData(List<SchedulePO> schedulePOList, List<?> dataRulePOList) {
        if (CollectionUtils.isEmpty(schedulePOList) || CollectionUtils.isEmpty(dataRulePOList)) {
            return;
        }
        List<RuleMeetingPO> ruleList = (List<RuleMeetingPO>) dataRulePOList;
        schedulePOList.forEach(schedulePO -> {
            List<ScheduleMeetingPO> existing = scheduleMeetingMapper.selectList(
                    new LambdaQueryWrapper<ScheduleMeetingPO>().eq(ScheduleMeetingPO::getScheduleId, schedulePO.getId()));
            for (int i = 0; i < ruleList.size(); i++) {
                RuleMeetingPO ruleMeeting = ruleList.get(i);
                if (i < existing.size()) {
                    ScheduleMeetingPO updated = RuleMeetingAssembler.buildScheduleMeetingPO(
                            existing.get(i).getId(), schedulePO, ruleMeeting);
                    scheduleMeetingMapper.updateById(updated);
                } else {
                    scheduleMeetingMapper.insert(RuleMeetingAssembler.buildScheduleMeetingPO(
                            snowflake.nextId(), schedulePO, ruleMeeting));
                }
            }
            for (int i = ruleList.size(); i < existing.size(); i++) {
                scheduleMeetingMapper.deleteById(existing.get(i).getId());
            }
        });
    }

}
