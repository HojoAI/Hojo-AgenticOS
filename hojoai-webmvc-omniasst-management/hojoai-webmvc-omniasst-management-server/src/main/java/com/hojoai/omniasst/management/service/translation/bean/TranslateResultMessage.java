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
package com.hojoai.omniasst.management.service.translation.bean;

import java.io.Serializable;

/**
 * The DTO for translate result message from Redis channel.
 *
 * @author zhangxuepeng
 * @version 1.0, 2025/12/16
 * @since hojoai-webmvc-omniasst-management 1.0.0
 */
public class TranslateResultMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * The chat id of a translation session.
     */
    private String chatId;

    /**
     * The original text recognized from speech.
     */
    private String originalText;

    /**
     * The translated text.
     */
    private String translatedText;

    /**
     * The timestamp of this message (millisecond).
     */
    private Long timestamp;

    /**
     * The configured source language.
     */
    private String sourceLanguage;

    /**
     * The configured target language.
     */
    private String targetLanguage;

    /**
     * The detected language of current text.
     */
    private String detectedLanguage;

    /**
     * The user id.
     */
    private String userId;

    /**
     * Whether this message is the end of translation (0: not end, 1: end).
     */
    private Integer isEnd;

    /**
     * The start time of the translation session (millisecond timestamp).
     * Only present when isEnd = 1.
     */
    private Long startTime;

    /**
     * The end time of the translation session (millisecond timestamp).
     * Only present when isEnd = 1.
     */
    private Long endTime;

    public TranslateResultMessage() {
    }

    public String getChatId() {
        return chatId;
    }

    public void setChatId(String chatId) {
        this.chatId = chatId;
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

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
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

    public String getDetectedLanguage() {
        return detectedLanguage;
    }

    public void setDetectedLanguage(String detectedLanguage) {
        this.detectedLanguage = detectedLanguage;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Integer getIsEnd() {
        return isEnd;
    }

    public void setIsEnd(Integer isEnd) {
        this.isEnd = isEnd;
    }

    public Long getStartTime() {
        return startTime;
    }

    public void setStartTime(Long startTime) {
        this.startTime = startTime;
    }

    public Long getEndTime() {
        return endTime;
    }

    public void setEndTime(Long endTime) {
        this.endTime = endTime;
    }
}
