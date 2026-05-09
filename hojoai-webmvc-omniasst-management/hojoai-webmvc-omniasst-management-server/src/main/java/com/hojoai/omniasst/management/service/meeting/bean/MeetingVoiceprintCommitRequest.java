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
package com.hojoai.omniasst.management.service.meeting.bean;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 与文档 3.3 一致：昵称页提交（JSON）。
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MeetingVoiceprintCommitRequest {

    private String userId;
    private String nickname;
    private String voiceName;
    private String provider;

    @JsonProperty("user_id")
    private String userIdSnake;

    @JsonProperty("nick_name")
    private String nicknameSnake;

    @JsonProperty("voice_name")
    private String voiceNameSnake;

    public String getUserId() {
        if (userId != null && !userId.isBlank()) {
            return userId.trim();
        }
        if (userIdSnake != null && !userIdSnake.isBlank()) {
            return userIdSnake.trim();
        }
        return null;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserIdSnake() {
        return userIdSnake;
    }

    public void setUserIdSnake(String userIdSnake) {
        this.userIdSnake = userIdSnake;
    }

    public String getNickname() {
        if (nickname != null && !nickname.isBlank()) {
            return nickname.trim();
        }
        if (nicknameSnake != null && !nicknameSnake.isBlank()) {
            return nicknameSnake.trim();
        }
        return null;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getNicknameSnake() {
        return nicknameSnake;
    }

    public void setNicknameSnake(String nicknameSnake) {
        this.nicknameSnake = nicknameSnake;
    }

    public String getVoiceName() {
        if (voiceName != null && !voiceName.isBlank()) {
            return voiceName.trim();
        }
        if (voiceNameSnake != null && !voiceNameSnake.isBlank()) {
            return voiceNameSnake.trim();
        }
        return null;
    }

    public void setVoiceName(String voiceName) {
        this.voiceName = voiceName;
    }

    public String getVoiceNameSnake() {
        return voiceNameSnake;
    }

    public void setVoiceNameSnake(String voiceNameSnake) {
        this.voiceNameSnake = voiceNameSnake;
    }

    public String getProvider() {
        return provider == null || provider.isBlank() ? null : provider.trim();
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }
}
