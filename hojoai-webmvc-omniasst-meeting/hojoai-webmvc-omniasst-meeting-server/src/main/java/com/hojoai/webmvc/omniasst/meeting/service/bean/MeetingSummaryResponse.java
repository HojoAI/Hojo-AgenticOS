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
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.List;

/**
 * The MeetingSummaryResponse class for meeting summary model.
 *
 * @author lianghay
 * @version 1.0, 2026/01/05
 * @since hojoai-webmvc-omniasst-meeting 1.0.0
 */

@JsonInclude(JsonInclude.Include.NON_NULL)
public class MeetingSummaryResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    @JsonProperty("title")
    private String title;

    @JsonProperty("basicInfo")
    private BasicInfo basicInfo;

    @JsonProperty("chapterSkim")
    private List<ChapterSkimItem> chapterSkim;

    @JsonProperty("coreTopics")
    private List<CoreTopicItem> coreTopics;

    @JsonProperty("relatedMaterials")
    private List<String> relatedMaterials;

    @JsonProperty("attendeeBexAnalysis")
    private List<AttendeeBexAnalysisItem> attendeeBexAnalysis;

    @JsonProperty("BexAdvice")
    private BexAdvice bexAdvice;

    @JsonProperty("todo")
    private List<TodoItem> todo;

    public MeetingSummaryResponse() {
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public BasicInfo getBasicInfo() {
        return basicInfo;
    }

    public void setBasicInfo(BasicInfo basicInfo) {
        this.basicInfo = basicInfo;
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

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class BasicInfo implements Serializable {

        private static final long serialVersionUID = 1L;

        @JsonProperty("attendees")
        private List<String> attendees;

        @JsonProperty("meetingType")
        private String meetingType;

        @JsonProperty("summary")
        private String summary;

        @JsonProperty("BexInterpretation")
        private String bexInterpretation;

        public BasicInfo() {
        }

        public String getMeetingType() {
            return meetingType;
        }

        public void setMeetingType(String meetingType) {
            this.meetingType = meetingType;
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
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ChapterSkimItem implements Serializable {

        private static final long serialVersionUID = 1L;

        @JsonProperty("timestamp")
        private String timestamp;

        @JsonProperty("content")
        private String content;

        @JsonProperty("BexVibe")
        private String bexVibe;

        @JsonProperty("BexInterpretation")
        private String bexInterpretation;

        public ChapterSkimItem() {
        }

        public String getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(String timestamp) {
            this.timestamp = timestamp;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public String getBexVibe() {
            return bexVibe;
        }

        public void setBexVibe(String bexVibe) {
            this.bexVibe = bexVibe;
        }

        public String getBexInterpretation() {
            return bexInterpretation;
        }

        public void setBexInterpretation(String bexInterpretation) {
            this.bexInterpretation = bexInterpretation;
        }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class CoreTopicItem implements Serializable {

        private static final long serialVersionUID = 1L;

        @JsonProperty("topic")
        private String topic;

        @JsonProperty("content")
        private String content;

        public CoreTopicItem() {
        }

        public String getTopic() {
            return topic;
        }

        public void setTopic(String topic) {
            this.topic = topic;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class AttendeeBexAnalysisItem implements Serializable {

        private static final long serialVersionUID = 1L;

        @JsonProperty("name")
        private String name;

        @JsonProperty("engagement")
        private String engagement;

        @JsonProperty("role")
        private String role;

        @JsonProperty("subtext")
        private String subtext;

        @JsonProperty("meVs")
        private String meVs;

        public AttendeeBexAnalysisItem() {
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getEngagement() {
            return engagement;
        }

        public void setEngagement(String engagement) {
            this.engagement = engagement;
        }

        public String getRole() {
            return role;
        }

        public void setRole(String role) {
            this.role = role;
        }

        public String getSubtext() {
            return subtext;
        }

        public void setSubtext(String subtext) {
            this.subtext = subtext;
        }

        public String getMeVs() {
            return meVs;
        }

        public void setMeVs(String meVs) {
            this.meVs = meVs;
        }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class BexAdvice implements Serializable {

        private static final long serialVersionUID = 1L;

        @JsonProperty("deductions")
        private List<BexAdviceItem> deductions;

        @JsonProperty("credits")
        private List<BexAdviceItem> credits;

        public BexAdvice() {
        }

        public List<BexAdviceItem> getDeductions() {
            return deductions;
        }

        public void setDeductions(List<BexAdviceItem> deductions) {
            this.deductions = deductions;
        }

        public List<BexAdviceItem> getCredits() {
            return credits;
        }

        public void setCredits(List<BexAdviceItem> credits) {
            this.credits = credits;
        }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class BexAdviceItem implements Serializable {

        private static final long serialVersionUID = 1L;

        @JsonProperty("keyPoint")
        private String keyPoint;

        @JsonProperty("keyContent")
        private String keyContent;

        public BexAdviceItem() {
        }

        public String getKeyPoint() {
            return keyPoint;
        }

        public void setKeyPoint(String keyPoint) {
            this.keyPoint = keyPoint;
        }

        public String getKeyContent() {
            return keyContent;
        }

        public void setKeyContent(String keyContent) {
            this.keyContent = keyContent;
        }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class TodoItem implements Serializable {

        private static final long serialVersionUID = 1L;

        @JsonProperty("id")
        private String id;

        @JsonProperty("assignee")
        private String assignee;

        @JsonProperty("assigneeId")
        private String assigneeId;

        @JsonProperty("datetime")
        private String datetime;

        @JsonProperty("event")
        private String event;

        @JsonProperty("status")
        private Integer status;

        @JsonProperty("isRecorderTodo")
        private Boolean isRecorderTodo;

        @JsonProperty("completed")
        private Integer completed;

        @JsonProperty("scheduleId")
        private String scheduleId;

        public TodoItem() {
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getAssignee() {
            return assignee;
        }

        public void setAssignee(String assignee) {
            this.assignee = assignee;
        }

        public String getAssigneeId() {
            return assigneeId;
        }

        public void setAssigneeId(String assigneeId) {
            this.assigneeId = assigneeId;
        }

        public String getDatetime() {
            return datetime;
        }

        public void setDatetime(String datetime) {
            this.datetime = datetime;
        }

        public String getEvent() {
            return event;
        }

        public void setEvent(String event) {
            this.event = event;
        }

        public Integer getStatus() {
            return status;
        }

        public void setStatus(Integer status) {
            this.status = status;
        }

        public Boolean getIsRecorderTodo() {
            return isRecorderTodo;
        }

        public void setIsRecorderTodo(Boolean isRecorderTodo) {
            this.isRecorderTodo = isRecorderTodo;
        }

        public Integer getCompleted() {
            return completed;
        }

        public void setCompleted(Integer completed) {
            this.completed = completed;
        }

        public String getScheduleId() {
            return scheduleId;
        }

        public void setScheduleId(String scheduleId) {
            this.scheduleId = scheduleId;
        }
    }
}
