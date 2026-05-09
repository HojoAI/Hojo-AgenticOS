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
package com.hojoai.webmvc.omniasst.meeting.websocket.bean;

import org.apache.commons.lang3.StringUtils;

public enum IMessageEvent {
    REQ_HEARTBEAT("REQ_HEARTBEAT"),
    REQ_MEETING_AUDIO_DATA("REQ_MEETING_AUDIO_DATA"),
    REQ_MEETING_PAUSE("REQ_MEETING_PAUSE"),
    REQ_MEETING_RESUME("REQ_MEETING_RESUME"),
    REQ_MEETING_STOP("REQ_MEETING_STOP"),
    REQ_MEETING_MARK("REQ_MEETING_MARK"),
    BACK_HEARTBEAT("BACK_HEARTBEAT"),
    BACK_MEETING_ID("BACK_MEETING_ID"),
    BACK_MEETING_ASR_TEXT("BACK_MEETING_ASR_TEXT"),
    BACK_MEETING_SEGMENT_COMPLETE("BACK_MEETING_SEGMENT_COMPLETE"),
    BACK_MEETING_PAUSE_ACK("BACK_MEETING_PAUSE_ACK"),
    BACK_MEETING_RESUME_ACK("BACK_MEETING_RESUME_ACK"),
    BACK_MEETING_STOP_ACK("BACK_MEETING_STOP_ACK"),
    BACK_MEETING_MARK_ACK("BACK_MEETING_MARK_ACK"),
    BACK_ERROR("BACK_ERROR"),
    GENERAL_ERROR("GENERAL_ERROR"),
    ILLEGAL_ARGUMENT("ILLEGAL_ARGUMENT"),
    UNSUPPORTED_OPERATION("UNSUPPORTED_OPERATION");

    private String eventType;
    private String reqEventKey;

    IMessageEvent(String eventType) { this.eventType = eventType; }
    IMessageEvent(String eventType, String reqEventKey) {
        this.eventType = eventType;
        this.reqEventKey = reqEventKey;
    }

    public static IMessageEvent getByEventType(String eventType) {
        if (StringUtils.isBlank(eventType)) return null;
        for (IMessageEvent event : IMessageEvent.values()) {
            if (event.getEventType().equals(eventType)) return event;
        }
        return null;
    }

    public String getEventType() { return eventType; }
    public String getReqEventKey() { return reqEventKey; }
}

