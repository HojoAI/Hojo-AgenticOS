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
package com.hojoai.webmvc.omniasst.meeting.service.bean;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * 阶段一：暂存按 {@code userId} 绑定；阶段二 {@code commit} 仅需相同 {@code userId}。克隆语音 ID 若存在则一并返回（仍未与用户正式绑定）。
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MeetingVoiceprintRegisterPrepareResponse {

    private String cloneVoiceId;
    /**
     * 对外 JSON 字段名为 {@code voiceId}（与 management 原始 JSON 的 {@code voice_id} 同义）。
     * commit 会从 Redis 会话里读取，前端可不回传；联调 {@code bypass-voice-recognition=true} 时可能为 null。
     */
    @JsonAlias({"voiceId", "voice_id"})
    @JsonInclude(JsonInclude.Include.ALWAYS)
    private String voiceId;

    public String getCloneVoiceId() {
        return cloneVoiceId;
    }

    public void setCloneVoiceId(String cloneVoiceId) {
        this.cloneVoiceId = cloneVoiceId;
    }

    public String getVoiceId() {
        return voiceId;
    }

    public void setVoiceId(String voiceId) {
        this.voiceId = voiceId;
    }
}
