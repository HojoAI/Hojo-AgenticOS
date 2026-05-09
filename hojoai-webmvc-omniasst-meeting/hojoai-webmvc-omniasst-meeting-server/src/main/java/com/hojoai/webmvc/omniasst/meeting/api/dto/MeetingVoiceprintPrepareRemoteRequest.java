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
package com.hojoai.webmvc.omniasst.meeting.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * 转发给 management：阶段一克隆/预处理（不落用户正式关联）。
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MeetingVoiceprintPrepareRemoteRequest {

    private String userId;
    /** Base64 编码的注册音频 */
    private String audioBase64;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getAudioBase64() {
        return audioBase64;
    }

    public void setAudioBase64(String audioBase64) {
        this.audioBase64 = audioBase64;
    }
}
