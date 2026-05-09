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
package com.hojoai.omniasst.focus.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.hojoai.omniasst.focus.dto.val.AiDetailVal;
import com.hojoai.omniasst.focus.dto.val.FileVal;
import com.hojoai.omniasst.focus.dto.val.LocationInfoVal;

import java.util.Date;
import java.util.List;

/**
 * Schedule Detail Response DTO
 * Contains detailed schedule information
 *
 * @author My
 * @version 1.0 2026/01/06
 * @since hojoai-webmvc-omniasst.focus 1.0.0
 */
public class ScheduleDetailResponseDTO {

    /**
     * 日程id（与 API 返回示例一致，序列化为数字）
     */
    private String scheduleId;

    /**
     * 日程规则 id（关联的 schedule_rule 主键）
     */
    private String scheduleRuleId;

    /**
     * 是否系统日历同步
     */
    @JsonProperty("isSysSync")
    private Boolean isSysSync;

    /**
     * 日程名称
     */
    private String title;

    /**
     * 日程类型
     * 0-任务；1-会议；2-运动
     */
    private Integer type;

    /**
     * 开始时间
     */
    private Date startTime;

    /**
     * 结束时间
     */
    private Date endTime;

    /**
     * 状态
     * 0-待进行；1-进行中；2-已完成；3-已取消
     */
    private Integer status;

    /**
     * 标签
     * 0-正常；1-Focus；2-冲突；3-建议
     */
    private Integer tag;

    /**
     * 精力
     */
    private String energy;

    /**
     * 图标
     * 任务|会议|运动|文档|提醒|电话会议|视频会议|网球等
     */
    private String icon;

    /**
     * 辅助信息
     */
    private String auxiliaryInfo;

    /**
     * AI详情
     */
    private AiDetailVal aiDetail;

    /**
     * 是否重复（必填）
     */
    @JsonProperty("isRepeat")
    private Boolean isRepeat;

    /**
     * 重复时间（可选），isRepeat为true时必填
     */
    private List<String> repeatTime;

    /**
     * 重复时间单位（可选），isRepeat为true时必填
     * MM-分钟；H-小时；D-日；W-周；M-月；Y-年
     */
    private String repeatUnit;

    /**
     * 是否提醒（必填）
     */
    @JsonProperty("isRemind")
    private Boolean isRemind;

    /**
     * 提醒时间（可选），isRemind为true时必填
     */
    private Integer remindTime;

    /**
     * 提醒时间单位（可选），isRemind为true时必填
     * MM-分钟；H-小时；D-日；W-周；M-月；Y-年
     */
    private String remindUnit;

    /**
     * 是否创建AI建议（必填）
     */
    @JsonProperty("isCreateAiSuggestion")
    private Boolean isCreateAiSuggestion;

    /**
     * 参与人（可选）
     */
    private String participant;

    /**
     * 位置信息（可选）
     */
    private LocationInfoVal locationInfo;

    /**
     * 备注（可选）
     */
    private String remark;

    /**
     * 各类型日程参数（可选），不同类型日程参数见具体类型说明
     */
    private Object data;

    /**
     * 附件集合
     */
    private List<FileVal> fileList;

    public ScheduleDetailResponseDTO() {
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

    public Boolean getIsSysSync() {
        return isSysSync;
    }

    public void setIsSysSync(Boolean isSysSync) {
        this.isSysSync = isSysSync;
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

    public String getAuxiliaryInfo() {
        return auxiliaryInfo;
    }

    public void setAuxiliaryInfo(String auxiliaryInfo) {
        this.auxiliaryInfo = auxiliaryInfo;
    }

    public AiDetailVal getAiDetail() {
        return aiDetail;
    }

    public void setAiDetail(AiDetailVal aiDetail) {
        this.aiDetail = aiDetail;
    }

    public Boolean getIsRepeat() {
        return isRepeat;
    }

    public void setIsRepeat(Boolean isRepeat) {
        this.isRepeat = isRepeat;
    }

    public List<String> getRepeatTime() {
        return repeatTime;
    }

    public void setRepeatTime(List<String> repeatTime) {
        this.repeatTime = repeatTime;
    }

    public String getRepeatUnit() {
        return repeatUnit;
    }

    public void setRepeatUnit(String repeatUnit) {
        this.repeatUnit = repeatUnit;
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

    public Boolean getIsCreateAiSuggestion() {
        return isCreateAiSuggestion;
    }

    public void setCreateAiSuggestion(Boolean isCreateAiSuggestion) {
        this.isCreateAiSuggestion = isCreateAiSuggestion;
    }

    public String getParticipant() {
        return participant;
    }

    public void setParticipant(String participant) {
        this.participant = participant;
    }

    public LocationInfoVal getLocationInfo() {
        return locationInfo;
    }

    public void setLocationInfo(LocationInfoVal locationInfo) {
        this.locationInfo = locationInfo;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public List<FileVal> getFileList() {
        return fileList;
    }

    public void setFileList(List<FileVal> fileList) {
        this.fileList = fileList;
    }

}

