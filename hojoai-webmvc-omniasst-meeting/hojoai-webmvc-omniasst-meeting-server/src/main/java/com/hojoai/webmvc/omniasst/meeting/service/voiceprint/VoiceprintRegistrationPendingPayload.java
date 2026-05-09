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
package com.hojoai.webmvc.omniasst.meeting.service.voiceprint;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * 阶段一写入 Redis 的暂存数据；阶段二提交前不写入用户正式声纹库。
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class VoiceprintRegistrationPendingPayload {

    private String userId;
    private double[] voiceprint;
    /** 与 prepare 请求一致的 Base64（无 data: 前缀），commit 时转发 management multipart 使用 */
    private String audioBase64;
    /** management 阶段一返回的临时克隆 ID，可能为空 */
    private String cloneVoiceId;
    /**
     * 会议侧在 prepare 调用 management {@code /v0/voice/clone} 得到的音色 ID（multipart 时 needSave 由请求决定）；
     * commit 时用昵称执行 {@code /v0/voice/save}。
     */
    private String timbreVoiceId;
    /** 与 timbreVoiceId 配套的 provider，默认 hojo */
    private String timbreProvider;
    /**
     * 为 true 时表示用户选择「复用已有声纹」：commit 不再调用 {@code /v0/voiceprint/register}，仅按需 {@code /v0/voice/save} 更新昵称。
     */
    private Boolean reuseExisting;
    /**
     * multipart prepare {@code type=0}（声音，默认）：阶段一仅克隆、不写声纹向量；commit 仅 {@code /v0/voice/save} 昵称，不调声纹注册。
     */
    private Boolean voiceOnly;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public double[] getVoiceprint() {
        return voiceprint;
    }

    public void setVoiceprint(double[] voiceprint) {
        this.voiceprint = voiceprint;
    }

    public String getAudioBase64() {
        return audioBase64;
    }

    public void setAudioBase64(String audioBase64) {
        this.audioBase64 = audioBase64;
    }

    public String getCloneVoiceId() {
        return cloneVoiceId;
    }

    public void setCloneVoiceId(String cloneVoiceId) {
        this.cloneVoiceId = cloneVoiceId;
    }

    public String getTimbreVoiceId() {
        return timbreVoiceId;
    }

    public void setTimbreVoiceId(String timbreVoiceId) {
        this.timbreVoiceId = timbreVoiceId;
    }

    public String getTimbreProvider() {
        return timbreProvider;
    }

    public void setTimbreProvider(String timbreProvider) {
        this.timbreProvider = timbreProvider;
    }

    public Boolean getReuseExisting() {
        return reuseExisting;
    }

    public void setReuseExisting(Boolean reuseExisting) {
        this.reuseExisting = reuseExisting;
    }

    public Boolean getVoiceOnly() {
        return voiceOnly;
    }

    public void setVoiceOnly(Boolean voiceOnly) {
        this.voiceOnly = voiceOnly;
    }
}
