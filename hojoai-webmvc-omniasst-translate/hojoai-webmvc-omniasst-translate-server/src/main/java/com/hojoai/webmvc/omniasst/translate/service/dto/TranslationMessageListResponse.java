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
package com.hojoai.webmvc.omniasst.translate.service.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.io.Serializable;

/**
 * 单条翻译消息在列表接口中的展示 DTO。
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TranslationMessageListResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 消息业务 ID */
    private String translationMessageId;
    /** 所属会话 ID */
    private String translationConversationId;
    /** 源语言 */
    private String sourceLanguage;
    /** 目标语言 */
    private String targetLanguage;
    /** 原文/识别文本 */
    private String originalText;
    /** 译文 */
    private String translatedText;
    /** 检测语种 */
    private String detectedLanguage;
    /** 消息业务时间戳（毫秒） */
    private Long timestamp;
    /** 创建时间展示字符串 */
    private String createdAt;

    public String getTranslationMessageId() {
        return translationMessageId;
    }

    public void setTranslationMessageId(String translationMessageId) {
        this.translationMessageId = translationMessageId;
    }

    public String getTranslationConversationId() {
        return translationConversationId;
    }

    public void setTranslationConversationId(String translationConversationId) {
        this.translationConversationId = translationConversationId;
    }

    public String getSourceLanguage() {
        return sourceLanguage;
    }

    public void setSourceLanguage(String sourceLanguage) {
        this.sourceLanguage = sourceLanguage;
    }

    public String getTargetLanguage() {
        return targetLanguage;
    }

    public void setTargetLanguage(String targetLanguage) {
        this.targetLanguage = targetLanguage;
    }

    public String getOriginalText() {
        return originalText;
    }

    public void setOriginalText(String originalText) {
        this.originalText = originalText;
    }

    public String getTranslatedText() {
        return translatedText;
    }

    public void setTranslatedText(String translatedText) {
        this.translatedText = translatedText;
    }

    public String getDetectedLanguage() {
        return detectedLanguage;
    }

    public void setDetectedLanguage(String detectedLanguage) {
        this.detectedLanguage = detectedLanguage;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}

