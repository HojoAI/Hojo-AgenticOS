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
 * 翻译会话持久化实体，对应表 translation_conversations；业务主键为 translationConversationId（与 chatId 一致）。
 */
@TableName(value = "translation_conversations")
public class TranslationConversation implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 自增主键 */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 业务会话 ID，与 WebSocket 侧 chatId 对应 */
    @TableField(value = "translation_conversation_id")
    private String translationConversationId;

    /** 所属用户 ID */
    @TableField(value = "user_id")
    private String userId;

    /** 源语言代码 */
    @TableField(value = "source_language")
    private String sourceLanguage;

    /** 目标语言代码 */
    @TableField(value = "target_language")
    private String targetLanguage;

    /** 会话开始时间戳（毫秒） */
    @TableField(value = "start_time")
    private Long startTime;

    /** 会话结束时间戳（毫秒） */
    @TableField(value = "end_time")
    private Long endTime;

    /** 会话标题（可由 LLM 生成） */
    @TableField(value = "title")
    private String title;

    /** 会话摘要（可由 LLM 生成） */
    @TableField(value = "summary")
    private String summary;

    /** 会话持续时长（毫秒） */
    @TableField(value = "duration")
    private Long duration;

    /** 是否进行中：1 活跃，0 已结束 */
    @TableField(value = "is_active")
    private Integer isActive;

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

