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

import org.apache.commons.lang3.StringUtils;

/**
 * The ManualUpdateScheduleRequestDTO class for ManualUpdateScheduleRequestDTO.
 *
 * @author My
 * @version 1.0, 2026/03/13
 * @since hojoai-webmvc-omniasst-focus 1.0.0
 */
public class ManualModifyScheduleRequestDTO extends ManualScheduleRequestDTO {

    private String userId;

    private String scheduleRuleId;

    public boolean isInvalid() {
        if (StringUtils.isBlank(userId)) {
            return true;
        }
        if (StringUtils.isBlank(scheduleRuleId)) {
            return true;
        }

        return super.isInvalid();
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
