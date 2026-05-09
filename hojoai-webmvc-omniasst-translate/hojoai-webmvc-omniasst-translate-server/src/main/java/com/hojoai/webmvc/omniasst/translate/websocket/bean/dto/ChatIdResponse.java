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
 * 配置成功后下发给客户端的本连接翻译会话 ID（与持久化 translation_conversation_id 一致）。
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ChatIdResponse {
    /** 会话 chatId */
    private String chatId;
    public String getChatId() { return chatId; }
    public void setChatId(String chatId) { this.chatId = chatId; }
}

