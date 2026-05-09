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
package com.hojoai.omniasst.management.service.meeting.bean;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.List;

/**
 * BEX advice (contains deductions and credits).
 *
 * @author zhangxuepeng
 * @version 1.0, 2025/12/25
 * @since hojoai-webmvc-omniasst-management 1.0.0
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BexAdvice implements Serializable {

    private static final long serialVersionUID = 1L;

    @JsonProperty("deductions")
    private List<BexAdviceItem> deductions;

    @JsonProperty("credits")
    private List<BexAdviceItem> credits;

    public BexAdvice() {
    }

    public List<BexAdviceItem> getDeductions() {
        return deductions;
    }

    public void setDeductions(List<BexAdviceItem> deductions) {
        this.deductions = deductions;
    }

    public List<BexAdviceItem> getCredits() {
        return credits;
    }

    public void setCredits(List<BexAdviceItem> credits) {
        this.credits = credits;
    }
}

