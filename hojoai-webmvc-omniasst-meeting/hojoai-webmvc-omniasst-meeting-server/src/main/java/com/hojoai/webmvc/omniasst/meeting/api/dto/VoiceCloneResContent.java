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

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * management {@code /v0/voice/clone} 的 resContent：常返回 {@code voice_id}，与 {@code voiceId} 同义。
 * 与 {@link com.hojoai.webmvc.omniasst.meeting.service.bean.MeetingVoiceprintRegisterPrepareResponse} 一致仅用 {@link JsonAlias}，避免依赖 Jackson 2.9+ 的 {@code JsonProperty#alternate}（老版本 classpath 可能运行报错）。
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class VoiceCloneResContent {

    @JsonAlias({"voiceId", "voice_id"})
    private String voiceId;

    public VoiceCloneResContent() {}

    public VoiceCloneResContent(String voiceId) {
        this.voiceId = voiceId;
    }

    public String getVoiceId() {
        return voiceId;
    }

    public void setVoiceId(String voiceId) {
        this.voiceId = voiceId;
    }
}
