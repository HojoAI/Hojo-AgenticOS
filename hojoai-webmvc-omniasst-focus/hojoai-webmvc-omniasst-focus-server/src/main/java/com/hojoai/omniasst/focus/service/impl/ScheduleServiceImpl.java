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

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hojoai.common.jackson.JacksonUtil;
import com.hojoai.omniasst.focus.common.enums.*;
import com.hojoai.omniasst.focus.domain.*;
import com.hojoai.omniasst.focus.domain.strategy.ScheduleStrategy;
import com.hojoai.omniasst.focus.dto.request.*;
import com.hojoai.omniasst.focus.dto.response.*;
import com.hojoai.omniasst.focus.dto.val.*;
import com.hojoai.omniasst.focus.repository.mapper.*;
import com.hojoai.omniasst.focus.repository.po.*;
import com.hojoai.omniasst.focus.service.ScheduleService;
import com.hojoai.omniasst.focus.utils.DateUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RDelayedQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import xyz.downgoon.snowflake.Snowflake;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Schedule Service Implementation
 * Responsibility: Domain orchestration, DTO/PO conversion, does not directly call Repository
 *
 * @author My
 * @version 1.0 2024/12/29
 * @since hojoai-webmvc-omniasst.focus 1.0.0
 */
@Service
public class ScheduleServiceImpl implements ScheduleService {

    private static final Logger log = LoggerFactory.getLogger(ScheduleServiceImpl.class);

    @Resource
    private ScheduleDomainService scheduleServiceDomain;

    @Resource
    private AIDomainService aiDomainService;

    @Resource
    private ScheduleAsyncDomainService scheduleAsyncDomainService;

    @Resource
    private ScheduleDomainService scheduleDomainService;

    @Resource
    private ScheduleRuleDomainService scheduleRuleDomainService;

    @Resource
    private List<ScheduleStrategy<?, ?>> createScheduleStrategies;

    @Resource
    private Snowflake snowflake;

    @Resource
    private RDelayedQueue<String> scheduleTaskDelayedQueue;

    @Resource
    private ScheduleFlightMapper scheduleFlightMapper;

    @Resource
    private ScheduleTrainMapper scheduleTrainMapper;

    @Resource
    private ScheduleLocationMapper scheduleLocationMapper;

    @Resource
    private ScheduleMeetingMapper scheduleMeetingMapper;

    @Resource
    private ScheduleMovieMapper scheduleMovieMapper;

    @Resource
    private ScheduleSportMapper scheduleSportMapper;

    @Resource
    private AttachmentDomainService attachmentDomainService;

    private Map<Integer, ScheduleStrategy<?, ?>> strategyMap;

    private static final DateTimeFormatter DATE_TIME_RESPONSE = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static final String CREATE_CONTENT = "好的，想加个会议？运动？来，说清楚点：啥事？啥时候？我给你排进日程里～（别怕说太碎，我连“下午晚点去趟超市”都能给你拆成精准时间块）";

    private static final String REFUSE_TEXT = "这句我没太接住你的意思（挠头），你再补一句“你想我帮你做什么+具体要求”就行，我马上接上。\uD83D\uDE42\uD83D\uDCDD";

    @PostConstruct
    public void initStrategyMap() {
        strategyMap = createScheduleStrategies.stream()
                .collect(Collectors.toMap(ScheduleStrategy::getType, Function.identity()));
    }

    @Override
    public ScheduleResponseDTO queryScheduleByDate(QueryScheduleByDateRequestDTO request) throws Exception {
        // 1. Parameter conversion: Timestamp to LocalDate
        LocalDate date = convertTimestampToLocalDate(request.getDate());
        if (date == null) {
            throw new IllegalArgumentException("date is required");
        }

        // 2. Call Domain layer
        List<SchedulePO> scheduleList = scheduleServiceDomain.listByUserIdAndDate(request.getUserId(), date, request.getIsShowSysSync());
        if (scheduleList == null) {
            scheduleList = Collections.emptyList();
        }

        // 3. Convert PO list to ScheduleResponseDTO with daily briefing
        return convertToScheduleResponse(scheduleList, date, request.getUserId());
    }

    @Override
    public ScheduleDetailResponseDTO queryScheduleDetail(QueryScheduleDetailRequestDTO request) throws Exception {
        Long scheduleIdLong;
        try {
            scheduleIdLong = Long.valueOf(request.getScheduleId());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("scheduleId格式无效");
        }
        // 1. Call Domain layer to get schedule
        SchedulePO schedulePO = scheduleServiceDomain.getByUserIdAndScheduleId(request.getUserId(), scheduleIdLong);
        if (schedulePO == null) {
            return null;
        }

        ScheduleRulePO rulePO = null;
        if (schedulePO.getRuleId() != null) {
            rulePO = scheduleRuleDomainService.getScheduleRuleById(schedulePO.getRuleId());
        }

        ScheduleStrategy<?, ?> strategy = rulePO != null ? strategyMap.get(rulePO.getType()) : null;

        // 2. Query schedule details (highlights) from Domain layer
        List<ScheduleDetailPO> scheduleDetailList = scheduleServiceDomain.listScheduleDetails(scheduleIdLong);

        // 3. 各类型日程明细由策略 getScheduleData 统一查询（会议/运动/电影/航班/火车）
        List<?> scheduleDataList = Collections.emptyList();
        if (strategy != null) {
            List<?> fromStrategy = strategy.getScheduleData(schedulePO.getId());
            scheduleDataList = fromStrategy != null ? fromStrategy : Collections.emptyList();
        }

        // 4. 位置与附件
        ScheduleLocationPO locationPO = scheduleLocationMapper.selectOne(
                new LambdaQueryWrapper<ScheduleLocationPO>().eq(ScheduleLocationPO::getScheduleId, scheduleIdLong));
        List<ScheduleAttachmentPO> attachmentList = attachmentDomainService.queryByScheduleIdList(
                Collections.singletonList(scheduleIdLong));

        return convertToScheduleDetailResponse(schedulePO, scheduleDetailList, locationPO, rulePO,
                scheduleDataList, attachmentList);
    }

    @Override
    public Boolean updateSchedule(String scheduleId, UpdateScheduleRequestDTO request) throws Exception {
        // 1. Get existing schedule
        SchedulePO schedulePO = scheduleServiceDomain.getByUserIdAndScheduleId(request.getUserId(), Long.valueOf(scheduleId));
        
        if (schedulePO == null) {
            throw new IllegalArgumentException("Schedule not found: " + scheduleId);
        }
        
        // 2. Update fields
        if (request.getTitle() != null) {
            schedulePO.setTitle(request.getTitle());
        }
        if (request.getType() != null) {
            schedulePO.setType(request.getType());
        }
        if (request.getStatus() != null) {
            schedulePO.setStatus(request.getStatus());
        }
        if (request.getStartTime() != null) {
            schedulePO.setStartTime(convertDateToLocalDateTime(request.getStartTime()));
        }
        if (request.getEndTime() != null) {
            schedulePO.setEndTime(convertDateToLocalDateTime(request.getEndTime()));
        }
        if (request.getIsRemind() != null) {
            schedulePO.setIsRemind(request.getIsRemind());
        }
        if (request.getDescription() != null) {
            schedulePO.setRemark(request.getDescription());
        }
        if (request.getEnergy() != null) {
            schedulePO.setEnergy(request.getEnergy());
        }
        if (request.getAuxiliaryInfo() != null) {
            schedulePO.setAuxiliaryInfo(request.getAuxiliaryInfo());
        }
        
        // 3. Update through Domain layer
        return scheduleServiceDomain.updateSchedule(schedulePO);
    }

    @Override
    public List<CreateScheduleResponseDTO> createSchedule(CreateScheduleRequestDTO request) throws Exception {
        // 1. Convert DTO to PO
        SchedulePO schedulePO = buildSchedulePOFromRequest(request);
        
        // 2. Set default remind before minutes (10 if isRemind is true and not specified)
        Integer remindBeforeMinutes = request.getRemindBeforeMinutes();
        if (Boolean.TRUE.equals(request.getIsRemind()) && remindBeforeMinutes == null) {
            remindBeforeMinutes = 10;
        }
        
        // 3. Call Domain layer (includes domain logic: conflict detection, reminder creation)
        Long scheduleId = scheduleServiceDomain.createSchedule(schedulePO, 
                remindBeforeMinutes, 
                convertRemindType(request.getIsRemind()), Boolean.TRUE);
        
        // 4. Build response
        CreateScheduleResponseDTO responseDTO = new CreateScheduleResponseDTO();
        responseDTO.setScheduleId(scheduleId);
        responseDTO.setTitle(request.getTitle());
        responseDTO.setStatus(ScheduleStatusEnum.PENDING.getCode()); // Default status: Pending
        
        return Collections.singletonList(responseDTO);
    }

    @Override
    public List<CreateScheduleResponseDTO> cardCreateSchedule(String userId, CardCreateScheduleRequestDTO request) throws Exception {
        // 1. Convert List<String> to List<Long>
        List<Long> scheduleIdList = request.getScheduleIdList() != null ? 
                request.getScheduleIdList().stream()
                        .map(Long::parseLong)
                        .collect(Collectors.toList()) : 
                Collections.emptyList();
        
        // 2. Call Domain layer to card create schedules
        List<SchedulePO> createdSchedules = scheduleServiceDomain.cardCreateSchedule(userId, scheduleIdList);

        // 调用pa主对话
        scheduleServiceDomain.invokeToPAConversation(userId, request);

        List<LocalDate> dateList = createdSchedules.stream().map(schedulePO -> schedulePO.getStartTime().toLocalDate()).toList();

        List<SchedulePO> existingSchedules = scheduleDomainService.querySchedulesByDateList(userId, dateList);

        createdSchedules.forEach(schedule -> {
            List<SchedulePO> existScheduleList = existingSchedules.stream()
                    .filter(existingSchedule -> existingSchedule.getStartTime().toLocalDate().equals(schedule.getStartTime().toLocalDate()))
                    .toList();

            scheduleDomainService.suggetionGapHandle(schedule, existScheduleList);
        });

        scheduleAsyncDomainService.asyncHandle(userId, createdSchedules, existingSchedules);

        // 3. Convert PO list to response DTO list and create remind tasks
        return createdSchedules.stream()
                .map(schedulePO -> {
                    long seconds = Duration.between(LocalDateTime.now(), schedulePO.getStartTime()).toSeconds();

                    if (seconds > 0) {
                        // 使用Redisson延时队列替代redisTemplate
                        String scheduleId = String.valueOf(schedulePO.getId());
                        scheduleTaskDelayedQueue.offer(scheduleId, seconds, TimeUnit.SECONDS);
                        log.info("已将日程任务加入延时队列 - scheduleId: {}, 延迟时间: {}秒", scheduleId, seconds);
                    }

                    CreateScheduleResponseDTO responseDTO = new CreateScheduleResponseDTO();
                    responseDTO.setScheduleId(schedulePO.getId());
                    responseDTO.setTitle(schedulePO.getTitle());
                    responseDTO.setStatus(schedulePO.getStatus());

                    return responseDTO;
                })
                .collect(Collectors.toList());
    }

    @Override
    public void createScheduleFromAI(String userId, String conversationId, String queryStr, SseEmitter emitter) throws Exception {
        log.info("开始处理AI创建日程请求, userId={}, talkId={}, queryStr={}", userId, conversationId, queryStr);

        // 设置超时和完成回调
        emitter.onTimeout(() -> {
            log.warn("SSE连接超时 - userId: {}, conversationId: {}", userId, conversationId);
            emitter.complete();
        });
        
        emitter.onCompletion(() -> {
            log.info("SSE连接关闭 - userId: {}, conversationId: {}", userId, conversationId);
        });
        
        emitter.onError(throwable -> {
            log.error("SSE连接错误 - userId: {}, conversationId: {}", userId, conversationId, throwable);
        });

        try {
            // 拒识
            if ("创建日程".equals(queryStr)) {
                ConversationCreateScheduleResponseDTO responseDTO = buildConversationResponse(conversationId, "", CREATE_CONTENT, Boolean.TRUE, "bubble");
                emitter.send(SseEmitter.event().data(responseDTO).name("message"));
                emitter.complete();
                return;
            }

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

            aiNLURequest.setUserQuery(queryStr);
            aiNLURequest.setCalendarList(calendarList);
            aiNLURequest.setCurrentTime(todayDate);
            aiNLURequest.setStream(Boolean.FALSE);

            // 3. 调用AI服务进行意图识别（使用 default prompt）
            IntentRecogResponseDTO intentRecogResponse = aiDomainService.chat(UrlEnum.INTENT_RECOGNITION, aiNLURequest, IntentRecogResponseDTO.class);

            // 如果识别结果为空，则直接返回
            if (Objects.isNull(intentRecogResponse) || CollectionUtils.isEmpty(intentRecogResponse.getQuery()) || CollectionUtils.isEmpty(intentRecogResponse.getDate())) {
                ConversationCreateScheduleResponseDTO responseDTO = buildConversationResponse(conversationId, "", REFUSE_TEXT, Boolean.TRUE, "bubble");
                emitter.send(SseEmitter.event().data(responseDTO).name("message"));
                emitter.complete();
                return;
            }

            // 5. 根据识别的日期列表查询已存在的日程
            List<SchedulePO> existingSchedules = scheduleServiceDomain.querySchedulesByDates(userId, intentRecogResponse.getDate());

            log.info("查询到的已存在日程数量: {}", existingSchedules.size());

            // 6. 构建AI日程创建请求
            AIScheduleCreateRequestDTO aiRequest = AIScheduleCreateRequestDTO.buildAIScheduleCreateRequest(intentRecogResponse, existingSchedules);
            aiRequest.setStream(Boolean.TRUE);

            // 7. 调用AI流式服务创建日程
            String aiRequestJson = JacksonUtil.bean2Json(aiRequest);
            log.info("调用AI创建日程请求: {}", aiRequestJson);

            // 用于收集完整响应的缓冲区
            AtomicReference<StringBuilder> fullResponseRef = new AtomicReference<>(new StringBuilder());
            
            // 用于流式解析的状态变量
            AtomicReference<Integer> lastAdviceLength = new AtomicReference<>(0);
            
            // 用于跟踪personality是否已发送的标志位
            AtomicReference<Boolean> personalitySent = new AtomicReference<>(false);
            
            // 用于跟踪已处理的schedule_cards数量
            AtomicReference<Integer> processedScheduleCardsCount = new AtomicReference<>(0);
            
            // 用于累积 schedule_cards 的 valList
            AtomicReference<List<PACreateScheduleVal>> scheduleValListRef = new AtomicReference<>(new ArrayList<>());
            
            // 用于跟踪是否已发送 "scheduleList:["
            AtomicReference<Boolean> scheduleListStartSent = new AtomicReference<>(false);
            
            // 用于跟踪是否已发送结束标记 "]}}"
            AtomicReference<Boolean> scheduleListEndSent = new AtomicReference<>(false);
            
            // 用于累积 schedulePO 的集合
            AtomicReference<List<SchedulePO>> schedulePOListRef = new AtomicReference<>(new ArrayList<>());
            
            // 8. 调用AI流式服务并处理响应
            aiDomainService.chatStream(UrlEnum.SCHEDULE_CREATE, aiRequest)
                    .doOnNext(chunk -> returnAdviceHandle(userId, conversationId, chunk, fullResponseRef, lastAdviceLength, personalitySent, processedScheduleCardsCount, scheduleValListRef, scheduleListStartSent, scheduleListEndSent, schedulePOListRef, emitter))
                    .doOnComplete(() -> createScheduleHandle(userId, conversationId, "", schedulePOListRef, scheduleListStartSent, scheduleListEndSent, emitter))
                    .doOnError(error -> {
                        throw new RuntimeException(error);
                    })
                    .subscribe(); // 订阅以启动流式处理
            
        } catch (Throwable e) {
            log.error("Error occured while processing AI response", e);
            ConversationCreateScheduleResponseDTO responseDTO = buildConversationResponse(conversationId, "", REFUSE_TEXT, Boolean.TRUE, "bubble");
            emitter.send(SseEmitter.event().data(responseDTO).name("message"));
            emitter.complete();
        }
    }

    @Override
    public String meetingCreateScheduleFromAI(String userId, MeetingCreateScheduleRequestDTO request) throws Exception {
        log.info("开始处理Meeting AI创建日程请求, userId={}, meetingId={}, queryStr={}", 
                userId, request.getMeetingId(), request.getQueryStr());

        long scheduleId = snowflake.nextId();

        scheduleAsyncDomainService.meetingHandle(userId, request, scheduleId);

        return String.valueOf(scheduleId);
    }

    @Override
    public SchedulePO getScheduleByUserIdAndMeetingId(String userId, String meetingId) {
        return scheduleServiceDomain.getScheduleByUserIdAndMeetingId(userId, meetingId);
    }

    private void returnAdviceHandle(String userId, String conversationId, String chunk,
                                   AtomicReference<StringBuilder> fullResponseRef, 
                                   AtomicReference<Integer> lastAdviceLength,
                                   AtomicReference<Boolean> personalitySent,
                                   AtomicReference<Integer> processedScheduleCardsCount,
                                   AtomicReference<List<PACreateScheduleVal>> scheduleValListRef,
                                   AtomicReference<Boolean> scheduleListStartSent,
                                   AtomicReference<Boolean> scheduleListEndSent,
                                   AtomicReference<List<SchedulePO>> schedulePOListRef,
                                   SseEmitter emitter) {
        log.info("收到AI流式响应chunk: {}", chunk);
        fullResponseRef.get().append(chunk);

        // 实时提取 Advice 字段的增量内容
        try {
            // 尝试提取 personality 字段数据，只在未发送过时发送
            if (!personalitySent.get()) {
                String personality = extractPersonality(fullResponseRef.get().toString());
                if (personality != null && !personality.isEmpty()) {
                    log.info("提取到 personality: {}", personality);
                    // 发送personality数据，只发送一次
                    ConversationCreateScheduleResponseDTO responseDTO = buildConversationResponse(conversationId, "", personality, Boolean.FALSE, "bubble");
                    emitter.send(SseEmitter.event().data(responseDTO).name("message"));
                    // 标记为已发送
                    personalitySent.set(true);
                }
            }
            
            String adviceIncrement = extractAdviceIncrement(fullResponseRef.get().toString(), lastAdviceLength.get());

            if (adviceIncrement != null && !adviceIncrement.isEmpty()) {
                lastAdviceLength.set(lastAdviceLength.get() + adviceIncrement.length());

                // 构建ConversationCreateScheduleResponseDTO
                ConversationCreateScheduleResponseDTO responseDTO = buildConversationResponse(conversationId, "", adviceIncrement, Boolean.FALSE, "bubble");

                // 发送SSE事件
                emitter.send(SseEmitter.event().data(responseDTO).name("message"));
            }
            
            // 检测并处理 schedule_cards
            handleScheduleCardsStream(userId, conversationId, fullResponseRef.get().toString(), processedScheduleCardsCount, scheduleValListRef, scheduleListStartSent, scheduleListEndSent, schedulePOListRef, emitter);
        } catch (Exception e) {
            log.error("提取 Advice 增量或发送SSE失败", e);
            emitter.completeWithError(e);
        }
    }

    private void createScheduleHandle(String userId, String conversationId, String messageId,
                                      AtomicReference<List<SchedulePO>> schedulePOListRef,
                                      AtomicReference<Boolean> scheduleListStartSent,
                                      AtomicReference<Boolean> scheduleListEndSent,
                                      SseEmitter emitter) {
        try {
            // 使用 handleScheduleCardsStream 中累积的 schedulePO 集合
            List<SchedulePO> savedSchedules = schedulePOListRef.get();

            log.info("AI创建日程完成，成功保存 {} 个日程", savedSchedules.size());

            // 如果已经在流式处理过程中发送了卡片数据，只需要检查是否发送了结束标记
            if (scheduleListStartSent.get() && !scheduleListEndSent.get()) {
                // 流式处理过程中已经发送了开始标记和对象数据，但还没有发送结束标记
                // 发送结束标记
                ConversationCreateScheduleResponseDTO endResponse = buildConversationResponse(
                        conversationId, messageId, "]}}", Boolean.TRUE, "card");
                emitter.send(SseEmitter.event().data(endResponse).name("message"));
                scheduleListEndSent.set(true);
                log.info("流式处理完成后发送 scheduleList 结束标记: ]}} (共 {} 个日程)", savedSchedules.size());
            } else if (!scheduleListStartSent.get() && !savedSchedules.isEmpty()) {
                // 如果流式处理过程中没有发送任何数据，说明可能没有检测到 schedule_cards
                // 或者流式处理出现了问题，这里发送完整的卡片数据作为兜底
                List<PACreateScheduleVal> valList = new ArrayList<>();
                DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
                savedSchedules.forEach(schedule -> {
                    PACreateScheduleVal val = new PACreateScheduleVal();

                    val.setScheduleId(String.valueOf(schedule.getId()));

                    val.setTitle(schedule.getTitle());
                    val.setIcon(schedule.getIcon());

                    val.setStartTime(DateUtils.localDateTimeToLong(schedule.getStartTime()));
                    val.setEndTime(DateUtils.localDateTimeToLong(schedule.getEndTime()));

                    val.setSubtitle(StringUtils.defaultString(schedule.getAuxiliaryInfo()));

                    valList.add(val);
                });

                ScheduleListVal scheduleListVal = new ScheduleListVal();
                scheduleListVal.setScheduleList(valList);

                CardVal cardVal = new CardVal();
                cardVal.setName("ScheduleCreateCard");
                cardVal.setType("component");
                cardVal.setData(scheduleListVal);

                // 发送完成标记
                ConversationCreateScheduleResponseDTO finishResponse = buildConversationResponse(conversationId, messageId, JacksonUtil.bean2Json(cardVal), Boolean.TRUE, "card");
                
                // 发送最终事件
                emitter.send(SseEmitter.event().data(finishResponse).name("message"));
                log.info("发送完整的卡片数据作为兜底 (共 {} 个日程)", savedSchedules.size());
            }
            
            // 完成流式输出
            emitter.complete();
        } catch (Exception e) {
            log.error("处理AI创建的日程失败", e);
            emitter.completeWithError(e);
        }
    }

    /**
     * 构建ConversationCreateScheduleResponseDTO对象
     */
    private ConversationCreateScheduleResponseDTO buildConversationResponse(
            String conversationId, String messageId, String text, Boolean finished, String subType) {
        ConversationCreateScheduleResponseDTO response = new ConversationCreateScheduleResponseDTO();
        response.setConversationId(conversationId);
        response.setMessageId(messageId);
        response.setAvatarId("assistant");
        response.setTimestamp(System.currentTimeMillis());
        response.setFinished(finished);

        // 构建data字段
        ConversationDataVal dataVal = new ConversationDataVal();
        dataVal.setType("text");
        dataVal.setSubType(subType);
        dataVal.setText(text);

        response.setData(dataVal);

        return response;
    }

    /**
     * 构建意图识别的prompt
     *
     * @param queryStr 用户查询
     * @param todayDate 今日日期
     * @param calendarList 日历列表
     * @return 完整的prompt
     */
    private String buildIntentRecogPrompt(String queryStr, String todayDate, String calendarList) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("# 用户query：\n");
        prompt.append(queryStr).append("\n\n");
        prompt.append("# 今日日期：\n");
        prompt.append(todayDate).append("\n\n");
        prompt.append("# 日历列表：\n");
        prompt.append(calendarList).append("\n\n");
        return prompt.toString();
    }

    /**
     * 从流式 JSON 中提取 personality 字段的完整内容
     * 
     * @param currentJson 当前累积的 JSON 字符串
     * @return personality 内容，如果未找到或未完成返回 null
     */
    private String extractPersonality(String currentJson) {
        try {
            // 查找 "personality" 字段的位置
            int personalityKeyStart = currentJson.indexOf("\"personality\"");
            if (personalityKeyStart == -1) {
                return null;
            }
            
            // 查找冒号后的值开始位置
            int colonPos = currentJson.indexOf(":", personalityKeyStart);
            if (colonPos == -1) {
                return null;
            }
            
            // 跳过空格，找到值的开始引号
            int valueStart = colonPos + 1;
            while (valueStart < currentJson.length() && 
                   (currentJson.charAt(valueStart) == ' ' || currentJson.charAt(valueStart) == '\t')) {
                valueStart++;
            }
            
            if (valueStart >= currentJson.length() || currentJson.charAt(valueStart) != '"') {
                return null;
            }
            
            valueStart++; // 跳过开始引号
            
            // 解析字符串值，正确处理转义字符
            StringBuilder personality = new StringBuilder();
            boolean escaped = false;
            boolean completed = false;
            
            for (int i = valueStart; i < currentJson.length(); i++) {
                char c = currentJson.charAt(i);
                
                if (escaped) {
                    // 处理转义字符
                    switch (c) {
                        case 'n':
                            personality.append('\n');
                            break;
                        case 'r':
                            personality.append('\r');
                            break;
                        case 't':
                            personality.append('\t');
                            break;
                        case '"':
                            personality.append('"');
                            break;
                        case '\\':
                            personality.append('\\');
                            break;
                        case 'b':
                            personality.append('\b');
                            break;
                        case 'f':
                            personality.append('\f');
                            break;
                        case 'u':
                            if (i + 4 < currentJson.length()) {
                                try {
                                    String hex = currentJson.substring(i + 1, i + 5);
                                    char unicode = (char) Integer.parseInt(hex, 16);
                                    personality.append(unicode);
                                    i += 4; // 跳过 4 位十六进制数字
                                } catch (NumberFormatException e) {
                                    personality.append(c);
                                }
                            }
                            break;
                        default:
                            personality.append(c);
                    }
                    escaped = false;
                } else if (c == '\\') {
                    escaped = true;
                } else if (c == '"') {
                    // personality 字段结束
                    completed = true;
                    break;
                } else {
                    personality.append(c);
                }
            }
            
            // 只有在字段完整时才返回
            if (completed) {
                log.debug("提取 personality 成功: {}", personality.toString());
                return personality.toString();
            }
            
        } catch (Exception e) {
            log.warn("提取 personality 失败", e);
        }
        
        return null;
    }
    
    /**
     * 使用 Jackson JsonParser 从流式 JSON 中提取 Advice 字段的增量内容
     * 
     * @param currentJson 当前累积的 JSON 字符串
     * @param processedLength 已处理的 Advice 内容长度
     * @return 新的 Advice 内容片段
     */
    private String extractAdviceIncrement(String currentJson, int processedLength) {
        try {
            // 查找 "Advice" 字段的位置
            int adviceKeyStart = currentJson.indexOf("\"Advice\"");
            if (adviceKeyStart == -1) {
                return null;
            }
            
            // 查找冒号后的值开始位置
            int colonPos = currentJson.indexOf(":", adviceKeyStart);
            if (colonPos == -1) {
                return null;
            }
            
            // 跳过空格，找到值的开始引号
            int valueStart = colonPos + 1;
            while (valueStart < currentJson.length() && 
                   (currentJson.charAt(valueStart) == ' ' || currentJson.charAt(valueStart) == '\t')) {
                valueStart++;
            }
            
            if (valueStart >= currentJson.length() || currentJson.charAt(valueStart) != '"') {
                return null;
            }
            
            valueStart++; // 跳过开始引号
            
            // 使用 JsonParser 解析字符串值，正确处理转义字符
            StringBuilder advice = new StringBuilder();
            boolean escaped = false;
            boolean completed = false;
            
            for (int i = valueStart; i < currentJson.length(); i++) {
                char c = currentJson.charAt(i);
                
                if (escaped) {
                    // 处理转义字符
                    switch (c) {
                        case 'n':
                            advice.append('\n');
                            break;
                        case 'r':
                            advice.append('\r');
                            break;
                        case 't':
                            advice.append('\t');
                            break;
                        case '"':
                            advice.append('"');
                            break;
                        case '\\':
                            advice.append('\\');
                            break;
                        case 'b':
                            advice.append('\b');
                            break;
                        case 'f':
                            advice.append('\f');
                            break;
                        case 'u':
                            if (i + 4 < currentJson.length()) {
                                try {
                                    String hex = currentJson.substring(i + 1, i + 5);
                                    char unicode = (char) Integer.parseInt(hex, 16);
                                    advice.append(unicode);
                                    i += 4; // 跳过 4 位十六进制数字
                                } catch (NumberFormatException e) {
                                    advice.append(c);
                                }
                            }
                            break;
                        default:
                            advice.append(c);
                    }
                    escaped = false;
                } else if (c == '\\') {
                    escaped = true;
                } else if (c == '"') {
                    // Advice 字段结束
                    completed = true;
                    break;
                } else {
                    advice.append(c);
                }
            }
            
            // 返回新增的部分
            String fullAdvice = advice.toString();
            if (fullAdvice.length() > processedLength) {
                String increment = fullAdvice.substring(processedLength);
                log.debug("提取 Advice 增量成功，已处理长度: {}, 当前总长度: {}, 增量长度: {}", 
                         processedLength, fullAdvice.length(), increment.length());
                return increment;
            }
            
        } catch (Exception e) {
            log.warn("提取 Advice 增量失败", e);
        }
        
        return null;
    }
    
    /**
     * 处理流式响应中的 schedule_cards，解析每一个从 "{" 到 "}" 的数据，转换成 AIScheduleVal 对象
     * 
     * 处理逻辑：
     * 1. 查找 "schedule_cards" 字段，定位数组开始位置 "["
     * 2. 逐个识别每个对象：以 "{" 为开始，以 "}" 为结束
     * 3. 提取完整的对象 JSON 字符串，转换成 AIScheduleVal 对象
     * 4. 封装为 SchedulePO 并保存，发送 SSE 事件
     * 5. valList 在多次调用之间累积，而不是每次都创建新的列表
     * 6. 发送顺序：先发送 "scheduleList:[", 然后逐个发送日程对象（对象间用 "," 分隔），最后发送 "]"
     * 
     * @param userId 用户ID
     * @param conversationId 会话ID
     * @param currentJson 当前累积的 JSON 字符串
     * @param processedScheduleCardsCount 已处理的 schedule_cards 数量
     * @param scheduleValListRef 用于累积 schedule_cards 的 valList 引用
     * @param scheduleListStartSent 是否已发送 "scheduleList:[" 的标记
     * @param schedulePOListRef 用于累积 schedulePO 的集合引用
     * @param emitter SSE 发射器
     */
    private void handleScheduleCardsStream(String userId, String conversationId, String currentJson,
                                          AtomicReference<Integer> processedScheduleCardsCount,
                                          AtomicReference<List<PACreateScheduleVal>> scheduleValListRef,
                                          AtomicReference<Boolean> scheduleListStartSent,
                                          AtomicReference<Boolean> scheduleListEndSent,
                                          AtomicReference<List<SchedulePO>> schedulePOListRef,
                                          SseEmitter emitter) {
        try {
            // 1. 查找 "schedule_cards" 字段的位置
            int scheduleCardsKeyStart = currentJson.indexOf("\"schedule_cards\"");
            if (scheduleCardsKeyStart == -1) {
                return; // 还没有 schedule_cards 字段
            }
            
            // 2. 查找数组开始位置 "["
            int arrayStart = currentJson.indexOf("[", scheduleCardsKeyStart);
            if (arrayStart == -1) {
                return; // 数组还未开始
            }
            
            // 3. 确定搜索范围：从数组开始位置到字符串末尾（流式数据可能还未完成）
            int searchStart = arrayStart + 1; // 从 "[" 后面开始搜索
            int searchEnd = currentJson.length();
            int processedCount = processedScheduleCardsCount.get();
            int foundObjectCount = 0; // 当前找到的对象数量
            
            // 4. 逐个解析从 "{" 到 "}" 的对象
            while (searchStart < searchEnd) {
                // 跳过空白字符和逗号
                while (searchStart < searchEnd && 
                       (Character.isWhitespace(currentJson.charAt(searchStart)) || 
                        currentJson.charAt(searchStart) == ',')) {
                    searchStart++;
                }
                
                if (searchStart >= searchEnd) {
                    break;
                }
                
                // 如果遇到 "]" 说明数组结束
                if (currentJson.charAt(searchStart) == ']') {
                    break;
                }
                
                // 查找对象的开始位置 "{"
                if (currentJson.charAt(searchStart) != '{') {
                    searchStart++;
                    continue;
                }
                
                int objectStart = searchStart;
                
                // 查找对象的结束位置 "}"
                int objectEnd = findObjectEndInJson(currentJson, objectStart, searchEnd);
                if (objectEnd == -1) {
                    // 对象还不完整，等待更多数据
                    break;
                }
                
                // 跳过已处理的对象
                foundObjectCount++;
                if (foundObjectCount <= processedCount) {
                    searchStart = objectEnd + 1;
                    continue;
                }
                
                // 5. 提取完整的对象 JSON（从 "{" 到 "}"）
                String objectJson = currentJson.substring(objectStart, objectEnd + 1);
                log.info("提取到完整的 schedule_card 对象 (第{}个): {}", foundObjectCount, objectJson);
                
                // 6. 解析成 AIScheduleVal 对象
                AIScheduleVal card = null;
                try {
                    card = JacksonUtil.json2Bean(objectJson, AIScheduleVal.class);
                    log.info("成功解析为 AIScheduleVal 对象: {}", card.getTitle());
                } catch (Exception e) {
                    log.warn("解析 schedule_card 对象失败: {}, 对象内容: {}", e.getMessage(), objectJson);
                    searchStart = objectEnd + 1;
                    continue;
                }
                
                // 检查 card 是否完整
                if (card == null || StringUtils.isBlank(card.getTitle())) {
                    log.debug("schedule_card 还不完整，跳过处理");
                    searchStart = objectEnd + 1;
                    continue;
                }
                
                // 7. 处理完整的 schedule_card：封装为 SchedulePO 并保存
                try {
                    // 封装为 SchedulePO
                    SchedulePO schedulePO = AIScheduleVal.convertAIScheduleVal2SchedulePO(userId, card);
                    
                    // 确定提醒类型
                    String remindType = card.getIsRemind() != null && card.getIsRemind() ? "PUSH" : null;
                    
                    // 提醒时间（如果启用提醒但没有指定时间，默认10分钟）
                    Integer remindBeforeMinutes = card.getRemindBeforeMinutes();
                    if (Boolean.TRUE.equals(card.getIsRemind()) && remindBeforeMinutes == null) {
                        remindBeforeMinutes = 10;
                    }
                    
                    // 调用 Domain 层创建日程
                    Long scheduleId = scheduleServiceDomain.createSchedule(schedulePO, remindBeforeMinutes, remindType, Boolean.FALSE);
                    schedulePO.setId(scheduleId);
                    
                    // 将 schedulePO 添加到集合中
                    List<SchedulePO> schedulePOList = schedulePOListRef.get();
                    schedulePOList.add(schedulePO);
                    
                    log.info("成功保存 schedule_card: {}, scheduleId: {}", card.getTitle(), scheduleId);
                    
                    // 封装参数，构建 SSE 事件数据
                    DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
                    PACreateScheduleVal val = new PACreateScheduleVal();
                    val.setScheduleId(String.valueOf(scheduleId));
                    val.setTitle(schedulePO.getTitle());
                    val.setIcon(schedulePO.getIcon());

                    val.setStartTime(DateUtils.localDateTimeToLong(schedulePO.getStartTime()));
                    val.setEndTime(DateUtils.localDateTimeToLong(schedulePO.getEndTime()));

                    val.setSubtitle(StringUtils.defaultString(schedulePO.getAuxiliaryInfo()));

                    // 在已有的 valList 基础上添加新的 val（累积）
                    List<PACreateScheduleVal> valList = scheduleValListRef.get();
                    valList.add(val);
                    
                    // 发送 SSE 事件的顺序：
                    // 1. 第一次发送 "scheduleList:["
                    if (!scheduleListStartSent.get()) {
                        ConversationCreateScheduleResponseDTO startResponse = buildConversationResponse(
                                conversationId, "", "{\"name\":\"ScheduleCreateCard\",\"type\":\"component\",\"data\":{\"scheduleList\":[", Boolean.FALSE, "card");
                        emitter.send(SseEmitter.event().data(startResponse).name("message"));
                        scheduleListStartSent.set(true);
                        log.info("发送 scheduleList:[");
                    }
                    
                    // 2. 如果不是第一个对象，先发送 ","
                    if (processedCount > 0) {
                        ConversationCreateScheduleResponseDTO commaResponse = buildConversationResponse(
                                conversationId, "", ",", Boolean.FALSE, "card");
                        emitter.send(SseEmitter.event().data(commaResponse).name("message"));
                        log.debug("发送逗号分隔符");
                    }
                    
                    // 3. 发送当前日程对象的 JSON
                    String valJson = JacksonUtil.bean2Json(val);
                    ConversationCreateScheduleResponseDTO valResponse = buildConversationResponse(
                            conversationId, "", valJson, Boolean.FALSE, "card");
                    emitter.send(SseEmitter.event().data(valResponse).name("message"));
                    log.info("发送日程对象: {} (第{}个)", card.getTitle(), foundObjectCount);
                    
                    // 更新已处理的数量
                    processedCount++;
                    processedScheduleCardsCount.set(processedCount);
                    searchStart = objectEnd + 1;
                    
                    // 4. 检查是否是最后一个对象（下一个字符是 "]"）
                    // 跳过空白字符，检查下一个字符是否是 "]"
                    int nextPos = objectEnd + 1;
                    while (nextPos < searchEnd && Character.isWhitespace(currentJson.charAt(nextPos))) {
                        nextPos++;
                    }
                    // 如果下一个字符是 "]"，说明数组已经结束，这是最后一个对象
                    if (nextPos < searchEnd && currentJson.charAt(nextPos) == ']') {
                        // 发送结束标记
                        if (!scheduleListEndSent.get()) {
                            ConversationCreateScheduleResponseDTO endResponse = buildConversationResponse(
                                    conversationId, "", "]}}", Boolean.TRUE, "card");
                            emitter.send(SseEmitter.event().data(endResponse).name("message"));
                            scheduleListEndSent.set(true);
                            log.info("发送 scheduleList 结束标记: ]}} (共 {} 个日程)", valList.size());
                        }
                    }
                    
                } catch (Exception e) {
                    log.error("处理 schedule_card 失败: {}", card.getTitle(), e);
                    searchStart = objectEnd + 1;
                    continue;
                }
            }
            
            // 5. 循环结束后，检查数组是否已经结束
            // 如果已经发送了开始标记，并且处理了至少一个对象，但还没有发送结束标记，则检查数组是否已结束
            if (!scheduleListEndSent.get() && scheduleListStartSent.get() && processedScheduleCardsCount.get() > 0) {
                // 查找数组结束位置 "]"（从数组开始位置之后查找）
                int arrayEndPos = currentJson.indexOf("]", arrayStart);
                if (arrayEndPos != -1) {
                    // 验证找到的 "]" 确实在数组范围内（在 arrayStart 之后）
                    // 并且确保这个 "]" 是数组的结束标记（前面应该有对象或空白字符）
                    boolean isValidArrayEnd = false;
                    // 从 arrayEndPos 向前查找，跳过空白字符，应该能找到对象结束的 "}" 或逗号
                    for (int i = arrayEndPos - 1; i >= arrayStart; i--) {
                        char c = currentJson.charAt(i);
                        if (c == '}') {
                            isValidArrayEnd = true;
                            break;
                        } else if (!Character.isWhitespace(c) && c != ',') {
                            break;
                        }
                    }
                    
                    if (isValidArrayEnd) {
                        ConversationCreateScheduleResponseDTO endResponse = buildConversationResponse(
                                conversationId, "", "]}}", Boolean.TRUE, "card");
                        emitter.send(SseEmitter.event().data(endResponse).name("message"));
                        scheduleListEndSent.set(true);
                        log.info("循环结束后发送 scheduleList 结束标记: ]}} (共 {} 个日程)", scheduleValListRef.get().size());
                    }
                }
            }
            
        } catch (Exception e) {
            log.warn("处理 schedule_cards 流式响应失败", e);
            // 不抛出异常，避免中断整个流式处理
        }
    }
    
    /**
     * 在 JSON 字符串中查找对象的结束位置
     * 
     * @param json JSON 字符串
     * @param objectStart 对象开始位置（必须是 '{'）
     * @param searchEnd 搜索结束位置
     * @return 对象结束位置（'}' 的位置），如果对象不完整返回 -1
     */
    private int findObjectEndInJson(String json, int objectStart, int searchEnd) {
        if (objectStart < 0 || objectStart >= json.length() || json.charAt(objectStart) != '{') {
            return -1;
        }
        
        int braceCount = 1;
        int i = objectStart + 1;
        boolean inString = false;
        boolean escaped = false;
        
        while (i < searchEnd && i < json.length()) {
            char c = json.charAt(i);
            
            if (escaped) {
                escaped = false;
                i++;
                continue;
            }
            
            if (c == '\\') {
                escaped = true;
                i++;
                continue;
            }
            
            if (c == '"') {
                inString = !inString;
                i++;
                continue;
            }
            
            if (inString) {
                i++;
                continue;
            }
            
            if (c == '{') {
                braceCount++;
            } else if (c == '}') {
                braceCount--;
                if (braceCount == 0) {
                    // 找到对象结束位置
                    return i;
                }
            }
            
            i++;
        }
        
        // 对象还不完整
        return -1;
    }
    
    /**
     * 提取 schedule_cards 数组部分
     * 
     * @param currentJson 当前累积的 JSON 字符串
     * @param arrayStart 数组开始位置
     * @return 数组部分的字符串，如果无法提取则返回 null
     */
    private String extractScheduleCardsArray(String currentJson, int arrayStart) {
        if (arrayStart < 0 || arrayStart >= currentJson.length()) {
            return null;
        }
        
        // 从数组开始位置查找数组结束位置
        int braceCount = 0;
        int bracketCount = 1; // 数组开始有一个 [
        int i = arrayStart + 1;
        boolean inString = false;
        boolean escaped = false;
        
        while (i < currentJson.length()) {
            char c = currentJson.charAt(i);
            
            if (escaped) {
                escaped = false;
                i++;
                continue;
            }
            
            if (c == '\\') {
                escaped = true;
                i++;
                continue;
            }
            
            if (c == '"') {
                inString = !inString;
                i++;
                continue;
            }
            
            if (inString) {
                i++;
                continue;
            }
            
            if (c == '{') {
                braceCount++;
            } else if (c == '}') {
                braceCount--;
            } else if (c == '[') {
                bracketCount++;
            } else if (c == ']') {
                bracketCount--;
                if (bracketCount == 0) {
                    // 找到数组结束位置
                    return currentJson.substring(arrayStart, i + 1);
                }
            }
            
            i++;
        }
        
        // 数组还未完整，返回当前能提取的部分
        return currentJson.substring(arrayStart);
    }
    
    /**
     * 查找下一个对象的开始位置（跳过空白和逗号）
     * 
     * @param arrayPart 数组部分字符串
     * @param start 开始搜索的位置
     * @return 对象开始位置，如果没找到返回 -1
     */
    private int findNextObjectStart(String arrayPart, int start) {
        if (start < 0 || start >= arrayPart.length()) {
            return -1;
        }
        
        // 跳过空白字符和逗号
        int i = start;
        while (i < arrayPart.length()) {
            char c = arrayPart.charAt(i);
            if (c == '{') {
                return i;
            } else if (!Character.isWhitespace(c) && c != ',') {
                // 遇到非空白非逗号字符，说明格式有问题
                return -1;
            }
            i++;
        }
        
        return -1;
    }
    
    /**
     * 查找对象的结束位置
     * 
     * @param arrayPart 数组部分字符串
     * @param objectStart 对象开始位置
     * @return 对象结束位置，如果对象不完整返回 -1
     */
    private int findObjectEnd(String arrayPart, int objectStart) {
        if (objectStart < 0 || objectStart >= arrayPart.length() || arrayPart.charAt(objectStart) != '{') {
            return -1;
        }
        
        int braceCount = 1;
        int i = objectStart + 1;
        boolean inString = false;
        boolean escaped = false;
        
        while (i < arrayPart.length()) {
            char c = arrayPart.charAt(i);
            
            if (escaped) {
                escaped = false;
                i++;
                continue;
            }
            
            if (c == '\\') {
                escaped = true;
                i++;
                continue;
            }
            
            if (c == '"') {
                inString = !inString;
                i++;
                continue;
            }
            
            if (inString) {
                i++;
                continue;
            }
            
            if (c == '{') {
                braceCount++;
            } else if (c == '}') {
                braceCount--;
                if (braceCount == 0) {
                    // 找到对象结束位置
                    return i;
                }
            }
            
            i++;
        }
        
        // 对象还不完整
        return -1;
    }
    
    /**
     * 保存AI创建的日程到数据库
     *
     * @param userId 用户ID
     * @param aiCreateResponse AI创建日程响应
     * @throws Exception 保存失败时抛出异常
     */
    private List<SchedulePO> saveSchedulesFromAI(String userId, AICreateScheduleResponseDTO aiCreateResponse) throws Exception {
        List<SchedulePO> savedSchedules = new ArrayList<>();
        
        if (aiCreateResponse == null || aiCreateResponse.getScheduleCards() == null || 
            aiCreateResponse.getScheduleCards().isEmpty()) {
            log.warn("AI响应中没有日程卡片，跳过保存");
            return savedSchedules;
        }
        
        List<AIScheduleVal> scheduleCards = aiCreateResponse.getScheduleCards();
        log.info("开始保存 {} 个AI创建的日程", scheduleCards.size());
        
        for (int i = 0; i < scheduleCards.size(); i++) {
            AIScheduleVal card = scheduleCards.get(i);

            // 转换 ScheduleCardVal 为 SchedulePO
            SchedulePO schedulePO = AIScheduleVal.convertAIScheduleVal2SchedulePO(userId, card);

            // 确定提醒类型
            String remindType = card.getIsRemind() != null && card.getIsRemind() ? "PUSH" : null;

            // 提醒时间（如果启用提醒但没有指定时间，默认10分钟）
            Integer remindBeforeMinutes = card.getRemindBeforeMinutes();
            if (Boolean.TRUE.equals(card.getIsRemind()) && remindBeforeMinutes == null) {
                remindBeforeMinutes = 10;
            }

            // 调用 Domain 层创建日程，返回生成的 scheduleId
            Long scheduleId = scheduleServiceDomain.createSchedule(schedulePO, remindBeforeMinutes, remindType, Boolean.FALSE);

            savedSchedules.add(schedulePO);
        }
        
        return savedSchedules;
    }

    /**
     * Build SchedulePO from CreateScheduleRequestDTO
     *
     * @param request Create schedule request DTO
     * @return SchedulePO
     */
    private SchedulePO buildSchedulePOFromRequest(CreateScheduleRequestDTO request) {
        SchedulePO schedulePO = new SchedulePO();
        schedulePO.setUserId(request.getUserId());
        schedulePO.setTitle(request.getTitle());
        schedulePO.setType(request.getType());
        
        // Convert Date to LocalDateTime
        schedulePO.setStartTime(convertDateToLocalDateTime(request.getStartTime()));
        schedulePO.setEndTime(convertDateToLocalDateTime(request.getEndTime()));
        
        // Repeat settings (default false if not specified)
        // schedulePO.setIsRepeat(Optional.ofNullable(request.getIsRepeat()).orElse(Boolean.FALSE));
        // schedulePO.setRepeatTime(request.getRepeatTime());
        // schedulePO.setRepeatUnit(request.getRepeatUnit());
        
        // Other fields
        schedulePO.setEnergy(request.getEnergy());
        schedulePO.setAuxiliaryInfo(request.getAuxiliaryInfo());
        schedulePO.setIsRemind(Optional.ofNullable(request.getIsRemind()).orElse(Boolean.FALSE));
        schedulePO.setIsCreate(Boolean.TRUE);
        
        return schedulePO;
    }

    /**
     * Convert Date to LocalDateTime
     *
     * @param date Date object
     * @return LocalDateTime
     */
    private LocalDateTime convertDateToLocalDateTime(Date date) {
        return Optional.ofNullable(date)
                .map(d -> d.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime())
                .orElse(null);
    }

    /**
     * Convert timestamp to LocalDate
     *
     * @param timestamp Timestamp in milliseconds
     * @return LocalDate
     */
    private LocalDate convertTimestampToLocalDate(Long timestamp) {
        return Optional.ofNullable(timestamp)
                .map(ts -> new Date(ts).toInstant().atZone(ZoneId.systemDefault()).toLocalDate())
                .orElse(null);
    }

    /**
     * Convert LocalDateTime to Date
     *
     * @param localDateTime LocalDateTime object
     * @return Date
     */
    private Date convertLocalDateTimeToDate(LocalDateTime localDateTime) {
        return Optional.ofNullable(localDateTime)
                .map(time -> Date.from(time.atZone(ZoneId.systemDefault()).toInstant()))
                .orElse(null);
    }

    /**
     * Convert isRemind Boolean to remind type String
     *
     * @param isRemind Whether to remind
     * @return Remind type string
     */
    private String convertRemindType(Boolean isRemind) {
        return Optional.ofNullable(isRemind).orElse(Boolean.FALSE) ? "system" : null;
    }

    /**
     * Convert PO list to ScheduleResponseDTO with daily briefing
     *
     * @param scheduleList Schedule PO list
     * @param date         Query date
     * @return ScheduleResponseDTO
     */
    private ScheduleResponseDTO convertToScheduleResponse(List<SchedulePO> scheduleList, LocalDate date, String userId) {
        ScheduleResponseDTO responseDTO = new ScheduleResponseDTO();
        
        // Generate daily briefing
        DailyBriefingVal dailyBriefing = generateDailyBriefing(date, userId, scheduleList);
        responseDTO.setDailyBriefing(dailyBriefing);
        
        // Convert PO list to ScheduleVal list
        List<ScheduleVal> scheduleValList = scheduleList.stream()
                .map(this::convertToScheduleVal)
                .collect(Collectors.toList());
        responseDTO.setScheduleList(scheduleValList);

        // 按类型加载运动/会议/观影/航班/火车等 data 并填入各 ScheduleVal
        fillScheduleData(scheduleList, scheduleValList);
        
        return responseDTO;
    }

    /**
     * 按日程类型批量查询附属表，将 data 填入 scheduleValList
     */
    private void fillScheduleData(List<SchedulePO> scheduleList, List<ScheduleVal> scheduleValList) {
        if (CollectionUtils.isEmpty(scheduleList)) {
            return;
        }
        Map<Long, Object> scheduleIdToData = new HashMap<>();
        List<Long> meetingIds = scheduleList.stream().filter(s -> TypeEnum.MEETING.getCode().equals(s.getType())).map(SchedulePO::getId).collect(Collectors.toList());
        List<Long> sportIds = scheduleList.stream().filter(s -> TypeEnum.SPORT.getCode().equals(s.getType())).map(SchedulePO::getId).collect(Collectors.toList());
        List<Long> movieIds = scheduleList.stream().filter(s -> TypeEnum.MOVIE.getCode().equals(s.getType())).map(SchedulePO::getId).collect(Collectors.toList());
        List<Long> flightIds = scheduleList.stream().filter(s -> TypeEnum.FLIGHT.getCode().equals(s.getType())).map(SchedulePO::getId).collect(Collectors.toList());
        List<Long> trainIds = scheduleList.stream().filter(s -> TypeEnum.TRAIN.getCode().equals(s.getType())).map(SchedulePO::getId).collect(Collectors.toList());

        if (!meetingIds.isEmpty()) {
            List<ScheduleMeetingPO> meetings = scheduleMeetingMapper.selectList(new LambdaQueryWrapper<ScheduleMeetingPO>().in(ScheduleMeetingPO::getScheduleId, meetingIds));
            for (ScheduleMeetingPO po : meetings) {
                scheduleIdToData.put(po.getScheduleId(), meetingPOToVal(po));
            }
        }
        if (!sportIds.isEmpty()) {
            List<ScheduleSportPO> sports = scheduleSportMapper.selectList(new LambdaQueryWrapper<ScheduleSportPO>().in(ScheduleSportPO::getScheduleId, sportIds));
            for (ScheduleSportPO po : sports) {
                scheduleIdToData.put(po.getScheduleId(), sportPOToVal(po));
            }
        }
        if (!movieIds.isEmpty()) {
            List<ScheduleMoviePO> movies = scheduleMovieMapper.selectList(new LambdaQueryWrapper<ScheduleMoviePO>().in(ScheduleMoviePO::getScheduleId, movieIds));
            Map<Long, SchedulePO> idToSchedule = scheduleList.stream().collect(Collectors.toMap(SchedulePO::getId, s -> s, (a, b) -> a));
            for (ScheduleMoviePO po : movies) {
                SchedulePO schedulePO = idToSchedule.get(po.getScheduleId());
                scheduleIdToData.put(po.getScheduleId(), moviePOToVal(po, schedulePO != null ? schedulePO.getTitle() : null));
            }
        }
        if (!flightIds.isEmpty()) {
            List<ScheduleFlightPO> flights = scheduleFlightMapper.selectList(new LambdaQueryWrapper<ScheduleFlightPO>().in(ScheduleFlightPO::getScheduleId, flightIds));
            for (ScheduleFlightPO po : flights) {
                scheduleIdToData.putIfAbsent(po.getScheduleId(), flightPOToVal(po));
            }
        }
        if (!trainIds.isEmpty()) {
            List<ScheduleTrainPO> trains = scheduleTrainMapper.selectList(new LambdaQueryWrapper<ScheduleTrainPO>().in(ScheduleTrainPO::getScheduleId, trainIds));
            for (ScheduleTrainPO po : trains) {
                scheduleIdToData.putIfAbsent(po.getScheduleId(), trainPOToVal(po));
            }
        }

        for (ScheduleVal val : scheduleValList) {
            if (val.getScheduleId() != null && scheduleIdToData.containsKey(Long.valueOf(val.getScheduleId()))) {
                val.setData(scheduleIdToData.get(Long.valueOf(val.getScheduleId())));
            }
        }
    }

    private com.hojoai.omniasst.focus.dto.val.MeetingVal meetingPOToVal(ScheduleMeetingPO po) {
        com.hojoai.omniasst.focus.dto.val.MeetingVal val = new com.hojoai.omniasst.focus.dto.val.MeetingVal();
        val.setParticipant(po.getParticipant());
        val.setMeetingId(po.getMeetingId());
        val.setMeetingTitle(po.getMeetingTitle());
        val.setMeetingUrl(po.getMeetingUrl());
        return val;
    }

    private SportQueryVal sportPOToVal(ScheduleSportPO po) {
        SportQueryVal val = new SportQueryVal();
        if (po.getStartTime() != null) {
            val.setStartTime(convertLocalDateTimeToDate(po.getStartTime()));
        }
        if (po.getEndTime() != null) {
            val.setEndTime(convertLocalDateTimeToDate(po.getEndTime()));
        }
        val.setHeartRate(po.getHeartRate());
        return val;
    }

    private com.hojoai.omniasst.focus.dto.val.MovieVal moviePOToVal(ScheduleMoviePO po, String movieName) {
        com.hojoai.omniasst.focus.dto.val.MovieVal val = new com.hojoai.omniasst.focus.dto.val.MovieVal();
        val.setMovieName(movieName);
        val.setCinemaInfo(po.getCinemaInfo());
        val.setSeatNo(po.getSeatNo());
        val.setPicUrl(po.getPicUrl());
        val.setDuration(po.getDuration());
        if (po.getStartTime() != null) {
            val.setStartTime(convertLocalDateTimeToDate(po.getStartTime()));
        }
        if (po.getEndTime() != null) {
            val.setEndTime(convertLocalDateTimeToDate(po.getEndTime()));
        }
        return val;
    }

    private com.hojoai.omniasst.focus.dto.val.FlightVal flightPOToVal(ScheduleFlightPO po) {
        com.hojoai.omniasst.focus.dto.val.FlightVal val = new com.hojoai.omniasst.focus.dto.val.FlightVal();
        val.setFlightNo(po.getFlightNo());
        val.setFlightType(po.getFlightType());
        val.setSeatNo(po.getSeatNo());
        val.setBoardingGate(po.getBoardingGate());
        val.setCategory(po.getCategory());
        val.setCompany(po.getCompany());
        val.setDepTimezone(po.getDepTimezone());
        val.setArrTimezone(po.getArrTimezone());
        val.setDepCode(po.getDepCode());
        val.setArrCode(po.getArrCode());
        val.setDepAirport(po.getDepAirport());
        val.setArrAirport(po.getArrAirport());
        val.setDepCity(po.getDepCity());
        val.setArrCity(po.getArrCity());
        val.setDepTerminal(po.getDepTerminal());
        val.setArrTerminal(po.getArrTerminal());
        val.setDepPlanTime(convertLocalDateTimeToDate(po.getDepPlanTime()));
        val.setArrPlanTime(convertLocalDateTimeToDate(po.getArrPlanTime()));
        val.setDepReadyTime(convertLocalDateTimeToDate(po.getDepReadyTime()));
        val.setArrReadyTime(convertLocalDateTimeToDate(po.getArrReadyTime()));
        val.setDepTime(convertLocalDateTimeToDate(po.getDepTime()));
        val.setArrTime(convertLocalDateTimeToDate(po.getArrTime()));
        val.setState(po.getState());
        val.setStopFlag(po.getStopFlag());
        val.setDuration(po.getDuration());
        val.setOntimeRate(po.getOntimeRate());
        return val;
    }

    private com.hojoai.omniasst.focus.dto.val.TrainVal trainPOToVal(ScheduleTrainPO po) {
        com.hojoai.omniasst.focus.dto.val.TrainVal val = new com.hojoai.omniasst.focus.dto.val.TrainVal();
        val.setTrainNo(po.getTrainNo());
        val.setSeatNo(po.getSeatNo());
        val.setTicketGate(po.getTicketGate());
        val.setDepStan(po.getDepStan());
        val.setArrStan(po.getArrStan());
        val.setDepCode(po.getDepCode());
        val.setArrCode(po.getArrCode());
        val.setDuration(po.getDuration());
        val.setDepPlanTime(convertLocalDateTimeToDate(po.getDepPlanTime()));
        val.setArrPlanTime(convertLocalDateTimeToDate(po.getArrPlanTime()));
        return val;
    }

    /**
     * Convert SchedulePO to ScheduleVal
     *
     * @param schedulePO Schedule PO
     * @return ScheduleVal
     */
    private ScheduleVal convertToScheduleVal(SchedulePO schedulePO) {
        ScheduleVal scheduleVal = new ScheduleVal();
        
        scheduleVal.setScheduleId(String.valueOf(schedulePO.getId()));
        scheduleVal.setScheduleRuleId(String.valueOf(schedulePO.getRuleId()));
        scheduleVal.setTitle(schedulePO.getTitle());
        scheduleVal.setType(schedulePO.getType());
        
        // Convert LocalDateTime to Date
        scheduleVal.setStartTime(convertLocalDateTimeToDate(schedulePO.getStartTime()));
        scheduleVal.setEndTime(convertLocalDateTimeToDate(schedulePO.getEndTime()));
        
        scheduleVal.setStatus(schedulePO.getStatus());
        scheduleVal.setIcon(schedulePO.getIcon());
        scheduleVal.setTag(schedulePO.getTag());
        scheduleVal.setIsSysSync(1 == schedulePO.getIsSysSync());
        
        scheduleVal.setEnergy(schedulePO.getEnergy());
        scheduleVal.setAuxiliaryInfo(schedulePO.getAuxiliaryInfo());
        scheduleVal.setAiSuggestion(schedulePO.getAiSuggestion());
        scheduleVal.setIsRemind(Boolean.TRUE.equals(schedulePO.getIsRemind()));
        scheduleVal.setHasConflict(Boolean.TRUE.equals(schedulePO.getIsConflict()));
        scheduleVal.setConflictSuggestion(schedulePO.getConflictSuggestion());
        
        // Query conflict schedule ID list if has conflict
        if (Boolean.TRUE.equals(scheduleVal.getHasConflict())) {
            List<Long> conflictIds = scheduleServiceDomain.listConflictScheduleIds(schedulePO.getId());
            scheduleVal.setConflictScheduleIdList(conflictIds);
        }
        
        return scheduleVal;
    }

    /**
     * Generate daily briefing
     *
     * @param date         Date
     * @param userId       User ID
     * @param scheduleList Schedule list (to avoid redundant database queries)
     * @return DailyBriefingVal
     */
    private DailyBriefingVal generateDailyBriefing(LocalDate date, String userId, List<SchedulePO> scheduleList) {
        DailyBriefingVal briefingVal = new DailyBriefingVal();
        
        // Query daily briefing from Domain layer
        DailyBriefingPO dailyBriefingPO = scheduleServiceDomain.queryDailyBriefing(userId, date);
        
        if (dailyBriefingPO != null) {
            briefingVal.setTitle(dailyBriefingPO.getTitle());
            briefingVal.setTips(dailyBriefingPO.getTips());
            briefingVal.setSummaryText(dailyBriefingPO.getSummaryText());
            briefingVal.setDescription(dailyBriefingPO.getDescription());
            briefingVal.setIcon(dailyBriefingPO.getIcon());
            
            // Find matching schedule from scheduleList by scheduleId
            if (dailyBriefingPO.getScheduleId() != null) {
                scheduleList.stream()
                        .filter(schedule -> schedule.getId().equals(dailyBriefingPO.getScheduleId()))
                        .findFirst()
                        .ifPresent(schedulePO -> briefingVal.setType(schedulePO.getType()));
            }

        }

        if (CollectionUtils.isEmpty(scheduleList)) {
            briefingVal.setBackground(BackgroundEnum.EMPTY.getCode());
            return briefingVal;
        }

        if (scheduleList.stream().anyMatch(schedulePO -> TagEnum.CONFLICT.getCode().equals(schedulePO.getTag()))) {
            briefingVal.setBackground(BackgroundEnum.CONFLICT.getCode());
            return briefingVal;
        }

        if (scheduleList.stream().anyMatch(schedulePO -> TagEnum.FOCUS.getCode().equals(schedulePO.getTag()))) {
            briefingVal.setBackground(BackgroundEnum.FOCUS.getCode());
            return briefingVal;
        }

        briefingVal.setBackground(BackgroundEnum.SCHEDULE.getCode());

        return briefingVal;
    }

    /**
     * Convert SchedulePO and ScheduleDetailPO list to ScheduleDetailResponseDTO（与 ScheduleDetailResponseDTO 字段一致）
     * <p>
     * 类型明细与参与人等均来自 {@code scheduleDataList}（策略 {@link ScheduleStrategy#getScheduleData(Long)}），
     * 不再重复查询各 ScheduleXxx 附属表。
     *
     * @param schedulePO         Schedule PO
     * @param scheduleDetailList Schedule detail list
     * @param locationPO         位置（可为 null）
     * @param rulePO             规则（重复、提醒单位等，可为 null）
     * @param scheduleDataList   策略层按 scheduleId 查询的明细列表（组装 data、会议参与人等）
     * @param attachmentList     日程附件列表（可为 null）
     * @return ScheduleDetailResponseDTO
     */
    private ScheduleDetailResponseDTO convertToScheduleDetailResponse(SchedulePO schedulePO, List<ScheduleDetailPO> scheduleDetailList,
                                                                       ScheduleLocationPO locationPO,
                                                                       ScheduleRulePO rulePO, List<?> scheduleDataList,
                                                                       List<ScheduleAttachmentPO> attachmentList) {
        ScheduleDetailResponseDTO responseDTO = new ScheduleDetailResponseDTO();

        responseDTO.setScheduleId(String.valueOf(schedulePO.getId()));
        if (schedulePO.getRuleId() != null) {
            responseDTO.setScheduleRuleId(String.valueOf(schedulePO.getRuleId()));
        }
        responseDTO.setIsSysSync(1 == schedulePO.getIsSysSync());
        responseDTO.setTitle(schedulePO.getTitle());
        responseDTO.setType(schedulePO.getType());
        responseDTO.setStartTime(convertLocalDateTimeToDate(schedulePO.getStartTime()));
        responseDTO.setEndTime(convertLocalDateTimeToDate(schedulePO.getEndTime()));
        responseDTO.setStatus(schedulePO.getStatus());
        responseDTO.setTag(schedulePO.getTag());
        responseDTO.setEnergy(schedulePO.getEnergy());
        responseDTO.setIcon(schedulePO.getIcon());
        responseDTO.setAuxiliaryInfo(schedulePO.getAuxiliaryInfo());
        responseDTO.setRemark(schedulePO.getRemark());

        // 重复：来自规则
        boolean isRepeat = rulePO != null && Boolean.TRUE.equals(rulePO.getIsRepeat());
        responseDTO.setIsRepeat(isRepeat);
        if (isRepeat) {
            responseDTO.setRepeatTime(parseRepeatTimeTokenList(rulePO.getRepeatTime()));
            responseDTO.setRepeatUnit(rulePO.getRepeatUnit());
        }

        // 提醒：日程或规则任一为 true 视为开启；时间与单位均取规则字段
        boolean isRemind = Boolean.TRUE.equals(schedulePO.getIsRemind())
                || (rulePO != null && Boolean.TRUE.equals(rulePO.getIsRemind()));
        responseDTO.setIsRemind(isRemind);
        if (isRemind && rulePO != null && rulePO.getRemindTime() != null) {
            responseDTO.setRemindTime(rulePO.getRemindTime());
            responseDTO.setRemindUnit(rulePO.getRemindUnit());
        }

        // 是否创建 AI 建议：优先规则 isCreateSug，否则日程 isAiSuggestion
        if (rulePO != null && rulePO.getIsCreateSug() != null) {
            responseDTO.setCreateAiSuggestion(rulePO.getIsCreateSug() == 1);
        } else {
            responseDTO.setCreateAiSuggestion(schedulePO.getIsAiSuggestion());
        }

        responseDTO.setParticipant(buildParticipantsString(firstMeetingPoFromScheduleData(scheduleDataList), rulePO));
        responseDTO.setLocationInfo(buildLocationInfoVal(locationPO));

        // AI 详情
        if (schedulePO.getAiDetailType() != null || schedulePO.getAiDetailTitle() != null
                || (scheduleDetailList != null && !scheduleDetailList.isEmpty())) {
            AiDetailVal aiDetail = new AiDetailVal();
            aiDetail.setAiType(schedulePO.getAiDetailType());
            aiDetail.setTitle(schedulePO.getAiDetailTitle());
            List<String> highLightList = scheduleDetailList == null ? Collections.emptyList() :
                    scheduleDetailList.stream()
                            .map(ScheduleDetailPO::getHighLight)
                            .filter(Objects::nonNull)
                            .collect(Collectors.toList());
            aiDetail.setHighLightList(highLightList);
            responseDTO.setAiDetail(aiDetail);
        }

        responseDTO.setData(buildScheduleDetailData(schedulePO.getType(), scheduleDataList, schedulePO));

        // 附件列表
        if (CollectionUtils.isNotEmpty(attachmentList)) {
            List<FileVal> fileValList = attachmentList.stream().map(att -> {
                FileVal fv = new FileVal();
                fv.setUid(att.getUid());
                fv.setName(att.getName());
                fv.setStatus(att.getStatus());
                fv.setSize(att.getSize());
                fv.setType(att.getType());
                fv.setUrl(att.getUrl());
                fv.setThumbUrl(att.getThumbUrl());
                return fv;
            }).collect(Collectors.toList());
            responseDTO.setFileList(fileValList);
        }

        return responseDTO;
    }

    /** 从策略返回的明细列表中取第一条会议 PO（用于 participant） */
    private ScheduleMeetingPO firstMeetingPoFromScheduleData(List<?> scheduleDataList) {
        if (CollectionUtils.isEmpty(scheduleDataList)) {
            return null;
        }
        for (Object o : scheduleDataList) {
            if (o instanceof ScheduleMeetingPO) {
                return (ScheduleMeetingPO) o;
            }
        }
        return null;
    }

    /**
     * 规则中的 repeatTime 字符串拆成列表（如周重复 "1,3" -> ["1","3"]）
     */
    private List<String> parseRepeatTimeTokenList(String repeatTime) {
        if (StringUtils.isBlank(repeatTime)) {
            return null;
        }
        List<String> list = new ArrayList<>();
        for (String p : repeatTime.split(",")) {
            String t = p.trim();
            if (StringUtils.isNotBlank(t)) {
                list.add(t);
            }
        }
        return list.isEmpty() ? null : list;
    }

    /** 参与人：优先会议 PO，否则规则参与人 */
    private String buildParticipantsString(ScheduleMeetingPO meetingPO, ScheduleRulePO rulePO) {
        if (meetingPO != null && StringUtils.isNotBlank(meetingPO.getParticipant())) {
            return meetingPO.getParticipant();
        }
        if (rulePO != null && StringUtils.isNotBlank(rulePO.getParticipant())) {
            return rulePO.getParticipant();
        }
        return null;
    }

    /** 组装 LocationInfoVal（与 focus_schedule_location 字段对应） */
    private LocationInfoVal buildLocationInfoVal(ScheduleLocationPO locationPO) {
        if (locationPO == null) {
            return null;
        }
        if (StringUtils.isBlank(locationPO.getLocationName()) && StringUtils.isBlank(locationPO.getLocationInfo())
                && locationPO.getLatitude() == null && locationPO.getLongitude() == null
                && StringUtils.isBlank(locationPO.getProvince()) && StringUtils.isBlank(locationPO.getCity())
                && StringUtils.isBlank(locationPO.getDistrict()) && StringUtils.isBlank(locationPO.getTownship())) {
            return null;
        }
        LocationInfoVal val = new LocationInfoVal();
        val.setName(locationPO.getLocationName());
        val.setLocation(locationPO.getLocationInfo());
        val.setLongitude(locationPO.getLongitude());
        val.setLatitude(locationPO.getLatitude());
        val.setProvince(locationPO.getProvince());
        val.setCity(locationPO.getCity());
        val.setDistrict(locationPO.getDistrict());
        val.setTownship(locationPO.getTownship());
        return val;
    }

    /**
     * 各类型日程 data：仅使用策略 {@link ScheduleStrategy#getScheduleData(Long)} 返回的 scheduleDataList
     */
    private Object buildScheduleDetailData(Integer type, List<?> scheduleDataList, SchedulePO schedulePO) {
        if (CollectionUtils.isEmpty(scheduleDataList)) {
            return null;
        }
        if (TypeEnum.MEETING.getCode().equals(type)) {
            for (Object o : scheduleDataList) {
                if (o instanceof ScheduleMeetingPO) {
                    return meetingPOToVal((ScheduleMeetingPO) o);
                }
            }
        } else if (TypeEnum.SPORT.getCode().equals(type)) {
            for (Object o : scheduleDataList) {
                if (o instanceof ScheduleSportPO) {
                    return sportPOToVal((ScheduleSportPO) o);
                }
            }
        } else if (TypeEnum.MOVIE.getCode().equals(type)) {
            for (Object o : scheduleDataList) {
                if (o instanceof ScheduleMoviePO) {
                    return moviePOToVal((ScheduleMoviePO) o, schedulePO.getTitle());
                }
            }
        } else if (TypeEnum.FLIGHT.getCode().equals(type)) {
            List<FlightVal> vals = new ArrayList<>();
            for (Object o : scheduleDataList) {
                if (o instanceof ScheduleFlightPO) {
                    vals.add(flightPOToVal((ScheduleFlightPO) o));
                }
            }
            if (!vals.isEmpty()) {
                return vals.size() == 1 ? vals.get(0) : vals;
            }
        } else if (TypeEnum.TRAIN.getCode().equals(type)) {
            List<TrainVal> vals = new ArrayList<>();
            for (Object o : scheduleDataList) {
                if (o instanceof ScheduleTrainPO) {
                    vals.add(trainPOToVal((ScheduleTrainPO) o));
                }
            }
            if (!vals.isEmpty()) {
                return vals.size() == 1 ? vals.get(0) : vals;
            }
        }
        return null;
    }

    @Override
    public String deleteSchedule(Long scheduleId, String deleteType, DeleteScheduleRequestDTO request) {
        if (deleteType == null) {
            return null;
        }
        // 1-当前日程
        if (ModifyTypeEnum.CURRENT.getCode().equals(deleteType)) {
            SchedulePO po = scheduleDomainService.getByUserIdAndScheduleId(request.getUserId(), scheduleId);
            if (po == null) {
                return null;
            }
            scheduleDomainService.deleteScheduleList(Collections.singletonList(po));

            scheduleAsyncDomainService.dailyBriefingHandle(request.getUserId(), po.getStartTime().toLocalDate(), TagEnum.FOCUS);

            return Objects.nonNull(po.getIsSysSync()) && 1 == po.getIsSysSync() ? String.valueOf(po.getRuleId()) : null;
        }
        // 先查出当前日程，2/3 依赖 ruleId 与 startTime
        SchedulePO current = scheduleDomainService.getByUserIdAndScheduleId(request.getUserId(), scheduleId);
        if (current == null) {
            return null;
        }
        Long ruleId = current.getRuleId();
        // 若当前日程无规则（rule_id 为 null），2 和 3 均只删当前一条
        if (ruleId == null) {
            scheduleDomainService.deleteScheduleList(Collections.singletonList(current));
            return null;
        }
        // 2-当前及后续日程：同规则且 start_time >= 当前 start_time
        if (ModifyTypeEnum.AFTER.getCode().equals(deleteType)) {
            List<SchedulePO> list = scheduleDomainService.listByUserIdAndRuleIdFromTime(
                    request.getUserId(), ruleId, current.getStartTime());
            if (CollectionUtils.isEmpty(list)) {
                return null;
            }
            scheduleDomainService.deleteScheduleList(list);

            list.stream()
                    .map(po -> po.getStartTime().toLocalDate())
                    .distinct()
                    .forEach(date -> scheduleAsyncDomainService.dailyBriefingHandle(request.getUserId(), date, TagEnum.FOCUS));

            ScheduleRulePO rulePO = scheduleRuleDomainService.getScheduleRuleById(ruleId);
            if (rulePO != null) {
                rulePO.setRuleEndTime(current.getStartTime());
                scheduleRuleDomainService.updateScheduleRule(rulePO);
            }

            return Objects.nonNull(list.getFirst().getIsSysSync()) && 1 == list.getFirst().getIsSysSync() ? String.valueOf(ruleId) : null;
        }
        // 3-所有日程：同规则下全部
        if (ModifyTypeEnum.ALL.getCode().equals(deleteType)) {
            List<SchedulePO> list = scheduleDomainService.listByUserIdAndRuleId(request.getUserId(), ruleId);
            if (CollectionUtils.isEmpty(list)) {
                return null;
            }
            scheduleDomainService.deleteScheduleList(list);

            list.stream()
                    .map(po -> po.getStartTime().toLocalDate())
                    .distinct()
                    .forEach(date -> scheduleAsyncDomainService.dailyBriefingHandle(request.getUserId(), date, TagEnum.FOCUS));

            ScheduleRulePO rulePO = scheduleRuleDomainService.getScheduleRuleById(ruleId);
            if (rulePO != null) {
                rulePO.setIsDelete(true);
                scheduleRuleDomainService.updateScheduleRule(rulePO);
            }

            return Objects.nonNull(list.getFirst().getIsSysSync()) && 1 == list.getFirst().getIsSysSync() ? String.valueOf(ruleId) : null;
        }
        log.warn("deleteSchedule: 不支持的 deleteType={}", deleteType);
        return null;
    }

}
