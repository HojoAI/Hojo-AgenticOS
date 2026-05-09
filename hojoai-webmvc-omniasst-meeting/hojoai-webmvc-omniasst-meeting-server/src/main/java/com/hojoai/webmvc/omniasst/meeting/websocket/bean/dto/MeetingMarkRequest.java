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

/**
 * 端侧按时间偏移打标（相对会议时间轴，毫秒），对应协议 REQ_MEETING_MARK。
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MeetingMarkRequest {

    /** 当前时间戳（相对会议开始录制的时间轴，毫秒） */
    private Long offsetMs;

    public Long getOffsetMs() {
        return offsetMs;
    }

    public void setOffsetMs(Long offsetMs) {
        this.offsetMs = offsetMs;
    }
}
