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
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 转发 management {@code POST /v0/voice/save}；与 management 文档字段对齐，可选字段前端可省略。
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MeetingVoiceSaveRequest {

    private String userId;
    private String voiceName;
    private String provider;
    private String voiceId;
    /** 用户昵称；management {@code POST /v0/voice/save} 可选字段 */
    private String nickname;

    /**
     * 仅会议侧使用：VOICE=声音复刻（默认，可不传）；勿传给 management。
     */
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String replicateType;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
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

    public String getVoiceId() {
        return voiceId;
    }

    public void setVoiceId(String voiceId) {
        this.voiceId = voiceId;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getReplicateType() {
        return replicateType;
    }

    public void setReplicateType(String replicateType) {
        this.replicateType = replicateType;
    }
}
