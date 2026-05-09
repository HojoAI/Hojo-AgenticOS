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
package com.hojoai.omniasst.focus.dto.val;

/**
 * 日程信息
 * <p>startTime/endTime 格式：yyyy-MM-ddTHH:mm:ssZ（ISO 8601，UTC）
 *
 * @author My
 * @version 1.0
 * @since hojoai-webmvc-omniasst.focus 1.0.0
 */
public class ScheduleInfo {

    /** 开始时间，格式 yyyy-MM-ddTHH:mm:ssZ */
    private String startTime;

    /** 结束时间，格式 yyyy-MM-ddTHH:mm:ssZ */
    private String endTime;

    /** 是否全天日程 */
    private Boolean isAllDay;

    /** 时区 */
    private String ianaTimezone;

    public ScheduleInfo() {
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public Boolean getIsAllDay() {
        return isAllDay;
    }

    public void setIsAllDay(Boolean isAllDay) {
        this.isAllDay = isAllDay;
    }

    public String getIanaTimezone() {
        return ianaTimezone;
    }

    public void setIanaTimezone(String ianaTimezone) {
        this.ianaTimezone = ianaTimezone;
    }
}
