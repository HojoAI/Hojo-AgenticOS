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

import com.hojoai.omniasst.focus.repository.po.DailyBriefingPO;

import java.time.LocalDate;

/**
 * Daily Briefing Repository Interface
 *
 * @author My
 * @version 1.0 2026/01/06
 * @since hojoai-webmvc-omniasst.focus 1.0.0
 */
public interface DailyBriefingRepository {

    /**
     * Query daily briefing by user ID and date
     *
     * @param userId User ID
     * @param date   Briefing date
     * @return Daily briefing PO
     */
    DailyBriefingPO queryByUserIdAndDate(String userId, LocalDate date);

    /**
     * Create daily briefing
     *
     * @param dailyBriefingPO Daily briefing PO
     * @return Daily briefing ID
     */
    Long create(DailyBriefingPO dailyBriefingPO);

    /**
     * Update daily briefing
     *
     * @param dailyBriefingPO Daily briefing PO
     * @return Update result
     */
    boolean update(DailyBriefingPO dailyBriefingPO);
}

