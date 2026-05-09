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
package com.hojoai.omniasst.sysagent.service.speech.bean;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 流式 ASR 响应体。
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ASRResponse {

    /** 返回码，0 表示成功，非 0 表示错误或异常。 */
    @JsonProperty("rtn")
    private int rtn;

    /** 错误或提示信息。 */
    @JsonProperty("msg")
    private String msg;

    /** 是否整路识别结束：非 0 表示已结束。 */
    @JsonProperty("is_end")
    private int isEnd;

    /** 当前分片对应的文本结构（稳定稿、临时稿等）。 */
    @JsonProperty("text")
    private ASRText text;

    public int getRtn() {
        return rtn;
    }

    public void setRtn(int rtn) {
        this.rtn = rtn;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public int getIsEnd() {
        return isEnd;
    }

    public void setIsEnd(int isEnd) {
        this.isEnd = isEnd;
    }

    public ASRText getText() {
        return text;
    }

    public void setText(ASRText text) {
        this.text = text;
    }

    /**
     * 单次回调中的识别文本形态（稳定句、临时句、合并文本）。
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ASRText {

        /** 已固定的识别文本（通常可上屏的最终分句）。 */
        @JsonProperty("fixed_text")
        private String fixedText;
        /** 临时识别结果，可能随后续分片变化。 */
        @JsonProperty("tmpText")
        private String tmpText;
        /** 兼容字段：完整或当前最佳文本。 */
        @JsonProperty("text")
        private String text;

        public String getFixedText() {
            return fixedText;
        }

        public void setFixedText(String fixedText) {
            this.fixedText = fixedText;
        }

        public String getTmpText() {
            return tmpText;
        }

        public void setTmpText(String tmpText) {
            this.tmpText = tmpText;
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }
    }
}
