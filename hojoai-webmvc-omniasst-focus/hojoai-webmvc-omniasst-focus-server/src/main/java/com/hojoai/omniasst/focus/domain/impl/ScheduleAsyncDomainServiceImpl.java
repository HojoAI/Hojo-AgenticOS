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
package com.hojoai.omniasst.focus.domain.impl;

import com.hojoai.central.provider.grpc.PushReqDTO;
import com.hojoai.central.provider.service.common.enums.AgentActionEnum;
import com.hojoai.central.provider.service.common.enums.AgentTypeEnum;
import com.hojoai.central.provider.service.dto.request.push.PushRequestDTO;
import com.hojoai.common.jackson.JacksonUtil;
import com.hojoai.omniasst.focus.common.enums.TagEnum;
import com.hojoai.omniasst.focus.common.enums.UrlEnum;
import com.hojoai.omniasst.focus.configure.IConfigurationForGrpc;
import com.hojoai.omniasst.focus.domain.*;
import com.hojoai.omniasst.focus.dto.request.*;
import com.hojoai.omniasst.focus.dto.response.AICreateScheduleResponseDTO;
import com.hojoai.omniasst.focus.dto.response.AIScheduleDetailSuggestionResponseDTO;
import com.hojoai.omniasst.focus.dto.response.AISuggestionVal;
import com.hojoai.omniasst.focus.dto.response.IntentRecogResponseDTO;
import com.hojoai.omniasst.focus.dto.val.AIScheduleCommonVal;
import com.hojoai.omniasst.focus.dto.val.AIScheduleVal;
import com.hojoai.omniasst.focus.repository.po.ScheduleAttachmentPO;
import com.hojoai.omniasst.focus.repository.po.SchedulePO;
import com.hojoai.omniasst.focus.service.api.ManagementAPI;
import com.hojoai.omniasst.focus.service.api.bean.AttachmentItem;
import com.hojoai.omniasst.focus.service.api.bean.CreateRecommendationRequest;
import com.hojoai.omniasst.focus.service.api.bean.DeleteRecommendationRequest;
import com.hojoai.omniasst.focus.utils.DateUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.redisson.api.RDelayedQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import xyz.downgoon.snowflake.Snowflake;

import javax.annotation.Resource;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 *
 * @author My
 * @version 1.0, 2026/01/17
 * @since hojoai-webmvc-omniasst-focus 1.0.0
 */
@Component
public class ScheduleAsyncDomainServiceImpl implements ScheduleAsyncDomainService {

    private static final Logger log = LoggerFactory.getLogger(ScheduleAsyncDomainServiceImpl.class);

    @Resource
    private AIHandleDomainService aiHandleDomainService;

    @Resource
    private ScheduleTaskDomainService scheduleTaskService;

    @Resource
    private AIDomainService aiDomainService;

    @Resource
    private ScheduleDomainService scheduleDomainService;

    @Resource
    private ManagementAPI managementAPI;

    @Resource
    private RDelayedQueue<String> scheduleTaskDelayedQueue;

    @Resource
    private IConfigurationForGrpc iConfigurationForGrpc;

    @Resource
    private Snowflake snowflake;

    @Async
    @Override
    public void dailyBriefingHandle(String userId, LocalDate date, TagEnum tagEnum) {
        scheduleTaskService.executeAIScheduleAnalysis(userId, date, TagEnum.FOCUS);
    }

    /**
     * 异步调用 AI 获取日程建议并更新数据库
     *
     * @param userId 用户ID
     * @param schedules 已保存的日程列表
     */
    @Async
    @Override
    public void getConflictAndSuggestion(String userId, List<SchedulePO> schedules) throws RuntimeException {
        List<Long> scheduleIds = schedules.stream()
                .map(SchedulePO::getId)
                .collect(Collectors.toList());

        log.info("开始异步获取日程建议, userId={}, scheduleIds={}", userId, scheduleIds);

        if (schedules.isEmpty()) {
            log.warn("日程列表为空，跳过获取建议");
            return;
        }

        // 根据schedules中startTime的年月日判断，如果某一天存在isConflict为true，输出LocalDate集合
        schedules.stream()
                .filter(schedule -> schedule.getStartTime() != null)
                .map(schedule -> schedule.getStartTime().toLocalDate())
                .distinct()
                .forEach(localDate -> scheduleTaskService.taskHandle(userId, localDate, TagEnum.CONFLICT));

        List<SchedulePO> updateScheduleList = schedules.stream()
                .map(schedule -> {
                    AIScheduleCommonVal requestDTO = AIScheduleCommonVal.convertSchedulePO2AIScheduleCommonVal(schedule);

                    AISuggestionHttpRequestDTO request = new AISuggestionHttpRequestDTO();
                    request.setAiScheduleCommonVal(requestDTO);
                    request.setStream(Boolean.FALSE);

                    AIPreHttpRequestDTO aiPreHttpRequestDTO = new AIPreHttpRequestDTO();
                    aiPreHttpRequestDTO.setAiScheduleCommonVal(requestDTO);
                    aiPreHttpRequestDTO.setStream(Boolean.FALSE);

                    AISuggestionVal aiResponseJson;
                    AIScheduleDetailSuggestionResponseDTO detailSuggestionJson;

                    // 1. 调用 ScheduleSuggestion 获取日程建议
                    aiResponseJson = aiDomainService.chat(UrlEnum.SCHEDULE_SUGGESTION, request, AISuggestionVal.class);
                    log.info("AI建议响应: {}", JacksonUtil.bean2Json(aiResponseJson));

                    // 2. 调用 SchedulePreEventSuggestion 获取详情建议
                    detailSuggestionJson = aiDomainService.chat(UrlEnum.SCHEDULE_PRE_EVENT_SUGGESTION, aiPreHttpRequestDTO, AIScheduleDetailSuggestionResponseDTO.class);
                    log.info("AI详情建议响应: {}", JacksonUtil.bean2Json(detailSuggestionJson));

                    aiHandleDomainService.scheduleSuggestionHandle(schedule, aiResponseJson, detailSuggestionJson);

                    return schedule;
                }).toList();

        scheduleDomainService.batchUpdateSchedules(updateScheduleList);

        sendPushHandle(updateScheduleList);
    }

    private void sendPushHandle(List<SchedulePO> updateScheduleList) {
        PushRequestDTO requestDTO = new PushRequestDTO();

        requestDTO.setUserIdList(Collections.singletonList(updateScheduleList.getFirst().getUserId()));
        requestDTO.setType(AgentTypeEnum.FOCUS_AI_DETAIL);
        requestDTO.setAction(AgentActionEnum.WEB_REFRESH_PAGE);
        requestDTO.setBroadcast(false);

        PushReqDTO pushReqDTO = PushReqDTO.newBuilder()
                .setPushReqStr(JacksonUtil.bean2Json(requestDTO))
                .build();

        iConfigurationForGrpc.pushClient().push(pushReqDTO);
    }

    @Async
    @Override
    public void meetingHandle(String userId, MeetingCreateScheduleRequestDTO request, long scheduleId) throws Exception {
        // 1. 准备调用AI的输入参数
        LocalDateTime now = LocalDateTime.now();
        String dateStr = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        DayOfWeek dayOfWeek = now.getDayOfWeek();
        String[] weekDays = {"星期一", "星期二", "星期三", "星期四", "星期五", "星期六", "星期日"};
        String weekDayStr = weekDays[dayOfWeek.getValue() - 1];
        String timeStr = now.format(DateTimeFormatter.ofPattern("HH:mm"));
        String todayDate = dateStr + " 本周" + weekDayStr + " " + timeStr;
        String calendarList = DateUtils.getCurrentAndNextWeekDates();

        AINLURequest aiNLURequest = new AINLURequest();

        aiNLURequest.setUserQuery(request.getQueryStr());
        aiNLURequest.setCalendarList(calendarList);
        aiNLURequest.setCurrentTime(todayDate);
        aiNLURequest.setStream(Boolean.FALSE);

        // 3. 调用AI服务进行意图识别（使用 default prompt）
        IntentRecogResponseDTO intentRecogResponse = aiDomainService.chat(UrlEnum.INTENT_RECOGNITION, aiNLURequest, IntentRecogResponseDTO.class);

        // 5. 根据识别的日期列表查询已存在的日程
        List<SchedulePO> existingSchedules = scheduleDomainService.querySchedulesByDates(userId, intentRecogResponse.getDate());
        log.info("查询到的已存在日程数量: {}", existingSchedules.size());

        // 6. 构建AI日程创建请求
        AIScheduleCreateRequestDTO aiRequest = AIScheduleCreateRequestDTO.buildAIScheduleCreateRequest(intentRecogResponse, existingSchedules);
        aiRequest.setStream(Boolean.FALSE);

        AICreateScheduleResponseDTO aiCreateResponse = aiDomainService.chat(UrlEnum.SCHEDULE_CREATE, aiRequest, AICreateScheduleResponseDTO.class);
        log.info("AI创建日程响应: {}", JacksonUtil.bean2Json(aiCreateResponse));

        // 9. 保存日程到数据库，并设置 meetingId、meetingTitle 和 syncTime
        List<SchedulePO> savedSchedules = saveSchedulesFromAIWithMeetingInfo(userId, scheduleId,
                aiCreateResponse, request.getMeetingId(), request.getMeetingTitle());

        savedSchedules.forEach(schedule -> {
            List<SchedulePO> existScheduleList = existingSchedules.stream()
                    .filter(existingSchedule -> existingSchedule.getStartTime().toLocalDate().equals(schedule.getStartTime().toLocalDate()))
                    .toList();

            scheduleDomainService.suggetionGapHandle(schedule, existScheduleList);
        });

        this.asyncHandle(userId, savedSchedules, existingSchedules);
    }

    /**
     * 根据existingSchedules和savedSchedules中的startTime的年月判断，找出存在3个以上日程的日期
     *
     * @param existingSchedules 已存在的日程列表
     * @param savedSchedules 新保存的日程列表
     * @return 存在3个以上日程的日期集合（LocalDate）
     */
    @Async
    @Override
    public void suggestionAndDailyBriefingHandle(String userId, List<SchedulePO> existingSchedules, List<SchedulePO> savedSchedules) {
        // 合并两个列表
        List<SchedulePO> allSchedules = new ArrayList<>();
        if (existingSchedules != null) {
            allSchedules.addAll(existingSchedules);
        }
        if (savedSchedules != null) {
            allSchedules.addAll(savedSchedules);
        }

        // 按日期分组统计日程数量
        Map<LocalDate, Long> dateScheduleCountMap = allSchedules.stream()
                .filter(schedule -> schedule.getStartTime() != null)
                .collect(Collectors.groupingBy(
                        schedule -> schedule.getStartTime().toLocalDate(),
                        Collectors.counting()
                ));

        // 找出存在3个以上日程的日期
        List<LocalDate> localDateList = dateScheduleCountMap.entrySet().stream()
                .filter(entry -> entry.getValue() > 2)
                .map(Map.Entry::getKey)
                .toList();

        for (LocalDate date : localDateList) {
            scheduleTaskService.taskHandle(userId, date, TagEnum.SUGGESTION);
            scheduleTaskService.taskHandle(userId, date, TagEnum.FOCUS);
        }
    }

    @Async
    @Override
    public void asyncHandle(String userId, List<SchedulePO> createdSchedules, List<SchedulePO> existingSchedules) {
        // 冲突建议和AI建议
        this.getConflictAndSuggestion(userId, createdSchedules);

        this.suggestionAndDailyBriefingHandle(userId, existingSchedules, createdSchedules);
    }

    /**
     * 保存AI创建的日程到数据库（带会议信息）
     *
     * @param userId 用户ID
     * @param firstScheduleId 第一个日程使用的预生成ID
     * @param aiCreateResponse AI创建日程响应
     * @param meetingId 会议ID
     * @param meetingTitle 会议标题
     * @throws Exception 保存失败时抛出异常
     */
    private List<SchedulePO> saveSchedulesFromAIWithMeetingInfo(String userId, long firstScheduleId,
                                                                AICreateScheduleResponseDTO aiCreateResponse,
                                                                String meetingId, String meetingTitle) throws Exception {
        List<SchedulePO> savedSchedules = new ArrayList<>();

        if (aiCreateResponse == null || aiCreateResponse.getScheduleCards() == null ||
                aiCreateResponse.getScheduleCards().isEmpty()) {
            log.warn("AI响应中没有日程卡片，跳过保存");
            return savedSchedules;
        }

        List<AIScheduleVal> scheduleCards = aiCreateResponse.getScheduleCards();
        log.info("开始保存 {} 个AI创建的日程（会议相关）", scheduleCards.size());

        for (int i = 0; i < scheduleCards.size(); i++) {
            AIScheduleVal card = scheduleCards.get(i);

            // 转换 ScheduleCardVal 为 SchedulePO
            SchedulePO schedulePO = AIScheduleVal.convertAIScheduleVal2SchedulePO(userId, card);

            // 设置预生成的 scheduleId：第一个使用传入的 firstScheduleId，后续的使用 snowflake 生成
            long currentScheduleId = (i == 0) ? firstScheduleId : snowflake.nextId();
            if (Objects.isNull(schedulePO.getId()) || 0 == schedulePO.getId()) {
                schedulePO.setId(currentScheduleId);
            }
            log.info("为第 {} 个日程分配 scheduleId: {}", i + 1, currentScheduleId);

            // 会议维度字段落在 focus_schedule_meeting 等子表，由 createSchedule 链路内持久化，此处不写入 SchedulePO

            // 确定提醒类型
            String remindType = card.getIsRemind() != null && card.getIsRemind() ? "PUSH" : null;

            // 提醒时间（如果启用提醒但没有指定时间，默认10分钟）
            Integer remindBeforeMinutes = card.getRemindBeforeMinutes();
            if (Boolean.TRUE.equals(card.getIsRemind()) && remindBeforeMinutes == null) {
                remindBeforeMinutes = 10;
            }

            // 调用 Domain 层创建日程（schedulePO 中已设置好 id）
            scheduleDomainService.createSchedule(schedulePO, remindBeforeMinutes, remindType, Boolean.TRUE);

            savedSchedules.add(schedulePO);

            long seconds = Duration.between(LocalDateTime.now(), schedulePO.getStartTime()).toSeconds();

            if (seconds > 0) {
                scheduleTaskDelayedQueue.offer(String.valueOf(schedulePO.getId()), seconds, TimeUnit.SECONDS);
            }
        }

        return savedSchedules;
    }

    @Async
    @Override
    public void scheduleChangeAiHandle(String userId, List<SchedulePO> changedSchedules) {
        if (changedSchedules == null || changedSchedules.isEmpty()) {
            return;
        }
        log.info("scheduleChangeAiHandle 开始, userId={}, changedCount={}", userId, changedSchedules.size());

        try {
            Map<LocalDate, List<SchedulePO>> groupedByDate = changedSchedules.stream()
                    .filter(s -> s.getStartTime() != null)
                    .collect(Collectors.groupingBy(s -> s.getStartTime().toLocalDate()));

            for (Map.Entry<LocalDate, List<SchedulePO>> entry : groupedByDate.entrySet()) {
                LocalDate date = entry.getKey();
                List<SchedulePO> dailyChangedSchedules = entry.getValue();

                dailyChangedSchedules.stream()
                        .filter(schedule -> schedule.getStartTime() != null)
                        .map(schedule -> schedule.getStartTime().toLocalDate())
                        .distinct()
                        .forEach(localDate -> scheduleTaskService.taskHandle(userId, localDate, TagEnum.CONFLICT));

                // AI 建议生成（规则1和规则2共用）
                int changedCount = dailyChangedSchedules.size();

                List<SchedulePO> updatedSchedules = dailyChangedSchedules.stream()
                        .filter(SchedulePO::getIsAiSuggestion)
                        .map(schedule -> {
                            AIScheduleCommonVal commonVal = AIScheduleCommonVal.convertSchedulePO2AIScheduleCommonVal(schedule);

                            AISuggestionHttpRequestDTO suggestionReq = new AISuggestionHttpRequestDTO();
                            suggestionReq.setAiScheduleCommonVal(commonVal);
                            suggestionReq.setStream(false);

                            AIPreHttpRequestDTO preReq = new AIPreHttpRequestDTO();
                            preReq.setAiScheduleCommonVal(commonVal);
                            preReq.setStream(false);

                            AISuggestionVal aiSuggestion = aiDomainService.chat(
                                    UrlEnum.SCHEDULE_SUGGESTION, suggestionReq, AISuggestionVal.class);
                            log.info("AI建议响应, scheduleId={}: {}", schedule.getId(), JacksonUtil.bean2Json(aiSuggestion));

                            AIScheduleDetailSuggestionResponseDTO detailSuggestion = aiDomainService.chat(
                                    UrlEnum.SCHEDULE_PRE_EVENT_SUGGESTION, preReq, AIScheduleDetailSuggestionResponseDTO.class);
                            log.info("AI详情建议响应, scheduleId={}: {}", schedule.getId(), JacksonUtil.bean2Json(detailSuggestion));

                            scheduleDomainService.deleteScheduleDetails(schedule.getId());
                            aiHandleDomainService.scheduleSuggestionHandle(schedule, aiSuggestion, detailSuggestion);
                            return schedule;
                        }).toList();

                if (CollectionUtils.isNotEmpty(updatedSchedules)) {
                    scheduleDomainService.batchUpdateSchedules(updatedSchedules);
                    sendPushHandle(updatedSchedules);
                }

                // 规则1：当天变化日程数 > 3 → 触发 Daily Briefing
                if (changedCount > 3) {
                    scheduleTaskService.taskHandle(userId, date, TagEnum.FOCUS);
                }
            }

            log.info("scheduleChangeAiHandle 完成, userId={}", userId);
        } catch (Exception e) {
            log.error("scheduleChangeAiHandle 执行失败, userId={}", userId, e);
        }
    }

    @Async
    @Override
    public void createRecommendation(SchedulePO schedulePO, List<ScheduleAttachmentPO> attachmentPOList) {
        try {
            CreateRecommendationRequest request = new CreateRecommendationRequest();
            request.setUserId(schedulePO.getUserId());
            request.setType("schedule");
            request.setTitle(schedulePO.getTitle());
            request.setRelatedId(String.valueOf(schedulePO.getId()));

            Map<String, Object> extra = new HashMap<>();
            extra.put("scheduleStartTime", schedulePO.getStartTime());
            extra.put("scheduleEndTime", schedulePO.getEndTime());
            extra.put("scheduleTitle", schedulePO.getTitle());
            request.setExtra(extra);

            if (CollectionUtils.isNotEmpty(attachmentPOList)) {
                List<AttachmentItem> attachments = attachmentPOList.stream().map(po -> {
                    AttachmentItem item = new AttachmentItem();
                    item.setName(po.getName());
                    item.setSize(po.getSize() != null ? po.getSize().longValue() : null);
                    item.setUrl(po.getUrl());
                    item.setThumbUrl(po.getThumbUrl());
                    if (StringUtils.hasText(po.getType()) && po.getType().contains("/")) {
                        item.setType(po.getType().substring(0, po.getType().indexOf("/")));
                    } else {
                        item.setType(po.getType());
                    }
                    return item;
                }).collect(Collectors.toList());
                request.setAttachments(attachments);
            }

            managementAPI.createRecommendation(request);
        } catch (Exception e) {
            log.error("createRecommendation error, scheduleId={}", schedulePO.getId(), e);
        }
    }

    @Async
    @Override
    public void deleteRecommendation(String userId, String scheduleId) {
        try {
            DeleteRecommendationRequest request = new DeleteRecommendationRequest();
            request.setUserId(userId);
            request.setRelatedId(scheduleId);

            managementAPI.deleteRecommendation(request);
        } catch (Exception e) {
            log.error("deleteRecommendation error, scheduleId={}", scheduleId, e);
        }
    }

}
