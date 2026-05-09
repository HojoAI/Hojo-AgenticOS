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
import com.hojoai.omniasst.focus.repository.ScheduleConflictRepository;
import com.hojoai.omniasst.focus.repository.mapper.ScheduleConflictMapper;
import com.hojoai.omniasst.focus.repository.po.ScheduleConflictPO;
import org.springframework.stereotype.Repository;
import xyz.downgoon.snowflake.Snowflake;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Schedule Conflict Repository Implementation
 *
 * @author My
 * @version 1.0 2024/12/29
 * @since hojoai-webmvc-omniasst.focus 1.0.0
 */
@Repository
public class ScheduleConflictRepositoryImpl implements ScheduleConflictRepository {

    @Resource
    private ScheduleConflictMapper scheduleConflictMapper;

    @Resource
    private Snowflake snowflake;

    @Override
    public Long create(ScheduleConflictPO conflictPO) {
        // Generate ID using Snowflake algorithm
        conflictPO.setId(snowflake.nextId());

        LocalDateTime localDateTime = LocalDateTime.now();

        conflictPO.setCreateTime(localDateTime);
        conflictPO.setUpdateTime(localDateTime);

        scheduleConflictMapper.insert(conflictPO);
        return conflictPO.getId();
    }

    @Override
    public List<Long> listConflictScheduleIds(Long scheduleId) {
        return Optional.of(new LambdaQueryWrapper<ScheduleConflictPO>())
                .map(wrapper -> wrapper.eq(ScheduleConflictPO::getScheduleId, scheduleId)
                        .eq(ScheduleConflictPO::getIsDelete, Boolean.FALSE))
                .map(scheduleConflictMapper::selectList)
                .orElse(List.of())
                .stream()
                .map(ScheduleConflictPO::getConflictScheduleId)
                .collect(Collectors.toList());
    }
}
