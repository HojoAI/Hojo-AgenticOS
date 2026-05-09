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
public class MeetingAsrTextResponse {
    private String fixedText;
    private String tmpText;
    private Integer isEnd;
    private String segmentId;
    private String segmentStartTime;
    private String speakerId;
    private String speakerName;
    private String speakerAvatar;
    /** 该语音段落是否已被打标 */
    private Boolean isMark;

    public String getFixedText() { return fixedText; }
    public void setFixedText(String fixedText) { this.fixedText = fixedText; }
    public String getTmpText() { return tmpText; }
    public void setTmpText(String tmpText) { this.tmpText = tmpText; }
    public Integer getIsEnd() { return isEnd; }
    public void setIsEnd(Integer isEnd) { this.isEnd = isEnd; }
    public String getSegmentId() { return segmentId; }
    public void setSegmentId(String segmentId) { this.segmentId = segmentId; }
    public String getSegmentStartTime() { return segmentStartTime; }
    public void setSegmentStartTime(String segmentStartTime) { this.segmentStartTime = segmentStartTime; }

    public String getSpeakerId() {
        return speakerId;
    }

    public void setSpeakerId(String speakerId) {
        this.speakerId = speakerId;
    }

    public String getSpeakerName() {
        return speakerName;
    }

    public void setSpeakerName(String speakerName) {
        this.speakerName = speakerName;
    }

    public String getSpeakerAvatar() {
        return speakerAvatar;
    }

    public void setSpeakerAvatar(String speakerAvatar) {
        this.speakerAvatar = speakerAvatar;
    }

    public Boolean getIsMark() {
        return isMark;
    }

    public void setIsMark(Boolean mark) {
        isMark = mark;
    }
}

