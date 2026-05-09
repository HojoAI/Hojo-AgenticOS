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
package com.hojoai.webmvc.omniasst.meeting.controller;

import com.hojoai.common.response.Response;
import com.hojoai.webmvc.omniasst.meeting.service.bean.MeetingVoiceprintUiException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 将 {@link MeetingVoiceprintUiException} 转为 {@link Response}：分支码写在 {@code resMessage}（与前端约定一致）。
 */
@RestControllerAdvice
@Order(0)
public class MeetingVoiceprintRegistrationExceptionHandler {

    /**
     * 将声纹注册 UI 异常包装为 HTTP 200 + 业务失败体（{@code resMessage} 为约定码）。
     *
     * @param ex 携带 uiCode 与说明
     * @return {@link Response}，HTTP 状态仍为 200 以便前端统一解析
     */
    @ExceptionHandler(MeetingVoiceprintUiException.class)
    public ResponseEntity<Response<Object>> handleMeetingVoiceprintUi(MeetingVoiceprintUiException ex) {
        String detail = StringUtils.trimToNull(ex.getMessage());
        if (detail == null) {
            detail = StringUtils.trimToNull(ex.getLocalizedMessage());
        }
        Response<Object> body = VoiceprintRegisterResponseSupport.failWithUiCode(ex.getUiCode(), detail);
        return ResponseEntity.status(HttpStatus.OK).body(body);
    }
}
