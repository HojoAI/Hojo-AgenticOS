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
 * 日程表PO
 *
 * @author My
 * @version 1.0 2024/12/29
 * @since hojoai-webmvc-omniasst.focus 1.0.0
 */
@TableName("focus_schedule")
public class SchedulePO {

    /** 主键ID（Snowflake） */
    @TableId(value = "id", type = IdType.INPUT)
    private Long id;

    /** 用户ID */
    private String userId;

    /** 规则id */
    private Long ruleId;

    /** 系统日程id */
    private String sysId;

    /** 是否系统同步 */
    private Integer isSysSync;

    /** 对话id */
    private String conversationId;

    /** 日程名称 */
    private String title;

    /** 日程描述 */
    private String remark;

    /** 日程类型：-1-默认，0-任务，1-会议，2-运动 */
    private Integer type;

    /** 开始时间 */
    private LocalDateTime startTime;

    /** 结束时间 */
    private LocalDateTime endTime;

    /** 时区 */
    private String timeZone;

    /** 是否日程简报 */
    private Boolean isDailyBriefing;

    /** 辅助信息 */
    private String auxiliaryInfo;

    /** 状态：0-待进行，1-进行中，2-已完成，3-已取消 */
    private Integer status;

    /** 是否提醒：0-不提醒，1-提醒 */
    private Boolean isRemind;

    /** 是否ai建议 */
    private Boolean isAiSuggestion;

    /** 是否存在冲突：0-无冲突，1-有冲突 */
    private Boolean isConflict;

    /** 冲突日程数量 */
    private Integer conflictCount;

    /** 冲突建议 */
    private String conflictSuggestion;

    /** 标签（0-正常；1-Focus；2-冲突；3-建议） */
    private Integer tag;

    /** 精力 */
    private String energy;

    /** 图标 */
    private String icon;

    /** AI建议 */
    private String aiSuggestion;

    /** AI类型 */
    private String aiDetailType;

    /** AI详情标题 */
    private String aiDetailTitle;

    /** 是否创建 */
    private Boolean isCreate;

    private String attachmentList;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /** 逻辑删除：0-未删除，1-已删除 */
    @TableLogic
    private Boolean isDelete;

    /** 乐观锁版本号 */
    private Integer version;

    public SchedulePO() {
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

    public Long getRuleId() {
        return ruleId;
    }

    public void setRuleId(Long ruleId) {
        this.ruleId = ruleId;
    }

    public String getSysId() {
        return sysId;
    }

    public void setSysId(String sysId) {
        this.sysId = sysId;
    }

    public Integer getIsSysSync() {
        return isSysSync;
    }

    public void setIsSysSync(Integer isSysSync) {
        this.isSysSync = isSysSync;
    }

    public String getConversationId() {
        return conversationId;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public String getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
    }

    public Boolean getIsDailyBriefing() {
        return isDailyBriefing;
    }

    public void setIsDailyBriefing(Boolean isDailyBriefing) {
        this.isDailyBriefing = isDailyBriefing;
    }

    public String getAuxiliaryInfo() {
        return auxiliaryInfo;
    }

    public void setAuxiliaryInfo(String auxiliaryInfo) {
        this.auxiliaryInfo = auxiliaryInfo;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Boolean getIsRemind() {
        return isRemind;
    }

    public void setIsRemind(Boolean isRemind) {
        this.isRemind = isRemind;
    }

    public Boolean getIsAiSuggestion() {
        return isAiSuggestion;
    }

    public void setIsAiSuggestion(Boolean isAiSuggestion) {
        this.isAiSuggestion = isAiSuggestion;
    }

    public Boolean getIsConflict() {
        return isConflict;
    }

    public void setIsConflict(Boolean isConflict) {
        this.isConflict = isConflict;
    }

    public Integer getConflictCount() {
        return conflictCount;
    }

    public void setConflictCount(Integer conflictCount) {
        this.conflictCount = conflictCount;
    }

    public String getConflictSuggestion() {
        return conflictSuggestion;
    }

    public void setConflictSuggestion(String conflictSuggestion) {
        this.conflictSuggestion = conflictSuggestion;
    }

    public Integer getTag() {
        return tag;
    }

    public void setTag(Integer tag) {
        this.tag = tag;
    }

    public String getEnergy() {
        return energy;
    }

    public void setEnergy(String energy) {
        this.energy = energy;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getAiSuggestion() {
        return aiSuggestion;
    }

    public void setAiSuggestion(String aiSuggestion) {
        this.aiSuggestion = aiSuggestion;
    }

    public String getAiDetailType() {
        return aiDetailType;
    }

    public void setAiDetailType(String aiDetailType) {
        this.aiDetailType = aiDetailType;
    }

    public String getAiDetailTitle() {
        return aiDetailTitle;
    }

    public void setAiDetailTitle(String aiDetailTitle) {
        this.aiDetailTitle = aiDetailTitle;
    }

    public Boolean getIsCreate() {
        return isCreate;
    }

    public void setIsCreate(Boolean isCreate) {
        this.isCreate = isCreate;
    }

    public String getAttachmentList() {
        return attachmentList;
    }

    public void setAttachmentList(String attachmentList) {
        this.attachmentList = attachmentList;
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
