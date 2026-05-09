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

import com.hojoai.omniasst.common.api.conversation.UpdateMessageExtraRequest;
import com.hojoai.omniasst.focus.common.enums.RemindStatusEnum;
import com.hojoai.omniasst.focus.common.enums.ScheduleStatusEnum;
import com.hojoai.omniasst.focus.common.enums.TagEnum;
import com.hojoai.omniasst.focus.domain.ScheduleDomainService;
import com.hojoai.omniasst.focus.dto.request.CardCreateScheduleRequestDTO;
import com.hojoai.omniasst.focus.repository.*;
import com.hojoai.omniasst.focus.repository.po.DailyBriefingPO;
import com.hojoai.omniasst.focus.repository.po.ScheduleDetailPO;
import com.hojoai.omniasst.focus.repository.po.SchedulePO;
import com.hojoai.omniasst.focus.repository.po.ScheduleReminderPO;
import com.hojoai.omniasst.focus.service.api.ManagementAPI;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Schedule Domain Service Implementation
 *
 * @author My
 * @version 1.0 2024/12/29
 * @since hojoai-webmvc-omniasst.focus 1.0.0
 */
@Component
public class ScheduleDomainServiceImpl implements ScheduleDomainService {

    private static final Logger logger = LoggerFactory.getLogger(ScheduleDomainServiceImpl.class);

    @Resource
    private ScheduleRepository scheduleRepository;

    @Resource
    private ScheduleReminderRepository scheduleReminderRepository;

    @Resource
    private ScheduleConflictRepository scheduleConflictRepository;

    @Resource
    private DailyBriefingRepository dailyBriefingRepository;

    @Resource
    private ScheduleDetailRepository scheduleDetailRepository;

    @Resource
    private ManagementAPI managementAPI;

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public Long createSchedule(SchedulePO schedulePO, Integer remindBeforeMinutes, String remindType, Boolean isCreate) {
        // 1. Set day of week
        // schedulePO.setWeekDay(schedulePO.getStartTime().getDayOfWeek().getValue());

        // 2. Detect time conflicts
        String startTime = schedulePO.getStartTime().format(DATE_TIME_FORMATTER);
        String endTime = schedulePO.getEndTime().format(DATE_TIME_FORMATTER);
        List<SchedulePO> conflicts = scheduleRepository.findConflicts(schedulePO.getUserId(), startTime, endTime);

        // 3. Set conflict information
        schedulePO.setIsConflict(CollectionUtils.isEmpty(conflicts) ? Boolean.FALSE : Boolean.TRUE);
        schedulePO.setConflictCount(conflicts.size());
        schedulePO.setStatus(Optional.ofNullable(schedulePO.getStatus()).orElse(ScheduleStatusEnum.PENDING.getCode()));
        schedulePO.setIsCreate(isCreate);

        if (TagEnum.CONFLICT.getCode().equals(schedulePO.getTag()) && CollectionUtils.isEmpty(conflicts)) {
            schedulePO.setTag(TagEnum.NORMAL.getCode());
        }
        
        // 4. Create schedule
        Long scheduleId = scheduleRepository.create(schedulePO);

        // 6. Create reminder
        Optional.of(schedulePO)
                .filter(s -> s.getIsRemind() != null && s.getIsRemind())
                .filter(s -> remindBeforeMinutes != null && remindType != null)
                .ifPresent(s -> createReminder(scheduleId, s.getUserId(), s.getStartTime(),
                        remindBeforeMinutes, remindType));

        return scheduleId;
    }

    /**
     * Create reminder
     */
    private void createReminder(Long scheduleId, String userId, LocalDateTime startTime,
                                Integer remindBeforeMinutes, String remindType) {
        ScheduleReminderPO reminderPO = new ScheduleReminderPO();
        reminderPO.setScheduleId(scheduleId);
        reminderPO.setUserId(userId);
        reminderPO.setRemindBeforeMinutes(remindBeforeMinutes);
        reminderPO.setRemindTime(startTime.minusMinutes(remindBeforeMinutes));
        reminderPO.setRemindStatus(RemindStatusEnum.NOT_REMINDED.getCode());
        reminderPO.setRemindType(remindType);
        // reminderPO.setRemindContent("Your schedule is about to start, please be prepared");

        scheduleReminderRepository.create(reminderPO);
    }

    @Override
    public List<SchedulePO> listByUserIdAndDate(String userId, LocalDate date, String isShowSysSync) {
        return scheduleRepository.listByUserIdAndDate(userId, date, isShowSysSync);
    }

    @Override
    public SchedulePO getByUserIdAndScheduleId(String userId, Long scheduleId) {
        return scheduleRepository.getByUserIdAndScheduleId(userId, scheduleId);
    }

    @Override
    public List<SchedulePO> listByUserIdAndRuleId(String userId, Long ruleId) {
        return scheduleRepository.listByUserIdAndRuleId(userId, ruleId);
    }

    @Override
    public List<SchedulePO> listByUserIdAndRuleIdFromTime(String userId, Long ruleId, LocalDateTime startTime) {
        return scheduleRepository.listByUserIdAndRuleIdFromTime(userId, ruleId, startTime);
    }

    @Override
    public boolean deleteScheduleList(List<SchedulePO> schedulePOList) {
        return scheduleRepository.delete(schedulePOList);
    }

    @Override
    public List<Long> listConflictScheduleIds(Long scheduleId) {
        return scheduleConflictRepository.listConflictScheduleIds(scheduleId);
    }

    @Override
    public DailyBriefingPO queryDailyBriefing(String userId, LocalDate date) {
        return dailyBriefingRepository.queryByUserIdAndDate(userId, date);
    }

    @Override
    public List<ScheduleDetailPO> listScheduleDetails(Long scheduleId) {
        return scheduleDetailRepository.listByScheduleId(scheduleId);
    }

    @Override
    public boolean updateSchedule(SchedulePO schedulePO) {
        return scheduleRepository.update(schedulePO);
    }

    @Override
    public List<SchedulePO> cardCreateSchedule(String userId, List<Long> scheduleIdList) {
        // 1. Query schedules where isCreate is false
        List<SchedulePO> scheduleList = scheduleRepository.listByUserIdAndScheduleIds(userId, scheduleIdList);
        
        if (scheduleList.isEmpty()) {
            return List.of();
        }
        
        // 2. Extract schedule IDs
        List<SchedulePO> updateScheduleList = scheduleList.stream()
                .map(schedule -> {
                    schedule.setIsCreate(Boolean.TRUE);
                    return schedule;
                }).toList();

        // 3. Update isCreate to true
        scheduleRepository.updateIsCreateToTrue(updateScheduleList);
        
        // 4. Return the schedule list
        return updateScheduleList;
    }

    @Override
    public boolean batchUpdateSchedules(List<SchedulePO> scheduleList) {
        return scheduleRepository.batchUpdate(scheduleList);
    }

    /**
     * 根据日期列表查询日程
     *
     * @param userId 用户ID
     * @param dateStrList 日期字符串列表 (格式: yyyy-MM-dd)
     * @return 日程列表
     * @throws Exception 查询失败时抛出异常
     */
    @Override
    public List<SchedulePO> querySchedulesByDates(String userId, List<String> dateStrList) throws Exception {
        if (dateStrList == null || dateStrList.isEmpty()) {
            return new ArrayList<>();
        }

        List<SchedulePO> allSchedules = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        for (String dateStr : dateStrList) {
            try {
                LocalDate date = LocalDate.parse(dateStr, formatter);
                List<SchedulePO> schedules = scheduleRepository.listByUserIdAndDate(userId, date);
                if (schedules != null && !schedules.isEmpty()) {
                    allSchedules.addAll(schedules);
                }
            } catch (Exception e) {
                logger.warn("解析日期失败或查询日程失败, dateStr={}", dateStr, e);
            }
        }

        return allSchedules;
    }

    @Override
    public List<SchedulePO> querySchedulesByDateList(String userId, List<LocalDate> dateList) throws Exception {
        List<SchedulePO> allSchedules = new ArrayList<>();

        dateList.forEach(date -> {
            List<SchedulePO> schedules = scheduleRepository.listByUserIdAndDate(userId, date);

            if (CollectionUtils.isNotEmpty(schedules)) {
                allSchedules.addAll(schedules);
            }
        });

        return allSchedules;
    }

    @Async
    @Override
    public void invokeToPAConversation(String userId, CardCreateScheduleRequestDTO request) {
        UpdateMessageExtraRequest updateMessageExtraRequest = new UpdateMessageExtraRequest();
        updateMessageExtraRequest.setConversationId(request.getConversationId());
        updateMessageExtraRequest.setMessageId(request.getMessageId());
        updateMessageExtraRequest.setContentId(request.getContentId());

        updateMessageExtraRequest.setExtra(request.getScheduleIdList());

        managementAPI.createConversationById(updateMessageExtraRequest);
    }

    @Override
    public void suggetionGapHandle(SchedulePO schedulePO, List<SchedulePO> existingScheduleList) {
        if (schedulePO == null || schedulePO.getStartTime() == null || schedulePO.getEndTime() == null) {
            logger.warn("新增日程信息不完整，跳过处理");
            return;
        }

        if (CollectionUtils.isEmpty(existingScheduleList)) {
            return;
        }

        LocalDateTime newStartTime = schedulePO.getStartTime();
        LocalDateTime newEndTime = schedulePO.getEndTime();
        List<SchedulePO> schedulesToDelete = new ArrayList<>();

        // 1. 找到新增日程前后最近的日程
        SchedulePO beforeSchedule = null;
        SchedulePO afterSchedule = null;
        
        for (SchedulePO schedule : existingScheduleList) {
            if (schedule.getStartTime() == null || schedule.getEndTime() == null) {
                continue;
            }
            
            // 找到新增日程之前的最近日程（结束时间在新增日程开始时间之前或相等）
            if (schedule.getEndTime().isBefore(newStartTime) || schedule.getEndTime().equals(newStartTime)) {
                if (beforeSchedule == null || schedule.getEndTime().isAfter(beforeSchedule.getEndTime())) {
                    beforeSchedule = schedule;
                }
            }
            
            // 找到新增日程之后的最近日程（开始时间在新增日程结束时间之后或相等）
            if (schedule.getStartTime().isAfter(newEndTime) || schedule.getStartTime().equals(newEndTime)) {
                if (afterSchedule == null || schedule.getStartTime().isBefore(afterSchedule.getStartTime())) {
                    afterSchedule = schedule;
                }
            }
        }

        // 2. 判断新增日程前后间隔是否小于2小时，并且中间存在tag=3的日程
        if (beforeSchedule != null && afterSchedule != null) {
            // 计算新增日程开始时间到前一个日程结束时间的间隔
            long gapBeforeHours = ChronoUnit.HOURS.between(beforeSchedule.getEndTime(), newStartTime);
            // 计算新增日程结束时间到后一个日程开始时间的间隔
            long gapAfterHours = ChronoUnit.HOURS.between(newEndTime, afterSchedule.getStartTime());
            
            // 如果前后间隔都小于2小时，查找中间tag=3的日程（在前一个日程结束时间到后一个日程开始时间之间）
            if (gapBeforeHours < 2 && gapAfterHours < 2) {
                LocalDateTime gapStart = beforeSchedule.getEndTime();
                LocalDateTime gapEnd = afterSchedule.getStartTime();

                SchedulePO finalBeforeSchedule = beforeSchedule;
                SchedulePO finalAfterSchedule = afterSchedule;
                List<SchedulePO> tag3SchedulesInGap = existingScheduleList.stream()
                        .filter(schedule -> {
                            if (schedule.getStartTime() == null || schedule.getEndTime() == null) {
                                return false;
                            }
                            // tag=3 且时间在前后日程之间（不包括前后日程本身）
                            return schedule.getTag() != null && schedule.getTag() == 3
                                    && schedule.getStartTime().isAfter(gapStart)
                                    && schedule.getEndTime().isBefore(gapEnd)
                                    && !schedule.equals(finalBeforeSchedule)
                                    && !schedule.equals(finalAfterSchedule);
                        })
                        .toList();
                
                if (CollectionUtils.isNotEmpty(tag3SchedulesInGap)) {
                    schedulesToDelete.addAll(tag3SchedulesInGap);
                }
            }
        }

        // 3. 过滤出tag=3的日程
        List<SchedulePO> tag3Schedules = existingScheduleList.stream()
                .filter(schedule -> schedule.getTag() != null && schedule.getTag() == 3)
                .toList();

        // 4. 判断新增日程与tag=3的日程是否有时间交集，如果有则删除
        for (SchedulePO tag3Schedule : tag3Schedules) {
            if (tag3Schedule.getStartTime() == null || tag3Schedule.getEndTime() == null) {
                continue;
            }
            
            LocalDateTime tag3Start = tag3Schedule.getStartTime();
            LocalDateTime tag3End = tag3Schedule.getEndTime();
            
            // 判断是否有时间交集：新增日程的开始时间小于tag=3日程的结束时间，且新增日程的结束时间大于tag=3日程的开始时间
            boolean hasOverlap = newStartTime.isBefore(tag3End) && newEndTime.isAfter(tag3Start);
            
            if (hasOverlap) {
                schedulesToDelete.add(tag3Schedule);
            }
        }

        // 去重并删除日程
        if (CollectionUtils.isNotEmpty(schedulesToDelete)) {
            List<SchedulePO> uniqueSchedulesToDelete = schedulesToDelete.stream()
                    .distinct()
                    .toList();
            
            scheduleRepository.delete(uniqueSchedulesToDelete);
            logger.info("删除了 {} 个符合条件的日程", uniqueSchedulesToDelete.size());
        }
    }

    @Override
    public SchedulePO getScheduleByUserIdAndMeetingId(String userId, String meetingId) {
        return scheduleRepository.getScheduleByUserIdAndMeetingId(userId, meetingId);
    }

    @Override
    public void deleteScheduleList(Long ruleId) {
        scheduleRepository.deleteByRuleId(ruleId);
    }

    @Override
    public boolean hasTimeConflict(String userId, SchedulePO schedule) {
        if (schedule == null || schedule.getStartTime() == null || schedule.getEndTime() == null) {
            return false;
        }
        String startTime = schedule.getStartTime().format(DATE_TIME_FORMATTER);
        String endTime = schedule.getEndTime().format(DATE_TIME_FORMATTER);
        List<SchedulePO> conflicts = scheduleRepository.findConflicts(userId, startTime, endTime);
        if (CollectionUtils.isEmpty(conflicts)) {
            return false;
        }
        return conflicts.stream().anyMatch(c -> !c.getId().equals(schedule.getId()));
    }

    @Override
    public void deleteScheduleDetails(Long scheduleId) {
        scheduleDetailRepository.deleteByScheduleId(scheduleId);
    }

}
