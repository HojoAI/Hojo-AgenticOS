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

@JsonInclude(JsonInclude.Include.NON_NULL)
public class TranslateModelResponse {

    @JsonProperty("msg")
    private String msg;
    @JsonProperty("status_code")
    private Integer statusCode;
    @JsonProperty("data")
    private TranslateResponseData translateResponseData;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class TranslateResponseData {

        @JsonProperty("translation")
        private String translation;

        public String getTranslation() {
            return translation;
        }

        public void setTranslation(String translation) {
            this.translation = translation;
        }
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public Integer getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(Integer statusCode) {
        this.statusCode = statusCode;
    }

    public TranslateResponseData getTranslateResponseData() {
        return translateResponseData;
    }

    public void setTranslateResponseData(TranslateResponseData translateResponseData) {
        this.translateResponseData = translateResponseData;
    }
}

