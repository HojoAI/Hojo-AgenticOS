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

/**
 * 个人声纹注册：携带与前端约定的 {@link VoiceprintRegisterUiCodes}，由全局处理器转为统一 {@code Response}。
 */
public class MeetingVoiceprintUiException extends RuntimeException {

    private final String uiCode;

    /**
     * @param uiCode 与前端约定的业务码（参见 {@link VoiceprintRegisterUiCodes}）
     * @param message 展示或日志用说明
     */
    public MeetingVoiceprintUiException(String uiCode, String message) {
        super(message);
        this.uiCode = uiCode;
    }

    /**
     * @param uiCode 与前端约定的业务码
     * @param message 展示或日志用说明
     * @param cause 底层原因（如 Feign 调用失败）
     */
    public MeetingVoiceprintUiException(String uiCode, String message, Throwable cause) {
        super(message, cause);
        this.uiCode = uiCode;
    }

    /**
     * @return 与前端约定的业务码
     */
    public String getUiCode() {
        return uiCode;
    }
}
