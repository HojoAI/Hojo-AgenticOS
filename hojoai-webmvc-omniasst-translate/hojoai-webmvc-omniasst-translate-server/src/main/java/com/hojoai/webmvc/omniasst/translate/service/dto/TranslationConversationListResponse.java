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
 * 会话列表中单条记录的展示 DTO。
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TranslationConversationListResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 会话业务 ID */
    private String translationConversationId;
    /** 用户 ID */
    private String userId;
    /** 标题 */
    private String title;
    /** 摘要 */
    private String summary;
    /** 源语言 */
    private String sourceLanguage;
    /** 目标语言 */
    private String targetLanguage;
    /** 开始时间戳（毫秒） */
    private Long startTime;
    /** 结束时间戳（毫秒） */
    private Long endTime;
    /** 持续时长（毫秒） */
    private Long duration;
    /** 是否进行中 */
    private Integer isActive;
    /** 创建时间展示字符串 */
    private String createdAt;
    /** 更新时间展示字符串 */
    private String updatedAt;

    public String getTranslationConversationId() {
        return translationConversationId;
    }

    public void setTranslationConversationId(String translationConversationId) {
        this.translationConversationId = translationConversationId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
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

    public Long getDuration() {
        return duration;
    }

    public void setDuration(Long duration) {
        this.duration = duration;
    }

    public Integer getIsActive() {
        return isActive;
    }

    public void setIsActive(Integer isActive) {
        this.isActive = isActive;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }
}

