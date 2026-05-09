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
package com.hojoai.webmvc.omniasst.translate.service.llm.bean;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.List;

/**
 * 调用标题摘要模型的请求体。
 */
public class GenerateTitleRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 源语言 */
    @JsonProperty("sourceLanguage")
    private String sourceLanguage;

    /** 目标语言 */
    @JsonProperty("targetLanguage")
    private String targetLanguage;

    /** 多轮翻译上下文（角色 + 原文/译文） */
    @JsonProperty("context")
    private List<TranslationContext> context;

    public GenerateTitleRequest() {
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

    public List<TranslationContext> getContext() {
        return context;
    }

    public void setContext(List<TranslationContext> context) {
        this.context = context;
    }
}

