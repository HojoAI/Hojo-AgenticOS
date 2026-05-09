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
 * 下发给客户端的 ASR 与译文及句末标记。
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AsrTextWithTranslateResponse {
    /** 当前识别侧展示的文本（随检测语种映射） */
    private String text;
    /** 译文 */
    private String tranText;
    /** 当前检测语种 */
    private String currLang;
    /** 是否句末：0 否，1 是 */
    private Integer isEnd;

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
    public String getTranText() { return tranText; }
    public void setTranText(String tranText) { this.tranText = tranText; }
    public String getCurrLang() { return currLang; }
    public void setCurrLang(String currLang) { this.currLang = currLang; }
    public Integer getIsEnd() { return isEnd; }
    public void setIsEnd(Integer isEnd) { this.isEnd = isEnd; }
}

