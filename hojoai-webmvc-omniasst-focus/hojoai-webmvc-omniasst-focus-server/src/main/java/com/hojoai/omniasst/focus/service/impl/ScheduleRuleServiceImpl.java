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

import com.hojoai.central.provider.service.utils.EnumUtil;
import com.hojoai.common.jackson.JacksonUtil;
import com.hojoai.common.response.Response;
import com.hojoai.common.response.ResponseStatusBasic;
import com.hojoai.omniasst.focus.common.assembler.ScheduleAssembler;
import com.hojoai.omniasst.focus.common.assembler.ScheduleRuleAssembler;
import com.hojoai.omniasst.focus.common.assembler.SysScheduleValAssembler;
import com.hojoai.omniasst.focus.common.enums.ModifyTypeEnum;
import com.hojoai.omniasst.focus.common.enums.TypeEnum;
import com.hojoai.omniasst.focus.common.enums.UrlEnum;
import com.hojoai.omniasst.focus.domain.*;
import com.hojoai.omniasst.focus.domain.strategy.ScheduleStrategy;
import com.hojoai.omniasst.focus.dto.request.AITitleHttpRequestDTO;
import com.hojoai.omniasst.focus.dto.request.ManualModifyScheduleRequestDTO;
import com.hojoai.omniasst.focus.dto.request.ManualScheduleRequestDTO;
import com.hojoai.omniasst.focus.dto.request.SysScheduleRequestDTO;
import com.hojoai.omniasst.focus.dto.response.SysModifyScheduleResponseDTO;
import com.hojoai.omniasst.focus.dto.response.SysScheduleResponseDTO;
import com.hojoai.omniasst.focus.dto.response.TypeIconQueryResponseDTO;
import com.hojoai.omniasst.focus.dto.val.SysScheduleVal;
import com.hojoai.omniasst.focus.repository.po.ScheduleAttachmentPO;
import com.hojoai.omniasst.focus.repository.po.SchedulePO;
import com.hojoai.omniasst.focus.repository.po.ScheduleRulePO;
import com.hojoai.omniasst.focus.service.ScheduleRuleService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.redisson.api.RDelayedQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * The ScheduleRuleServiceImpl class for ScheduleRuleServiceImpl.
 *
 * @author My
 * @version 1.0, 2026/03/03
 * @since hojoai-webmvc-omniasst-focus 1.0.0
 */
@Service
public class ScheduleRuleServiceImpl implements ScheduleRuleService {

    private static final Logger log = LoggerFactory.getLogger(ScheduleRuleServiceImpl.class);

    @Resource
    private ScheduleRuleDomainService scheduleRuleDomainService;

    @Resource
    private ScheduleDomainService scheduleDomainService;

    @Resource
    private ScheduleAsyncDomainService scheduleAsyncDomainService;

    @Resource
    private AttachmentDomainService attachmentDomainService;

    @Resource
    private MapDomainService mapDomainService;

    @Resource
    private AIDomainService aIDomainService;

    @Resource
    private RDelayedQueue<String> scheduleTaskDelayedQueue;

    @Resource
    private List<ScheduleStrategy<?, ?>> createScheduleStrategies;

    private Map<Integer, ScheduleStrategy<?, ?>> strategyMap;

    @PostConstruct
    public void initStrategyMap() {
        strategyMap = createScheduleStrategies.stream()
                .collect(Collectors.toMap(ScheduleStrategy::getType, Function.identity()));
    }

    @Override
    public Pair<ScheduleRulePO, List<SchedulePO>> manualCreateSchedule(String userId, ManualScheduleRequestDTO requestDTO) {
        ScheduleStrategy<?, ?> strategy = strategyMap.get(requestDTO.getType());

        // 创建日程规则
        ScheduleRulePO rulePO = scheduleRuleDomainService.createScheduleRule(userId, requestDTO);

        // 创建日程实例
        List<SchedulePO> schedulePOList = scheduleRuleDomainService.createSchedule(rulePO);

        if (Objects.nonNull(strategy)) {
            List<?> dataRulePOList = strategy.createScheduleRule(requestDTO, rulePO);

            strategy.createSchedule(schedulePOList, dataRulePOList);
        }

        // 保存地图
        mapDomainService.save(userId, requestDTO.getLocationInfo());

        // 保存规则附件
        List<ScheduleAttachmentPO> scheduleAttachmentList = attachmentDomainService.saveAttachment(rulePO.getId(), requestDTO.getFileList(), schedulePOList);

        scheduleAsyncDomainService.scheduleChangeAiHandle(userId, schedulePOList);

        // 3. Convert PO list to response DTO list and create remind tasks
        redisDelayQueueHandle(schedulePOList, rulePO);

        asyncRecommendationHandle(schedulePOList, scheduleAttachmentList);

        return Pair.of(rulePO, schedulePOList);
    }

    @Async
    @Override
    public void sysAsyncScheduleHandle(String userId, SysScheduleRequestDTO requestDTO) {
        List<ScheduleRulePO> scheduleRulePOList = scheduleRuleDomainService.sysAddSchedule(userId, requestDTO.getAddScheduleList());
        List<SysScheduleVal> addList = requestDTO.getAddScheduleList() != null ? requestDTO.getAddScheduleList() : List.of();

        List<SchedulePO> allSchedulePOList = new ArrayList<>();

        if (CollectionUtils.isNotEmpty(scheduleRulePOList)) {
            for (int i = 0; i < scheduleRulePOList.size(); i++) {
                ScheduleRulePO scheduleRulePO = scheduleRulePOList.get(i);

                // 调用模型，判断类型和icon
                AITitleHttpRequestDTO aiTitleHttpRequestDTO = new AITitleHttpRequestDTO();
                aiTitleHttpRequestDTO.setEnteredTitle(scheduleRulePO.getTitle());

                TypeIconQueryResponseDTO typeIconQueryResponseDTO = aIDomainService.chat(UrlEnum.SCHEDULE_MATCH_TITLE_ICON, aiTitleHttpRequestDTO, TypeIconQueryResponseDTO.class);
                log.info("sysAsyncScheduleHandle.TypeIconQueryResponseDTO -> {}", JacksonUtil.bean2Json(typeIconQueryResponseDTO));
                scheduleRulePO.setType(Objects.requireNonNull(EnumUtil.getEnumByValue(typeIconQueryResponseDTO.getType().toLowerCase(), TypeEnum.class)).getCode());
                scheduleRulePO.setIcon(typeIconQueryResponseDTO.getIcon());

                // 根据索引获取 addScheduleList 中对应的 SysScheduleVal（与 sysAddSchedule 生成规则顺序一致）
                SysScheduleVal sysScheduleVal = i < addList.size() ? addList.get(i) : null;
                ScheduleStrategy<?, ?> strategy = scheduleRulePO.getType() != null ? strategyMap.get(scheduleRulePO.getType()) : null;

                List<SchedulePO> schedulePOList;
                if (strategy != null && sysScheduleVal != null) {
                    List<?> dataRulePOList = strategy.createScheduleRule(scheduleRulePO, sysScheduleVal);
                    schedulePOList = scheduleRuleDomainService.createSchedule(scheduleRulePO);
                    strategy.createSchedule(schedulePOList, dataRulePOList);
                } else {
                    schedulePOList = scheduleRuleDomainService.createSchedule(scheduleRulePO);
                }
                allSchedulePOList.addAll(schedulePOList);

                allSchedulePOList.forEach(schedulePO -> scheduleAsyncDomainService.createRecommendation(schedulePO, null));

                // 3. Convert PO list to response DTO list and create remind tasks
                redisDelayQueueHandle(schedulePOList, scheduleRulePO);
            }
        }

        List<SchedulePO> updateSchedulePOList = scheduleRuleDomainService.sysUpdateSchedule(userId, requestDTO.getUpdateScheduleList() != null ? requestDTO.getUpdateScheduleList() : List.of());
        updateSchedulePOList.forEach(schedulePO -> scheduleAsyncDomainService.createRecommendation(schedulePO, null));

        if (CollectionUtils.isNotEmpty(updateSchedulePOList)) {
            allSchedulePOList.addAll(updateSchedulePOList);
        }

        if (CollectionUtils.isNotEmpty(allSchedulePOList)) {
            scheduleDomainService.batchUpdateSchedules(allSchedulePOList);
        }

        if (CollectionUtils.isNotEmpty(scheduleRulePOList)) {
            scheduleRuleDomainService.batchUpdateScheduleRules(scheduleRulePOList);
        }

        List<LocalDate> dateList = allSchedulePOList.stream().map(schedulePO -> schedulePO.getStartTime().toLocalDate()).toList();

        try {
            List<SchedulePO> existingSchedules = scheduleDomainService.querySchedulesByDateList(userId, dateList);
            allSchedulePOList.forEach(schedule -> {
                List<SchedulePO> existScheduleList = existingSchedules.stream()
                        .filter(existingSchedule -> existingSchedule.getStartTime().toLocalDate().equals(schedule.getStartTime().toLocalDate()))
                        .toList();

                scheduleDomainService.suggetionGapHandle(schedule, existScheduleList);
            });

            scheduleAsyncDomainService.asyncHandle(userId, allSchedulePOList, existingSchedules);
        } catch (Exception e) {
            log.error("ScheduleRuleServiceImpl.sysAsyncScheduleHandle error -> ", e);
        }

        scheduleRuleDomainService.sysDelSchedule(userId, requestDTO.getDelScheduleIdList() != null ? requestDTO.getDelScheduleIdList().stream().map(id -> {
            scheduleAsyncDomainService.deleteRecommendation(userId, id);
            SysScheduleVal v = new SysScheduleVal();
            v.setCloudEventId(String.valueOf(id));
            return v;
        }).collect(Collectors.toList()) : List.of());
    }

    private void redisDelayQueueHandle(List<SchedulePO> schedulePOList, ScheduleRulePO scheduleRulePO) {
        log.info("redis queue scheduleRule id is -> {}", scheduleRulePO.getId());
        schedulePOList.forEach(schedulePO -> {
            if (Boolean.TRUE.equals(scheduleRulePO.getIsRemind())
                    && scheduleRulePO.getRemindTime() != null
                    && StringUtils.hasText(scheduleRulePO.getRemindUnit())) {
                LocalDateTime remindDateTime = calcRemindDateTime(
                        schedulePO.getStartTime(), scheduleRulePO.getRemindUnit(), scheduleRulePO.getRemindTime());
                long seconds = Duration.between(LocalDateTime.now(), remindDateTime).toSeconds();
                if (seconds > 0) {
                    String scheduleId = String.valueOf(schedulePO.getId());
                    scheduleTaskDelayedQueue.offer(scheduleId, seconds, TimeUnit.SECONDS);
                    log.info("已将日程提醒任务加入延时队列 - scheduleId: {}, 提醒时间: {}, 延迟时间: {}秒",
                            scheduleId, remindDateTime, seconds);
                }
            }
        });
    }

    @Override
    public Response<SysScheduleResponseDTO> sysSyncScheduleHandle(String userId, SysScheduleRequestDTO requestDTO) {
        return scheduleRuleDomainService.getSysUpdateScheduleList(userId, requestDTO);
    }

    @Override
    public Response<SysModifyScheduleResponseDTO> manualModifySchedule(String scheduleId, String modifyType, ManualModifyScheduleRequestDTO requestDTO) {
        log.info("ScheduleRuleServiceImpl.manualModifySchedule params scheduleId is -> {}, modifyType is -> {}, requestDTO is -> {}", scheduleId, modifyType, JacksonUtil.bean2Json(requestDTO));
        String userId = requestDTO != null ? requestDTO.getUserId() : null;
        if (!StringUtils.hasText(userId) || !StringUtils.hasText(scheduleId) || modifyType == null) {
            return Response.newInstance(ResponseStatusBasic.ILLEGAL_ARGUMENT, null);
        }
        Long scheduleIdLong;
        try {
            scheduleIdLong = Long.parseLong(scheduleId);
        } catch (NumberFormatException e) {
            log.warn("manualModifySchedule: scheduleId 格式无效, scheduleId={}", scheduleId);
            return Response.newInstance(ResponseStatusBasic.ILLEGAL_ARGUMENT, null);
        }

        SchedulePO currentSchedule = scheduleDomainService.getByUserIdAndScheduleId(userId, scheduleIdLong);
        if (currentSchedule == null) {
            log.warn("manualModifySchedule: 日程不存在, scheduleId={}", scheduleId);
            return Response.newInstance(ResponseStatusBasic.ILLEGAL_ARGUMENT, null);
        }

        SysModifyScheduleResponseDTO responseDTO = null;

        ScheduleRulePO rulePO = scheduleRuleDomainService.getScheduleRuleById(currentSchedule.getRuleId());

        // 1. 仅更新当前日程实例
        if (ModifyTypeEnum.CURRENT.getCode().equals(modifyType)) {
            currentModifyHandle(requestDTO, userId, currentSchedule, scheduleIdLong, rulePO);
        }

        // 2. 更新该规则下的全部日程
        if (ModifyTypeEnum.ALL.getCode().equals(modifyType)) {
            responseDTO = modifyAllHandle(requestDTO, userId, rulePO);
        }

        // 3. 更新当前及之后日程（AFTER）：截断旧规则，从当前日程开始新建规则及日程
        if (ModifyTypeEnum.AFTER.getCode().equals(modifyType)) {
            responseDTO = afterModifyHandle(requestDTO, rulePO, currentSchedule, userId);
        }

        // 保存地图
        mapDomainService.save(userId, requestDTO.getLocationInfo());

        return Response.newInstance(ResponseStatusBasic.OK, responseDTO);
    }

    private SysModifyScheduleResponseDTO afterModifyHandle(ManualModifyScheduleRequestDTO requestDTO, ScheduleRulePO rulePO, SchedulePO currentSchedule, String userId) {
        // 1. 查询该规则下的全部日程
        List<SchedulePO> allScheduleList = scheduleDomainService.listByUserIdAndRuleId(userId, rulePO.getId());

        // 2. 判断 currentSchedule 是否为第一条日程（按 startTime 判断）
        if (CollectionUtils.isNotEmpty(allScheduleList)) {
            SchedulePO firstSchedule = allScheduleList.stream()
                    .min(Comparator.comparing(SchedulePO::getStartTime))
                    .orElse(null);
            if (firstSchedule != null && firstSchedule.getId().equals(currentSchedule.getId())) {
                log.info("ScheduleRuleServiceImpl.afterModifyHandle is first schedule, scheduleId is -> {}", currentSchedule.getId());
                return modifyAllHandle(requestDTO, userId, rulePO);
            }
        }

        // 3. 判断重复性信息是否改变
        if (Objects.equals(requestDTO.getIsRepeat(), rulePO.getIsRepeat())
                && Objects.equals(requestDTO.getRepeatUnit(), rulePO.getRepeatUnit())) {
            log.info("ScheduleRuleServiceImpl.afterModifyHandle not change, scheduleId is -> {}", currentSchedule.getId());
            // 情况1：重复性信息未改变 — 原地更新 SchedulePO，其余关联数据先删后增
            return afterModifyUnchangedRepeat(requestDTO, rulePO, currentSchedule, userId);
        } else {
            log.info("ScheduleRuleServiceImpl.afterModifyHandle is change, scheduleId is -> {}", currentSchedule.getId());
            // 情况2：重复性信息已改变 — 截断旧规则，删除当前及之后全部数据，重新创建
            return afterModifyChangedRepeat(requestDTO, rulePO, currentSchedule, userId);
        }
    }

    /**
     * AFTER 修改 — 重复性信息未改变：原地更新当前及之后的 SchedulePO，关联数据（XxxPO / LocationPO / AttachmentPO）先删后增
     */
    private SysModifyScheduleResponseDTO afterModifyUnchangedRepeat(ManualModifyScheduleRequestDTO requestDTO,
                                                                     ScheduleRulePO rulePO,
                                                                     SchedulePO currentSchedule,
                                                                     String userId) {
        // 更新 rulePO 字段
        ScheduleRulePO baseRule = ScheduleRuleAssembler.buildBaseScheduleRulePO(rulePO, requestDTO);
        ScheduleRuleAssembler.copyRuleFieldsFrom(rulePO, baseRule);
        scheduleRuleDomainService.updateScheduleRule(rulePO);

        // 获取当前日程及之后的全部日程
        List<SchedulePO> fromCurrentSchedules = scheduleDomainService.listByUserIdAndRuleIdFromTime(
                userId, rulePO.getId(), currentSchedule.getStartTime());

        if (CollectionUtils.isNotEmpty(fromCurrentSchedules)) {
            List<Long> scheduleIds = fromCurrentSchedules.stream()
                    .map(SchedulePO::getId).collect(Collectors.toList());

            // SchedulePO：原地更新
            fromCurrentSchedules.forEach(schedule -> ScheduleAssembler.updateSchedulePOFromRuleWithTime(schedule, rulePO));
            scheduleDomainService.batchUpdateSchedules(fromCurrentSchedules);

            // ScheduleXxxPO：先删后增
            ScheduleStrategy<?, ?> strategy = strategyMap.get(rulePO.getType());
            if (strategy != null) {
                strategy.deleteScheduleData(scheduleIds);
                List<?> dataRulePOList = strategy.updateScheduleRuleData(requestDTO, rulePO);
                strategy.createSchedule(fromCurrentSchedules, dataRulePOList);
            }

            // ScheduleLocationPO：先删后增
            scheduleRuleDomainService.deleteScheduleLocations(scheduleIds);
            scheduleRuleDomainService.updateRuleAndScheduleLocations(rulePO, fromCurrentSchedules, requestDTO.getLocationInfo());

            // ScheduleAttachmentPO：先删后增
            attachmentDomainService.deleteByScheduleIdList(scheduleIds);
            List<ScheduleAttachmentPO> scheduleAttachmentPOList = attachmentDomainService.saveAttachment(rulePO.getId(), requestDTO.getFileList(), fromCurrentSchedules);

            // AI 处理 + 延时提醒队列
            scheduleAsyncDomainService.scheduleChangeAiHandle(userId, fromCurrentSchedules);
            redisDelayQueueHandle(fromCurrentSchedules, rulePO);

            asyncRecommendationHandle(fromCurrentSchedules, scheduleAttachmentPOList);
        }

        return Objects.nonNull(rulePO.getIsSysSync()) && 1 == rulePO.getIsSysSync()
                ? buildSysModifyScheduleResponse(rulePO) : null;
    }

    /**
     * AFTER 修改 — 重复性信息已改变：截断旧规则 ruleEndTime，删除当前及之后全部数据，调用 manualCreateSchedule 重建
     */
    private SysModifyScheduleResponseDTO afterModifyChangedRepeat(ManualModifyScheduleRequestDTO requestDTO,
                                                                   ScheduleRulePO rulePO,
                                                                   SchedulePO currentSchedule,
                                                                   String userId) {
        // 截断旧规则
        rulePO.setRuleEndTime(currentSchedule.getStartTime());
        scheduleRuleDomainService.updateScheduleRule(rulePO);

        // 获取当前及之后的全部日程并删除所有关联数据
        List<SchedulePO> fromCurrentSchedules = scheduleDomainService.listByUserIdAndRuleIdFromTime(
                userId, rulePO.getId(), currentSchedule.getStartTime());

        if (CollectionUtils.isNotEmpty(fromCurrentSchedules)) {
            List<Long> scheduleIds = fromCurrentSchedules.stream()
                    .map(SchedulePO::getId).collect(Collectors.toList());

            ScheduleStrategy<?, ?> strategy = strategyMap.get(rulePO.getType());
            if (strategy != null) {
                strategy.deleteScheduleData(scheduleIds);
            }

            scheduleRuleDomainService.deleteScheduleLocations(scheduleIds);
            attachmentDomainService.deleteByScheduleIdList(scheduleIds);
            scheduleDomainService.deleteScheduleList(fromCurrentSchedules);

            fromCurrentSchedules.forEach(schedulePO -> scheduleAsyncDomainService.deleteRecommendation(userId, String.valueOf(schedulePO.getId())));
        }

        // 以当前日程日期为起点创建新日程
        requestDTO.setStartDate(currentSchedule.getStartTime().toLocalDate().toString());
        Pair<ScheduleRulePO, List<SchedulePO>> result = manualCreateSchedule(userId, requestDTO);

        ScheduleRulePO newRulePO = result.getLeft();
        List<SchedulePO> newSchedules = result.getRight();

        // 同步 sysSync 字段
        newRulePO.setIsSysSync(rulePO.getIsSysSync());
        newRulePO.setSysId(rulePO.getSysId());
        scheduleRuleDomainService.updateScheduleRule(newRulePO);

        newSchedules.forEach(schedulePO -> {
            schedulePO.setIsSysSync(rulePO.getIsSysSync());
            schedulePO.setSysId(rulePO.getSysId());
        });
        scheduleDomainService.batchUpdateSchedules(newSchedules);

        return Objects.nonNull(newRulePO.getIsSysSync()) && 1 == newRulePO.getIsSysSync()
                ? buildSysModifyScheduleResponse(newRulePO) : null;
    }

    private void currentModifyHandle(ManualModifyScheduleRequestDTO requestDTO, String userId, SchedulePO currentSchedule, Long scheduleIdLong, ScheduleRulePO rulePO) {
        ScheduleRulePO baseRule = ScheduleRuleAssembler.buildBaseScheduleRulePO(rulePO, requestDTO);
        ScheduleAssembler.updateSchedulePOFromRuleWithTime(currentSchedule, baseRule);

        ScheduleRuleAssembler.copyRuleFieldsFrom(rulePO, baseRule);
        scheduleRuleDomainService.updateScheduleRule(rulePO);

        scheduleDomainService.updateSchedule(currentSchedule);

        ScheduleStrategy<?, ?> strategy = strategyMap.get(currentSchedule.getType());
        if (strategy != null) {
            strategy.deleteScheduleData(Collections.singletonList(scheduleIdLong));
            List<?> ruleData = currentSchedule.getRuleId() != null
                    ? strategy.getScheduleRuleData(currentSchedule.getRuleId())
                    : List.of();
            strategy.createSchedule(Collections.singletonList(currentSchedule), ruleData);
        }

        // 更新当前日程位置
        scheduleRuleDomainService.updateScheduleLocation(scheduleIdLong, requestDTO.getLocationInfo());

        attachmentDomainService.deleteByScheduleIdList(Collections.singletonList(scheduleIdLong));

        List<ScheduleAttachmentPO> scheduleAttachmentList = attachmentDomainService.saveAttachmentBySchedule(currentSchedule, requestDTO.getFileList());

        scheduleAsyncDomainService.scheduleChangeAiHandle(userId, Collections.singletonList(currentSchedule));

        scheduleAsyncDomainService.createRecommendation(currentSchedule, scheduleAttachmentList);
    }

    private SysModifyScheduleResponseDTO modifyAllHandle(ManualModifyScheduleRequestDTO requestDTO, String userId, ScheduleRulePO rulePO) {
        List<SchedulePO> schedulePOList;

        if (Objects.equals(requestDTO.getIsRepeat(), rulePO.getIsRepeat()) && Objects.equals(requestDTO.getRepeatUnit(), rulePO.getRepeatUnit())) {
            log.info("ScheduleRuleServiceImpl.modifyAllHandle repeat is same, scheduleRuleId is -> {}", requestDTO.getScheduleRuleId());
            ScheduleRulePO baseRule = ScheduleRuleAssembler.buildBaseScheduleRulePO(rulePO, requestDTO);
            ScheduleRuleAssembler.copyRuleFieldsFrom(rulePO, baseRule);
            scheduleRuleDomainService.updateScheduleRule(rulePO);

            // 查询该规则下的全部现有日程并更新，不删除后重建
            List<SchedulePO> existingScheduleList = scheduleDomainService.listByUserIdAndRuleId(userId, rulePO.getId());
            if (CollectionUtils.isNotEmpty(existingScheduleList)) {
                existingScheduleList.forEach(schedule -> ScheduleAssembler.updateSchedulePOFromRuleWithTime(schedule, rulePO));
                scheduleDomainService.batchUpdateSchedules(existingScheduleList);
            }

            schedulePOList = CollectionUtils.isNotEmpty(existingScheduleList) ? existingScheduleList : List.of();

            // 更新类型属性（RuleXxxPO / ScheduleXxxPO），不删除后重建
            ScheduleStrategy<?, ?> strategy = strategyMap.get(rulePO.getType());
            if (strategy != null) {
                List<?> dataRulePOList = strategy.updateScheduleRuleData(requestDTO, rulePO);
                strategy.updateScheduleData(schedulePOList, dataRulePOList);
            }

            // 保存规则附件
            List<ScheduleAttachmentPO> scheduleAttachmentList = attachmentDomainService.saveAttachment(rulePO.getId(), requestDTO.getFileList(), schedulePOList);

            asyncRecommendationHandle(schedulePOList, scheduleAttachmentList);
        } else {
            log.info("ScheduleRuleServiceImpl.modifyAllHandle repeat is not same, scheduleRuleId is -> {}", requestDTO.getScheduleRuleId());
            ScheduleRulePO baseRule = ScheduleRuleAssembler.buildBaseScheduleRulePO(rulePO, requestDTO);
            ScheduleRuleAssembler.copyRuleFieldsFrom(rulePO, baseRule);

            scheduleRuleDomainService.updateScheduleRule(rulePO);

            List<SchedulePO> existingScheduleList = scheduleDomainService.listByUserIdAndRuleId(userId, rulePO.getId());

            scheduleDomainService.deleteScheduleList(rulePO.getId());

            schedulePOList = scheduleRuleDomainService.createSchedule(rulePO);

            ScheduleStrategy<?, ?> strategy = strategyMap.get(rulePO.getType());
            List<?> newRuleData;
            if (strategy != null) {
                strategy.deleteScheduleRuleData(rulePO.getId());

                newRuleData = strategy.createScheduleRule(requestDTO, rulePO);

                strategy.createSchedule(schedulePOList, newRuleData);
            }

            existingScheduleList.forEach(schedulePO -> scheduleAsyncDomainService.deleteRecommendation(userId, String.valueOf(schedulePO.getId())));

            // 保存规则附件
            List<ScheduleAttachmentPO> scheduleAttachmentList = attachmentDomainService.saveAttachment(rulePO.getId(), requestDTO.getFileList(), schedulePOList);

            asyncRecommendationHandle(schedulePOList, scheduleAttachmentList);
        }

        // 更新规则及所有日程位置
        scheduleRuleDomainService.updateRuleAndScheduleLocations(rulePO, schedulePOList, requestDTO.getLocationInfo());

        attachmentDomainService.deleteRuleId(rulePO.getId());

        scheduleAsyncDomainService.scheduleChangeAiHandle(userId, schedulePOList);

        return Objects.nonNull(rulePO.getIsSysSync()) && 1 == rulePO.getIsSysSync() ? buildSysModifyScheduleResponse(rulePO) : null;
    }

    private void asyncRecommendationHandle(List<SchedulePO> existingScheduleList, List<ScheduleAttachmentPO> scheduleAttachmentList) {
        existingScheduleList.forEach(schedulePO -> {
            if (CollectionUtils.isEmpty(scheduleAttachmentList)) {
                scheduleAsyncDomainService.createRecommendation(schedulePO, null);
            } else {
                List<ScheduleAttachmentPO> list = scheduleAttachmentList.stream().filter(scheduleAttachmentPO -> scheduleAttachmentPO.getScheduleId().equals(schedulePO.getId())).toList();
                scheduleAsyncDomainService.createRecommendation(schedulePO, list);
            }
        });
    }

    @Override
    public void removeRepeatScheduleList(String userId, List<SysScheduleVal> addScheduleList) {
        scheduleRuleDomainService.removeRepeatSchedule(userId, addScheduleList);
    }

    /**
     * 根据提醒单位和提醒时间，计算实际提醒时刻（startTime 提前指定时长）
     * remindUnit: MM-分钟；H-小时；D-日；W-周；M-月；Y-年
     */
    private static LocalDateTime calcRemindDateTime(LocalDateTime startTime, String remindUnit, int remindTime) {
        switch (remindUnit.trim().toUpperCase()) {
            case "MM": return startTime.minusMinutes(remindTime);
            case "H":  return startTime.minusHours(remindTime);
            case "D":  return startTime.minusDays(remindTime);
            case "W":  return startTime.minusWeeks(remindTime);
            case "M":  return startTime.minusMonths(remindTime);
            case "Y":  return startTime.minusYears(remindTime);
            default:   return startTime.minusMinutes(remindTime);
        }
    }

    private static SysModifyScheduleResponseDTO buildSysModifyScheduleResponse(ScheduleRulePO rule) {
        SysModifyScheduleResponseDTO dto = new SysModifyScheduleResponseDTO();
        SysScheduleVal val = SysScheduleValAssembler.buildScheduleVal(rule);
        dto.setUpdateSchedule(val);
        dto.setLastSyncTime(LocalDateTime.now().toInstant(ZoneOffset.UTC).toEpochMilli());
        return dto;
    }

}
