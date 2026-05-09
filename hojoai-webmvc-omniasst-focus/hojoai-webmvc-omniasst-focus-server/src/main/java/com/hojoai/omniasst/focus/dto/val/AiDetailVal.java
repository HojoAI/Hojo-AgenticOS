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
package com.hojoai.omniasst.focus.dto.val;

import java.util.List;

/**
 * AI Detail Value DTO
 * Contains AI analysis information
 *
 * @author My
 * @version 1.0 2026/01/06
 * @since hojoai-webmvc-omniasst.focus 1.0.0
 */
public class AiDetailVal {

    /**
     * AI类型
     * "AI任务分析" | "AI会前准备" | "AI训练状态评估"
     */
    private String aiType;

    /**
     * 标题
     */
    private String title;

    /**
     * 重点建议 (markdown)
     */
    private List<String> highLightList;

    public AiDetailVal() {
    }

    public String getAiType() {
        return aiType;
    }

    public void setAiType(String aiType) {
        this.aiType = aiType;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<String> getHighLightList() {
        return highLightList;
    }

    public void setHighLightList(List<String> highLightList) {
        this.highLightList = highLightList;
    }
}

