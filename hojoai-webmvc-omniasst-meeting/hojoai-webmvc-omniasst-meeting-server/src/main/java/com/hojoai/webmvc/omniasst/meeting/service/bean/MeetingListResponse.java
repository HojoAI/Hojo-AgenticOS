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

import java.io.Serializable;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class MeetingListResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    private String meetingId;

    private String userId;

    private String title;

    private Long startTime;

    private Long endTime;

    private Long totalDuration;

    private Integer summaryStatus;

    private String createdAt;

    private String summary;

    private Integer knowledgeCount;

    private Integer estimatedAiTime;

    public MeetingListResponse() {
    }

    public String getMeetingId() {
        return meetingId;
    }

    public void setMeetingId(String meetingId) {
        this.meetingId = meetingId;
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

    public Long getStartTime() {
        return startTime;
    }

    public void setStartTime(Long startTime) {
        this.startTime = startTime;
    }

    public Long getEndTime() {
        return endTime;
    }

    public void setEndTime(Long endTime) {
        this.endTime = endTime;
    }

    public Long getTotalDuration() {
        return totalDuration;
    }

    public void setTotalDuration(Long totalDuration) {
        this.totalDuration = totalDuration;
    }

    public Integer getSummaryStatus() {
        return summaryStatus;
    }

    public void setSummaryStatus(Integer summaryStatus) {
        this.summaryStatus = summaryStatus;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public Integer getKnowledgeCount() {
        return knowledgeCount;
    }

    public void setKnowledgeCount(Integer knowledgeCount) {
        this.knowledgeCount = knowledgeCount;
    }

    public Integer getEstimatedAiTime() {
        return estimatedAiTime;
    }

    public void setEstimatedAiTime(Integer estimatedAiTime) {
        this.estimatedAiTime = estimatedAiTime;
    }
}
