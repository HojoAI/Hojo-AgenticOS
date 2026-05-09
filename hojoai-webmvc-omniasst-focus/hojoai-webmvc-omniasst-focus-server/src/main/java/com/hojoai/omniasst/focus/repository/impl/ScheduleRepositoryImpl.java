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
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.hojoai.central.provider.service.utils.EnumUtil;
import com.hojoai.omniasst.focus.common.enums.ScheduleStatusEnum;
import com.hojoai.omniasst.focus.common.enums.TagEnum;
import com.hojoai.omniasst.focus.dto.val.AIConflictVal;
import com.hojoai.omniasst.focus.repository.ScheduleRepository;
import com.hojoai.omniasst.focus.repository.mapper.ScheduleMapper;
import com.hojoai.omniasst.focus.repository.mapper.ScheduleMeetingMapper;
import com.hojoai.omniasst.focus.repository.po.ScheduleMeetingPO;
import com.hojoai.omniasst.focus.repository.po.SchedulePO;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.stereotype.Repository;
import xyz.downgoon.snowflake.Snowflake;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Schedule Repository Implementation
 *
 * @author My
 * @version 1.0 2024/12/29
 * @since hojoai-webmvc-omniasst.focus 1.0.0
 */
@Repository
public class ScheduleRepositoryImpl implements ScheduleRepository {

    @Resource
    private ScheduleMapper scheduleMapper;

    @Resource
    private ScheduleMeetingMapper scheduleMeetingMapper;

    @Resource
    private Snowflake snowflake;

    @Resource
    private SqlSessionFactory sqlSessionFactory;

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final int BATCH_SIZE = 1000;

    @Override
    public List<SchedulePO> listByUserIdAndDate(String userId, LocalDate date) {
        if (date == null) {
            return List.of();
        }
        LocalDateTime startOfDay = LocalDateTime.of(date, LocalTime.MIN);
        LocalDateTime endOfDay = LocalDateTime.of(date, LocalTime.MAX);

        return Optional.of(new LambdaQueryWrapper<SchedulePO>())
                .map(wrapper -> wrapper.eq(SchedulePO::getUserId, userId)
                        .eq(SchedulePO::getIsDelete, Boolean.FALSE)
                        .eq(SchedulePO::getIsCreate, Boolean.TRUE)
                        .le(SchedulePO::getStartTime, endOfDay)
                        .ge(SchedulePO::getEndTime, startOfDay)
                        .orderByAsc(SchedulePO::getStartTime))
                .map(scheduleMapper::selectList)
                .orElse(List.of());
    }

    @Override
    public List<SchedulePO> listByDate(LocalDate date) {
        LocalDateTime startOfDay = LocalDateTime.of(date, LocalTime.MIN);
        LocalDateTime endOfDay = LocalDateTime.of(date, LocalTime.MAX);

        return Optional.of(new LambdaQueryWrapper<SchedulePO>())
                .map(wrapper -> wrapper.eq(SchedulePO::getIsDelete, Boolean.FALSE)
                        .eq(SchedulePO::getIsCreate, Boolean.TRUE)
                        .ge(SchedulePO::getStartTime, startOfDay)
                        .le(SchedulePO::getEndTime, endOfDay)
                        .orderByAsc(SchedulePO::getUserId)
                        .orderByAsc(SchedulePO::getStartTime))
                .map(scheduleMapper::selectList)
                .orElse(List.of());
    }

    @Override
    public SchedulePO getByUserIdAndScheduleId(String userId, Long scheduleId) {
        return Optional.of(new LambdaQueryWrapper<SchedulePO>())
                .map(wrapper -> wrapper.eq(SchedulePO::getUserId, userId)
                        .eq(SchedulePO::getId, scheduleId)
                        .eq(SchedulePO::getIsDelete, Boolean.FALSE)
                        .eq(SchedulePO::getIsCreate, Boolean.TRUE))
                .map(scheduleMapper::selectOne)
                .orElse(null);
    }

    @Override
    public Long create(SchedulePO schedulePO) {
        // Generate ID using Snowflake algorithm
        if (Objects.isNull(schedulePO.getId()) || 0 == schedulePO.getId()) {
            schedulePO.setId(snowflake.nextId());
        }

        LocalDateTime localDateTime = LocalDateTime.now();

        schedulePO.setCreateTime(localDateTime);
        schedulePO.setUpdateTime(localDateTime);

        scheduleMapper.insert(schedulePO);

        return schedulePO.getId();
    }

    @Override
    public List<SchedulePO> findConflicts(String userId, String startTime, String endTime) {
        LocalDateTime start = LocalDateTime.parse(startTime, DATE_TIME_FORMATTER);
        LocalDateTime end = LocalDateTime.parse(endTime, DATE_TIME_FORMATTER);

        return Optional.of(new LambdaQueryWrapper<SchedulePO>())
                .map(wrapper -> wrapper.eq(SchedulePO::getUserId, userId)
                        .eq(SchedulePO::getIsDelete, Boolean.FALSE)
                        .eq(SchedulePO::getIsCreate, Boolean.TRUE)
                        .ne(SchedulePO::getStatus, ScheduleStatusEnum.COMPLETED.getCode())
                        .ne(SchedulePO::getStatus, ScheduleStatusEnum.CANCELLED.getCode())
                        .and(w -> w.between(SchedulePO::getStartTime, start, end)
                                .or().between(SchedulePO::getEndTime, start, end)
                                .or().and(w2 -> w2.le(SchedulePO::getStartTime, start)
                                        .ge(SchedulePO::getEndTime, end))))
                .map(scheduleMapper::selectList)
                .orElse(List.of());
    }

    @Override
    public boolean updateConflictInfo(AIConflictVal conflictVal) {
        return Optional.of(new LambdaUpdateWrapper<SchedulePO>())
                .map(wrapper -> wrapper.eq(SchedulePO::getId, conflictVal.getId())
                        .set(SchedulePO::getIsConflict, Boolean.TRUE)
                        .set(SchedulePO::getTag, Objects.requireNonNull(EnumUtil.getEnumByValue(conflictVal.getTags(), TagEnum.class)).getCode())
                        .set(SchedulePO::getConflictSuggestion, conflictVal.getConflictSuggestion()))
                .map(wrapper -> scheduleMapper.update(null, wrapper))
                .filter(count -> count > 0)
                .isPresent();
    }

    @Override
    public boolean update(SchedulePO schedulePO) {
        return scheduleMapper.updateById(schedulePO) > 0;
    }

    @Override
    public boolean updateIsCreateToTrue(List<SchedulePO> scheduleList) {
        if (CollectionUtils.isEmpty(scheduleList)) {
            return false;
        }

        scheduleMapper.updateById(scheduleList);

        return true;
    }

    @Override
    public boolean batchUpdate(List<SchedulePO> scheduleList) {
        scheduleList.forEach(schedule -> schedule.setUpdateTime(LocalDateTime.now()));

        return CollectionUtils.isNotEmpty(scheduleMapper.updateById(scheduleList));
    }

    @Override
    public List<SchedulePO> listByUserIdAndScheduleIds(String userId, List<Long> scheduleIds) {
        if (scheduleIds == null || scheduleIds.isEmpty()) {
            return List.of();
        }
        
        return Optional.of(new LambdaQueryWrapper<SchedulePO>())
                .map(wrapper -> wrapper.eq(SchedulePO::getUserId, userId)
                        .in(SchedulePO::getId, scheduleIds)
                        .eq(SchedulePO::getIsDelete, Boolean.FALSE))
                .map(scheduleMapper::selectList)
                .orElse(List.of());
    }

    @Override
    public List<SchedulePO> listByUserIdAndRuleId(String userId, Long ruleId) {
        if (userId == null || ruleId == null) {
            return List.of();
        }
        return Optional.of(new LambdaQueryWrapper<SchedulePO>())
                .map(wrapper -> wrapper.eq(SchedulePO::getUserId, userId)
                        .eq(SchedulePO::getRuleId, ruleId)
                        .eq(SchedulePO::getIsDelete, Boolean.FALSE)
                        .eq(SchedulePO::getIsCreate, Boolean.TRUE))
                .map(scheduleMapper::selectList)
                .orElse(List.of());
    }

    @Override
    public List<SchedulePO> listByUserIdAndRuleIdFromTime(String userId, Long ruleId, LocalDateTime startTime) {
        if (userId == null || ruleId == null || startTime == null) {
            return List.of();
        }
        return Optional.of(new LambdaQueryWrapper<SchedulePO>())
                .map(wrapper -> wrapper.eq(SchedulePO::getUserId, userId)
                        .eq(SchedulePO::getRuleId, ruleId)
                        .ge(SchedulePO::getStartTime, startTime)
                        .eq(SchedulePO::getIsDelete, Boolean.FALSE)
                        .eq(SchedulePO::getIsCreate, Boolean.TRUE)
                        .orderByAsc(SchedulePO::getStartTime))
                .map(scheduleMapper::selectList)
                .orElse(List.of());
    }

    @Override
    public List<String> listAllUserIds() {
        return scheduleMapper.selectAllUserIds();
    }

    @Override
    public boolean delete(List<SchedulePO> schedulePOList) {
        if (CollectionUtils.isEmpty(schedulePOList)) {
            return false;
        }
        // 逻辑删除：0-未删除，1-已删除；将 is_delete 置为 1
        List<Long> idList = schedulePOList.stream().map(SchedulePO::getId).toList();
        LambdaUpdateWrapper<SchedulePO> wrapper = new LambdaUpdateWrapper<SchedulePO>()
                .in(SchedulePO::getId, idList)
                .set(SchedulePO::getIsDelete, Boolean.TRUE)  // 1-已删除
                .set(SchedulePO::getUpdateTime, LocalDateTime.now());
        return scheduleMapper.update(null, wrapper) > 0;
    }

    @Override
    public SchedulePO getScheduleByUserIdAndMeetingId(String userId, String meetingId) {
        if (StringUtils.isBlank(userId) || StringUtils.isBlank(meetingId)) {
            return null;
        }
        ScheduleMeetingPO meeting = scheduleMeetingMapper.selectOne(
                new LambdaQueryWrapper<ScheduleMeetingPO>()
                        .eq(ScheduleMeetingPO::getMeetingId, meetingId)
                        .last("LIMIT 1"));
        if (meeting == null || meeting.getScheduleId() == null) {
            return null;
        }
        SchedulePO schedule = scheduleMapper.selectById(meeting.getScheduleId());
        if (schedule == null || !userId.equals(schedule.getUserId())) {
            return null;
        }
        return schedule;
    }

    @Override
    public boolean deleteByRuleId(Long ruleId) {
        if (ruleId == null) {
            return false;
        }
        LambdaUpdateWrapper<SchedulePO> wrapper = new LambdaUpdateWrapper<SchedulePO>()
                .eq(SchedulePO::getRuleId, ruleId)
                .eq(SchedulePO::getIsDelete, Boolean.FALSE)
                .set(SchedulePO::getIsDelete, Boolean.TRUE)
                .set(SchedulePO::getUpdateTime, LocalDateTime.now());
        return scheduleMapper.update(null, wrapper) > 0;
    }

    @Override
    public List<SchedulePO> listByUserIdAndDate(String userId, LocalDate date, String isShowSysSync) {
        if (date == null) {
            return List.of();
        }
        LocalDateTime startOfDay = LocalDateTime.of(date, LocalTime.MIN);
        LocalDateTime endOfDay = LocalDateTime.of(date, LocalTime.MAX);

        boolean excludeSysSync = !"1".equals(isShowSysSync);
        return Optional.of(new LambdaQueryWrapper<SchedulePO>())
                .map(wrapper -> wrapper.eq(SchedulePO::getUserId, userId)
                        .eq(SchedulePO::getIsDelete, Boolean.FALSE)
                        .eq(SchedulePO::getIsCreate, Boolean.TRUE)
                        .ne(excludeSysSync, SchedulePO::getIsSysSync, 1)
                        .le(SchedulePO::getStartTime, endOfDay)
                        .ge(SchedulePO::getEndTime, startOfDay)
                        .orderByAsc(SchedulePO::getStartTime))
                .map(scheduleMapper::selectList).orElse(List.of());
    }
}
