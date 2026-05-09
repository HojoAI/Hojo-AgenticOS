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
 * WebSocket 侧组装的翻译结果或连接生命周期事件，供持久化与推送消费。
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TranslateResultMessage {
    /** 本会话 chatId，与库表 translation_conversation_id 一致 */
    private String chatId;
    /** 用户 ID */
    private String userId;
    /** 原文或 ASR 文本 */
    private String originalText;
    /** 译文 */
    private String translatedText;
    /** 本条消息时间戳（毫秒） */
    private Long timestamp;
    /** 配置的源语言 */
    private String sourceLanguage;
    /** 配置的目标语言 */
    private String targetLanguage;
    /** 当前句识别语种 */
    private String detectedLanguage;
    /**
     * 是否结束：普通句结束时可为 0；连接结束或句末完整结束时为 1，用于区分落库与会话收尾逻辑。
     */
    private Integer isEnd;
    /** 连接建立时间（连接结束事件时填充） */
    private Long startTime;
    /** 连接关闭时间（连接结束事件时填充） */
    private Long endTime;

    public String getChatId() { return chatId; }
    public void setChatId(String chatId) { this.chatId = chatId; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getOriginalText() { return originalText; }
    public void setOriginalText(String originalText) { this.originalText = originalText; }
    public String getTranslatedText() { return translatedText; }
    public void setTranslatedText(String translatedText) { this.translatedText = translatedText; }
    public Long getTimestamp() { return timestamp; }
    public void setTimestamp(Long timestamp) { this.timestamp = timestamp; }
    public String getSourceLanguage() { return sourceLanguage; }
    public void setSourceLanguage(String sourceLanguage) { this.sourceLanguage = sourceLanguage; }
    public String getTargetLanguage() { return targetLanguage; }
    public void setTargetLanguage(String targetLanguage) { this.targetLanguage = targetLanguage; }
    public String getDetectedLanguage() { return detectedLanguage; }
    public void setDetectedLanguage(String detectedLanguage) { this.detectedLanguage = detectedLanguage; }
    public Integer getIsEnd() { return isEnd; }
    public void setIsEnd(Integer isEnd) { this.isEnd = isEnd; }
    public Long getStartTime() { return startTime; }
    public void setStartTime(Long startTime) { this.startTime = startTime; }
    public Long getEndTime() { return endTime; }
    public void setEndTime(Long endTime) { this.endTime = endTime; }

    @Override
    public String toString() {
        return "TranslateResultMessage{" +
                "chatId='" + chatId + '\'' +
                ", userId='" + userId + '\'' +
                ", originalText='" + originalText + '\'' +
                ", translatedText='" + translatedText + '\'' +
                ", timestamp=" + timestamp +
                ", sourceLanguage='" + sourceLanguage + '\'' +
                ", targetLanguage='" + targetLanguage + '\'' +
                ", detectedLanguage='" + detectedLanguage + '\'' +
                ", isEnd=" + isEnd +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                '}';
    }
}
