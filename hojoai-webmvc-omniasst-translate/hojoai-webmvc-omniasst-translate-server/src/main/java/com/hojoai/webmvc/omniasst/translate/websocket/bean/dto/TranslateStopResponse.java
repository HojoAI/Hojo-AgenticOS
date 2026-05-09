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
 * 停止翻译或一句翻译完成时的确认信息。
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TranslateStopResponse {
    /** 状态，如 success / complete */
    private String status;
    /** 会话 chatId */
    private String chatId;
    /** 人类可读说明 */
    private String message;

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getChatId() { return chatId; }
    public void setChatId(String chatId) { this.chatId = chatId; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}

