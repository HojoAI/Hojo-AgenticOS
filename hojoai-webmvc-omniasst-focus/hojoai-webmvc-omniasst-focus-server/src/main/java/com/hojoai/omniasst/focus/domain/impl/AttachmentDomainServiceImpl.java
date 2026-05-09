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
package com.hojoai.omniasst.focus.domain.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hojoai.omniasst.focus.common.assembler.RuleAttachmentAssembler;
import com.hojoai.omniasst.focus.domain.AttachmentDomainService;
import com.hojoai.omniasst.focus.dto.val.FileVal;
import com.hojoai.omniasst.focus.repository.mapper.RuleAttachmentMapper;
import com.hojoai.omniasst.focus.repository.mapper.ScheduleAttachmentMapper;
import com.hojoai.omniasst.focus.repository.po.RuleAttachmentPO;
import com.hojoai.omniasst.focus.repository.po.ScheduleAttachmentPO;
import com.hojoai.omniasst.focus.repository.po.SchedulePO;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import xyz.downgoon.snowflake.Snowflake;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 附件领域服务实现
 *
 * @author My
 * @version 1.0 2026/03/23
 * @since hojoai-webmvc-omniasst-focus 1.0.0
 */
@Component
public class AttachmentDomainServiceImpl implements AttachmentDomainService {

    @Resource
    private RuleAttachmentMapper ruleAttachmentMapper;

    @Resource
    private ScheduleAttachmentMapper scheduleAttachmentMapper;

    @Resource
    private Snowflake snowflake;

    @Override
    public List<ScheduleAttachmentPO> saveAttachment(Long ruleId, List<FileVal> fileList, List<SchedulePO> schedulePOList) {
        if (ruleId == null || CollectionUtils.isEmpty(fileList)) {
            return null;
        }

        List<RuleAttachmentPO> ruleAttachmentPOList = fileList.stream().map(file -> RuleAttachmentAssembler.buildRuleAttachmentPO(snowflake.nextId(), ruleId, file)).toList();
        ruleAttachmentMapper.insert(ruleAttachmentPOList);

        if (CollectionUtils.isEmpty(schedulePOList)) {
            return null;
        }
        List<ScheduleAttachmentPO> scheduleAttachmentPOList = new ArrayList<>();
        for (SchedulePO schedule : schedulePOList) {
            for (RuleAttachmentPO ruleAttachment : ruleAttachmentPOList) {
                scheduleAttachmentPOList.add(RuleAttachmentAssembler.buildScheduleAttachmentPO(snowflake.nextId(), schedule, ruleAttachment));
            }
        }
        scheduleAttachmentMapper.insert(scheduleAttachmentPOList);

        return scheduleAttachmentPOList;
    }

    @Override
    public List<ScheduleAttachmentPO> saveAttachmentBySchedule(SchedulePO schedule, List<FileVal> fileList) {
        if (schedule == null || CollectionUtils.isEmpty(fileList)) {
            return null;
        }

        List<ScheduleAttachmentPO> scheduleAttachmentPOList = fileList.stream().map(file -> RuleAttachmentAssembler.buildScheduleAttachmentPO(snowflake.nextId(), schedule, file)).toList();
        scheduleAttachmentMapper.insert(scheduleAttachmentPOList);

        return scheduleAttachmentPOList;
    }

    @Override
    public List<ScheduleAttachmentPO> queryByScheduleIdList(List<Long> scheduleIdList) {
        if (CollectionUtils.isEmpty(scheduleIdList)) {
            return Collections.emptyList();
        }
        return scheduleAttachmentMapper.selectList(
                new LambdaQueryWrapper<ScheduleAttachmentPO>()
                        .in(ScheduleAttachmentPO::getScheduleId, scheduleIdList));
    }

    @Override
    public void deleteByScheduleIdList(List<Long> scheduleIdList) {
        if (CollectionUtils.isEmpty(scheduleIdList)) {
            return;
        }
        scheduleAttachmentMapper.delete(
                new LambdaQueryWrapper<ScheduleAttachmentPO>()
                        .in(ScheduleAttachmentPO::getScheduleId, scheduleIdList));
    }

    @Override
    public void deleteRuleId(Long ruleId) {
        ruleAttachmentMapper.delete(
                new LambdaQueryWrapper<RuleAttachmentPO>()
                        .in(RuleAttachmentPO::getRuleId, ruleId));

        scheduleAttachmentMapper.delete(
                new LambdaQueryWrapper<ScheduleAttachmentPO>()
                        .in(ScheduleAttachmentPO::getRuleId, ruleId));
    }

}
