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
package com.hojoai.omniasst.focus.dto.response;

import java.util.List;

/**
 * 意图识别响应DTO
 * 用于封装AI识别用户意图后的结果
 *
 * @author hojoai
 */
public class IntentRecogResponseDTO {

    /**
     * 日期列表
     */
    private List<String> date;

    /**
     * 处理后的查询语句
     */
    private List<String> query;

    private Object relay;

    public IntentRecogResponseDTO() {
    }

    public IntentRecogResponseDTO(List<String> date, List<String> query) {
        this.date = date;
        this.query = query;
    }

    public List<String> getDate() {
        return date;
    }

    public void setDate(List<String> date) {
        this.date = date;
    }

    public List<String> getQuery() {
        return query;
    }

    public void setQuery(List<String> query) {
        this.query = query;
    }

    public Object getRelay() {
        return relay;
    }

    public void setRelay(Object relay) {
        this.relay = relay;
    }

}

