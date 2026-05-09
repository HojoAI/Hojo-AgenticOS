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
package com.hojoai.webmvc.omniasst.translate.dao.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.util.Date;

/**
 * 单条翻译消息持久化实体，对应表 translation_messages。
 */
@TableName(value = "translation_messages")
public class TranslationMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 自增主键 */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 业务消息 ID（如 TM 前缀 + 雪花） */
    @TableField(value = "translation_message_id")
    private String translationMessageId;

    /** 所属会话 ID，对应 {@link TranslationConversation#getTranslationConversationId()} */
    @TableField(value = "translation_conversation_id")
    private String translationConversationId;

    /** 源语言代码 */
    @TableField(value = "source_language")
    private String sourceLanguage;

    /** 目标语言代码 */
    @TableField(value = "target_language")
    private String targetLanguage;

    /** 识别/原文内容（随检测语种映射在业务层解释） */
    @TableField(value = "original_text")
    private String originalText;

    /** 译文内容 */
    @TableField(value = "translated_text")
    private String translatedText;

    /** 当前句检测到的语种 */
    @TableField(value = "detected_language")
    private String detectedLanguage;

    /** 消息业务时间戳（毫秒），用于幂等 */
    @TableField(value = "timestamp")
    private Long timestamp;

    /** 记录创建时间 */
    @TableField(value = "created_at")
    private Date createdAt;

    /** 记录更新时间 */
    @TableField(value = "updated_at")
    private Date updatedAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }
}

