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

import com.hojoai.omniasst.focus.common.assembler.RuleTrainAssembler;
import com.hojoai.omniasst.focus.common.enums.TypeEnum;
import com.hojoai.omniasst.focus.domain.ManualCreateScheduleDataParser;
import com.hojoai.omniasst.focus.domain.strategy.ScheduleStrategy;
import com.hojoai.omniasst.focus.dto.request.ManualScheduleRequestDTO;
import com.hojoai.omniasst.focus.dto.val.SysScheduleVal;
import com.hojoai.omniasst.focus.dto.val.TrainVal;
import com.hojoai.omniasst.focus.repository.mapper.RuleTrainMapper;
import com.hojoai.omniasst.focus.repository.mapper.ScheduleTrainMapper;
import com.hojoai.omniasst.focus.repository.po.RuleTrainPO;
import com.hojoai.omniasst.focus.repository.po.SchedulePO;
import com.hojoai.omniasst.focus.repository.po.ScheduleRulePO;
import com.hojoai.omniasst.focus.repository.po.ScheduleTrainPO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;
import xyz.downgoon.snowflake.Snowflake;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * 手动创建火车日程策略实现
 *
 * @author My
 * @version 1.0 2026/03/03
 * @since hojoai-webmvc-omniasst-focus 1.0.0
 */
@Component
public class TrainStrategyImpl implements ScheduleStrategy<RuleTrainPO, ScheduleTrainPO> {

    @Resource
    private RuleTrainMapper ruleTrainMapper;

    @Resource
    private ScheduleTrainMapper scheduleTrainMapper;

    @Resource
    private Snowflake snowflake;

    @Override
    public Integer getType() {
        return TypeEnum.TRAIN.getCode();
    }

    @Override
    public List<RuleTrainPO> createScheduleRule(ManualScheduleRequestDTO requestDTO, ScheduleRulePO rulePO) {
        List<TrainVal> dataList = parseTrainValList(requestDTO.getData());
        if (dataList == null || dataList.isEmpty()) {
            return List.of();
        }
        List<RuleTrainPO> result = new ArrayList<>(dataList.size());
        for (TrainVal data : dataList) {
            result.add(createScheduleWithData(requestDTO, data, rulePO));
        }
        return result;
    }

    /**
     * 将 requestDTO.getData() 解析为 List&lt;TrainVal&gt;
     */
    @SuppressWarnings("unchecked")
    private List<TrainVal> parseTrainValList(Object data) {
        if (data == null) {
            return List.of();
        }
        if (data instanceof List) {
            List<?> rawList = (List<?>) data;
            List<TrainVal> list = new ArrayList<>(rawList.size());
            for (Object item : rawList) {
                TrainVal tv = ManualCreateScheduleDataParser.parseData(item, TrainVal.class);
                if (tv != null) {
                    list.add(tv);
                }
            }
            return list;
        }
        TrainVal single = ManualCreateScheduleDataParser.parseData(data, TrainVal.class);
        return single != null ? List.of(single) : List.of();
    }

    private RuleTrainPO createScheduleWithData(ManualScheduleRequestDTO requestDTO, TrainVal data, ScheduleRulePO rulePO) {
        RuleTrainPO po = RuleTrainAssembler.buildRuleTrainPO(snowflake.nextId(), data, rulePO);
        ruleTrainMapper.insert(po);
        return po;
    }

    @Override
    public List<RuleTrainPO> createScheduleRule(ScheduleRulePO scheduleRulePO, SysScheduleVal sysScheduleVal) {
        if (sysScheduleVal == null) {
            return List.of();
        }
        RuleTrainPO po = RuleTrainAssembler.buildRuleTrainPO(snowflake.nextId(), sysScheduleVal, scheduleRulePO);
        ruleTrainMapper.insert(po);
        return List.of(po);
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<ScheduleTrainPO> createSchedule(List<SchedulePO> schedulePOList, List<?> dataRulePOList) {
        if (schedulePOList == null || dataRulePOList == null) {
            return List.of();
        }
        List<RuleTrainPO> ruleList = (List<RuleTrainPO>) dataRulePOList;
        List<ScheduleTrainPO> result = new ArrayList<>();

        schedulePOList.forEach(schedulePO -> {
            List<ScheduleTrainPO> scheduleTrainPOList = ruleList.stream()
                    .map(ruleTrainPO -> RuleTrainAssembler.buildScheduleTrainPO(snowflake.nextId(), schedulePO.getId(), ruleTrainPO))
                    .toList();

            result.addAll(scheduleTrainPOList);
        });

        scheduleTrainMapper.insert(result);

        return result;
    }

    @Override
    public List<ScheduleTrainPO> getScheduleData(Long scheduleId) {
        if (scheduleId == null) {
            return List.of();
        }
        return scheduleTrainMapper.selectList(
                new LambdaQueryWrapper<ScheduleTrainPO>().eq(ScheduleTrainPO::getScheduleId, scheduleId));
    }

    @Override
    public List<RuleTrainPO> getScheduleRuleData(Long ruleId) {
        if (ruleId == null) {
            return List.of();
        }
        return ruleTrainMapper.selectList(
                new LambdaQueryWrapper<RuleTrainPO>().eq(RuleTrainPO::getRuleId, ruleId));
    }

    @Override
    public void deleteScheduleRuleData(Long ruleId) {
        if (ruleId == null) {
            return;
        }
        ruleTrainMapper.delete(
                new LambdaQueryWrapper<RuleTrainPO>().eq(RuleTrainPO::getRuleId, ruleId));
    }

    @Override
    public void deleteScheduleData(List<Long> scheduleIdList) {
        if (CollectionUtils.isEmpty(scheduleIdList)) {
            return;
        }
        scheduleTrainMapper.delete(
                new LambdaQueryWrapper<ScheduleTrainPO>().in(ScheduleTrainPO::getScheduleId, scheduleIdList));
    }

    @Override
    public List<RuleTrainPO> updateScheduleRuleData(ManualScheduleRequestDTO requestDTO, ScheduleRulePO rulePO) {
        List<TrainVal> dataList = parseTrainValList(requestDTO.getData());
        if (dataList.isEmpty()) {
            return List.of();
        }
        List<RuleTrainPO> existing = ruleTrainMapper.selectList(
                new LambdaQueryWrapper<RuleTrainPO>().eq(RuleTrainPO::getRuleId, rulePO.getId()));
        List<RuleTrainPO> result = new ArrayList<>();
        for (int i = 0; i < dataList.size(); i++) {
            TrainVal data = dataList.get(i);
            if (i < existing.size()) {
                RuleTrainPO updated = RuleTrainAssembler.buildRuleTrainPO(existing.get(i).getId(), data, rulePO);
                ruleTrainMapper.updateById(updated);
                result.add(updated);
            } else {
                RuleTrainPO po = RuleTrainAssembler.buildRuleTrainPO(snowflake.nextId(), data, rulePO);
                ruleTrainMapper.insert(po);
                result.add(po);
            }
        }
        for (int i = dataList.size(); i < existing.size(); i++) {
            ruleTrainMapper.deleteById(existing.get(i).getId());
        }
        return result;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void updateScheduleData(List<SchedulePO> schedulePOList, List<?> dataRulePOList) {
        if (CollectionUtils.isEmpty(schedulePOList) || CollectionUtils.isEmpty(dataRulePOList)) {
            return;
        }
        List<RuleTrainPO> ruleList = (List<RuleTrainPO>) dataRulePOList;
        schedulePOList.forEach(schedulePO -> {
            List<ScheduleTrainPO> existing = scheduleTrainMapper.selectList(
                    new LambdaQueryWrapper<ScheduleTrainPO>().eq(ScheduleTrainPO::getScheduleId, schedulePO.getId()));
            for (int i = 0; i < ruleList.size(); i++) {
                RuleTrainPO ruleTrain = ruleList.get(i);
                if (i < existing.size()) {
                    ScheduleTrainPO updated = RuleTrainAssembler.buildScheduleTrainPO(
                            existing.get(i).getId(), schedulePO.getId(), ruleTrain);
                    scheduleTrainMapper.updateById(updated);
                } else {
                    scheduleTrainMapper.insert(RuleTrainAssembler.buildScheduleTrainPO(
                            snowflake.nextId(), schedulePO.getId(), ruleTrain));
                }
            }
            for (int i = ruleList.size(); i < existing.size(); i++) {
                scheduleTrainMapper.deleteById(existing.get(i).getId());
            }
        });
    }

}
