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
 * 复制已有音色（复用声纹注册）：与 JSON {@code prepare} 在 {@code type=0}（复用、无音频）时逻辑一致。
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MeetingVoiceprintCopyVoiceRequest {

    /** 当前用户 ID，与录音结束 prepare 一致 */
    private String userId;
    /** 厂商，默认 hojo；与 prepare 的 {@code provider} 一致 */
    private String provider;
    /** 可选：{@code VOICEPRINT}（默认，可省略）；显式 {@code VOICE} 将拒绝并提示走声音复刻接口，与同模块 prepare 一致 */
    private String replicateType;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getReplicateType() {
        return replicateType;
    }

    public void setReplicateType(String replicateType) {
        this.replicateType = replicateType;
    }
}
