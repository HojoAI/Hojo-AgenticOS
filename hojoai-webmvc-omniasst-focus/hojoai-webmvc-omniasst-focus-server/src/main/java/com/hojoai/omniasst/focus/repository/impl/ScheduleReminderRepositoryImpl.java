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
package com.hojoai.omniasst.focus.repository.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hojoai.omniasst.focus.repository.ScheduleReminderRepository;
import com.hojoai.omniasst.focus.repository.mapper.ScheduleReminderMapper;
import com.hojoai.omniasst.focus.repository.po.ScheduleReminderPO;
import org.springframework.stereotype.Repository;
import xyz.downgoon.snowflake.Snowflake;

import javax.annotation.Resource;
import java.time.LocalDateTime;

/**
 * 日程提醒Repository实现类
 *
 * @author My
 * @version 1.0 2024/12/29
 * @since hojoai-webmvc-omniasst.focus 1.0.0
 */
@Repository
public class ScheduleReminderRepositoryImpl implements ScheduleReminderRepository {

    @Resource
    private ScheduleReminderMapper scheduleReminderMapper;

    @Resource
    private Snowflake snowflake;

    @Override
    public Long create(ScheduleReminderPO reminderPO) {
        // Generate ID using Snowflake algorithm
        reminderPO.setId(snowflake.nextId());

        LocalDateTime localDateTime = LocalDateTime.now();

        reminderPO.setCreateTime(localDateTime);
        reminderPO.setUpdateTime(localDateTime);

        scheduleReminderMapper.insert(reminderPO);
        return reminderPO.getId();
    }

    @Override
    public ScheduleReminderPO getByScheduleId(Long scheduleId) {
        LambdaQueryWrapper<ScheduleReminderPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ScheduleReminderPO::getScheduleId, scheduleId)
                .eq(ScheduleReminderPO::getIsDelete, Boolean.FALSE)
                .orderByDesc(ScheduleReminderPO::getCreateTime)
                .last("LIMIT 1");
        return scheduleReminderMapper.selectOne(wrapper);
    }
}
