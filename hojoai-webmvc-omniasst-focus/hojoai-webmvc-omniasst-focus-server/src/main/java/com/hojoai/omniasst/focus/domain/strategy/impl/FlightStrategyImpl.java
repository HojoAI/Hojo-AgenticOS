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

import com.hojoai.omniasst.focus.common.assembler.RuleFlightAssembler;
import com.hojoai.omniasst.focus.common.enums.TypeEnum;
import com.hojoai.omniasst.focus.domain.ManualCreateScheduleDataParser;
import com.hojoai.omniasst.focus.domain.strategy.ScheduleStrategy;
import com.hojoai.omniasst.focus.dto.request.ManualScheduleRequestDTO;
import com.hojoai.omniasst.focus.dto.val.FlightVal;
import com.hojoai.omniasst.focus.dto.val.SysScheduleVal;
import com.hojoai.omniasst.focus.repository.mapper.RuleFlightMapper;
import com.hojoai.omniasst.focus.repository.mapper.ScheduleFlightMapper;
import com.hojoai.omniasst.focus.repository.po.RuleFlightPO;
import com.hojoai.omniasst.focus.repository.po.ScheduleFlightPO;
import com.hojoai.omniasst.focus.repository.po.SchedulePO;
import com.hojoai.omniasst.focus.repository.po.ScheduleRulePO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;
import xyz.downgoon.snowflake.Snowflake;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 手动创建航班日程策略实现
 *
 * @author My
 * @version 1.0 2026/03/03
 * @since hojoai-webmvc-omniasst-focus 1.0.0
 */
@Component
public class FlightStrategyImpl implements ScheduleStrategy<RuleFlightPO, ScheduleFlightPO> {

    @Resource
    private RuleFlightMapper ruleFlightMapper;

    @Resource
    private ScheduleFlightMapper scheduleFlightMapper;

    @Resource
    private Snowflake snowflake;

    @Override
    public Integer getType() {
        return TypeEnum.FLIGHT.getCode();
    }

    @Override
    public List<RuleFlightPO> createScheduleRule(ManualScheduleRequestDTO requestDTO, ScheduleRulePO rulePO) {
        FlightVal data = ManualCreateScheduleDataParser.parseData(requestDTO.getData(), FlightVal.class);
        return Collections.singletonList(createScheduleWithData(requestDTO, data, rulePO));
    }

    /**
     * 将 requestDTO.getData() 解析为 List&lt;FlightVal&gt;
     */
    @SuppressWarnings("unchecked")
    private List<FlightVal> parseFlightValList(Object data) {
        if (data == null) {
            return List.of();
        }
        if (data instanceof List) {
            List<?> rawList = (List<?>) data;
            List<FlightVal> list = new ArrayList<>(rawList.size());
            for (Object item : rawList) {
                FlightVal fv = ManualCreateScheduleDataParser.parseData(item, FlightVal.class);
                if (fv != null) {
                    list.add(fv);
                }
            }
            return list;
        }
        FlightVal single = ManualCreateScheduleDataParser.parseData(data, FlightVal.class);
        return single != null ? List.of(single) : List.of();
    }

    private RuleFlightPO createScheduleWithData(ManualScheduleRequestDTO requestDTO, FlightVal data, ScheduleRulePO rulePO) {
        RuleFlightPO po = RuleFlightAssembler.buildRuleFlightPO(snowflake.nextId(), data, rulePO);
        ruleFlightMapper.insert(po);
        return po;
    }

    @Override
    public List<RuleFlightPO> createScheduleRule(ScheduleRulePO scheduleRulePO, SysScheduleVal sysScheduleVal) {
        if (sysScheduleVal == null) {
            return List.of();
        }
        RuleFlightPO po = RuleFlightAssembler.buildRuleFlightPO(snowflake.nextId(), sysScheduleVal, scheduleRulePO);
        ruleFlightMapper.insert(po);
        return List.of(po);
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<ScheduleFlightPO> createSchedule(List<SchedulePO> schedulePOList, List<?> dataRulePOList) {
        if (schedulePOList == null || dataRulePOList == null) {
            return List.of();
        }
        List<RuleFlightPO> ruleList = (List<RuleFlightPO>) dataRulePOList;
        List<ScheduleFlightPO> result = new ArrayList<>();

        schedulePOList.forEach(schedulePO -> {
            List<ScheduleFlightPO> scheduleFlightPOList = ruleList.stream()
                    .map(ruleFlightPO -> RuleFlightAssembler.buildScheduleFlightPO(snowflake.nextId(), schedulePO.getId(), ruleFlightPO))
                    .toList();

            result.addAll(scheduleFlightPOList);
        });

        scheduleFlightMapper.insert(result);

        return result;
    }

    @Override
    public List<ScheduleFlightPO> getScheduleData(Long scheduleId) {
        if (scheduleId == null) {
            return List.of();
        }
        return scheduleFlightMapper.selectList(
                new LambdaQueryWrapper<ScheduleFlightPO>().eq(ScheduleFlightPO::getScheduleId, scheduleId));
    }

    @Override
    public List<RuleFlightPO> getScheduleRuleData(Long ruleId) {
        if (ruleId == null) {
            return List.of();
        }
        return ruleFlightMapper.selectList(
                new LambdaQueryWrapper<RuleFlightPO>().eq(RuleFlightPO::getRuleId, ruleId));
    }

    @Override
    public void deleteScheduleRuleData(Long ruleId) {
        if (ruleId == null) {
            return;
        }
        ruleFlightMapper.delete(
                new LambdaQueryWrapper<RuleFlightPO>().eq(RuleFlightPO::getRuleId, ruleId));
    }

    @Override
    public void deleteScheduleData(List<Long> scheduleIdList) {
        if (CollectionUtils.isEmpty(scheduleIdList)) {
            return;
        }
        scheduleFlightMapper.delete(
                new LambdaQueryWrapper<ScheduleFlightPO>().in(ScheduleFlightPO::getScheduleId, scheduleIdList));
    }

    @Override
    public List<RuleFlightPO> updateScheduleRuleData(ManualScheduleRequestDTO requestDTO, ScheduleRulePO rulePO) {
        FlightVal data = ManualCreateScheduleDataParser.parseData(requestDTO.getData(), FlightVal.class);
        if (data == null) {
            return List.of();
        }
        List<RuleFlightPO> existing = ruleFlightMapper.selectList(
                new LambdaQueryWrapper<RuleFlightPO>().eq(RuleFlightPO::getRuleId, rulePO.getId()));
        if (CollectionUtils.isNotEmpty(existing)) {
            RuleFlightPO updated = RuleFlightAssembler.buildRuleFlightPO(existing.get(0).getId(), data, rulePO);
            ruleFlightMapper.updateById(updated);
            return List.of(updated);
        } else {
            RuleFlightPO po = RuleFlightAssembler.buildRuleFlightPO(snowflake.nextId(), data, rulePO);
            ruleFlightMapper.insert(po);
            return List.of(po);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void updateScheduleData(List<SchedulePO> schedulePOList, List<?> dataRulePOList) {
        if (CollectionUtils.isEmpty(schedulePOList) || CollectionUtils.isEmpty(dataRulePOList)) {
            return;
        }
        List<RuleFlightPO> ruleList = (List<RuleFlightPO>) dataRulePOList;
        schedulePOList.forEach(schedulePO -> {
            List<ScheduleFlightPO> existing = scheduleFlightMapper.selectList(
                    new LambdaQueryWrapper<ScheduleFlightPO>().eq(ScheduleFlightPO::getScheduleId, schedulePO.getId()));
            for (int i = 0; i < ruleList.size(); i++) {
                RuleFlightPO ruleFlight = ruleList.get(i);
                if (i < existing.size()) {
                    ScheduleFlightPO updated = RuleFlightAssembler.buildScheduleFlightPO(
                            existing.get(i).getId(), schedulePO.getId(), ruleFlight);
                    scheduleFlightMapper.updateById(updated);
                } else {
                    scheduleFlightMapper.insert(RuleFlightAssembler.buildScheduleFlightPO(
                            snowflake.nextId(), schedulePO.getId(), ruleFlight));
                }
            }
            for (int i = ruleList.size(); i < existing.size(); i++) {
                scheduleFlightMapper.deleteById(existing.get(i).getId());
            }
        });
    }

}
