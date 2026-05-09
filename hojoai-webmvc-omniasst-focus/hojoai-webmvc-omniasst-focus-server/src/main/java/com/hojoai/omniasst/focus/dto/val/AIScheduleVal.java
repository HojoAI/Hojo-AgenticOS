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
import com.hojoai.omniasst.focus.common.enums.PriorityEnum;
import com.hojoai.omniasst.focus.common.enums.ScheduleStatusEnum;
import com.hojoai.omniasst.focus.common.enums.TagEnum;
import com.hojoai.omniasst.focus.common.enums.TypeEnum;
import com.hojoai.omniasst.focus.repository.po.SchedulePO;
import org.apache.commons.lang3.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

/**
 * 日程卡片值对象
 * 
 * @author hojoai
 * @date 2026-01-08
 */
public class AIScheduleVal {

    /**
     * 事项标题
     */
    private String title;

    /**
     * 事项类型 (-1|0|1|2)
     */
    private String type;

    /**
     * 事项标签 (0|1|2|3)
     */
    private String tags;

    /**
     * 开始时间 (格式: yyyy-MM-dd HH:mm)
     */
    private String startTime;

    /**
     * 结束时间 (格式: yyyy-MM-dd HH:mm)
     */
    private String endTime;

    /**
     * 事项信息
     */
    private String info;

    /**
     * 是否提醒
     */
    private Boolean isRemind;

    /**
     * 提前提醒分钟数
     */
    private Integer remindBeforeMinutes;

    /**
     * 事项图标 (如: "![icon](Tasks)")
     * 图标类型：
     * - 常规: Tasks/Docs/Reminders
     * - 会议: Video Meeting/Audio Meeting
     * - 运动: Tennis/Boxing/Badminton/Lifting/Running
     * - 兜底: General/Meeting/Sport
     */
    private String icon;

    /**
     * AI建议
     */
    private String aiSuggestion;

    private Boolean isAiSuggestion;

    public static SchedulePO convertAIScheduleVal2SchedulePO(String userId, AIScheduleVal aiScheduleVal) {
        SchedulePO schedulePO = new SchedulePO();

        // 基本信息
        schedulePO.setUserId(userId);
        schedulePO.setTitle(aiScheduleVal.getTitle());

        if (StringUtils.isNotBlank(aiScheduleVal.getType())) {
            schedulePO.setType(Objects.requireNonNull(EnumUtil.getEnumByValue(aiScheduleVal.getType(), TypeEnum.class)).getCode());
        }

        // 时间解析 (格式: yyyy-MM-dd HH:mm)
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        if (aiScheduleVal.getStartTime() != null) {
            schedulePO.setStartTime(LocalDateTime.parse(aiScheduleVal.getStartTime(), formatter));
        }
        if (aiScheduleVal.getEndTime() != null) {
            schedulePO.setEndTime(LocalDateTime.parse(aiScheduleVal.getEndTime(), formatter));
        }

        // 标签 (直接设置String类型)
        if (StringUtils.isNotBlank(aiScheduleVal.getTags())) {
            schedulePO.setTag(Objects.requireNonNull(EnumUtil.getEnumByValue(aiScheduleVal.getTags(), TagEnum.class)).getCode());
        }

        // 其他信息
        schedulePO.setAuxiliaryInfo(aiScheduleVal.getInfo());
        schedulePO.setIcon(aiScheduleVal.getIcon());
        schedulePO.setAiSuggestion(aiScheduleVal.getAiSuggestion());
        schedulePO.setIsRemind(aiScheduleVal.getIsRemind() != null ? aiScheduleVal.getIsRemind() : false);

        // 默认值
        schedulePO.setStatus(ScheduleStatusEnum.PENDING.getCode());
        schedulePO.setIsConflict(false);
        schedulePO.setIsDailyBriefing(false);
        schedulePO.setIsCreate(true);

        return schedulePO;
    }

    public AIScheduleVal() {
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public Boolean getIsRemind() {
        return isRemind;
    }

    public void setIsRemind(Boolean isRemind) {
        this.isRemind = isRemind;
    }

    public Integer getRemindBeforeMinutes() {
        return remindBeforeMinutes;
    }

    public void setRemindBeforeMinutes(Integer remindBeforeMinutes) {
        this.remindBeforeMinutes = remindBeforeMinutes;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getAiSuggestion() {
        return aiSuggestion;
    }

    public void setAiSuggestion(String aiSuggestion) {
        this.aiSuggestion = aiSuggestion;
    }

    public Boolean getIsAiSuggestion() {
        return isAiSuggestion;
    }

    public void setIsAiSuggestion(Boolean isAiSuggestion) {
        this.isAiSuggestion = isAiSuggestion;
    }

    // Builder pattern
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String title;
        private String type;
        private String tags;
        private String startTime;
        private String endTime;
        private String info;
        private Boolean isRemind;
        private Integer remindBeforeMinutes;
        private String icon;
        private String aiSuggestion;
        private Boolean isAiSuggestion;

        public Builder title(String title) {
            this.title = title;
            return this;
        }

        public Builder type(String type) {
            this.type = type;
            return this;
        }

        public Builder tags(String tags) {
            this.tags = tags;
            return this;
        }

        public Builder startTime(String startTime) {
            this.startTime = startTime;
            return this;
        }

        public Builder endTime(String endTime) {
            this.endTime = endTime;
            return this;
        }

        public Builder info(String info) {
            this.info = info;
            return this;
        }

        public Builder isRemind(Boolean isRemind) {
            this.isRemind = isRemind;
            return this;
        }

        public Builder remindBeforeMinutes(Integer remindBeforeMinutes) {
            this.remindBeforeMinutes = remindBeforeMinutes;
            return this;
        }

        public Builder icon(String icon) {
            this.icon = icon;
            return this;
        }

        public Builder aiSuggestion(String aiSuggestion) {
            this.aiSuggestion = aiSuggestion;
            return this;
        }

        public Builder isAiSuggestion(Boolean isAiSuggestion) {
            this.isAiSuggestion = isAiSuggestion;
            return this;
        }

        public AIScheduleVal build() {
            AIScheduleVal card = new AIScheduleVal();
            card.title = this.title;
            card.type = this.type;
            card.tags = this.tags;
            card.startTime = this.startTime;
            card.endTime = this.endTime;
            card.info = this.info;
            card.isRemind = this.isRemind;
            card.remindBeforeMinutes = this.remindBeforeMinutes;
            card.icon = this.icon;
            card.aiSuggestion = this.aiSuggestion;
            card.isAiSuggestion = this.isAiSuggestion;
            return card;
        }
    }
}

