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

/**
 * 单轮对话在 LLM 请求中的表示：说话人角色与双语文本。
 */
public class TranslationContext implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 角色标识，如 me / them */
    @JsonProperty("role")
    private String role;

    /** 源侧文本 */
    @JsonProperty("sourceText")
    private String sourceText;

    /** 目标侧文本 */
    @JsonProperty("targetText")
    private String targetText;

    public TranslationContext() {
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getSourceText() {
        return sourceText;
    }

    public void setSourceText(String sourceText) {
        this.sourceText = sourceText;
    }

    public String getTargetText() {
        return targetText;
    }

    public void setTargetText(String targetText) {
        this.targetText = targetText;
    }
}

