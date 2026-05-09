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
package com.hojoai.omniasst.focus.service.impl;

import com.hojoai.omniasst.focus.domain.ScheduleTaskDomainService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDate;

/**
 * Schedule Task Scheduler
 * 使用Spring @Scheduled实现系统级定时任务
 *
 * @author My
 * @version 1.0 2026/01/11
 * @since hojoai-webmvc-omniasst-focus 1.0.0
 */
@Component
@EnableScheduling
public class ScheduleTaskInitializer {

    private static final Logger log = LoggerFactory.getLogger(ScheduleTaskInitializer.class);

    @Resource
    private ScheduleTaskDomainService scheduleTaskDomainService;

    /**
     * 每天执行AI日程主动建议
     * cron表达式: 秒 分 时 日 月 周
     */
    // @Scheduled(cron = "0 0 3 * * ?")
    public void executeFreeSuggestion() {
        log.info("定时任务触发 -  AI日程主动建议");
        try {
            LocalDate today = LocalDate.now();
            scheduleTaskDomainService.executeSystemAIScheduleFreeSuggestion(today);
            log.info("定时任务完成 - AI日程主动建议");
        } catch (Exception e) {
            log.error("定时任务执行失败 - AI日程主动建议", e);
        }
    }

    /**
     * 每天执行AI日程分析
     * cron表达式: 秒 分 时 日 月 周
     */
    @Scheduled(cron = "0 0 4,12 * * ?")
    public void everyDayExecuteAnalysis() {
        log.info("定时任务触发 -  AI日程分析");
        try {
            LocalDate today = LocalDate.now();
            scheduleTaskDomainService.executeTodayAIScheduleAnalysis(today);
            log.info("定时任务完成 - AI日程分析");
        } catch (Exception e) {
            log.error("定时任务执行失败 - AI日程分析", e);
        }
    }

    /**
     * 每周执行AI日程分析
     * cron表达式: 秒 分 时 日 月 周
     */
    @Scheduled(cron = "0 0 12 * * 7")
    public void sundayExecuteAnalysis() {
        log.info("定时任务触发 -  AI日程分析");
        try {
            LocalDate today = LocalDate.now();
            scheduleTaskDomainService.executeSystemAIScheduleAnalysis(today);
            log.info("定时任务完成 - AI日程分析");
        } catch (Exception e) {
            log.error("定时任务执行失败 - AI日程分析", e);
        }
    }

}

