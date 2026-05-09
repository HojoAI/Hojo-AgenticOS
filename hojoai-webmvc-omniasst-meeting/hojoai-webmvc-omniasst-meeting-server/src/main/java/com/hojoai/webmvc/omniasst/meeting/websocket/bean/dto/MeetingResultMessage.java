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
package com.hojoai.webmvc.omniasst.meeting.websocket.bean.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class MeetingResultMessage {
    private String meetingId;
    private Long timestamp;
    private String userId;
    private SegmentInfo segment;
    private String ossUrl;
    private Long totalDuration;
    private Long startTime;
    private Long endTime;
    private Integer needSave;

    public String getMeetingId() { return meetingId; }
    public void setMeetingId(String meetingId) { this.meetingId = meetingId; }
    public Long getTimestamp() { return timestamp; }
    public void setTimestamp(Long timestamp) { this.timestamp = timestamp; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public SegmentInfo getSegment() { return segment; }
    public void setSegment(SegmentInfo segment) { this.segment = segment; }
    public String getOssUrl() { return ossUrl; }
    public void setOssUrl(String ossUrl) { this.ossUrl = ossUrl; }
    public Long getTotalDuration() { return totalDuration; }
    public void setTotalDuration(Long totalDuration) { this.totalDuration = totalDuration; }
    public Long getStartTime() { return startTime; }
    public void setStartTime(Long startTime) { this.startTime = startTime; }
    public Long getEndTime() { return endTime; }
    public void setEndTime(Long endTime) { this.endTime = endTime; }
    public Integer getNeedSave() { return needSave; }
    public void setNeedSave(Integer needSave) { this.needSave = needSave; }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class SegmentInfo {
        private String segmentId;
        private String text;
        private String speakerId;
        private String speakerName;
        private String speakerAvatar;
        private Long startTime;
        private Long endTime;
        private Long duration;

        public String getSegmentId() { return segmentId; }
        public void setSegmentId(String segmentId) { this.segmentId = segmentId; }
        public String getText() { return text; }
        public void setText(String text) { this.text = text; }
        public String getSpeakerId() { return speakerId; }
        public void setSpeakerId(String speakerId) { this.speakerId = speakerId; }
        public String getSpeakerName() { return speakerName; }
        public void setSpeakerName(String speakerName) { this.speakerName = speakerName; }
        public String getSpeakerAvatar() { return speakerAvatar; }
        public void setSpeakerAvatar(String speakerAvatar) { this.speakerAvatar = speakerAvatar; }
        public Long getStartTime() { return startTime; }
        public void setStartTime(Long startTime) { this.startTime = startTime; }
        public Long getEndTime() { return endTime; }
        public void setEndTime(Long endTime) { this.endTime = endTime; }
        public Long getDuration() { return duration; }
        public void setDuration(Long duration) { this.duration = duration; }
    }
}

