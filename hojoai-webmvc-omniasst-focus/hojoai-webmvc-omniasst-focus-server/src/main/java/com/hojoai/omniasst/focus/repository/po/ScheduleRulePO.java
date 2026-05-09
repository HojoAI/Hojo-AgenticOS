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
package com.hojoai.omniasst.focus.repository.po;

import com.baomidou.mybatisplus.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * 日程规则表PO（focus_schedule_rule）
 *
 * @author My
 * @version 1.0 2026/03/02
 * @since hojoai-webmvc-omniasst-focus 1.0.0
 */
@TableName("focus_schedule_rule")
public class ScheduleRulePO {

    @TableId(value = "id", type = IdType.INPUT)
    private Long id;

    /** 用户ID */
    private String userId;

    /** 对话id */
    private String conversationId;

    /** 系统日程id */
    private String sysId;

    /** 日程标题 */
    private String title;

    /** 日程类型：-1-默认；0-任务；1-会议；2-运动；3-电影；4-航班；5-火车 */
    private Integer type;

    /** 图标 */
    private String icon;

    /** 开始日期 */
    private LocalDate startDate;

    /** 结束日期 */
    private LocalDate endDate;

    /** 开始时间 */
    private LocalTime startTime;

    /** 结束时间 */
    private LocalTime endTime;

    /** 时区 */
    private String timeZone;

    /** 是否系统同步 */
    private Integer isSysSync;

    /** 是否固定日期 */
    private Integer isFixedDate;

    /** 是否重复 */
    private Boolean isRepeat;

    /** 重复时间 */
    private String repeatTime;

    /** 重复时间单位：MM-分钟；H-小时；D-日；W-周；M-月；Y-年 */
    private String repeatUnit;

    /** 重复时间间隔 */
    private Integer repeatInterval;

    /** 是否提醒：0-不提醒，1-提醒 */
    private Boolean isRemind;

    /** 提醒时间 */
    private Integer remindTime;

    /** 提醒时间单位：MM-分钟；H-小时；D-日；W-周；M-月；Y-年 */
    private String remindUnit;

    /** 规则开始时间 */
    private LocalDateTime ruleStartTime;

    /** 规则结束时间 */
    private LocalDateTime ruleEndTime;

    /** 是否创建AI建议 */
    private Integer isCreateSug;

    /** 参与人 */
    private String participant;

    /** 地点id */
    private Long locationId;

    /** 日程备注 */
    private String remark;

    /** 是否创建 */
    private Boolean isCreate;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Boolean isDelete;

    private Integer version;

    public ScheduleRulePO() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getConversationId() {
        return conversationId;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }

    public String getSysId() {
        return sysId;
    }

    public void setSysId(String sysId) {
        this.sysId = sysId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    public String getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
    }

    public Integer getIsSysSync() {
        return isSysSync;
    }

    public void setIsSysSync(Integer isSysSync) {
        this.isSysSync = isSysSync;
    }

    public Integer getIsFixedDate() {
        return isFixedDate;
    }

    public void setIsFixedDate(Integer isFixedDate) {
        this.isFixedDate = isFixedDate;
    }

    public Boolean getIsRepeat() {
        return isRepeat;
    }

    public void setIsRepeat(Boolean isRepeat) {
        this.isRepeat = isRepeat;
    }

    public String getRepeatTime() {
        return repeatTime;
    }

    public void setRepeatTime(String repeatTime) {
        this.repeatTime = repeatTime;
    }

    public String getRepeatUnit() {
        return repeatUnit;
    }

    public void setRepeatUnit(String repeatUnit) {
        this.repeatUnit = repeatUnit;
    }

    public Integer getRepeatInterval() {
        return repeatInterval;
    }

    public void setRepeatInterval(Integer repeatInterval) {
        this.repeatInterval = repeatInterval;
    }

    public Boolean getIsRemind() {
        return isRemind;
    }

    public void setIsRemind(Boolean isRemind) {
        this.isRemind = isRemind;
    }

    public Integer getRemindTime() {
        return remindTime;
    }

    public void setRemindTime(Integer remindTime) {
        this.remindTime = remindTime;
    }

    public String getRemindUnit() {
        return remindUnit;
    }

    public void setRemindUnit(String remindUnit) {
        this.remindUnit = remindUnit;
    }

    public LocalDateTime getRuleStartTime() {
        return ruleStartTime;
    }

    public void setRuleStartTime(LocalDateTime ruleStartTime) {
        this.ruleStartTime = ruleStartTime;
    }

    public LocalDateTime getRuleEndTime() {
        return ruleEndTime;
    }

    public void setRuleEndTime(LocalDateTime ruleEndTime) {
        this.ruleEndTime = ruleEndTime;
    }

    public Integer getIsCreateSug() {
        return isCreateSug;
    }

    public void setIsCreateSug(Integer isCreateSug) {
        this.isCreateSug = isCreateSug;
    }

    public String getParticipant() {
        return participant;
    }

    public void setParticipant(String participant) {
        this.participant = participant;
    }

    public Long getLocationId() {
        return locationId;
    }

    public void setLocationId(Long locationId) {
        this.locationId = locationId;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public Boolean getIsCreate() {
        return isCreate;
    }

    public void setIsCreate(Boolean isCreate) {
        this.isCreate = isCreate;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }

    public Boolean getIsDelete() {
        return isDelete;
    }

    public void setIsDelete(Boolean isDelete) {
        this.isDelete = isDelete;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }
}
