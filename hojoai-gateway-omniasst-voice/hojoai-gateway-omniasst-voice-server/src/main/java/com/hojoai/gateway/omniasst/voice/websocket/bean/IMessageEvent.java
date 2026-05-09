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
package com.hojoai.gateway.omniasst.voice.websocket.bean;

import org.apache.commons.lang3.StringUtils;

public enum IMessageEvent {

    REQ_TTS_STREAM("REQ_TTS_STREAM"),

    REQ_TTS_TEXT("REQ_TTS_TEXT"),

    REQ_HEARTBEAT("REQ_HEARTBEAT"),

    BACK_HEARTBEAT("BACK_HEARTBEAT"),

    BACK_ASR_TEXT("BACK_ASR_TEXT"),

    BACK_ASR_COMPLETE("BACK_ASR_COMPLETE"),

    BACK_COMMON_TTS_AUDIO("BACK_COMMON_TTS_AUDIO"),

    BACK_COMMON_TTS_FLAG("BACK_COMMON_TTS_FLAG"),

    BACK_ERROR("BACK_ERROR"),

    GENERAL_ERROR("GENERAL_ERROR"),

    ILLEGAL_ARGUMENT("ILLEGAL_ARGUMENT"),

    UNSUPPORTED_OPERATION("UNSUPPORTED_OPERATION"),

    REQ_ASR_TRANSLATE_CONFIG("REQ_ASR_TRANSLATE_CONFIG"),
    REQ_ASR_TRANSLATE_AUDIO_DATA("REQ_ASR_TRANSLATE_AUDIO_DATA"),
    BACK_ASR_TRANSLATE_CONFIG_ACK("BACK_ASR_TRANSLATE_CONFIG_ACK"),
    BACK_ASR_TRANSLATE_RESULT("BACK_ASR_TRANSLATE_RESULT");

    private String eventType;
    private String reqEventKey;

    IMessageEvent(String eventType) {
        this.eventType = eventType;
    }

    IMessageEvent(String eventType, String reqEventKey) {
        this.eventType = eventType;
        this.reqEventKey = reqEventKey;
    }

    public static IMessageEvent getByEventType(String eventType) {
        if (StringUtils.isBlank(eventType)) {
            return null;
        }
        for (IMessageEvent event : IMessageEvent.values()) {
            if (event.getEventType().equals(eventType)) {
                return event;
            }
        }
        return null;
    }

    public String getEventType() {
        return eventType;
    }

    public String getReqEventKey() {
        return reqEventKey;
    }
}
