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

/**
 * AI对话响应DTO
 * 
 * @author hojoai
 * @date 2026-01-08
 */
public class AIChatResponseDTO {

    /**
     * AI回复内容
     */
    @JsonProperty("content")
    private String content;

    /**
     * 模型名称
     */
    @JsonProperty("model")
    private String model;

    /**
     * 是否为流式响应
     */
    @JsonProperty("stream")
    private Boolean stream;

    /**
     * 生成的token数量(如果API返回)
     */
    @JsonProperty("tokensUsed")
    private Integer tokensUsed;

    /**
     * 响应时间(毫秒)
     */
    @JsonProperty("responseTime")
    private Long responseTime;

    public AIChatResponseDTO() {
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public Boolean getStream() {
        return stream;
    }

    public void setStream(Boolean stream) {
        this.stream = stream;
    }

    public Integer getTokensUsed() {
        return tokensUsed;
    }

    public void setTokensUsed(Integer tokensUsed) {
        this.tokensUsed = tokensUsed;
    }

    public Long getResponseTime() {
        return responseTime;
    }

    public void setResponseTime(Long responseTime) {
        this.responseTime = responseTime;
    }

    // Builder pattern
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String content;
        private String model;
        private Boolean stream;
        private Integer tokensUsed;
        private Long responseTime;

        public Builder content(String content) {
            this.content = content;
            return this;
        }

        public Builder model(String model) {
            this.model = model;
            return this;
        }

        public Builder stream(Boolean stream) {
            this.stream = stream;
            return this;
        }

        public Builder tokensUsed(Integer tokensUsed) {
            this.tokensUsed = tokensUsed;
            return this;
        }

        public Builder responseTime(Long responseTime) {
            this.responseTime = responseTime;
            return this;
        }

        public AIChatResponseDTO build() {
            AIChatResponseDTO dto = new AIChatResponseDTO();
            dto.content = this.content;
            dto.model = this.model;
            dto.stream = this.stream;
            dto.tokensUsed = this.tokensUsed;
            dto.responseTime = this.responseTime;
            return dto;
        }
    }
}

