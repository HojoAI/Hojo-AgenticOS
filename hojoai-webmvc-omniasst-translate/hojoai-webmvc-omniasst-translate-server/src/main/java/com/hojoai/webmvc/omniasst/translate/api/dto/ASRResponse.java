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
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;

/**
 * 实时 ASR 响应；text 字段支持字符串或对象两种 JSON 形态，由内部反序列化器兼容解析。
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class ASRResponse {
    @JsonProperty("rtn")
    private int rtn;
    @JsonProperty("msg")
    private String msg;
    @JsonProperty("is_end")
    private int isEnd;
    @JsonProperty("vid")
    private String vid;
    @JsonProperty("lang_detect")
    private String langDetect;
    @JsonProperty("text")
    @JsonDeserialize(using = ASRTextDeserializer.class)
    private ASRText text;

    public String getLangDetect() {
        return "<UNK>".equals(langDetect) || StringUtils.isBlank(langDetect) ? StringUtils.EMPTY : langDetect;
    }

    public void setLangDetect(String langDetect) { this.langDetect = langDetect; }
    public int getRtn() { return rtn; }
    public void setRtn(int rtn) { this.rtn = rtn; }
    public String getMsg() { return msg; }
    public void setMsg(String msg) { this.msg = msg; }
    public int getIsEnd() { return isEnd; }
    public void setIsEnd(int isEnd) { this.isEnd = isEnd; }
    public String getVid() { return vid; }
    public void setVid(String vid) { this.vid = vid; }
    public ASRText getText() { return text; }
    public void setText(ASRText text) { this.text = text; }

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    public static class ASRText {
        @JsonProperty("fixed_text")
        private String fixedText;
        @JsonProperty("tmpText")
        private String tmpText;
        @JsonProperty("text")
        private String text;

        public String getFixedText() { return fixedText; }
        public void setFixedText(String fixedText) { this.fixedText = fixedText; }
        public String getTmpText() { return tmpText; }
        public void setTmpText(String tmpText) { this.tmpText = tmpText; }
        public String getText() { return text; }
        public void setText(String text) { this.text = text; }
    }

    static class ASRTextDeserializer extends JsonDeserializer<ASRText> {
        @Override
        public ASRResponse.ASRText deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            JsonToken token = p.getCurrentToken();
            if (token == JsonToken.VALUE_STRING) {
                return new ASRResponse.ASRText();
            } else if (token == JsonToken.START_OBJECT) {
                return p.readValueAs(ASRResponse.ASRText.class);
            } else {
                return new ASRResponse.ASRText();
            }
        }
    }
}

