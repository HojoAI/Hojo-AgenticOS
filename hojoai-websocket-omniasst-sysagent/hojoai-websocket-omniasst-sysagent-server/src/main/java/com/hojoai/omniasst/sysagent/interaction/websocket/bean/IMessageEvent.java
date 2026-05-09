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
package com.hojoai.omniasst.sysagent.interaction.websocket.bean;

/**
 * The enum class for web socket message event.
 *
 * @author kevanwong
 * @version 1.0, 2025/08/19
 * @since hojoai-websocket-omniasst-sysagent 1.0.0
 */
public enum IMessageEvent {

    GENERAL_ERROR("GENERAL_ERROR"),

    ILLEGAL_ARGUMENT("ILLEGAL_ARGUMENT"),

    UNSUPPORTED_OPERATION("UNSUPPORTED_OPERATION"),

    BUILD_RTC_TOKEN("BUILD_RTC_TOKEN"),

    BUILD_RTC_TOKEN_RESULT("BUILD_RTC_TOKEN_RESULT"),

    TICKET_RESULT("TICKET_RESULT")
    ;

    private String eventType;

    IMessageEvent(String eventType) {
        this.eventType = eventType;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }
}
