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

import com.hojoai.omniasst.focus.common.enums.TagEnum;

import java.time.LocalDate;

/**
 *
 * @author My
 * @version 1.0, 2026/01/17
 * @since hojoai-webmvc-omniasst-focus 1.0.0
 */
public interface ScheduleTaskDomainService {

    void executeSystemAIScheduleFreeSuggestion(LocalDate today);

    void executeSystemAIScheduleAnalysis(LocalDate date);

    void taskHandle(String userId, LocalDate date, TagEnum tagEnum);

    void executeAIScheduleAnalysis(String userId, LocalDate date, TagEnum tagEnum);

    void executeTodayAIScheduleAnalysis(LocalDate today);

    void executeToday2NextWeekAIScheduleAnalysis(String userId, LocalDate date);

}
