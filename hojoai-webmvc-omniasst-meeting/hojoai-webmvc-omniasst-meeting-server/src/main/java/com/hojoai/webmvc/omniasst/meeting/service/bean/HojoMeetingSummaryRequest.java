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
import java.util.List;

/**
 * Hojo 会议总结接口请求体。
 *
 * @since hojoai-webmvc-omniasst-meeting 1.0.0
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class HojoMeetingSummaryRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 会议逐字稿内容，格式：发言人：文本\n */
    private String transcriptContent;
    /** 发言人名称集合 */
    private List<String> attendeeIdentity;
    /** 录制者名字，无则空字符串 */
    private String myIdentity;
    /** 固定 false */
    private Boolean stream;

    public String getTranscriptContent() {
        return transcriptContent;
    }

    public void setTranscriptContent(String transcriptContent) {
        this.transcriptContent = transcriptContent;
    }

    public List<String> getAttendeeIdentity() {
        return attendeeIdentity;
    }

    public void setAttendeeIdentity(List<String> attendeeIdentity) {
        this.attendeeIdentity = attendeeIdentity;
    }

    public String getMyIdentity() {
        return myIdentity;
    }

    public void setMyIdentity(String myIdentity) {
        this.myIdentity = myIdentity;
    }

    public Boolean getStream() {
        return stream;
    }

    public void setStream(Boolean stream) {
        this.stream = stream;
    }
}
