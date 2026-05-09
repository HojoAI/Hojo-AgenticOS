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
package com.hojoai.omniasst.management.common;

import com.hojoai.common.response.ResponseStatus;

/**
 * The common enum for the extra inner response status.
 *
 * @author kevanwong
 * @version 1.0, 2020/05/12
 * @since hojoai-gateway-omniasst 1.0.0
 */
public enum ResponseStatusExtra implements ResponseStatus {

    CONVERSATION_NOT_FOUND("CONVERSATION_NOT_FOUND", "对话不存在"),
    MESSAGE_ID_NOT_FOUND("MESSAGE_ID_NOT_FOUND", "消息ID不存在"),
    MESSAGE_NOT_FOUND("MESSAGE_NOT_FOUND", "消息不存在"),
    TASK_NOT_FOUND("TASK_NOT_FOUND", "任务不存在");

    private String resCode;
    private String resMessage;

    private ResponseStatusExtra(String resCode, String resMessage) {
        this.resCode = resCode;
        this.resMessage = resMessage;
    }

    public String getResCode() {
        return this.resCode;
    }

    public void setResCode(String resCode) {
        this.resCode = resCode;
    }

    public String getResMessage() {
        return this.resMessage;
    }

    public void setResMessage(String resMessage) {
        this.resMessage = resMessage;
    }
}

