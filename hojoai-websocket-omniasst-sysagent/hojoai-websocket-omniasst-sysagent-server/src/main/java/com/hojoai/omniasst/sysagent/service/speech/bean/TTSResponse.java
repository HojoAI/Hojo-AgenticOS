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
 * TTS 侧返回的结构化响应（与部分 ASR 字段命名复用，属历史协议兼容）。
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TTSResponse {

    /** 返回码。 */
    @JsonProperty("rtn")
    private int rtn;

    /** 提示或错误信息。 */
    @JsonProperty("msg")
    private String msg;

    /** 是否流/会话结束标记。 */
    @JsonProperty("is_end")
    private int isEnd;

    /** 文本类附属信息（若协议返回）。 */
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
     * 与 TTS 响应共用的文本块结构（命名沿用了 ASR 侧字段）。
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ASRText {

        /** 稳定文本片段。 */
        @JsonProperty("fixed_text")
        private String fixedText;
        /** 临时文本片段。 */
        @JsonProperty("tmpText")
        private String tmpText;
        /** 通用文本字段。 */
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
