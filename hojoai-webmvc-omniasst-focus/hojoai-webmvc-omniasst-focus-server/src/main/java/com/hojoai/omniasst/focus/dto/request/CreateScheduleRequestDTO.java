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

import java.util.Date;

/**
 * Create Schedule Request DTO
 *
 * @author My
 * @version 1.0 2024/12/29
 * @since hojoai-webmvc-omniasst.focus 1.0.0
 */
public class CreateScheduleRequestDTO {

    /**
     * User ID
     */
    private String userId;

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
     * Energy (精力, optional)
     */
    private String energy;

    /**
     * Auxiliary info (辅助信息, optional)
     */
    private String auxiliaryInfo;

    /**
     * Is repeat (optional, default false)
     */
    private Boolean isRepeat;

    /**
     * Repeat time (optional, empty when repeatUnit is D)
     */
    private Integer repeatTime;

    /**
     * Repeat unit (optional)
     * D-Day; W-Week; M-Month; Y-Year
     */
    private String repeatUnit;

    /**
     * Is remind (optional, default false)
     */
    private Boolean isRemind;

    /**
     * Remind before minutes (optional, default 10 when isRemind is true)
     */
    private Integer remindBeforeMinutes;

    public CreateScheduleRequestDTO() {
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
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

    public Boolean getIsRepeat() {
        return isRepeat;
    }

    public void setIsRepeat(Boolean isRepeat) {
        this.isRepeat = isRepeat;
    }

    public Integer getRepeatTime() {
        return repeatTime;
    }

    public void setRepeatTime(Integer repeatTime) {
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

    public Integer getRemindBeforeMinutes() {
        return remindBeforeMinutes;
    }

    public void setRemindBeforeMinutes(Integer remindBeforeMinutes) {
        this.remindBeforeMinutes = remindBeforeMinutes;
    }
}
