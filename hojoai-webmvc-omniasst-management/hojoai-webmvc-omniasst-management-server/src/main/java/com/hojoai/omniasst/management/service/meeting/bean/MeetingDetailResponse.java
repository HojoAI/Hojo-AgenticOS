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
package com.hojoai.omniasst.management.service.meeting.bean;

import java.io.Serializable;
import java.util.List;

/**
 * The response bean for meeting detail.
 *
 * @author zhangxuepeng
 * @version 1.0, 2025/12/16
 * @since hojoai-webmvc-omniasst-management 1.0.0
 */
public class MeetingDetailResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    private String meetingId;

    private String userId;

    private String title;

    private Long startTime;

    private Long endTime;

    private Long totalDuration;

    private Integer summaryStatus;

    private String createdAt;

    private String updatedAt;

    private String ossUrl;

    private List<MeetingSegment> segments;

    private List<String> attendees;

    private String summary;

    private String bexInterpretation;

    private List<ChapterSkimItem> chapterSkim;

    private List<CoreTopicItem> coreTopics;

    private List<String> relatedMaterials;

    private List<String> knowledge;

    private List<AttendeeBexAnalysisItem> attendeeBexAnalysis;

    private BexAdvice bexAdvice;

    private List<TodoItem> todo;

    public MeetingDetailResponse() {
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

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getOssUrl() {
        return ossUrl;
    }

    public void setOssUrl(String ossUrl) {
        this.ossUrl = ossUrl;
    }

    public List<MeetingSegment> getSegments() {
        return segments;
    }

    public void setSegments(List<MeetingSegment> segments) {
        this.segments = segments;
    }

    public List<String> getAttendees() {
        return attendees;
    }

    public void setAttendees(List<String> attendees) {
        this.attendees = attendees;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getBexInterpretation() {
        return bexInterpretation;
    }

    public void setBexInterpretation(String bexInterpretation) {
        this.bexInterpretation = bexInterpretation;
    }

    public List<ChapterSkimItem> getChapterSkim() {
        return chapterSkim;
    }

    public void setChapterSkim(List<ChapterSkimItem> chapterSkim) {
        this.chapterSkim = chapterSkim;
    }

    public List<CoreTopicItem> getCoreTopics() {
        return coreTopics;
    }

    public void setCoreTopics(List<CoreTopicItem> coreTopics) {
        this.coreTopics = coreTopics;
    }

    public List<String> getRelatedMaterials() {
        return relatedMaterials;
    }

    public void setRelatedMaterials(List<String> relatedMaterials) {
        this.relatedMaterials = relatedMaterials;
    }

    public List<String> getKnowledge() {
        return knowledge;
    }

    public void setKnowledge(List<String> knowledge) {
        this.knowledge = knowledge;
    }

    public List<AttendeeBexAnalysisItem> getAttendeeBexAnalysis() {
        return attendeeBexAnalysis;
    }

    public void setAttendeeBexAnalysis(List<AttendeeBexAnalysisItem> attendeeBexAnalysis) {
        this.attendeeBexAnalysis = attendeeBexAnalysis;
    }

    public BexAdvice getBexAdvice() {
        return bexAdvice;
    }

    public void setBexAdvice(BexAdvice bexAdvice) {
        this.bexAdvice = bexAdvice;
    }

    public List<TodoItem> getTodo() {
        return todo;
    }

    public void setTodo(List<TodoItem> todo) {
        this.todo = todo;
    }
}
