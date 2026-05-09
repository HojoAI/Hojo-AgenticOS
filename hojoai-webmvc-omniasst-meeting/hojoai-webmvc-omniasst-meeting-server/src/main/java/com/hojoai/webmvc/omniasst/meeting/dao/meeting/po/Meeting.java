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
package com.hojoai.webmvc.omniasst.meeting.dao.meeting.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.util.Date;

@TableName(value = "meetings")
public class Meeting implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField(value = "meeting_id")
    private String meetingId;

    @TableField(value = "user_id")
    private String userId;

    @TableField(value = "title")
    private String title;

    @TableField(value = "meeting_type")
    private String meetingType;

    @TableField(value = "start_time")
    private Long startTime;

    @TableField(value = "end_time")
    private Long endTime;

    @TableField(value = "total_duration")
    private Long totalDuration;

    @TableField(value = "summary_status")
    private Integer summaryStatus;

    @TableField(value = "oss_url")
    private String ossUrl;

    @TableField(value = "segments_json")
    private String segmentsJson;

    @TableField(value = "attendees")
    private String attendees;

    @TableField(value = "summary")
    private String summary;

    @TableField(value = "bex_interpretation")
    private String bexInterpretation;

    @TableField(value = "chapter_skim")
    private String chapterSkim;

    @TableField(value = "core_topics")
    private String coreTopics;

    @TableField(value = "related_materials")
    private String relatedMaterials;

    @TableField(value = "knowledge")
    private String knowledge;

    @TableField(value = "knowledge_count")
    private Integer knowledgeCount;

    @TableField(value = "attendee_bex_analysis")
    private String attendeeBexAnalysis;

    @TableField(value = "bex_advice")
    private String bexAdvice;

    @TableField(value = "todo")
    private String todo;

    @TableField(value = "created_at")
    private Date createdAt;

    @TableField(value = "updated_at")
    private Date updatedAt;

    public Meeting() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getOssUrl() {
        return ossUrl;
    }

    public void setOssUrl(String ossUrl) {
        this.ossUrl = ossUrl;
    }

    public String getSegmentsJson() {
        return segmentsJson;
    }

    public void setSegmentsJson(String segmentsJson) {
        this.segmentsJson = segmentsJson;
    }

    public String getAttendees() {
        return attendees;
    }

    public void setAttendees(String attendees) {
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

    public String getChapterSkim() {
        return chapterSkim;
    }

    public void setChapterSkim(String chapterSkim) {
        this.chapterSkim = chapterSkim;
    }

    public String getCoreTopics() {
        return coreTopics;
    }

    public void setCoreTopics(String coreTopics) {
        this.coreTopics = coreTopics;
    }

    public String getRelatedMaterials() {
        return relatedMaterials;
    }

    public void setRelatedMaterials(String relatedMaterials) {
        this.relatedMaterials = relatedMaterials;
    }

    public String getKnowledge() {
        return knowledge;
    }

    public void setKnowledge(String knowledge) {
        this.knowledge = knowledge;
    }

    public Integer getKnowledgeCount() {
        return knowledgeCount;
    }

    public void setKnowledgeCount(Integer knowledgeCount) {
        this.knowledgeCount = knowledgeCount;
    }

    public String getAttendeeBexAnalysis() {
        return attendeeBexAnalysis;
    }

    public void setAttendeeBexAnalysis(String attendeeBexAnalysis) {
        this.attendeeBexAnalysis = attendeeBexAnalysis;
    }

    public String getBexAdvice() {
        return bexAdvice;
    }

    public void setBexAdvice(String bexAdvice) {
        this.bexAdvice = bexAdvice;
    }

    public String getTodo() {
        return todo;
    }

    public void setTodo(String todo) {
        this.todo = todo;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getMeetingType() {
        return meetingType;
    }

    public void setMeetingType(String meetingType) {
        this.meetingType = meetingType;
    }
}

