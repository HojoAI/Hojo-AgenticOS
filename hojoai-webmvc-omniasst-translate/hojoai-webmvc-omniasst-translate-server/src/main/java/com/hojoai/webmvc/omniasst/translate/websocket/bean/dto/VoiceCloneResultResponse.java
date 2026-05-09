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
package com.hojoai.webmvc.omniasst.translate.websocket.bean.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 异步音色克隆完成后的结果：成功时返回 voice_id，失败时返回错误码与信息。
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class VoiceCloneResultResponse {
    /** 是否成功 */
    private Boolean success;
    /** 克隆得到的音色 ID */
    @JsonProperty("voice_id")
    private String voiceId;
    /** 失败时的错误码 */
    private String errorCode;
    /** 失败时的错误描述 */
    private String errorMessage;

    public Boolean getSuccess() { return success; }
    public void setSuccess(Boolean success) { this.success = success; }
    public String getVoiceId() { return voiceId; }
    public void setVoiceId(String voiceId) { this.voiceId = voiceId; }
    public String getErrorCode() { return errorCode; }
    public void setErrorCode(String errorCode) { this.errorCode = errorCode; }
    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
}
