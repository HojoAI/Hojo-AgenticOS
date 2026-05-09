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
 * 客户端发起的 ASR+翻译配置（语言对及扩展属性）。
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AsrTranslateConfigRequest {
    /** 请求类型，业务自定义 */
    private String type;
    /** 是否启用翻译 */
    private Boolean enableTranslate;
    /** 源语言代码 */
    private String sourceLanguage;
    /** 目标语言代码 */
    private String targetLanguage;
    /** 扩展属性 */
    private ExtendAttr extendAttr;
    /** ASR 优先识别语言，可选 */
    private String asrLanguage;

    /** 扩展字段：关联业务会话 ID 等 */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ExtendAttr {
        /** 业务侧会话 ID */
        private String conversationId;
        public String getConversationId() { return conversationId; }
        public void setConversationId(String conversationId) { this.conversationId = conversationId; }
    }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public Boolean getEnableTranslate() { return enableTranslate; }
    public void setEnableTranslate(Boolean enableTranslate) { this.enableTranslate = enableTranslate; }
    public String getSourceLanguage() { return sourceLanguage; }
    public void setSourceLanguage(String sourceLanguage) { this.sourceLanguage = sourceLanguage; }
    public String getTargetLanguage() { return targetLanguage; }
    public void setTargetLanguage(String targetLanguage) { this.targetLanguage = targetLanguage; }
    public String getAsrLanguage() { return asrLanguage; }
    public void setAsrLanguage(String asrLanguage) { this.asrLanguage = asrLanguage; }
    public ExtendAttr getExtendAttr() { return extendAttr; }
    public void setExtendAttr(ExtendAttr extendAttr) { this.extendAttr = extendAttr; }
}

