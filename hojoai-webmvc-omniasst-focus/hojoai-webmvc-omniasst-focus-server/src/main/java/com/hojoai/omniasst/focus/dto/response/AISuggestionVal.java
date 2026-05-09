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

import com.hojoai.central.provider.service.utils.EnumUtil;
import com.hojoai.omniasst.focus.common.enums.TagEnum;
import com.hojoai.omniasst.focus.dto.val.AIScheduleCommonVal;
import com.hojoai.omniasst.focus.repository.po.SchedulePO;
import org.apache.commons.lang3.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

/**
 *
 * @author My
 * @version 1.0, 2026/01/11
 * @since hojoai-webmvc-omniasst-focus 1.0.0
 */
public class AISuggestionVal extends AIScheduleCommonVal {

    private String tagExtra;

    public String getTagExtra() {
        return tagExtra;
    }

    public void setTagExtra(String tagExtra) {
        this.tagExtra = tagExtra;
    }

    /**
     * 根据 AI 建议更新日程
     *
     * @param schedule              日程
     * @param aiSuggestionVal AI 建议
     */
    public static void updateSchedulesWithAISuggestions(SchedulePO schedule, AISuggestionVal aiSuggestionVal) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        // 更新标题
        if (aiSuggestionVal.getTitle() != null && !aiSuggestionVal.getTitle().equals(schedule.getTitle())) {
            schedule.setTitle(aiSuggestionVal.getTitle());
        }

        if (StringUtils.isNotBlank(aiSuggestionVal.getTags())) {
            schedule.setTag(Objects.requireNonNull(EnumUtil.getEnumByValue(aiSuggestionVal.getTags(), TagEnum.class)).getCode());
        }

        // 更新时间
        if (aiSuggestionVal.getStartTime() != null) {
            LocalDateTime newStartTime = LocalDateTime.parse(aiSuggestionVal.getStartTime(), formatter);
            if (!newStartTime.equals(schedule.getStartTime())) {
                schedule.setStartTime(newStartTime);
            }
        }

        if (aiSuggestionVal.getEndTime() != null) {
            LocalDateTime newEndTime = LocalDateTime.parse(aiSuggestionVal.getEndTime(), formatter);
            if (!newEndTime.equals(schedule.getEndTime())) {
                schedule.setEndTime(newEndTime);
            }
        }

        // 更新辅助信息
        if (aiSuggestionVal.getInfo() != null && !aiSuggestionVal.getInfo().equals(schedule.getAuxiliaryInfo())) {
            schedule.setAuxiliaryInfo(aiSuggestionVal.getInfo());
        }

        // 更新图标
        if (aiSuggestionVal.getIcon() != null && !aiSuggestionVal.getIcon().equals(schedule.getIcon())) {
            schedule.setIcon(aiSuggestionVal.getIcon());
        }

        // 更新 AI 建议
        if (aiSuggestionVal.getAiSuggestion() != null && !aiSuggestionVal.getAiSuggestion().equals(schedule.getAiSuggestion())) {
            schedule.setAiSuggestion(aiSuggestionVal.getAiSuggestion());
        }
    }

}
