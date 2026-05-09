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

import com.hojoai.omniasst.focus.common.assembler.RuleSportAssembler;
import com.hojoai.omniasst.focus.common.enums.TypeEnum;
import com.hojoai.omniasst.focus.domain.ManualCreateScheduleDataParser;
import com.hojoai.omniasst.focus.domain.strategy.ScheduleStrategy;
import com.hojoai.omniasst.focus.dto.request.ManualScheduleRequestDTO;
import com.hojoai.omniasst.focus.dto.val.SportCreateOrUpdateVal;
import com.hojoai.omniasst.focus.dto.val.SysScheduleVal;
import com.hojoai.omniasst.focus.repository.mapper.RuleSportMapper;
import com.hojoai.omniasst.focus.repository.mapper.ScheduleSportMapper;
import com.hojoai.omniasst.focus.repository.po.RuleSportPO;
import com.hojoai.omniasst.focus.repository.po.SchedulePO;
import com.hojoai.omniasst.focus.repository.po.ScheduleRulePO;
import com.hojoai.omniasst.focus.repository.po.ScheduleSportPO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;
import xyz.downgoon.snowflake.Snowflake;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * 手动创建运动日程策略实现
 *
 * @author My
 * @version 1.0 2026/03/03
 * @since hojoai-webmvc-omniasst-focus 1.0.0
 */
@Component
public class SportStrategyImpl implements ScheduleStrategy<RuleSportPO, ScheduleSportPO> {

    @Resource
    private RuleSportMapper ruleSportMapper;

    @Resource
    private ScheduleSportMapper scheduleSportMapper;

    @Resource
    private Snowflake snowflake;

    @Override
    public Integer getType() {
        return TypeEnum.SPORT.getCode();
    }

    @Override
    public List<RuleSportPO> createScheduleRule(ManualScheduleRequestDTO requestDTO, ScheduleRulePO rulePO) {
        SportCreateOrUpdateVal data = ManualCreateScheduleDataParser.parseData(requestDTO.getData(), SportCreateOrUpdateVal.class);
        return List.of(createScheduleWithData(requestDTO, data, rulePO));
    }

    private RuleSportPO createScheduleWithData(ManualScheduleRequestDTO requestDTO, SportCreateOrUpdateVal data, ScheduleRulePO rulePO) {
        RuleSportPO po = RuleSportAssembler.buildRuleSportPO(snowflake.nextId(), data, rulePO);
        ruleSportMapper.insert(po);
        return po;
    }

    @Override
    public List<RuleSportPO> createScheduleRule(ScheduleRulePO scheduleRulePO, SysScheduleVal sysScheduleVal) {
        if (sysScheduleVal == null) {
            return List.of();
        }
        RuleSportPO po = RuleSportAssembler.buildRuleSportPO(snowflake.nextId(), sysScheduleVal, scheduleRulePO);
        ruleSportMapper.insert(po);
        return List.of(po);
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<ScheduleSportPO> createSchedule(List<SchedulePO> schedulePOList, List<?> dataRulePOList) {
        if (schedulePOList == null || dataRulePOList == null) {
            return List.of();
        }
        List<RuleSportPO> ruleList = (List<RuleSportPO>) dataRulePOList;
        List<ScheduleSportPO> result = new ArrayList<>();

        schedulePOList.forEach(schedulePO -> {
            List<ScheduleSportPO> scheduleSportPOList = ruleList.stream()
                    .map(ruleSportPO -> RuleSportAssembler.buildScheduleSportPO(snowflake.nextId(), schedulePO, ruleSportPO))
                    .toList();

            result.addAll(scheduleSportPOList);
        });

        scheduleSportMapper.insert(result);

        return result;
    }

    @Override
    public List<ScheduleSportPO> getScheduleData(Long scheduleId) {
        if (scheduleId == null) {
            return List.of();
        }
        return scheduleSportMapper.selectList(
                new LambdaQueryWrapper<ScheduleSportPO>().eq(ScheduleSportPO::getScheduleId, scheduleId));
    }

    @Override
    public List<RuleSportPO> getScheduleRuleData(Long ruleId) {
        if (ruleId == null) {
            return List.of();
        }
        return ruleSportMapper.selectList(
                new LambdaQueryWrapper<RuleSportPO>().eq(RuleSportPO::getRuleId, ruleId));
    }

    @Override
    public void deleteScheduleRuleData(Long ruleId) {
        if (ruleId == null) {
            return;
        }
        ruleSportMapper.delete(
                new LambdaQueryWrapper<RuleSportPO>().eq(RuleSportPO::getRuleId, ruleId));
    }

    @Override
    public void deleteScheduleData(List<Long> scheduleIdList) {
        if (CollectionUtils.isEmpty(scheduleIdList)) {
            return;
        }
        scheduleSportMapper.delete(
                new LambdaQueryWrapper<ScheduleSportPO>().in(ScheduleSportPO::getScheduleId, scheduleIdList));
    }

    @Override
    public List<RuleSportPO> updateScheduleRuleData(ManualScheduleRequestDTO requestDTO, ScheduleRulePO rulePO) {
        SportCreateOrUpdateVal data = ManualCreateScheduleDataParser.parseData(requestDTO.getData(), SportCreateOrUpdateVal.class);
        if (data == null) {
            return List.of();
        }
        List<RuleSportPO> existing = ruleSportMapper.selectList(
                new LambdaQueryWrapper<RuleSportPO>().eq(RuleSportPO::getRuleId, rulePO.getId()));
        if (CollectionUtils.isNotEmpty(existing)) {
            RuleSportPO updated = RuleSportAssembler.buildRuleSportPO(existing.get(0).getId(), data, rulePO);
            ruleSportMapper.updateById(updated);
            return List.of(updated);
        } else {
            RuleSportPO po = RuleSportAssembler.buildRuleSportPO(snowflake.nextId(), data, rulePO);
            ruleSportMapper.insert(po);
            return List.of(po);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void updateScheduleData(List<SchedulePO> schedulePOList, List<?> dataRulePOList) {
        if (CollectionUtils.isEmpty(schedulePOList) || CollectionUtils.isEmpty(dataRulePOList)) {
            return;
        }
        List<RuleSportPO> ruleList = (List<RuleSportPO>) dataRulePOList;
        schedulePOList.forEach(schedulePO -> {
            List<ScheduleSportPO> existing = scheduleSportMapper.selectList(
                    new LambdaQueryWrapper<ScheduleSportPO>().eq(ScheduleSportPO::getScheduleId, schedulePO.getId()));
            for (int i = 0; i < ruleList.size(); i++) {
                RuleSportPO ruleSport = ruleList.get(i);
                if (i < existing.size()) {
                    ScheduleSportPO updated = RuleSportAssembler.buildScheduleSportPO(
                            existing.get(i).getId(), schedulePO, ruleSport);
                    scheduleSportMapper.updateById(updated);
                } else {
                    scheduleSportMapper.insert(RuleSportAssembler.buildScheduleSportPO(
                            snowflake.nextId(), schedulePO, ruleSport));
                }
            }
            for (int i = ruleList.size(); i < existing.size(); i++) {
                scheduleSportMapper.deleteById(existing.get(i).getId());
            }
        });
    }

}
