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

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class MeetingVoiceprintRegisterCommitRequest {

    private String userId;
    /** 展示用昵称；可选，可省略（management 允许空 {@code voiceName}/{@code userName}） */
    private String nickname;
    /** 音色/展示名称，兼容旧命名；可选，优先使用 {@link #nickname} */
    private String voiceName;
    /** 厂商标识，转发 management {@code voice/save}；必填，须与 prepare 约定一致（如 {@code hojo}、{@code minimax}） */
    private String provider;

    /**
     * 为 {@code true} 时：仅更新昵称（音色 user_voice_profile + 声纹展示名），不要求 prepare 阶段 Redis 暂存。
     * 请求体仍须带 {@link #nickname} 或 {@link #voiceName}（解析后非空）、{@link #userId}、{@link #provider}。
     */
    private Boolean updateNicknameOnly;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getVoiceName() {
        return voiceName;
    }

    public void setVoiceName(String voiceName) {
        this.voiceName = voiceName;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public Boolean getUpdateNicknameOnly() {
        return updateNicknameOnly;
    }

    public void setUpdateNicknameOnly(Boolean updateNicknameOnly) {
        this.updateNicknameOnly = updateNicknameOnly;
    }
}
