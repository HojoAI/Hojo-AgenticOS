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
package com.hojoai.omniasst.focus.dto.request;

import com.hojoai.omniasst.focus.common.enums.TypeEnum;
import com.hojoai.omniasst.focus.dto.val.FileVal;
import com.hojoai.omniasst.focus.dto.val.FlightVal;
import com.hojoai.omniasst.focus.dto.val.LocationInfoVal;
import com.hojoai.omniasst.focus.dto.val.MeetingVal;
import com.hojoai.omniasst.focus.dto.val.MovieVal;
import com.hojoai.omniasst.focus.dto.val.SportVal;
import com.hojoai.omniasst.focus.dto.val.TrainVal;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

/**
 * 手动创建日程请求DTO
 *
 * @author My
 * @version 1.0 2026/03/02
 * @since hojoai-webmvc-omniasst.focus 1.0.0
 */
public class ManualScheduleRequestDTO {

    /**
     * 日程名称（必填）
     */
    private String title;

    /**
     * 日程类型（必填）
     * -1-默认；0-任务；1-会议；2-运动；3-电影；4-航班；5-火车
     */
    private Integer type;

    /**
     * icon（必填）
     */
    private String icon;

    /**
     * 开始日期（必填），格式：yyyy-MM-dd
     */
    private String startDate;

    /**
     * 结束日期（必填），格式：yyyy-MM-dd
     */
    private String endDate;

    /**
     * 开始时间（必填），格式：HH:mm:ss
     * 航班/火车为行程开始时间，电影为开场时间
     */
    private String startTime;

    /**
     * 结束时间（必填），格式：HH:mm:ss
     * 航班/火车为行程结束时间，电影为散场时间
     */
    private String endTime;

    /**
     * 时长（可选），endTime - startTime，单位：分钟
     */
    private Integer duration;

    /**
     * 是否重复（必填）
     */
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
     * 附件文件
     */
    private List<FileVal> fileList;

    public ManualScheduleRequestDTO() {
    }

    public boolean isInvalid() {
        if (StringUtils.isBlank(title)) {
            return true;
        }
        if (type == null) {
            return true;
        }
        if (StringUtils.isBlank(icon)) {
            return true;
        }
        if (StringUtils.isBlank(startDate)) {
            return true;
        }
        if (StringUtils.isBlank(startTime)) {
            return true;
        }
        if (StringUtils.isBlank(endTime)) {
            return true;
        }
        if (isRepeat == null) {
            return true;
        }
        if (isRepeat) {
            if (repeatTime == null || repeatTime.isEmpty()) {
                return true;
            }
            if (StringUtils.isBlank(repeatUnit)) {
                return true;
            }
        }
        if (isRemind == null) {
            return true;
        }
        if (isRemind) {
            if (remindTime == null) {
                return true;
            }
            if (StringUtils.isBlank(remindUnit)) {
                return true;
            }
        }
        if (isCreateAiSuggestion == null) {
            return true;
        }
        return false;
    }

    private boolean isDataInvalid() {
        if (TypeEnum.MOVIE.getCode().equals(type)) {
            return !(data instanceof MovieVal);
        }
        if (TypeEnum.MEETING.getCode().equals(type)) {
            return !(data instanceof MeetingVal);
        }
        if (TypeEnum.SPORT.getCode().equals(type)) {
            return !(data instanceof SportVal);
        }
        if (TypeEnum.FLIGHT.getCode().equals(type)) {
            return !(data instanceof FlightVal) || ((FlightVal) data).isInvalid();
        }
        if (TypeEnum.TRAIN.getCode().equals(type)) {
            return !(data instanceof TrainVal) || ((TrainVal) data).isInvalid();
        }
        return false;
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

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
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

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
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

    public void setIsCreateAiSuggestion(Boolean isCreateAiSuggestion) {
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
