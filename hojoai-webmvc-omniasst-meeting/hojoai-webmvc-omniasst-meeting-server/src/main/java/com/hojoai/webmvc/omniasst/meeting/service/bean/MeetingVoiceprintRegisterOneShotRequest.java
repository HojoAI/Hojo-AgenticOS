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

/**
 * 个人声纹注册单次提交：一次请求内完成原 prepare + commit，字段与两阶段合并等价。
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MeetingVoiceprintRegisterOneShotRequest {

    private String userId;
    private String audioBase64;
    private String nickname;
    /** 兼容旧参数命名：等价于 nickname */
    private String voiceName;
    /**
     * 转发 management 音色 clone/save；未传时默认 {@code hojo}。
     */
    private String provider;
    /** 新增兼容字段：0=VOICE（声音复刻），1=VOICEPRINT（声纹复刻） */
    private Integer type;
    /** 可选，与分阶段接口一致：不传或 VOICEPRINT 表示声纹复刻 */
    private String replicateType;

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

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public String getReplicateType() {
        return replicateType;
    }

    public void setReplicateType(String replicateType) {
        this.replicateType = replicateType;
    }
}
