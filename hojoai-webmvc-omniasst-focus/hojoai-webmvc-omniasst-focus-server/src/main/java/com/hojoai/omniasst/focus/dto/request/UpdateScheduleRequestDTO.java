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
 * Update Schedule Request DTO
 * Update schedule information
 *
 * @author My
 * @version 1.0 2024/12/30
 * @since hojoai-webmvc-omniasst.focus 1.0.0
 */
public class UpdateScheduleRequestDTO {

    /**
     * User ID (from path parameter)
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
     * Schedule status
     * 0-Pending; 1-In Progress; 2-Completed; 3-Cancelled
     */
    private Integer status;

    /**
     * Start time
     */
    private Date startTime;

    /**
     * End time
     */
    private Date endTime;

    /**
     * Is remind
     */
    private Boolean isRemind;

    /**
     * Description
     */
    private String description;

    /**
     * Energy
     */
    private String energy;

    /**
     * Auxiliary info
     */
    private String auxiliaryInfo;

    public UpdateScheduleRequestDTO() {
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

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
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

    public Boolean getIsRemind() {
        return isRemind;
    }

    public void setIsRemind(Boolean isRemind) {
        this.isRemind = isRemind;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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
}

