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
package com.hojoai.omniasst.sysagent.service.speech.bean;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 文本转语音（TTS）请求体。
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TTSRequest {

    /** 待合成的纯文本内容。 */
    @JsonProperty("text")
    private String text;

    /** 合成语种（如 zh、en）。 */
    @JsonProperty("lang")
    private String lang;

    /** 音色 ID，由 TTS 服务侧定义。 */
    @JsonProperty("voice_id")
    private String voiceId;

    /** 客户端请求 ID，用于链路追踪与幂等关联。 */
    @JsonProperty("req_id")
    private String reqId;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getLang() {
        return lang;
    }

    public void setLang(String lang) {
        this.lang = lang;
    }

    public String getVoiceId() {
        return voiceId;
    }

    public void setVoiceId(String voiceId) {
        this.voiceId = voiceId;
    }

    public String getReqId() {
        return reqId;
    }

    public void setReqId(String reqId) {
        this.reqId = reqId;
    }
}
