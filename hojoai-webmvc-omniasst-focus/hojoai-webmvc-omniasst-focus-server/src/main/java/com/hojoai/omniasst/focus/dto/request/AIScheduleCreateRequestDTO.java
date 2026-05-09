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

import com.fasterxml.jackson.annotation.JsonProperty;
import com.hojoai.central.provider.service.utils.EnumUtil;
import com.hojoai.omniasst.focus.common.enums.TagEnum;
import com.hojoai.omniasst.focus.dto.response.IntentRecogResponseDTO;
import com.hojoai.omniasst.focus.repository.po.SchedulePO;
import com.hojoai.omniasst.focus.utils.DateUtils;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 * @author My
 * @version 1.0, 2026/01/08
 * @since hojoai-webmvc-omniasst-focus 1.0.0
 */
public class AIScheduleCreateRequestDTO {

    @JsonProperty("currentTime")
    private String currentTime;
    /**
     * 用户输入的文本列表（每个元素代表一个日程事项）
     */
    @JsonProperty("userQueryList")
    private List<String> userText;

    @JsonProperty("calendarList")
    private String dateList;

    /**
     * 已存在的日程列表
     */
    @JsonProperty("existingSchedule")
    private List<ExistingSchedule> existingSchedule;

    @JsonProperty("stream")
    private Boolean stream;

    @JsonProperty("relay")
    private Object relay;

    /**
     * 构建AI日程创建请求
     *
     * @param intentRecogResponseDTO 意图识别响应
     * @param scheduleList 已存在的日程列表
     * @return AIScheduleCreateRequestDTO
     */
    public static AIScheduleCreateRequestDTO buildAIScheduleCreateRequest(IntentRecogResponseDTO intentRecogResponseDTO, List<SchedulePO> scheduleList) {
        AIScheduleCreateRequestDTO request = new AIScheduleCreateRequestDTO();
        request.setCurrentTime(DateUtils.getCurrentDateTimeWithWeekday());
        request.setUserText(intentRecogResponseDTO.getQuery());
        request.setDateList(DateUtils.getCurrentAndNextWeekDates());
        request.setRelay(intentRecogResponseDTO.getRelay());

        // 转换已存在的日程列表
        List<AIScheduleCreateRequestDTO.ExistingSchedule> existingSchedules = scheduleList.stream()
                .map(AIScheduleCreateRequestDTO::convertToExistingSchedule)
                .collect(Collectors.toList());

        request.setExistingSchedule(existingSchedules);

        return request;
    }

    /**
     * 将SchedulePO转换为ExistingSchedule对象
     *
     * @param schedulePO 日程PO
     * @return ExistingSchedule对象
     */
    private static AIScheduleCreateRequestDTO.ExistingSchedule convertToExistingSchedule(SchedulePO schedulePO) {
        AIScheduleCreateRequestDTO.ExistingSchedule schedule = new AIScheduleCreateRequestDTO.ExistingSchedule();

        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        schedule.setId(schedulePO.getId());
        schedule.setTitle(schedulePO.getTitle());
        schedule.setType(schedulePO.getType());
        schedule.setTags(String.valueOf(EnumUtil.getValueByCode(schedulePO.getTag(), TagEnum.class)));

        // 格式化时间
        if (schedulePO.getStartTime() != null) {
            schedule.setStartTime(schedulePO.getStartTime().format(timeFormatter));
        }
        if (schedulePO.getEndTime() != null) {
            schedule.setEndTime(schedulePO.getEndTime().format(timeFormatter));
        }

        schedule.setInfo(schedulePO.getAuxiliaryInfo());
        schedule.setIsRemind(schedulePO.getIsRemind());
        schedule.setRemindBeforeMinutes(null); // 提醒时间需要从reminder表查询，暂时设为null
        schedule.setIcon(schedulePO.getIcon());
        schedule.setAiSuggestion(schedulePO.getAiSuggestion());

        return schedule;
    }

    public AIScheduleCreateRequestDTO() {
    }

    public AIScheduleCreateRequestDTO(List<String> userText, List<ExistingSchedule> existingSchedule) {
        this.userText = userText;
        this.existingSchedule = existingSchedule;
    }

    public String getCurrentTime() {
        return currentTime;
    }

    public void setCurrentTime(String currentTime) {
        this.currentTime = currentTime;
    }

    public List<String> getUserText() {
        return userText;
    }

    public void setUserText(List<String> userText) {
        this.userText = userText;
    }

    public String getDateList() {
        return dateList;
    }

    public void setDateList(String dateList) {
        this.dateList = dateList;
    }

    public List<ExistingSchedule> getExistingSchedule() {
        return existingSchedule;
    }

    public void setExistingSchedule(List<ExistingSchedule> existingSchedule) {
        this.existingSchedule = existingSchedule;
    }

    public Boolean getStream() {
        return stream;
    }

    public void setStream(Boolean stream) {
        this.stream = stream;
    }

    public Object getRelay() {
        return relay;
    }

    public void setRelay(Object relay) {
        this.relay = relay;
    }

    /**
     * 已存在的日程信息
     */
    public static class ExistingSchedule {
        /**
         * 日程ID
         */
        @JsonProperty("id")
        private Long id;

        /**
         * 日程标题
         */
        @JsonProperty("title")
        private String title;

        /**
         * 日程类型
         */
        @JsonProperty("type")
        private Integer type;

        /**
         * 标签
         */
        @JsonProperty("tags")
        private String tags;

        /**
         * 开始时间 (格式: yyyy-MM-dd HH:mm)
         */
        @JsonProperty("startTime")
        private String startTime;

        /**
         * 结束时间 (格式: yyyy-MM-dd HH:mm)
         */
        @JsonProperty("endTime")
        private String endTime;

        /**
         * 额外信息
         */
        @JsonProperty("info")
        private String info;

        /**
         * 是否提醒
         */
        @JsonProperty("isRemind")
        private Boolean isRemind;

        /**
         * 提醒提前时间（分钟）
         */
        @JsonProperty("remindBeforeMinutes")
        private Integer remindBeforeMinutes;

        /**
         * 图标
         */
        @JsonProperty("icon")
        private String icon;

        /**
         * AI建议
         */
        @JsonProperty("aiSuggestion")
        private String aiSuggestion;

        public ExistingSchedule() {
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public Integer getType() {
            return type;
        }

        public void setType(Integer type) {
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
    }
}

