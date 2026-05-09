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
 * 重复信息
 *
 * @author My
 * @version 1.0
 * @since hojoai-webmvc-omniasst.focus 1.0.0
 */
public class RecurrenceInfo {

    /** 频率 */
    private Integer frequency;

    /** 重复间隔 */
    private Integer interval;

    /** 星期x */
    private List<Integer> daysOfWeek;

    /** 终止信息 */
    private EndConditionInfo endCondition;

    public RecurrenceInfo() {
    }

    public Integer getFrequency() {
        return frequency;
    }

    public void setFrequency(Integer frequency) {
        this.frequency = frequency;
    }

    public Integer getInterval() {
        return interval;
    }

    public void setInterval(Integer interval) {
        this.interval = interval;
    }

    public List<Integer> getDaysOfWeek() {
        return daysOfWeek;
    }

    public void setDaysOfWeek(List<Integer> daysOfWeek) {
        this.daysOfWeek = daysOfWeek;
    }

    public EndConditionInfo getEndCondition() {
        return endCondition;
    }

    public void setEndCondition(EndConditionInfo endCondition) {
        this.endCondition = endCondition;
    }
}
