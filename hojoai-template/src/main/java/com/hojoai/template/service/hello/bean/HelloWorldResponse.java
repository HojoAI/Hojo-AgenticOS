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
package com.hojoai.template.service.hello.bean;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * HelloWorld 接口出参 DTO，承载服务层生成的回复文案。
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class HelloWorldResponse {

    /** 服务根据 asking 拼装的应答文本 */
    private String answer;

    /**
     * @return 应答内容
     */
    public String getAnswer() {
        return answer;
    }

    /**
     * @param answer 应答内容
     */
    public void setAnswer(String answer) {
        this.answer = answer;
    }
}
