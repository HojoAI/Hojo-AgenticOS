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
package com.hojoai.omniasst.focus.domain;

import com.hojoai.omniasst.focus.dto.request.CardCreateScheduleRequestDTO;
import com.hojoai.omniasst.focus.repository.po.DailyBriefingPO;
import com.hojoai.omniasst.focus.repository.po.ScheduleDetailPO;
import com.hojoai.omniasst.focus.repository.po.SchedulePO;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Schedule Domain Service Interface
 *
 * @author My
 * @version 1.0 2024/12/29
 * @since hojoai-webmvc-omniasst.focus 1.0.0
 */
public interface ScheduleDomainService {

    /**
     * Create schedule (includes conflict detection, reminder creation and other domain logic)
     *
     * @param schedulePO           Schedule PO
     * @param remindBeforeMinutes Minutes before reminder
     * @param remindType          Reminder type
     * @param isCreate            isCreate
     * @return Schedule ID
     */
    Long createSchedule(SchedulePO schedulePO, Integer remindBeforeMinutes, String remindType, Boolean isCreate);

    /**
     * List schedules by user ID and date
     *
     * @param userId        User ID
     * @param date          Date
     * @param isShowSysSync
     * @return Schedule list
     */
    List<SchedulePO> listByUserIdAndDate(String userId, LocalDate date, String isShowSysSync);

    /**
     * Get schedule by user ID and schedule ID
     *
     * @param userId     User ID
     * @param scheduleId Schedule ID
     * @return Schedule detail
     */
    SchedulePO getByUserIdAndScheduleId(String userId, Long scheduleId);

    /**
     * List schedules by user ID and rule ID（用于删除规则下所有日程）
     *
     * @param userId User ID
     * @param ruleId Schedule rule ID
     * @return Schedule list
     */
    List<SchedulePO> listByUserIdAndRuleId(String userId, Long ruleId);

    /**
     * List schedules by user ID, rule ID, and startTime >= given time（当前及后续日程）
     *
     * @param userId    User ID
     * @param ruleId    Schedule rule ID
     * @param startTime 起始时间（含）
     * @return Schedule list
     */
    List<SchedulePO> listByUserIdAndRuleIdFromTime(String userId, Long ruleId, LocalDateTime startTime);

    /**
     * Delete schedules（逻辑删除，is_delete 置为 1）
     *
     *
     * @param schedulePOList Schedule list to delete
     * @return true if any deleted
     */
    boolean deleteScheduleList(List<SchedulePO> schedulePOList);

    /**
     * List conflict schedule IDs by schedule ID
     *
     * @param scheduleId Schedule ID
     * @return List of conflict schedule IDs
     */
    List<Long> listConflictScheduleIds(Long scheduleId);

    /**
     * Query daily briefing by user ID and date
     *
     * @param userId User ID
     * @param date   Date
     * @return Daily briefing PO
     */
    DailyBriefingPO queryDailyBriefing(String userId, LocalDate date);

    /**
     * List schedule details by schedule ID
     *
     * @param scheduleId Schedule ID
     * @return Schedule detail list
     */
    List<ScheduleDetailPO> listScheduleDetails(Long scheduleId);

    /**
     * Update schedule
     *
     * @param schedulePO Schedule PO
     * @return Update result
     */
    boolean updateSchedule(SchedulePO schedulePO);

    /**
     * Card create schedules (update isCreate from false to true)
     *
     * @param userId         User ID
     * @param scheduleIdList Schedule ID list
     * @return List of created schedules
     */
    List<SchedulePO> cardCreateSchedule(String userId, List<Long> scheduleIdList);

    /**
     * Batch update schedules
     *
     * @param scheduleList Schedule list to update
     * @return Update result
     */
    boolean batchUpdateSchedules(List<SchedulePO> scheduleList);

    List<SchedulePO> querySchedulesByDates(String userId, List<String> dateStrList) throws Exception;

    List<SchedulePO> querySchedulesByDateList(String userId, List<LocalDate> dateList) throws Exception;

    void invokeToPAConversation(String userId, CardCreateScheduleRequestDTO request);

    /**
     * 处理建议间隔，删除符合条件的日程
     *
     * @param schedulePO           本地将要新增的日程
     * @param existingScheduleList 当日已经存在的日程
     */
    void suggetionGapHandle(SchedulePO schedulePO, List<SchedulePO> existingScheduleList);

    SchedulePO getScheduleByUserIdAndMeetingId(String userId, String meetingId);

    void deleteScheduleList(Long ruleId);

    /**
     * 检查指定日程是否与同用户当天的其他日程存在时间重叠
     *
     * @param userId   用户ID
     * @param schedule 需要检查的日程
     * @return true 如果存在时间冲突
     */
    boolean hasTimeConflict(String userId, SchedulePO schedule);

    /**
     * 根据日程ID删除关联的详情记录
     *
     * @param scheduleId 日程ID
     */
    void deleteScheduleDetails(Long scheduleId);

}
