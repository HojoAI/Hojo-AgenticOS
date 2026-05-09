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
package com.hojoai.gateway.omniasst.voice.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.io.IOException;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class AsrTransResponse {

    @JsonProperty("rtn")
    private int rtn;

    @JsonProperty("msg")
    private String msg;

    @JsonProperty("vid")
    private String vid;

    @JsonProperty("is_end")
    private int isEnd;

    @JsonProperty("lang_detect")
    private String langDetect;

    @JsonProperty("text")
    @JsonDeserialize(using = AsrTransTextDeserializer.class)
    private AsrTransText text;

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

    public String getVid() {
        return vid;
    }

    public void setVid(String vid) {
        this.vid = vid;
    }

    public int getIsEnd() { 
        return isEnd; 
    }
    
    public void setIsEnd(int isEnd) { 
        this.isEnd = isEnd; 
    }
    
    public String getLangDetect() {
        return langDetect;
    }
    
    public void setLangDetect(String langDetect) {
        this.langDetect = langDetect;
    }
    
    public AsrTransText getText() { 
        return text; 
    }
    
    public void setText(AsrTransText text) { 
        this.text = text; 
    }

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    public static class AsrTransText {
        @JsonProperty("fixed_text")
        private String fixedText;
        
        @JsonProperty("tmp_text")
        private String tmpText;
        
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

    static class AsrTransTextDeserializer extends JsonDeserializer<AsrTransText> {
        @Override
        public AsrTransText deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            JsonToken token = p.getCurrentToken();
            if (token == JsonToken.VALUE_STRING) {
                return new AsrTransText();
            } else if (token == JsonToken.START_OBJECT) {
                return p.readValueAs(AsrTransText.class);
            } else {
                return new AsrTransText();
            }
        }
    }
}

