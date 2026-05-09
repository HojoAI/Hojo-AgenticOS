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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class IMessage<T> {

    @JsonProperty("event")
    private String eventType;
    @JsonProperty("payload")
    private T eventContent;

    protected IMessage() {
    }

    protected IMessage(String eventType) {
        this.eventType = eventType;
    }

    protected IMessage(String eventType, T eventContent) {
        this.eventType = eventType;
        this.eventContent = eventContent;
    }

    public static <T> IMessage<T> newInstance() {
        return new IMessage<T>();
    }

    public static <T> IMessage<T> newInstance(String eventType) {
        return new IMessage<T>(eventType);
    }

    public static <T> IMessage<T> newInstance(String eventType, T eventContent) {
        return new IMessage<T>(eventType, eventContent);
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public T getEventContent() {
        return eventContent;
    }

    public void setEventContent(T eventContent) {
        this.eventContent = eventContent;
    }
}
