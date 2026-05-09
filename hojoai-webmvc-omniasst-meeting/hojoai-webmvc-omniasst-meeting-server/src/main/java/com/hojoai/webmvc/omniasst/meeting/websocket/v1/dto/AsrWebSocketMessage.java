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
package com.hojoai.webmvc.omniasst.meeting.websocket.v1.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class AsrWebSocketMessage<T> {
    private String event;
    private Payload<T> payload;

    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    public Payload<T> getPayload() {
        return payload;
    }

    public void setPayload(Payload<T> payload) {
        this.payload = payload;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Payload<T> {
        private T pdata;

        public T getPdata() {
            return pdata;
        }

        public void setPdata(T pdata) {
            this.pdata = pdata;
        }
    }

    /**
     * 创建消息实例
     */
    public static <T> AsrWebSocketMessage<T> create(String event, T data) {
        AsrWebSocketMessage<T> message = new AsrWebSocketMessage<>();
        message.setEvent(event);
        
        Payload<T> payload = new Payload<>();
        payload.setPdata(data);
        message.setPayload(payload);
        
        return message;
    }
}
