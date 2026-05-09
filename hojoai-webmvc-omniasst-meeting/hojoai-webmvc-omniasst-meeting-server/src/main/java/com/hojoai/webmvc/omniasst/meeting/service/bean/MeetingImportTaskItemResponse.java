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
package com.hojoai.webmvc.omniasst.meeting.service.bean;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class MeetingImportTaskItemResponse {

    private String meetingId;
    private String title;
    private String pipelineStatus;
    private String recordingCategory;
    private String knowledgeStatus;
    private Integer estimatedRemainingMinutes;
    private String errorCode;
    private String errorMessage;

    public String getMeetingId() {
        return meetingId;
    }

    public void setMeetingId(String meetingId) {
        this.meetingId = meetingId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPipelineStatus() {
        return pipelineStatus;
    }

    public void setPipelineStatus(String pipelineStatus) {
        this.pipelineStatus = pipelineStatus;
    }

    public String getRecordingCategory() {
        return recordingCategory;
    }

    public void setRecordingCategory(String recordingCategory) {
        this.recordingCategory = recordingCategory;
    }

    public String getKnowledgeStatus() {
        return knowledgeStatus;
    }

    public void setKnowledgeStatus(String knowledgeStatus) {
        this.knowledgeStatus = knowledgeStatus;
    }

    public Integer getEstimatedRemainingMinutes() {
        return estimatedRemainingMinutes;
    }

    public void setEstimatedRemainingMinutes(Integer estimatedRemainingMinutes) {
        this.estimatedRemainingMinutes = estimatedRemainingMinutes;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
