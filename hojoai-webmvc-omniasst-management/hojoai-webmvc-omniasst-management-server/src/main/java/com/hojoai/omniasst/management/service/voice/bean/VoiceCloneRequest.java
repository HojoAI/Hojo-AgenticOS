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
package com.hojoai.omniasst.management.service.voice.bean;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class VoiceCloneRequest {

    private final String userId;
    private final String userIdSnake;

    private final String voiceName;
    private final String voiceNameSnake;

    private final String nickname;
    private final String nicknameSnake;

    private final String provider;

    private final String voiceId;
    private final String voiceIdSnake;

    @JsonCreator
    public VoiceCloneRequest(
            @JsonProperty("userId") String userId,
            @JsonProperty("user_id") String userIdSnake,
            @JsonProperty("voiceName") String voiceName,
            @JsonProperty("voice_name") String voiceNameSnake,
            @JsonProperty("nickname") String nickname,
            @JsonProperty("nick_name") String nicknameSnake,
            @JsonProperty("provider") String provider,
            @JsonProperty("voiceId") String voiceId,
            @JsonProperty("voice_id") String voiceIdSnake
    ) {
        this.userId = userId;
        this.userIdSnake = userIdSnake;
        this.voiceName = voiceName;
        this.voiceNameSnake = voiceNameSnake;
        this.nickname = nickname;
        this.nicknameSnake = nicknameSnake;
        this.provider = provider;
        this.voiceId = voiceId;
        this.voiceIdSnake = voiceIdSnake;
    }

    public String getUserId() {
        if (userId != null && !userId.isBlank()) {
            return userId.trim();
        }
        if (userIdSnake != null && !userIdSnake.isBlank()) {
            return userIdSnake.trim();
        }
        return null;
    }

    public String getVoiceName() {
        String camel = voiceName != null && !voiceName.isBlank() ? voiceName.trim() : null;
        String snake = voiceNameSnake != null && !voiceNameSnake.isBlank() ? voiceNameSnake.trim() : null;
        if (camel == null) {
            return snake;
        }
        if (snake == null) {
            return camel;
        }
        if (camel.equals(snake)) {
            return camel;
        }
        return camel.length() >= snake.length() ? camel : snake;
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

    public String getProvider() {
        return provider == null || provider.isBlank() ? null : provider.trim();
    }

    public String getVoiceId() {
        if (voiceId != null && !voiceId.isBlank()) {
            return voiceId.trim();
        }
        if (voiceIdSnake != null && !voiceIdSnake.isBlank()) {
            return voiceIdSnake.trim();
        }
        return null;
    }
}
