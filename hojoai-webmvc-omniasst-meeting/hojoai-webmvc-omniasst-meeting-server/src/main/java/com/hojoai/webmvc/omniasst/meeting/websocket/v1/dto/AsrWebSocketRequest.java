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
public class AsrWebSocketRequest {
    private String audioData;
    private String custom;
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

    /**
     * ASR WebSocket 自定义数据（Custom Data）
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class CustomData {
        private Long audioIndex;
        private Long previousAudioSendTime;
        private Long currentAudioTime;
        private Boolean isStop;

        public Long getAudioIndex() {
            return audioIndex;
        }

        public void setAudioIndex(Long audioIndex) {
            this.audioIndex = audioIndex;
        }

        public Long getPreviousAudioSendTime() {
            return previousAudioSendTime;
        }

        public void setPreviousAudioSendTime(Long previousAudioSendTime) {
            this.previousAudioSendTime = previousAudioSendTime;
        }

        public Long getCurrentAudioTime() {
            return currentAudioTime;
        }

        public void setCurrentAudioTime(Long currentAudioTime) {
            this.currentAudioTime = currentAudioTime;
        }

        public Boolean getIsStop() {
            return isStop;
        }

        public void setIsStop(Boolean isStop) {
            this.isStop = isStop;
        }
    }
}
