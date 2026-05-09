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

import java.io.Serializable;

/**
 * 会议详情页打标（HTTP），与 WebSocket {@code REQ_MEETING_MARK} 落库结构一致。
 */
public class MeetingDetailMarkRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    private String meetingId;

    private String userId;

    private String segmentId;

    /** 相对会议录制时间轴的毫秒偏移 */
    private Long offsetMs;

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

    public String getSegmentId() {
        return segmentId;
    }

    public void setSegmentId(String segmentId) {
        this.segmentId = segmentId;
    }

    public Long getOffsetMs() {
        return offsetMs;
    }

    public void setOffsetMs(Long offsetMs) {
        this.offsetMs = offsetMs;
    }
}
