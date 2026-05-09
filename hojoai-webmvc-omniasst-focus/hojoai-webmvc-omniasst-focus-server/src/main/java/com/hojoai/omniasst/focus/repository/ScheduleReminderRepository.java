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

import com.hojoai.omniasst.focus.repository.po.ScheduleReminderPO;

/**
 * 日程提醒Repository接口
 *
 * @author My
 * @version 1.0 2024/12/27
 * @since hojoai-webmvc-omniasst.focus 1.0.0
 */
public interface ScheduleReminderRepository {

    /**
     * 创建提醒
     *
     * @param reminderPO 提醒PO
     * @return 提醒ID
     */
    Long create(ScheduleReminderPO reminderPO);

    /**
     * 根据日程ID查询提醒
     *
     * @param scheduleId 日程ID
     * @return 提醒PO
     */
    ScheduleReminderPO getByScheduleId(Long scheduleId);
}

