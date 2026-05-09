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

import java.util.List;

/**
 * 系统日程值对象
 * 用于承载云端/系统同步的日程完整信息
 *
 * @author My
 * @version 1.0
 * @since hojoai-webmvc-omniasst.focus 1.0.0
 */
public class SysScheduleVal {

    /** 云端id */
    private String cloudEventId;

    /** 系统id */
    private String localSystemId;

    /** 版本号 */
    private Long version;

    /** 基本信息 */
    private BasicInfo basicInfo;

    /** 日程信息 */
    private ScheduleInfo scheduleInfo;

    /** 重复信息 */
    private RecurrenceInfo recurrence;

    /** 提醒时间 */
    private List<Integer> reminders;

    /** 参与人 */
    private List<AttendeeInfo> attendees;

    /** 扩展信息 */
    private ExtInfo extInfo;

    public SysScheduleVal() {
    }

    public String getCloudEventId() {
        return cloudEventId;
    }

    public void setCloudEventId(String cloudEventId) {
        this.cloudEventId = cloudEventId;
    }

    public String getLocalSystemId() {
        return localSystemId;
    }

    public void setLocalSystemId(String localSystemId) {
        this.localSystemId = localSystemId;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public BasicInfo getBasicInfo() {
        return basicInfo;
    }

    public void setBasicInfo(BasicInfo basicInfo) {
        this.basicInfo = basicInfo;
    }

    public ScheduleInfo getScheduleInfo() {
        return scheduleInfo;
    }

    public void setScheduleInfo(ScheduleInfo scheduleInfo) {
        this.scheduleInfo = scheduleInfo;
    }

    public RecurrenceInfo getRecurrence() {
        return recurrence;
    }

    public void setRecurrence(RecurrenceInfo recurrence) {
        this.recurrence = recurrence;
    }

    public List<Integer> getReminders() {
        return reminders;
    }

    public void setReminders(List<Integer> reminders) {
        this.reminders = reminders;
    }

    public List<AttendeeInfo> getAttendees() {
        return attendees;
    }

    public void setAttendees(List<AttendeeInfo> attendees) {
        this.attendees = attendees;
    }

    public ExtInfo getExtInfo() {
        return extInfo;
    }

    public void setExtInfo(ExtInfo extInfo) {
        this.extInfo = extInfo;
    }
}
