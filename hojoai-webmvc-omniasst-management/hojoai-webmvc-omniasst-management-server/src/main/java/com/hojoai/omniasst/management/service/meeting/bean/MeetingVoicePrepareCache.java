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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;

/**
 * prepare 阶段写入 Redis 的暂存数据，供 commit 落库音色 + 声纹。
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class MeetingVoicePrepareCache implements Serializable {

    private static final long serialVersionUID = 1L;

    private String userId;
    private String voiceId;
    private double[] voiceprint;
    private String provider;
    private Integer type;
    private Boolean needSave;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getVoiceId() {
        return voiceId;
    }

    public void setVoiceId(String voiceId) {
        this.voiceId = voiceId;
    }

    public double[] getVoiceprint() {
        return voiceprint;
    }

    public void setVoiceprint(double[] voiceprint) {
        this.voiceprint = voiceprint;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public Boolean getNeedSave() {
        return needSave;
    }

    public void setNeedSave(Boolean needSave) {
        this.needSave = needSave;
    }
}
