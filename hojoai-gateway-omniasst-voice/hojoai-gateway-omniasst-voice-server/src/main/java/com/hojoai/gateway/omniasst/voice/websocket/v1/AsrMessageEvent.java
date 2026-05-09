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
package com.hojoai.gateway.omniasst.voice.websocket.v1;

import org.apache.commons.lang3.StringUtils;

public enum AsrMessageEvent {
    
    // 请求事件
    REQ_HEARTBEAT("REQ_HEARTBEAT"),
    REQ_ASR_CONFIG("REQ_ASR_CONFIG"),
    REQ_ASR_AUDIO_DATA("REQ_ASR_AUDIO_DATA"),
    
    // 响应事件
    BACK_HEARTBEAT("BACK_HEARTBEAT"),
    BACK_ASR_CONFIG_ACK("BACK_ASR_CONFIG_ACK"),
    BACK_ASR_RESULT("BACK_ASR_RESULT"),
    RESP_ASR_RESULT("RESP_ASR_RESULT"),
    RESP_VAD_TIMEOUT("RESP_VAD_TIMEOUT"),
    BACK_ERROR("BACK_ERROR"),
    
    // 错误事件
    GENERAL_ERROR("GENERAL_ERROR"),
    ILLEGAL_ARGUMENT("ILLEGAL_ARGUMENT"),
    UNSUPPORTED_OPERATION("UNSUPPORTED_OPERATION");
    
    private final String eventType;
    
    AsrMessageEvent(String eventType) {
        this.eventType = eventType;
    }
    
    public static AsrMessageEvent getByEventType(String eventType) {
        if (StringUtils.isBlank(eventType)) {
            return null;
        }
        for (AsrMessageEvent event : AsrMessageEvent.values()) {
            if (event.getEventType().equals(eventType)) {
                return event;
            }
        }
        return null;
    }
    
    public String getEventType() {
        return eventType;
    }
}
