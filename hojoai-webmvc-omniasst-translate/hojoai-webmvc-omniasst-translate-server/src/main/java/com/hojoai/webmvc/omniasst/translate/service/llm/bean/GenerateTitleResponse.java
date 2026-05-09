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
 * 标题摘要模型响应包装。
 */
public class GenerateTitleResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 标题与摘要正文 */
    @JsonProperty("data")
    private TitleAndAbstract data;

    public GenerateTitleResponse() {
    }

    public TitleAndAbstract getData() {
        return data;
    }

    public void setData(TitleAndAbstract data) {
        this.data = data;
    }
}

