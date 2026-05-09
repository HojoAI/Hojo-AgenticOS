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
import com.hojoai.omniasst.focus.repository.ScheduleDetailRepository;
import com.hojoai.omniasst.focus.repository.mapper.ScheduleDetailMapper;
import com.hojoai.omniasst.focus.repository.po.ScheduleDetailPO;
import org.springframework.stereotype.Repository;
import xyz.downgoon.snowflake.Snowflake;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * 日程详情Repository实现类
 *
 * @author My
 * @version 1.0 2026/01/06
 * @since hojoai-webmvc-omniasst.focus 1.0.0
 */
@Repository
public class ScheduleDetailRepositoryImpl implements ScheduleDetailRepository {

    @Resource
    private ScheduleDetailMapper scheduleDetailMapper;

    @Resource
    private Snowflake snowflake;

    @Override
    public Long create(ScheduleDetailPO scheduleDetailPO) {
        // Generate ID using Snowflake algorithm
        scheduleDetailPO.setId(snowflake.nextId());
        LocalDateTime localDateTime = LocalDateTime.now();

        scheduleDetailPO.setCreateTime(localDateTime);
        scheduleDetailPO.setUpdateTime(localDateTime);
        scheduleDetailMapper.insert(scheduleDetailPO);
        return scheduleDetailPO.getId();
    }

    @Override
    public Boolean update(ScheduleDetailPO scheduleDetailPO) {
        scheduleDetailPO.setUpdateTime(LocalDateTime.now());
        int rows = scheduleDetailMapper.updateById(scheduleDetailPO);
        return rows > 0;
    }

    @Override
    public List<ScheduleDetailPO> listByScheduleId(Long scheduleId) {
        return Optional.of(new LambdaQueryWrapper<ScheduleDetailPO>())
                .map(wrapper -> wrapper.eq(ScheduleDetailPO::getScheduleId, scheduleId)
                        .eq(ScheduleDetailPO::getIsDelete, Boolean.FALSE))
                .map(scheduleDetailMapper::selectList)
                .orElse(Collections.emptyList());
    }

    @Override
    public int deleteByScheduleId(Long scheduleId) {
        LambdaQueryWrapper<ScheduleDetailPO> wrapper = new LambdaQueryWrapper<ScheduleDetailPO>()
                .eq(ScheduleDetailPO::getScheduleId, scheduleId);
        return scheduleDetailMapper.delete(wrapper);
    }
}

