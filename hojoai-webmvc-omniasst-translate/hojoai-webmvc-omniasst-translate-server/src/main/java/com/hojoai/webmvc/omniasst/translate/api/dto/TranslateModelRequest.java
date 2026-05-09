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
package com.hojoai.webmvc.omniasst.translate.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * NLU 文本翻译请求（带会话与上下文轮次）。
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TranslateModelRequest {
    @JsonProperty("sessionId")
    private String sessionId;
    @JsonProperty("text")
    private String text;
    @JsonProperty("lang")
    private String lang;
    @JsonProperty("style")
    private String style = "默认";
    @JsonProperty("context")
    private List<List<String>> context;

    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }
    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
    public String getLang() { return lang; }
    public void setLang(String lang) { this.lang = lang; }
    public String getStyle() { return style; }
    public void setStyle(String style) { this.style = style; }
    public List<List<String>> getContext() { return context; }
    public void setContext(List<List<String>> context) { this.context = context; }
}

