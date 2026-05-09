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
package com.hojoai.omniasst.focus.common.constants;

/**
 *
 * @author My
 * @version 1.0, 2026/01/16
 * @since hojoai-webmvc-omniasst-focus 1.0.0
 */
public interface ScheduleConstants {

    String REDIS_KEY_PREFIX = "schedule_task:";

    String REDIS_SCHEDULE_TASK_QUEUE = "schedule_task:queue";

    Long PUSH_DELAY_SECOND = 30L;

    /**
     * 意图识别
     */
    String KEY_INTENT_RECOGNITION = "IntentRecognition";

    /**
     * 冲突检测
     */
    String KEY_SCHEDULE_CONFLICT = "ScheduleConflict";

    /**
     * 日程创建
     */
    String KEY_SCHEDULE_CREATE = "ScheduleCreate";

    /**
     * 日程简报
     */
    String KEY_SCHEDULE_DAILY_BRIEFING = "ScheduleDailyBriefing";

    /**
     * focus
     */
    String KEY_SCHEDULE_FOCUS = "ScheduleFocus";

    /**
     * 事前建议
     */
    String KEY_SCHEDULE_PRE_EVENT_SUGGESTION = "SchedulePreEventSuggestion";

    /**
     * 主动建议
     */
    String KEY_SCHEDULE_PROACTIVE_SUGGESTION = "ScheduleProactiveSuggestion";

    /**
     * 日程建议
     */
    String KEY_SCHEDULE_SUGGESTION = "ScheduleSuggestion";

}
