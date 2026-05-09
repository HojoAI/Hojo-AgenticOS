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
 * HelloWorld 接口入参 DTO。JSON 序列化时忽略 null 字段。
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class HelloWorldRequest {

    /** 用户输入的问候/提问内容，由 Controller 校验非空 */
    private String asking;

    /**
     * @return 用户提问内容
     */
    public String getAsking() {
        return asking;
    }

    /**
     * @param asking 用户提问内容
     */
    public void setAsking(String asking) {
        this.asking = asking;
    }
}
