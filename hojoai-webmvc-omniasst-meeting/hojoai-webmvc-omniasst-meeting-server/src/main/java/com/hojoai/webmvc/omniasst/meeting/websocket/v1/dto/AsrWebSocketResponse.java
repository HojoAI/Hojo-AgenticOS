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
public class AsrWebSocketResponse {
    private String fixedText;
    private String tmpText;
    private String text;
    private Integer isEnd;
    private String custom;

    public String getFixedText() {
        return fixedText;
    }

    public void setFixedText(String fixedText) {
        this.fixedText = fixedText;
    }

    public String getTmpText() {
        return tmpText;
    }

    public void setTmpText(String tmpText) {
        this.tmpText = tmpText;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Integer getIsEnd() {
        return isEnd;
    }

    public void setIsEnd(Integer isEnd) {
        this.isEnd = isEnd;
    }

    public String getCustom() {
        return custom;
    }

    public void setCustom(String custom) {
        this.custom = custom;
    }

    @Override
    public String toString() {
        return "AsrWebSocketResponse{" +
                "fixedText='" + fixedText + '\'' +
                ", tmpText='" + tmpText + '\'' +
                ", text='" + text + '\'' +
                ", isEnd=" + isEnd +
                ", custom='" + custom + '\'' +
                '}';
    }
}
