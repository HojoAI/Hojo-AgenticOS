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
package com.hojoai.omniasst.focus.repository;

import com.hojoai.omniasst.focus.dto.val.AIConflictVal;
import com.hojoai.omniasst.focus.repository.po.SchedulePO;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Schedule Repository Interface
 *
 * @author My
 * @version 1.0 2024/12/29
 * @since hojoai-webmvc-omniasst.focus 1.0.0
 */
public interface ScheduleRepository {

    /**
     * List schedules by user ID and date
     *
     * @param userId User ID
     * @param date   Date
     * @return Schedule list
     */
    List<SchedulePO> listByUserIdAndDate(String userId, LocalDate date);

    /**
     * List all schedules by date (system-level query)
     *
     * @param date Date
     * @return Schedule list
     */
    List<SchedulePO> listByDate(LocalDate date);

    /**
     * Get schedule by user ID and schedule ID
     *
     * @param userId     User ID
     * @param scheduleId Schedule ID
     * @return Schedule detail
     */
    SchedulePO getByUserIdAndScheduleId(String userId, Long scheduleId);

    /**
     * Create schedule
     *
     * @param schedulePO Schedule PO
     * @return Schedule ID
     */
    Long create(SchedulePO schedulePO);

    /**
     * Find conflicting schedules by user ID and time range
     *
     * @param userId    User ID
     * @param startTime Start time
     * @param endTime   End time
     * @return Conflicting schedule list
     */
    List<SchedulePO> findConflicts(String userId, String startTime, String endTime);

    /**
     * Update conflict information
     *
     * @param conflictVal  Conflict conflictVal
     * @return Update result
     */
    boolean updateConflictInfo(AIConflictVal conflictVal);

    /**
     * Update schedule
     *
     * @param schedulePO Schedule PO
     * @return Update result
     */
    boolean update(SchedulePO schedulePO);

    /**
     * Update isCreate to true for given schedule IDs
     *
     * @param scheduleList Schedule ID list
     * @return Update result
     */
    boolean updateIsCreateToTrue(List<SchedulePO> scheduleList);

    /**
     * Batch update schedules
     *
     * @param scheduleList Schedule list to update
     * @return Update result
     */
    boolean batchUpdate(List<SchedulePO> scheduleList);

    /**
     * Batch get schedules by user ID and schedule IDs
     *
     * @param userId User ID
     * @param scheduleIds Schedule ID list
     * @return Schedule list
     */
    List<SchedulePO> listByUserIdAndScheduleIds(String userId, List<Long> scheduleIds);

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
     * @param startTime 起始时间（含），只返回 start_time >= startTime 的日程
     * @return Schedule list
     */
    List<SchedulePO> listByUserIdAndRuleIdFromTime(String userId, Long ruleId, LocalDateTime startTime);

    /**
     * List all distinct user IDs from schedules
     *
     * @return User ID list
     */
    List<String> listAllUserIds();

    /**
     * Delete schedules by setting isCreate to true
     *
     * @param schedulePOList Schedule PO list
     * @return Delete result
     */
    boolean delete(List<SchedulePO> schedulePOList);

    SchedulePO getScheduleByUserIdAndMeetingId(String userId, String meetingId);

    List<SchedulePO> listByUserIdAndDate(String userId, LocalDate date, String isShowSysSync);

    /**
     * Delete schedules by rule ID (logical delete)
     *
     * @param ruleId Schedule rule ID
     * @return Delete result
     */
    boolean deleteByRuleId(Long ruleId);

}
