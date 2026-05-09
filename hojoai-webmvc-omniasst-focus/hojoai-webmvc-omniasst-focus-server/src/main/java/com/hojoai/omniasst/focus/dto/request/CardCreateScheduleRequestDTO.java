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
package com.hojoai.omniasst.focus.dto.request;

import java.util.List;

/**
 * Card Create Schedule Request DTO
 *
 * @author My
 * @version 1.0 2026/01/07
 * @since hojoai-webmvc-omniasst.focus 1.0.0
 */
public class CardCreateScheduleRequestDTO {

    private String conversationId;

    private String messageId;

    private String contentId;

    private List<String> scheduleIdList;

    public CardCreateScheduleRequestDTO() {
    }

    public String getConversationId() {
        return conversationId;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getContentId() {
        return contentId;
    }

    public void setContentId(String contentId) {
        this.contentId = contentId;
    }

    public List<String> getScheduleIdList() {
        return scheduleIdList;
    }

    public void setScheduleIdList(List<String> scheduleIdList) {
        this.scheduleIdList = scheduleIdList;
    }
}

