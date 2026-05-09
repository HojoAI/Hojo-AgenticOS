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

import java.time.LocalDateTime;

/**
 * Schedule Conflict PO
 *
 * @author My
 * @version 1.0 2024/12/27
 * @since hojoai-webmvc-omniasst.focus 1.0.0
 */
@TableName("focus_schedule_conflict")
public class ScheduleConflictPO {

    @TableId(value = "id", type = IdType.INPUT)
    private Long id;

    private String userId;

    private Long scheduleId;

    private Long conflictScheduleId;

    private Integer conflictType;

    private Integer overlapMinutes;

    private Integer conflictLevel;

    private LocalDateTime conflictStartTime;

    private LocalDateTime conflictEndTime;

    private String remark;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Boolean isDelete;

    private Integer version;

    public ScheduleConflictPO() {
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

    public Long getScheduleId() {
        return scheduleId;
    }

    public void setScheduleId(Long scheduleId) {
        this.scheduleId = scheduleId;
    }

    public Long getConflictScheduleId() {
        return conflictScheduleId;
    }

    public void setConflictScheduleId(Long conflictScheduleId) {
        this.conflictScheduleId = conflictScheduleId;
    }

    public Integer getConflictType() {
        return conflictType;
    }

    public void setConflictType(Integer conflictType) {
        this.conflictType = conflictType;
    }

    public Integer getOverlapMinutes() {
        return overlapMinutes;
    }

    public void setOverlapMinutes(Integer overlapMinutes) {
        this.overlapMinutes = overlapMinutes;
    }

    public Integer getConflictLevel() {
        return conflictLevel;
    }

    public void setConflictLevel(Integer conflictLevel) {
        this.conflictLevel = conflictLevel;
    }

    public LocalDateTime getConflictStartTime() {
        return conflictStartTime;
    }

    public void setConflictStartTime(LocalDateTime conflictStartTime) {
        this.conflictStartTime = conflictStartTime;
    }

    public LocalDateTime getConflictEndTime() {
        return conflictEndTime;
    }

    public void setConflictEndTime(LocalDateTime conflictEndTime) {
        this.conflictEndTime = conflictEndTime;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
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

