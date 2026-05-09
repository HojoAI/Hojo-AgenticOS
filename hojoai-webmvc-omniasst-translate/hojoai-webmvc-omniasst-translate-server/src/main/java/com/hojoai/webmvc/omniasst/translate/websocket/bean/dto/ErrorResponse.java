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

/**
 * 统一错误载荷：错误码、说明与发生阶段（如 config / voice_service / process）。
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {
    /** 错误码 */
    private String errorCode;
    /** 错误描述 */
    private String errorMessage;
    /** 阶段标识，便于端侧定位 */
    private String stage;

    public String getErrorCode() { return errorCode; }
    public void setErrorCode(String errorCode) { this.errorCode = errorCode; }
    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    public String getStage() { return stage; }
    public void setStage(String stage) { this.stage = stage; }
}

