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

import com.hojoai.central.provider.service.utils.EnumUtil;
import com.hojoai.omniasst.focus.common.enums.TagEnum;
import com.hojoai.omniasst.focus.common.enums.TypeEnum;
import com.hojoai.omniasst.focus.repository.po.SchedulePO;

import java.time.format.DateTimeFormatter;

/**
 *
 * @author My
 * @version 1.0, 2026/01/08
 * @since hojoai-webmvc-omniasst-focus 1.0.0
 */
public class AIScheduleCommonVal extends AIScheduleVal {

    private Long id;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    /**
     * 将 SchedulePO 转换为 AIScheduleRequestDTO
     *
     * @param schedule 日程PO
     * @return AIScheduleRequestDTO
     */
    public static AIScheduleCommonVal convertSchedulePO2AIScheduleCommonVal(SchedulePO schedule) {
        AIScheduleCommonVal request = new AIScheduleCommonVal();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        request.setId(schedule.getId());
        request.setTitle(schedule.getTitle());
        request.setType(EnumUtil.getEnumByCode(schedule.getType(), TypeEnum.class).getValue());

        request.setTags(String.valueOf(EnumUtil.getValueByCode(schedule.getTag(), TagEnum.class)));

        if (schedule.getStartTime() != null) {
            request.setStartTime(schedule.getStartTime().format(formatter));
        }
        if (schedule.getEndTime() != null) {
            request.setEndTime(schedule.getEndTime().format(formatter));
        }

        request.setInfo(schedule.getAuxiliaryInfo());
        request.setIsRemind(schedule.getIsRemind());
        request.setIcon(schedule.getIcon());
        request.setAiSuggestion(schedule.getAiSuggestion());
        request.setIsAiSuggestion(schedule.getIsAiSuggestion());

        return request;
    }

}
