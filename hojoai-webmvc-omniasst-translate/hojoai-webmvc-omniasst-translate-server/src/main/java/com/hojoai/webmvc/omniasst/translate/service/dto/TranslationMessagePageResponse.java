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
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.hojoai.common.page.IPage;

import java.io.Serializable;

/**
 * The response bean for translation message page with conversation info.
 * Uses composition to include IPage and adds conversation information.
 *
 * @author zhangxuepeng
 * @version 1.0, 2025/12/29
 * @since hojoai-webmvc-omniasst-translate 1.0.0
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TranslationMessagePageResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 会话标题 */
    private String title;
    /** 会话摘要 */
    private String summary;
    /** 会话开始时间（毫秒时间戳） */
    private Long startTime;
    /** 会话结束时间（毫秒时间戳） */
    private Long endTime;
    /** 会话持续时长（毫秒） */
    private Long duration;
    /** 是否进行中 */
    private Integer isActive;
    /** 源语言 */
    private String sourceLanguage;
    /** 目标语言 */
    private String targetLanguage;

    /**
     * 分页消息列表；使用 {@link com.fasterxml.jackson.annotation.JsonUnwrapped} 将 IPage 字段展平到同一 JSON 层级。
     */
    @JsonUnwrapped
    private IPage<TranslationMessageListResponse> page;

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

    public IPage<TranslationMessageListResponse> getPage() {
        return page;
    }

    public void setPage(IPage<TranslationMessageListResponse> page) {
        this.page = page;
    }
}

