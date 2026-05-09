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

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

/**
 * 日程提醒表PO
 *
 * @author My
 * @version 1.0 2026/03/02
 * @since hojoai-webmvc-omniasst.focus 1.0.0
 */
@TableName("focus_schedule_remind")
public class ScheduleRemindPO {

    /** 主键id（Snowflake 生成） */
    @TableId(value = "id", type = IdType.INPUT)
    private Long id;

    /** 日程ID */
    private Long scheduleId;

    /** 用户ID */
    private String userId;

    /** 提醒时间 */
    private LocalDateTime remindTime;

    /** 提醒状态：0-未提醒，1-已提醒，2-提醒失败 */
    private Integer remindStatus;

    /** 提醒标题 */
    private String remindTitle;

    /** 提醒内容 */
    private String remindContent;

    /** 实际提醒时间 */
    private LocalDateTime remindedTime;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;

    /** 逻辑删除：0-未删除，1-已删除 */
    @TableLogic
    private Integer isDelete;

    /** 乐观锁版本号 */
    private Integer version;

    public ScheduleRemindPO() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getScheduleId() {
        return scheduleId;
    }

    public void setScheduleId(Long scheduleId) {
        this.scheduleId = scheduleId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public LocalDateTime getRemindTime() {
        return remindTime;
    }

    public void setRemindTime(LocalDateTime remindTime) {
        this.remindTime = remindTime;
    }

    public Integer getRemindStatus() {
        return remindStatus;
    }

    public void setRemindStatus(Integer remindStatus) {
        this.remindStatus = remindStatus;
    }

    public String getRemindTitle() {
        return remindTitle;
    }

    public void setRemindTitle(String remindTitle) {
        this.remindTitle = remindTitle;
    }

    public String getRemindContent() {
        return remindContent;
    }

    public void setRemindContent(String remindContent) {
        this.remindContent = remindContent;
    }

    public LocalDateTime getRemindedTime() {
        return remindedTime;
    }

    public void setRemindedTime(LocalDateTime remindedTime) {
        this.remindedTime = remindedTime;
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

    public Integer getIsDelete() {
        return isDelete;
    }

    public void setIsDelete(Integer isDelete) {
        this.isDelete = isDelete;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }
}
