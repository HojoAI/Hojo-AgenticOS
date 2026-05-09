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
package com.hojoai.omniasst.focus.common.enums;

import com.hojoai.central.provider.service.common.enums.CommonEnum;

/**
 * URL Enum
 * 包含 ScheduleConstants 中的常量值（除了 REDIS_KEY_PREFIX）
 *
 * @author My
 * @version 1.0, 2026/01/25
 * @since hojoai-webmvc-omniasst-focus 1.0.0
 */
public enum UrlEnum implements CommonEnum {

    /**
     * 意图识别
     */
    INTENT_RECOGNITION("IntentRecognition", "/agents/schedule/nlu"),

    /**
     * 冲突检测
     */
    SCHEDULE_CONFLICT("ScheduleConflict", "/agents/schedule/collideAdvice"),

    /**
     * 日程创建
     */
    SCHEDULE_CREATE("ScheduleCreate", "/agents/schedule/createEvents"),

    /**
     * 日程简报
     */
    SCHEDULE_DAILY_BRIEFING("ScheduleDailyBriefing", "/agents/schedule/dailyBrief"),

    /**
     * focus
     */
    SCHEDULE_FOCUS("ScheduleFocus", "/agents/schedule/focusTag"),

    /**
     * 事前建议
     */
    SCHEDULE_PRE_EVENT_SUGGESTION("SchedulePreEventSuggestion", "/agents/schedule/preAdvice"),

    /**
     * 主动建议
     */
    SCHEDULE_PROACTIVE_SUGGESTION("ScheduleProactiveSuggestion", "/agents/schedule/freeTime"),

    /**
     * 日程建议
     */
    SCHEDULE_SUGGESTION("ScheduleSuggestion", "/agents/schedule/cardAdvice"),

    /**
     * 匹配标题和icon
     */
    SCHEDULE_MATCH_TITLE_ICON("ScheduleMatchTitleIcon", "/agents/schedule/v1/matchTitleIcon"),

    /**
     * 获取电影名称
     */
    SCHEDULE_GET_MOVIE_NAME("ScheduleGetMovieName", "/agents/schedule/v1/getMovieName"),
    ;

    private final String code;

    private final String value;

    UrlEnum(String code, String value) {
        this.code = code;
        this.value = value;
    }

    public String getCode() {
        return code;
    }

    public String getValue() {
        return value;
    }

}
