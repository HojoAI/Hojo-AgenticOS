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
 * 转发给 management：阶段二正式绑定用户、昵称、声纹向量与克隆 ID。
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MeetingVoiceprintCommitRemoteRequest {

    private String userId;
    private String nickname;
    private double[] voiceprint;
    /** 阶段一返回的临时克隆 ID，可为空（仅向量注册时） */
    private String cloneVoiceId;

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

    public double[] getVoiceprint() {
        return voiceprint;
    }

    public void setVoiceprint(double[] voiceprint) {
        this.voiceprint = voiceprint;
    }

    public String getCloneVoiceId() {
        return cloneVoiceId;
    }

    public void setCloneVoiceId(String cloneVoiceId) {
        this.cloneVoiceId = cloneVoiceId;
    }
}
