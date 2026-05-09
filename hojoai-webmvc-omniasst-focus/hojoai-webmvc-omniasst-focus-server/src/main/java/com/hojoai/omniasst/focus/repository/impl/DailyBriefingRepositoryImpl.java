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
import com.hojoai.omniasst.focus.repository.DailyBriefingRepository;
import com.hojoai.omniasst.focus.repository.mapper.DailyBriefingMapper;
import com.hojoai.omniasst.focus.repository.po.DailyBriefingPO;
import org.springframework.stereotype.Repository;
import xyz.downgoon.snowflake.Snowflake;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Daily Briefing Repository Implementation
 *
 * @author My
 * @version 1.0 2026/01/06
 * @since hojoai-webmvc-omniasst.focus 1.0.0
 */
@Repository
public class DailyBriefingRepositoryImpl implements DailyBriefingRepository {

    @Resource
    private DailyBriefingMapper dailyBriefingMapper;

    @Resource
    private Snowflake snowflake;

    @Override
    public DailyBriefingPO queryByUserIdAndDate(String userId, LocalDate date) {
        return Optional.of(new LambdaQueryWrapper<DailyBriefingPO>())
                .map(wrapper -> wrapper.eq(DailyBriefingPO::getUserId, userId)
                        .eq(DailyBriefingPO::getBriefingDate, date)
                        .eq(DailyBriefingPO::getIsDelete, Boolean.FALSE))
                .map(dailyBriefingMapper::selectOne)
                .orElse(null);
    }

    @Override
    public Long create(DailyBriefingPO dailyBriefingPO) {
        // Generate ID using Snowflake algorithm
        dailyBriefingPO.setId(snowflake.nextId());

        LocalDateTime localDateTime = LocalDateTime.now();
        dailyBriefingPO.setCreateTime(localDateTime);
        dailyBriefingPO.setUpdateTime(localDateTime);

        dailyBriefingMapper.insert(dailyBriefingPO);
        return dailyBriefingPO.getId();
    }

    @Override
    public boolean update(DailyBriefingPO dailyBriefingPO) {
        dailyBriefingPO.setUpdateTime(LocalDateTime.now());
        int rows = dailyBriefingMapper.updateById(dailyBriefingPO);
        return rows > 0;
    }
}

