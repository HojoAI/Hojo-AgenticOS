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

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class MeetingVoiceprintRegisterPrepareRequest {

    private String userId;
    /** JSON 体录音：与旧字段 {@code audioBase64} 等价；另可用别名 {@code audio} */
    @JsonAlias({"audio"})
    private String audioBase64;
    /** 音色 clone 转发 management；未传默认 hojo */
    private String provider;
    /**
     * 仅 {@code /prepare} 使用，与「声音/声纹复刻」路由无关：{@code 0}=直接使用已有声纹（原复用流程）；{@code 1} 或省略=上传新录音。
     * 若需区分「走音色克隆接口」请仅用 {@link #replicateType}（{@code VOICE}/{@code VOICEPRINT}），勿用本字段代替。
     *  该参数用来控制是否覆盖已有声纹 =0 不覆盖   =1 覆盖
     */
    private Integer type;

    // 该参数用来控制是否覆盖已有音色
    /** 可选：VOICEPRINT=声纹复刻（默认，可不传）；若误传 VOICE 将提示改用声音复刻接口 */
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
