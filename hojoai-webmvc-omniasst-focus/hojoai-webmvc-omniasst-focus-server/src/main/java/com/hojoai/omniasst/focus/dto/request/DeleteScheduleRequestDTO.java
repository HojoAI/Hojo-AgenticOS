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
package com.hojoai.omniasst.focus.dto.request;

/**
 * 删除日程请求 DTO（330 新增）
 * <p>
 * deleteType=1 时删除当前日程；deleteType=2 时删除当前规则下所有日程，需传 scheduleRuleId。
 * </p>
 *
 * @author My
 * @version 1.0, 2026/03/11
 * @since hojoai-webmvc-omniasst-focus 1.0.0
 */
public class DeleteScheduleRequestDTO {

    /** 用户ID */
    private String userId;

    /** 日程规则ID（deleteType=2 时必填，查询时返回） */
    private String scheduleRuleId;

    public DeleteScheduleRequestDTO() {
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getScheduleRuleId() {
        return scheduleRuleId;
    }

    public void setScheduleRuleId(String scheduleRuleId) {
        this.scheduleRuleId = scheduleRuleId;
    }
}
