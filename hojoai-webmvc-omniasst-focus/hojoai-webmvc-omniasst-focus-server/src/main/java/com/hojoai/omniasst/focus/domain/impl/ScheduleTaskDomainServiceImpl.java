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

import com.hojoai.central.provider.service.common.constants.RedisConstants;
import com.hojoai.central.provider.service.utils.EnumUtil;
import com.hojoai.common.jackson.JacksonUtil;
import com.hojoai.omniasst.focus.common.constants.ScheduleConstants;
import com.hojoai.omniasst.focus.common.enums.TagEnum;
import com.hojoai.omniasst.focus.common.enums.TypeEnum;
import com.hojoai.omniasst.focus.common.enums.UrlEnum;
import com.hojoai.omniasst.focus.domain.AIDomainService;
import com.hojoai.omniasst.focus.domain.AIHandleDomainService;
import com.hojoai.omniasst.focus.domain.ScheduleDomainService;
import com.hojoai.omniasst.focus.domain.ScheduleTaskDomainService;
import com.hojoai.omniasst.focus.dto.request.*;
import com.hojoai.omniasst.focus.dto.response.*;
import com.hojoai.omniasst.focus.dto.val.AIConflictVal;
import com.hojoai.omniasst.focus.dto.val.AIDailyBriefingCardVal;
import com.hojoai.omniasst.focus.dto.val.AIScheduleCommonVal;
import com.hojoai.omniasst.focus.dto.val.AIScheduleVal;
import com.hojoai.omniasst.focus.repository.DailyBriefingRepository;
import com.hojoai.omniasst.focus.repository.ScheduleRepository;
import com.hojoai.omniasst.focus.repository.po.DailyBriefingPO;
import com.hojoai.omniasst.focus.repository.po.SchedulePO;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * AI Schedule Analysis Service
 * 处理AI日程分析定时任务
 *
 * @author My
 * @version 1.0 2026/01/11
 * @since hojoai-webmvc-omniasst-focus 1.0.0
 */
@Service
public class ScheduleTaskDomainServiceImpl implements ScheduleTaskDomainService {

    private static final Logger log = LoggerFactory.getLogger(ScheduleTaskDomainServiceImpl.class);

    @Resource
    private ScheduleRepository scheduleRepository;

    @Resource
    private DailyBriefingRepository dailyBriefingRepository;

    @Resource
    private AIDomainService aiDomainService;

    @Resource
    private ScheduleDomainService scheduleServiceDomain;

    @Resource
    private AIHandleDomainService aiHandleDomainService;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    private static final String REDIS_KEY = "redis_userId:";

    @Override
    public void executeSystemAIScheduleFreeSuggestion(LocalDate today) {
        this.taskHandle(null, today, TagEnum.SUGGESTION);
    }

    /**
     * 执行系统级AI日程分析任务
     * 查询当天所有日程，按用户分组后逐个执行AI分析
     *
     * @param date 日期
     */
    @Override
    public void executeSystemAIScheduleAnalysis(LocalDate date) {
        try {
            log.info("开始执行系统级AI日程分析任务 - date: {}", date);

            // 获取下周一到下周日的日期集合
            List<LocalDate> dateList = getDateListNextWeekSunday(date);
            log.info("获取到下周一到下周日的日期集合，共 {} 天: {}", dateList.size(), dateList);

            dateList.forEach(localDate -> taskHandle(null, localDate, TagEnum.FOCUS));

        } catch (Exception e) {
            log.error("系统级AI日程分析任务执行失败 - date: {}", date, e);
        }
    }

    @Override
    public void taskHandle(String userId, LocalDate date, TagEnum tagEnum) {
        // 1. 查询当天所有日程
        List<SchedulePO> allSchedules;
        if (StringUtils.isBlank(userId)) {
            allSchedules = scheduleRepository.listByDate(date);
        } else {
            allSchedules = scheduleRepository.listByUserIdAndDate(userId, date);
        }

        List<String> allUserIds = null;
        if (StringUtils.isBlank(userId) && TagEnum.FOCUS.equals(tagEnum)) {
            log.info("当天没有任何日程，查询所有用户 - date: {}", date);
            // 查询所有用户ID集合
            allUserIds = scheduleRepository.listAllUserIds();

            log.info("查询到 {} 个用户ID", allUserIds.size());
        }

        log.info("查询到当天共 {} 条日程记录", allSchedules.size());

        // 2. 按用户ID分组
        Map<String, List<SchedulePO>> schedulesByUser = allSchedules.stream()
                .collect(Collectors.groupingBy(SchedulePO::getUserId));

        log.info("涉及 {} 个用户", schedulesByUser.size());

        // 3. 对每个用户的日程执行AI分析
        int successCount = 0;
        int failCount = 0;

        for (Map.Entry<String, List<SchedulePO>> entry : schedulesByUser.entrySet()) {
            String selUserId = entry.getKey();
            List<SchedulePO> userSchedules = entry.getValue();

            if (CollectionUtils.isNotEmpty(allUserIds)) {
                allUserIds.remove(selUserId);
            }

            try {
                log.info("开始分析用户 {} 的 {} 条日程", selUserId, userSchedules.size());
                executeAIScheduleAnalysis(selUserId, date, tagEnum);
                successCount++;
                log.info("用户 {} 的日程分析完成", selUserId);
            } catch (Exception e) {
                failCount++;
                log.error("用户 {} 的日程分析失败", selUserId, e);
            }
        }

        if (CollectionUtils.isNotEmpty(allUserIds)) {
            allUserIds.forEach(selUserId -> executeAIScheduleAnalysis(selUserId, date, tagEnum));
        }

        log.info("系统级AI日程分析任务执行完成 - date: {}, 成功: {}, 失败: {}", date, successCount, failCount);
    }

    /**
     * 执行单个用户的AI日程分析任务
     *
     * @param userId  用户ID
     * @param date    日期
     * @param tagEnum tagEnum
     */
    @Override
    public void executeAIScheduleAnalysis(String userId, LocalDate date, TagEnum tagEnum) {
        try {
            log.info("开始执行AI日程分析任务 - userId: {}, date: {}", userId, date);

            // 1. 获取当天的日程
            List<SchedulePO> schedules = scheduleRepository.listByUserIdAndDate(userId, date);
            
            log.info("获取到 {} 条日程记录", schedules.size());

            // 2. 将日程列表转换为AIScheduleCommonVal列表
            List<AIScheduleCommonVal> scheduleVals = schedules.stream()
                    .filter(schedule -> !TagEnum.SUGGESTION.getCode().equals(schedule.getTag()))
                    .map(this::convertSchedulePOToAIScheduleCommonVal)
                    .collect(Collectors.toList());

            // 3. 创建请求DTO
            AITaskScheduleRequestDTO requestDTO = new AITaskScheduleRequestDTO();
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            requestDTO.setDate(date.format(dateFormatter));
            
            // 由于AITaskScheduleRequestDTO只包含一个schedule_cards字段，
            // 我们将第一个日程设置为主要对象（实际上AI会处理所有日程信息）
            if (!scheduleVals.isEmpty()) {
                requestDTO.setAiScheduleCommonValList(scheduleVals);
            }

            if (CollectionUtils.isEmpty(schedules) && TagEnum.FOCUS.equals(tagEnum)) {
                analyzeDailyBriefing(userId, date, requestDTO);
                return;
            }

            // 4. 执行AI分析任务（统一使用AITaskScheduleRequestDTO格式）
            if (CollectionUtils.isNotEmpty(schedules)) {
                if (TagEnum.SUGGESTION.equals(tagEnum)) {
                    analyzeProactiveSuggestion(userId, requestDTO, schedules);
                } else if (TagEnum.FOCUS.equals(tagEnum)) {
                    // dailyBriefing
                    analyzeDailyBriefing(userId, date, requestDTO);
                } else if (TagEnum.CONFLICT.equals(tagEnum)) {
                    analyzeScheduleConflict(schedules, requestDTO);
                    // focus
                    analyzeScheduleFocus(schedules, requestDTO);
                }
            }


            log.info("AI日程分析任务执行完成 - userId: {}, date: {}", userId, date);

        } catch (Exception e) {
            log.error("AI日程分析任务执行失败 - userId: {}, date: {}", userId, date, e);
        }
    }

    @Override
    public void executeTodayAIScheduleAnalysis(LocalDate today) {
        log.info("每日执行系统AI建议及DailyBriefing, date is -> {}", today);
        try {
            taskHandle(null, today, TagEnum.FOCUS);
        } catch (Exception e) {
            log.error("每日执行系统AI建议及DailyBriefing - date: {}", today, e);
        }
    }

    @Override
    public void executeToday2NextWeekAIScheduleAnalysis(String userId, LocalDate date) {
        try {
            log.info("开始执行AI Focus - date: {}", date);

            if (redisTemplate.hasKey(RedisConstants.REDIS_KEY_USER.concat(userId))) {
                return;
            }

            redisTemplate.opsForValue().set(REDIS_KEY.concat(userId), "1");

            // 获取从date到本周日的日期集合
            List<LocalDate> dateList = getDateListFromNowToNextWeekSunday(date);
            log.info("获取从 {} 到本周日的日期集合，共 {} 天: {}", date, dateList.size(), dateList);

            dateList.forEach(localDate -> taskHandle(userId, localDate, TagEnum.FOCUS));

        } catch (Exception e) {
            log.error("开始执行AI Focus 执行失败 - date: {}", date, e);
        }
    }

    /**
     * 转换SchedulePO为AIScheduleCommonVal
     */
    private AIScheduleCommonVal convertSchedulePOToAIScheduleCommonVal(SchedulePO schedule) {
        AIScheduleCommonVal val = new AIScheduleCommonVal();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        
        val.setId(schedule.getId());
        val.setTitle(schedule.getTitle());
        val.setType((EnumUtil.getEnumByCode(schedule.getType(), TypeEnum.class).getValue()));

        val.setTags(String.valueOf(EnumUtil.getValueByCode(schedule.getTag(), TagEnum.class)));

        if (schedule.getStartTime() != null) {
            val.setStartTime(schedule.getStartTime().format(formatter));
        }
        if (schedule.getEndTime() != null) {
            val.setEndTime(schedule.getEndTime().format(formatter));
        }
        
        val.setInfo(schedule.getAuxiliaryInfo());
        val.setIsRemind(schedule.getIsRemind());
        val.setIcon(schedule.getIcon());
        val.setAiSuggestion(schedule.getAiSuggestion());
        val.setIsAiSuggestion(schedule.getIsAiSuggestion());
        
        return val;
    }

    /**
     * 分析日程冲突
     */
    private void analyzeScheduleConflict(List<SchedulePO> schedules, AITaskScheduleRequestDTO requestDTO) {
        log.info("开始执行日程冲突分析");

        List<SchedulePO> validSchedules = schedules.stream()
                .filter(s -> s.getStartTime() != null && s.getEndTime() != null)
                .toList();

        Set<SchedulePO> conflictSet = new LinkedHashSet<>();
        for (int i = 0; i < validSchedules.size(); i++) {
            for (int j = i + 1; j < validSchedules.size(); j++) {
                SchedulePO a = validSchedules.get(i);
                SchedulePO b = validSchedules.get(j);
                if (a.getStartTime().isBefore(b.getEndTime()) && b.getStartTime().isBefore(a.getEndTime())) {
                    conflictSet.add(a);
                    conflictSet.add(b);
                }
            }
        }

        if (conflictSet.isEmpty()) {
            log.info("日程冲突分析完成 - 未检测到时间交叉的日程");
            return;
        }

        log.info("检测到 {} 条存在时间交叉的日程", conflictSet.size());

        List<AIScheduleCommonVal> conflictValList = conflictSet.stream()
                .map(this::convertSchedulePOToAIScheduleCommonVal)
                .collect(Collectors.toList());

        AIConflictHttpRequestDTO aiConflictHttpRequestDTO = new AIConflictHttpRequestDTO();
        aiConflictHttpRequestDTO.setCurrentDate(requestDTO.getDate());
        aiConflictHttpRequestDTO.setAiScheduleCommonValList(conflictValList);
        aiConflictHttpRequestDTO.setStream(Boolean.FALSE);

        // 调用AI服务分析冲突
        AIConflictResponseDTO responseDTO = aiDomainService.chat(UrlEnum.SCHEDULE_CONFLICT, aiConflictHttpRequestDTO, AIConflictResponseDTO.class);
        log.info("AI冲突分析响应: {}", JacksonUtil.bean2Json(responseDTO));

        // 更新冲突建议
        if (responseDTO.getConflictScheduleList() != null && !responseDTO.getConflictScheduleList().isEmpty()) {
            for (AIConflictVal conflictVal : responseDTO.getConflictScheduleList()) {
                if (conflictVal.getId() != null && conflictVal.getConflictSuggestion() != null) {
                    // 更新数据库中的冲突建议
                    scheduleRepository.updateConflictInfo(conflictVal);
                    log.info("更新日程 {} 的冲突建议", conflictVal.getId());
                }
            }
        }

        log.info("日程冲突分析完成");
    }

    /**
     * 分析主动建议并创建日程
     */
    private void analyzeProactiveSuggestion(String userId, AITaskScheduleRequestDTO requestDTO, List<SchedulePO> schedules) {
        try {
            log.info("开始执行主动建议分析");

            List<SchedulePO> suggestionList = schedules.stream()
                    .filter(schedulePO -> TagEnum.SUGGESTION.getCode().equals(schedulePO.getTag()))
                    .toList();

            if (CollectionUtils.isNotEmpty(suggestionList)) {
                scheduleRepository.delete(suggestionList);

                schedules.removeAll(suggestionList);
            }

            if (CollectionUtils.isEmpty(schedules)) {
                log.info("没有日程，不执行主动建议分析");
                return;
            }

            AITaskFreeSuggestionRequestDTO request = new AITaskFreeSuggestionRequestDTO();
            request.setDate(requestDTO.getDate());
            request.setAiScheduleCommonValList(requestDTO.getAiScheduleCommonValList());

            // 计算空闲时间
            List<String> freeTimeList = calculateFreeTime(schedules, requestDTO.getDate());

            if (CollectionUtils.isEmpty(freeTimeList)) {
                return;
            }

            request.setFreeTime(freeTimeList);

            // 调用AI服务获取建议
            AIProactiveSuggestionResponseDTO responseDTO = aiDomainService.chat(UrlEnum.SCHEDULE_PROACTIVE_SUGGESTION, request, AIProactiveSuggestionResponseDTO.class);
            log.info("AI主动建议响应: {}", JacksonUtil.bean2Json(responseDTO));

            // 根据建议创建日程（现在是列表）
            if (responseDTO.getAiScheduleValList() != null && !responseDTO.getAiScheduleValList().isEmpty()) {
                log.info("AI返回 {} 条主动建议日程", responseDTO.getAiScheduleValList().size());
                
                int successCount = 0;
                int failCount = 0;

                List<SchedulePO> updateScheduleList = new ArrayList<>();

                for (AIScheduleVal scheduleVal : responseDTO.getAiScheduleValList()) {
                    SchedulePO schedulePO = convertAIScheduleValToSchedulePO(userId, scheduleVal);

                    // 确定提醒类型
                    String remindType = scheduleVal.getIsRemind() != null && scheduleVal.getIsRemind() ? "PUSH" : null;
                    Integer remindBeforeMinutes = scheduleVal.getRemindBeforeMinutes();
                    if (Boolean.TRUE.equals(scheduleVal.getIsRemind()) && remindBeforeMinutes == null) {
                        remindBeforeMinutes = 10;
                    }

                    // 调用Domain层创建日程
                    Long scheduleId = scheduleServiceDomain.createSchedule(schedulePO, remindBeforeMinutes, remindType, Boolean.TRUE);
                    log.info("根据AI建议创建日程成功 [{}/{}], scheduleId: {}, title: {}",
                            ++successCount, responseDTO.getAiScheduleValList().size(), scheduleId, scheduleVal.getTitle());

                    AIScheduleCommonVal aiScheduleCommonVal = AIScheduleCommonVal.convertSchedulePO2AIScheduleCommonVal(schedulePO);

                    AISuggestionHttpRequestDTO aiHttpRequestDTO = new AISuggestionHttpRequestDTO();
                    aiHttpRequestDTO.setAiScheduleCommonVal(aiScheduleCommonVal);
                    aiHttpRequestDTO.setStream(Boolean.FALSE);

                    AIPreHttpRequestDTO aiPreHttpRequestDTO = new AIPreHttpRequestDTO();
                    aiPreHttpRequestDTO.setAiScheduleCommonVal(aiScheduleCommonVal);
                    aiPreHttpRequestDTO.setStream(Boolean.FALSE);

                    String aiScheduleCommonValJson = JacksonUtil.bean2Json(aiScheduleCommonVal);

                    // 1. 调用 ScheduleSuggestion 获取日程建议
                    AISuggestionVal aiResponseJson = aiDomainService.chat(UrlEnum.SCHEDULE_SUGGESTION, aiHttpRequestDTO, AISuggestionVal.class);
                    log.info("AI建议响应: {}", aiResponseJson);

                    // 2. 调用 SchedulePreEventSuggestion 获取详情建议
                    AIScheduleDetailSuggestionResponseDTO detailSuggestionJson = aiDomainService.chat(UrlEnum.SCHEDULE_PRE_EVENT_SUGGESTION, aiPreHttpRequestDTO, AIScheduleDetailSuggestionResponseDTO.class);
                    log.info("AI详情建议响应: {}", detailSuggestionJson);

                    aiHandleDomainService.scheduleSuggestionHandle(schedulePO, aiResponseJson, detailSuggestionJson);

                    updateScheduleList.add(schedulePO);
                }

                scheduleRepository.batchUpdate(updateScheduleList);
                
                log.info("主动建议日程创建完成 - 成功: {}, 失败: {}", successCount, failCount);
            } else {
                log.info("AI未返回主动建议日程");
            }

            log.info("主动建议分析完成");

        } catch (Exception e) {
            log.error("主动建议分析失败", e);
        }
    }

    /**
     * 分析日程焦点
     */
    private void analyzeScheduleFocus(List<SchedulePO> schedules, AITaskScheduleRequestDTO requestDTO) {
        try {
            log.info("开始执行日程焦点分析");

            List<SchedulePO> notSuggestionList = schedules.stream()
                    .filter(schedulePO -> !TagEnum.SUGGESTION.getCode().equals(schedulePO.getTag()))
                    .toList();

            // 调用AI服务分析焦点
            AIFocusResponseDTO responseDTO = aiDomainService.chat(UrlEnum.SCHEDULE_FOCUS, requestDTO, AIFocusResponseDTO.class);
            log.info("AI焦点分析响应: {}", JacksonUtil.bean2Json(responseDTO));

            // 更新日程信息（现在是列表）
            if (responseDTO.getAiScheduleCommonValList() != null && !responseDTO.getAiScheduleCommonValList().isEmpty()) {
                log.info("AI返回 {} 条焦点日程", responseDTO.getAiScheduleCommonValList().size());
                
                int successCount = 0;
                int failCount = 0;
                
                for (AIScheduleCommonVal focusVal : responseDTO.getAiScheduleCommonValList()) {
                    try {
                        if (focusVal.getId() != null) {
                            // 根据ID查找对应的日程
                            SchedulePO schedule = notSuggestionList.stream()
                                    .filter(s -> s.getId().equals(focusVal.getId()))
                                    .findFirst()
                                    .orElse(null);

                            if (schedule != null) {
                                // 更新日程的AI相关字段
                                updateScheduleWithFocusInfo(schedule, focusVal);
                                scheduleRepository.update(schedule);
                                successCount++;
                                log.info("更新日程焦点信息成功 [{}/{}], scheduleId: {}, title: {}", 
                                        successCount, responseDTO.getAiScheduleCommonValList().size(), 
                                        schedule.getId(), schedule.getTitle());
                            } else {
                                failCount++;
                                log.warn("未找到ID为 {} 的日程", focusVal.getId());
                            }
                        }
                    } catch (Exception e) {
                        failCount++;
                        log.error("更新日程焦点信息失败, scheduleId: {}", focusVal.getId(), e);
                    }
                }
                
                log.info("日程焦点信息更新完成 - 成功: {}, 失败: {}", successCount, failCount);
            } else {
                log.info("AI未返回焦点日程");
            }

            log.info("日程焦点分析完成");

        } catch (Exception e) {
            log.error("日程焦点分析失败", e);
        }
    }

    /**
     * 分析每日简报
     */
    private void analyzeDailyBriefing(String userId, LocalDate date, AITaskScheduleRequestDTO requestDTO) {
        try {
            log.info("开始执行每日简报分析");

            AIDailyBriefingRequestDTO aiDailyBriefingRequestDTO = new AIDailyBriefingRequestDTO();
            aiDailyBriefingRequestDTO.setAiScheduleCommonValList(Objects.isNull(requestDTO.getAiScheduleCommonValList()) ? new ArrayList<>() : requestDTO.getAiScheduleCommonValList());
            aiDailyBriefingRequestDTO.setStream(Boolean.FALSE);

            // 调用AI服务生成简报
            AIDailyBriefingResponseDTO responseDTO = aiDomainService.chat(UrlEnum.SCHEDULE_DAILY_BRIEFING, aiDailyBriefingRequestDTO, AIDailyBriefingResponseDTO.class);
            log.info("AI每日简报响应: {}", JacksonUtil.bean2Json(responseDTO));

            // 处理简报信息
            if (responseDTO.getAiDailyBriefingCardVal() != null) {
                AIDailyBriefingCardVal briefingVal = responseDTO.getAiDailyBriefingCardVal();

                // 查询是否已存在简报
                DailyBriefingPO existingBriefing = dailyBriefingRepository.queryByUserIdAndDate(userId, date);

                if (existingBriefing == null) {
                    // 创建新的简报
                    DailyBriefingPO newBriefing = new DailyBriefingPO();
                    newBriefing.setUserId(userId);
                    newBriefing.setBriefingDate(date);
                    newBriefing.setTips(briefingVal.getTips());
                    newBriefing.setSummaryText(briefingVal.getSummaryText());
                    
                    // 从focus_item中获取相关信息
                    if (responseDTO.getAiScheduleCommonVal() != null) {
                        AIScheduleCommonVal focusItem = responseDTO.getAiScheduleCommonVal();
                        newBriefing.setScheduleId(focusItem.getId());
                        newBriefing.setTitle(focusItem.getTitle());
                        newBriefing.setIcon(focusItem.getIcon());
                        newBriefing.setDescription(focusItem.getInfo());
                    }

                    Long briefingId = dailyBriefingRepository.create(newBriefing);
                    log.info("创建每日简报成功, briefingId: {}", briefingId);

                } else {
                    // 更新现有简报
                    existingBriefing.setTips(briefingVal.getTips());
                    existingBriefing.setSummaryText(briefingVal.getSummaryText());
                    
                    // 从focus_item中获取相关信息
                    if (responseDTO.getAiScheduleCommonVal() != null) {
                        AIScheduleCommonVal focusItem = responseDTO.getAiScheduleCommonVal();
                        existingBriefing.setScheduleId(focusItem.getId());
                        existingBriefing.setTitle(focusItem.getTitle());
                        existingBriefing.setIcon(focusItem.getIcon());
                        existingBriefing.setDescription(focusItem.getInfo());
                    }

                    dailyBriefingRepository.update(existingBriefing);
                    log.info("更新每日简报成功, briefingId: {}", existingBriefing.getId());
                }
            }

            log.info("每日简报分析完成");

        } catch (Exception e) {
            log.error("每日简报分析失败", e);
        }
    }

    /**
     * 将AIScheduleVal转换为SchedulePO
     */
    private SchedulePO convertAIScheduleValToSchedulePO(String userId, AIScheduleVal aiScheduleVal) {
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

        schedulePO.setTag(Objects.requireNonNull(EnumUtil.getEnumByValue(aiScheduleVal.getTags(), TagEnum.class)).getCode());

        // 其他信息
        schedulePO.setAuxiliaryInfo(aiScheduleVal.getInfo());
        schedulePO.setIcon(aiScheduleVal.getIcon());
        schedulePO.setAiSuggestion(aiScheduleVal.getAiSuggestion());
        schedulePO.setIsRemind(aiScheduleVal.getIsRemind() != null ? aiScheduleVal.getIsRemind() : false);
        
        // 默认值
        schedulePO.setStatus(0); // PENDING
        schedulePO.setIsConflict(Boolean.FALSE);
        schedulePO.setIsDailyBriefing(Boolean.FALSE);
        schedulePO.setIsAiSuggestion(Boolean.TRUE);
        schedulePO.setIsCreate(Boolean.TRUE);

        return schedulePO;
    }

    /**
     * 使用焦点信息更新日程
     */
    private void updateScheduleWithFocusInfo(SchedulePO schedule, AIScheduleCommonVal focusVal) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        if (focusVal.getTitle() != null && !focusVal.getTitle().equals(schedule.getTitle())) {
            schedule.setTitle(focusVal.getTitle());
        }

        if (StringUtils.isNotBlank(focusVal.getTags())) {
            schedule.setTag(Objects.requireNonNull(EnumUtil.getEnumByValue(focusVal.getTags(), TagEnum.class)).getCode());
        }

        if (focusVal.getStartTime() != null) {
            LocalDateTime newStartTime = LocalDateTime.parse(focusVal.getStartTime(), formatter);
            if (!newStartTime.equals(schedule.getStartTime())) {
                schedule.setStartTime(newStartTime);
            }
        }

        if (focusVal.getEndTime() != null) {
            LocalDateTime newEndTime = LocalDateTime.parse(focusVal.getEndTime(), formatter);
            if (!newEndTime.equals(schedule.getEndTime())) {
                schedule.setEndTime(newEndTime);
            }
        }

        if (focusVal.getInfo() != null && !focusVal.getInfo().equals(schedule.getAuxiliaryInfo())) {
            schedule.setAuxiliaryInfo(focusVal.getInfo());
        }

        if (focusVal.getIcon() != null && !focusVal.getIcon().equals(schedule.getIcon())) {
            schedule.setIcon(focusVal.getIcon());
        }

        if (focusVal.getAiSuggestion() != null && !focusVal.getAiSuggestion().equals(schedule.getAiSuggestion())) {
            schedule.setAiSuggestion(focusVal.getAiSuggestion());
        }
    }

    /**
     * 获取下周一到下周日的日期集合
     *
     * @param date 当前日期
     * @return 下周一到下周日的日期集合（共7天）
     */
    private List<LocalDate> getDateListNextWeekSunday(LocalDate date) {
        List<LocalDate> dates = new ArrayList<>();

        int dayOfWeek = date.getDayOfWeek().getValue();
        int daysUntilNextMonday = (DayOfWeek.MONDAY.getValue() - dayOfWeek + 7) % 7;
        if (daysUntilNextMonday == 0) {
            daysUntilNextMonday = 7;
        }

        LocalDate nextMonday = date.plusDays(daysUntilNextMonday);
        LocalDate nextSunday = nextMonday.plusDays(6);

        LocalDate current = nextMonday;
        while (!current.isAfter(nextSunday)) {
            dates.add(current);
            current = current.plusDays(1);
        }

        return dates;
    }

    /**
     * 获取从指定日期到本周日的日期集合
     *
     * @param date 起始日期
     * @return 日期集合（包含起始日期和本周日）
     */
    private List<LocalDate> getDateListFromNowToNextWeekSunday(LocalDate date) {
        List<LocalDate> dates = new ArrayList<>();

        int daysToThisSunday = DayOfWeek.SUNDAY.getValue() - date.getDayOfWeek().getValue();
        if (daysToThisSunday < 0) {
            daysToThisSunday = 0;
        }
        LocalDate thisSunday = date.plusDays(daysToThisSunday);

        LocalDate current = date;
        while (!current.isAfter(thisSunday)) {
            dates.add(current);
            current = current.plusDays(1);
        }

        return dates;
    }

    /**
     * 根据schedules中的startTime和endTime，获取当日的空闲时间
     * 空闲时间要求时间间隔在2小时以上
     * 所有空闲时间必须在当前时间之后
     * 
     * @param schedules 日程列表
     * @param dateStr 日期字符串，格式：yyyy-MM-dd
     * @return 空闲时间列表，格式：["11:00-13:10","13:50-16:00", "17:30-21:40"]
     */
    private List<String> calculateFreeTime(List<SchedulePO> schedules, String dateStr) {
        List<String> freeTimeList = new ArrayList<>();
        
        // 获取当前时间
        LocalDateTime now = LocalDateTime.now();
        
        if (CollectionUtils.isEmpty(schedules)) {
            // 如果没有日程，全天都是空闲时间（00:00-23:59）
            // 但需要确保开始时间在当前时间之后
            LocalDate date = LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            LocalDateTime dayStart = date.atStartOfDay();
            LocalDateTime dayEnd = date.atTime(23, 59, 59);
            
            // 如果当前时间在当天，从当前时间开始；否则从当天开始
            LocalDateTime actualStart = now.isAfter(dayStart) && now.toLocalDate().equals(date) ? now : dayStart;
            
            if (actualStart.isBefore(dayEnd) || actualStart.equals(dayEnd)) {
                DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
                freeTimeList.add(actualStart.toLocalTime().format(timeFormatter) + "-23:59");
            }
            return freeTimeList;
        }
        
        // 解析日期
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate date = LocalDate.parse(dateStr, dateFormatter);
        
        // 当天的开始时间和结束时间
        LocalDateTime dayStart = date.atStartOfDay(); // 00:00:00
        LocalDateTime dayEnd = date.atTime(23, 59, 59); // 23:59:59
        
        // 过滤出当天的日程，并按startTime排序
        List<SchedulePO> daySchedules = schedules.stream()
                .filter(s -> s.getStartTime() != null && s.getEndTime() != null)
                .filter(s -> {
                    LocalDate scheduleDate = s.getStartTime().toLocalDate();
                    return scheduleDate.equals(date);
                })
                .sorted(Comparator.comparing(SchedulePO::getStartTime))
                .collect(Collectors.toList());
        
        if (CollectionUtils.isEmpty(daySchedules)) {
            // 如果当天没有有效日程，全天都是空闲时间
            // 但需要确保开始时间在当前时间之后
            LocalDateTime actualStart = now.isAfter(dayStart) && now.toLocalDate().equals(date) ? now : dayStart;
            
            if (actualStart.isBefore(dayEnd) || actualStart.equals(dayEnd)) {
                DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
                freeTimeList.add(actualStart.toLocalTime().format(timeFormatter) + "-23:59");
            }
            return freeTimeList;
        }
        
        // 时间格式化器
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        
        // 检查第一个日程之前的时间段
        LocalDateTime firstStart = daySchedules.get(0).getStartTime();
        if (firstStart.isAfter(dayStart)) {
            // 确定实际的开始时间：取dayStart和now中的较大值
            LocalDateTime actualStart = now.isAfter(dayStart) && now.toLocalDate().equals(date) ? now : dayStart;
            
            // 如果实际开始时间在firstStart之前，且时间段至少2小时
            if (actualStart.isBefore(firstStart)) {
                long minutesBetween = java.time.Duration.between(actualStart, firstStart).toMinutes();
                if (minutesBetween >= 120) { // 2小时 = 120分钟
                    freeTimeList.add(actualStart.toLocalTime().format(timeFormatter) + "-" + 
                                    firstStart.toLocalTime().format(timeFormatter));
                }
            }
        }
        
        // 检查相邻日程之间的时间段
        for (int i = 0; i < daySchedules.size() - 1; i++) {
            LocalDateTime currentEnd = daySchedules.get(i).getEndTime();
            LocalDateTime nextStart = daySchedules.get(i + 1).getStartTime();
            
            if (currentEnd.isBefore(nextStart)) {
                // 确定实际的开始时间：取currentEnd和now中的较大值
                LocalDateTime actualStart = now.isAfter(currentEnd) && now.toLocalDate().equals(date) ? now : currentEnd;
                
                // 如果实际开始时间在nextStart之前，且时间段至少2小时
                if (actualStart.isBefore(nextStart)) {
                    long minutesBetween = java.time.Duration.between(actualStart, nextStart).toMinutes();
                    if (minutesBetween >= 120) { // 2小时 = 120分钟
                        freeTimeList.add(actualStart.toLocalTime().format(timeFormatter) + "-" + 
                                        nextStart.toLocalTime().format(timeFormatter));
                    }
                }
            }
        }
        
        // 检查最后一个日程之后的时间段
        LocalDateTime lastEnd = daySchedules.get(daySchedules.size() - 1).getEndTime();
        if (lastEnd.isBefore(dayEnd)) {
            // 确定实际的开始时间：取lastEnd和now中的较大值
            LocalDateTime actualStart = now.isAfter(lastEnd) && now.toLocalDate().equals(date) ? now : lastEnd;
            
            // 如果实际开始时间在dayEnd之前或等于dayEnd，且时间段至少2小时
            if (actualStart.isBefore(dayEnd) || actualStart.equals(dayEnd)) {
                long minutesBetween = java.time.Duration.between(actualStart, dayEnd).toMinutes();
                if (minutesBetween >= 120) { // 2小时 = 120分钟
                    freeTimeList.add(actualStart.toLocalTime().format(timeFormatter) + "-23:59");
                }
            }
        }
        
        log.info("计算空闲时间完成 - date: {}, 当前时间: {}, 空闲时间段数量: {}, 空闲时间: {}", 
                dateStr, now, freeTimeList.size(), freeTimeList);
        
        return freeTimeList;
    }

}

