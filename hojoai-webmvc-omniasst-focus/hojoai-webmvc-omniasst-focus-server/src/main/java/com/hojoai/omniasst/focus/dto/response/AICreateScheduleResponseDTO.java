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
package com.hojoai.omniasst.focus.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.hojoai.omniasst.focus.dto.val.AIScheduleVal;

import java.util.List;

/**
 * AI创建日程响应DTO
 * 
 * @author hojoai
 * @date 2026-01-08
 */
public class AICreateScheduleResponseDTO {

    private String personality;
    /**
     * AI建议/简报摘要
     */
    @JsonProperty("Advice")
    private String advice;

    /**
     * 日程卡片列表
     */
    @JsonProperty("schedule_cards")
    private List<AIScheduleVal> scheduleCards;

    public AICreateScheduleResponseDTO() {
    }

    public String getPersonality() {
        return personality;
    }

    public void setPersonality(String personality) {
        this.personality = personality;
    }

    public String getAdvice() {
        return advice;
    }

    public void setAdvice(String advice) {
        this.advice = advice;
    }

    public List<AIScheduleVal> getScheduleCards() {
        return scheduleCards;
    }

    public void setScheduleCards(List<AIScheduleVal> scheduleCards) {
        this.scheduleCards = scheduleCards;
    }

    // Builder pattern
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String advice;
        private List<AIScheduleVal> scheduleCards;

        public Builder advice(String advice) {
            this.advice = advice;
            return this;
        }

        public Builder scheduleCards(List<AIScheduleVal> scheduleCards) {
            this.scheduleCards = scheduleCards;
            return this;
        }

        public AICreateScheduleResponseDTO build() {
            AICreateScheduleResponseDTO dto = new AICreateScheduleResponseDTO();
            dto.advice = this.advice;
            dto.scheduleCards = this.scheduleCards;
            return dto;
        }
    }
}

