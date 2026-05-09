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
package com.hojoai.gateway.omniasst.voice.websocket.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AsrAudioDataRequest {
    
    @JsonProperty("audioData")
    private String audioData;
    
    @JsonProperty("custom")
    private String custom;
    
    @JsonProperty("isEnd")
    private Boolean isEnd = false;
    
    public String getAudioData() {
        return audioData;
    }
    
    public void setAudioData(String audioData) {
        this.audioData = audioData;
    }
    
    public String getCustom() {
        return custom;
    }
    
    public void setCustom(String custom) {
        this.custom = custom;
    }
    
    public Boolean getIsEnd() {
        return isEnd;
    }
    
    public void setIsEnd(Boolean isEnd) {
        this.isEnd = isEnd;
    }
    
    @Override
    public String toString() {
        return "AsrAudioDataRequest{" +
                "audioData='" + (audioData != null ? audioData.substring(0, Math.min(50, audioData.length())) + "..." : "null") + '\'' +
                ", custom='" + custom + '\'' +
                ", isEnd=" + isEnd +
                '}';
    }
}
