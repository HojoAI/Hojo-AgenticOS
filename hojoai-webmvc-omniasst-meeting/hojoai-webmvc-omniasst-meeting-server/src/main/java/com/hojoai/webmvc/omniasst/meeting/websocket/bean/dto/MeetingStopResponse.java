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
public class MeetingStopResponse {
    private String status;
    private String meetingId;
    private Long totalDuration;
    private Integer segmentCount;
    private String message;

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getMeetingId() { return meetingId; }
    public void setMeetingId(String meetingId) { this.meetingId = meetingId; }
    public Long getTotalDuration() { return totalDuration; }
    public void setTotalDuration(Long totalDuration) { this.totalDuration = totalDuration; }
    public Integer getSegmentCount() { return segmentCount; }
    public void setSegmentCount(Integer segmentCount) { this.segmentCount = segmentCount; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}

