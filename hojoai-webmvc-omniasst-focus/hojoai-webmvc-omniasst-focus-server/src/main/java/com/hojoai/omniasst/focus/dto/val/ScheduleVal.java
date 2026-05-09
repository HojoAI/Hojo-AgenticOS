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

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Date;
import java.util.List;

/**
 * Schedule Value DTO
 * Contains individual schedule information in the schedule list
 *
 * @author My
 * @version 1.0 2024/12/30
 * @since hojoai-webmvc-omniasst.focus 1.0.0
 */
public class ScheduleVal {

    /**
     * Schedule ID（与前端约定：JSON 中为数字）
     */
    private String scheduleId;

    /**
     * 日程规则 ID（关联 focus_schedule_rule，可为空）
     */
    private String scheduleRuleId;

    /**
     * Schedule title
     */
    private String title;

    /**
     * Schedule type
     * 0-Task; 1-Meeting; 2-Sport
     */
    private Integer type;

    /**
     * Start time
     */
    private Date startTime;

    /**
     * End time
     */
    private Date endTime;

    /**
     * Schedule status
     * 0-Pending; 1-In Progress; 2-Completed; 3-Cancelled
     */
    private Integer status;

    /**
     * Icon
     */
    private String icon;

    /**
     * Tag
     * 0-Normal; 1-Focus; 2-Conflict; 3-Suggestion
     */
    private Integer tag;

    /**
     * Energy (精力)
     */
    private String energy;

    /**
     * Auxiliary info (辅助信息)
     */
    private String auxiliaryInfo;

    /**
     * AI suggestion
     */
    private String aiSuggestion;

    /**
     * Is remind
     */
    private Boolean isRemind;

    /**
     * is sysSync
     */
    @JsonProperty("isSysSync")
    private Boolean isSysSync;

    /**
     * Has conflict
     */
    private Boolean hasConflict;

    /**
     * Conflict suggestion
     */
    private String conflictSuggestion;

    /**
     * Conflict schedule ID list
     */
    private List<Long> conflictScheduleIdList;

    /**
     * 类型扩展数据（运动/会议/观影/航班/火车等，结构随 type 不同，见各 type 的 data 模型）
     */
    private Object data;

    public ScheduleVal() {
    }

    public String getScheduleId() {
        return scheduleId;
    }

    public void setScheduleId(String scheduleId) {
        this.scheduleId = scheduleId;
    }

    public String getScheduleRuleId() {
        return scheduleRuleId;
    }

    public void setScheduleRuleId(String scheduleRuleId) {
        this.scheduleRuleId = scheduleRuleId;
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

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
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

    public String getAuxiliaryInfo() {
        return auxiliaryInfo;
    }

    public void setAuxiliaryInfo(String auxiliaryInfo) {
        this.auxiliaryInfo = auxiliaryInfo;
    }

    public String getAiSuggestion() {
        return aiSuggestion;
    }

    public void setAiSuggestion(String aiSuggestion) {
        this.aiSuggestion = aiSuggestion;
    }

    public Boolean getIsRemind() {
        return isRemind;
    }

    public void setIsRemind(Boolean isRemind) {
        this.isRemind = isRemind;
    }

    public Boolean getIsSysSync() {
        return isSysSync;
    }

    public void setIsSysSync(Boolean isSysSync) {
        this.isSysSync = isSysSync;
    }

    public Boolean getHasConflict() {
        return hasConflict;
    }

    public void setHasConflict(Boolean hasConflict) {
        this.hasConflict = hasConflict;
    }

    public String getConflictSuggestion() {
        return conflictSuggestion;
    }

    public void setConflictSuggestion(String conflictSuggestion) {
        this.conflictSuggestion = conflictSuggestion;
    }

    public List<Long> getConflictScheduleIdList() {
        return conflictScheduleIdList;
    }

    public void setConflictScheduleIdList(List<Long> conflictScheduleIdList) {
        this.conflictScheduleIdList = conflictScheduleIdList;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}

