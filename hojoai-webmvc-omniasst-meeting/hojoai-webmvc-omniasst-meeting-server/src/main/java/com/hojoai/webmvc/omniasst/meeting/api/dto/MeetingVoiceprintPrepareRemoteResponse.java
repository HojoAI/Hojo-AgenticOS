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
 * management 返回的临时克隆语音 ID（阶段一不写入用户 Profile，仅返回供阶段二提交）。
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MeetingVoiceprintPrepareRemoteResponse {

    private String cloneVoiceId;

    public String getCloneVoiceId() {
        return cloneVoiceId;
    }

    public void setCloneVoiceId(String cloneVoiceId) {
        this.cloneVoiceId = cloneVoiceId;
    }
}
