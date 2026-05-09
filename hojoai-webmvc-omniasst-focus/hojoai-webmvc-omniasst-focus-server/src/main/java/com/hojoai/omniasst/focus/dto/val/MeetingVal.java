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
package com.hojoai.omniasst.focus.dto.val;

/**
 * Meeting Value DTO
 * Contains meeting information
 *
 * @author My
 * @version 1.0 2026/01/06
 * @since hojoai-webmvc-omniasst.focus 1.0.0
 */
public class MeetingVal {

    /**
     * 参与者（可选）
     */
    private String participant;

    /**
     * 会议id（可选）
     */
    private String meetingId;

    /**
     * 会议标题（可选）
     */
    private String meetingTitle;

    /**
     * 会议链接（可选）
     */
    private String meetingUrl;

    public MeetingVal() {
    }

    public String getParticipant() {
        return participant;
    }

    public void setParticipant(String participant) {
        this.participant = participant;
    }

    public String getMeetingId() {
        return meetingId;
    }

    public void setMeetingId(String meetingId) {
        this.meetingId = meetingId;
    }

    public String getMeetingTitle() {
        return meetingTitle;
    }

    public void setMeetingTitle(String meetingTitle) {
        this.meetingTitle = meetingTitle;
    }

    public String getMeetingUrl() {
        return meetingUrl;
    }

    public void setMeetingUrl(String meetingUrl) {
        this.meetingUrl = meetingUrl;
    }
}
