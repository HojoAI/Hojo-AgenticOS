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

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hojoai.common.response.Response;
import com.hojoai.common.response.ResponseStatusBasic;
import com.hojoai.omniasst.focus.common.assembler.ScheduleAssembler;
import com.hojoai.omniasst.focus.common.assembler.ScheduleRuleAssembler;
import com.hojoai.omniasst.focus.common.assembler.SysScheduleValAssembler;
import com.hojoai.omniasst.focus.domain.ScheduleRuleDomainService;
import com.hojoai.omniasst.focus.dto.request.ManualScheduleRequestDTO;
import com.hojoai.omniasst.focus.dto.request.SysScheduleRequestDTO;
import com.hojoai.omniasst.focus.dto.response.SysScheduleResponseDTO;
import com.hojoai.omniasst.focus.dto.val.LocationInfoVal;
import com.hojoai.omniasst.focus.dto.val.SysMappingScheduleVal;
import com.hojoai.omniasst.focus.dto.val.SysScheduleVal;
import com.hojoai.omniasst.focus.repository.mapper.RuleLocationMapper;
import com.hojoai.omniasst.focus.repository.mapper.ScheduleLocationMapper;
import com.hojoai.omniasst.focus.repository.mapper.ScheduleMapper;
import com.hojoai.omniasst.focus.repository.mapper.ScheduleRuleMapper;
import com.hojoai.omniasst.focus.repository.po.RuleLocationPO;
import com.hojoai.omniasst.focus.repository.po.ScheduleLocationPO;
import com.hojoai.omniasst.focus.repository.po.SchedulePO;
import com.hojoai.omniasst.focus.repository.po.ScheduleRulePO;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import xyz.downgoon.snowflake.Snowflake;

import javax.annotation.Resource;
import java.time.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 日程规则领域服务实现：统一构建并保存 ScheduleRulePO
 *
 * @author My
 * @version 1.0 2026/03/03
 * @since hojoai-webmvc-omniasst-focus 1.0.0
 */
@Component
public class ScheduleRuleDomainServiceImpl implements ScheduleRuleDomainService {

    @Resource
    private ScheduleRuleMapper scheduleRuleMapper;

    @Resource
    private ScheduleMapper scheduleMapper;

    @Resource
    private RuleLocationMapper ruleLocationMapper;

    @Resource
    private ScheduleLocationMapper scheduleLocationMapper;

    @Resource
    private Snowflake snowflake;

    @Override
    public ScheduleRulePO createScheduleRule(String userId, ManualScheduleRequestDTO requestDTO) {
        ScheduleRulePO rulePO = ScheduleRuleAssembler.buildBaseScheduleRulePO(userId, requestDTO);

        if (rulePO.getId() == null || rulePO.getId() == 0) {
            rulePO.setId(snowflake.nextId());
        }

        scheduleRuleMapper.insert(rulePO);

        // 保存规则位置：创建时若传了 locationInfo，写入 focus_rule_location 并回填 rule.locationId
        if (requestDTO != null && requestDTO.getLocationInfo() != null) {
            RuleLocationPO ruleLocationPO = buildRuleLocationPO(rulePO.getId(), requestDTO.getLocationInfo());
            ruleLocationPO.setId(snowflake.nextId());
            ruleLocationMapper.insert(ruleLocationPO);
            rulePO.setLocationId(ruleLocationPO.getId());
            scheduleRuleMapper.updateById(rulePO);
        }

        return rulePO;
    }

    private static RuleLocationPO buildRuleLocationPO(Long ruleId, LocationInfoVal val) {
        RuleLocationPO po = new RuleLocationPO();
        po.setRuleId(ruleId);
        po.setName(val.getName());
        String info = StringUtils.hasText(val.getLocation()) ? val.getLocation() : val.getName();
        po.setLocationInfo(info);
        po.setLongitude(val.getLongitude());
        po.setLatitude(val.getLatitude());
        po.setProvince(val.getProvince());
        po.setCity(val.getCity());
        po.setDistrict(val.getDistrict());
        po.setTownship(val.getTownship());
        return po;
    }

    @Override
    public List<SchedulePO> createSchedule(ScheduleRulePO rulePO) {
        LocalTime startTimeOfDay = rulePO.getStartTime() != null ? rulePO.getStartTime() : LocalTime.of(0, 0);
        LocalTime endTimeOfDay = rulePO.getEndTime() != null ? rulePO.getEndTime() : LocalTime.of(23, 59);
        LocalDate ruleStartDate = rulePO.getRuleStartTime() != null ? rulePO.getRuleStartTime().toLocalDate() : null;
        LocalDate ruleEndDate = rulePO.getRuleEndTime() != null ? rulePO.getRuleEndTime().toLocalDate() : null;
        // endTime 为空时，后续使用当前年最后一天
        LocalDate effectiveEndDate = ruleEndDate != null ? ruleEndDate : Year.now().atMonth(12).atEndOfMonth();

        boolean isRepeat = Boolean.TRUE.equals(rulePO.getIsRepeat());
        boolean isFixedDate = rulePO.getIsFixedDate() != null && rulePO.getIsFixedDate() != 0;

        if (isRepeat && !isFixedDate) {
            LocalDate from = ruleStartDate != null ? ruleStartDate : LocalDate.now();
            LocalDate to = effectiveEndDate;

            List<LocalDate> dates = generateRepeatDates(from, to, rulePO.getRepeatTime(), rulePO.getRepeatUnit());

            List<SchedulePO> list = dates.stream().map(date -> ScheduleAssembler.buildSchedulePO(snowflake.nextId(), rulePO, date.atTime(startTimeOfDay), date.atTime(endTimeOfDay), Boolean.TRUE)).toList();

            scheduleMapper.insert(list);

            copyRuleLocationToSchedules(rulePO, list);
            return list;
        }

        LocalDate singleDate = ruleStartDate != null ? ruleStartDate : LocalDate.now();
        SchedulePO po = ScheduleAssembler.buildSchedulePO(snowflake.nextId(), rulePO,
                singleDate.atTime(startTimeOfDay),
                singleDate.atTime(endTimeOfDay), Boolean.FALSE);
        scheduleMapper.insert(po);
        copyRuleLocationToSchedules(rulePO, Collections.singletonList(po));
        return Collections.singletonList(po);
    }

    /**
     * 将规则位置复制到每条日程的 focus_schedule_location，供详情/列表展示
     */
    private void copyRuleLocationToSchedules(ScheduleRulePO rulePO, List<SchedulePO> schedulePOList) {
        if (rulePO.getLocationId() == null || CollectionUtils.isEmpty(schedulePOList)) {
            return;
        }
        RuleLocationPO ruleLoc = ruleLocationMapper.selectById(rulePO.getLocationId());
        if (ruleLoc == null) {
            return;
        }
        for (SchedulePO schedule : schedulePOList) {
            ScheduleLocationPO scheduleLocationPO = new ScheduleLocationPO();
            scheduleLocationPO.setId(snowflake.nextId());
            scheduleLocationPO.setScheduleId(schedule.getId());
            scheduleLocationPO.setLocationName(ruleLoc.getName());
            scheduleLocationPO.setLocationInfo(ruleLoc.getLocationInfo());
            scheduleLocationPO.setLongitude(ruleLoc.getLongitude());
            scheduleLocationPO.setLatitude(ruleLoc.getLatitude());
            scheduleLocationPO.setProvince(ruleLoc.getProvince());
            scheduleLocationPO.setCity(ruleLoc.getCity());
            scheduleLocationPO.setDistrict(ruleLoc.getDistrict());
            scheduleLocationPO.setTownship(ruleLoc.getTownship());
            scheduleLocationMapper.insert(scheduleLocationPO);
        }
    }

    private static ScheduleLocationPO buildScheduleLocationPO(Long scheduleId, LocationInfoVal val) {
        ScheduleLocationPO po = new ScheduleLocationPO();
        po.setScheduleId(scheduleId);
        po.setLocationName(val.getName());
        String info = StringUtils.hasText(val.getLocation()) ? val.getLocation() : val.getName();
        po.setLocationInfo(info);
        po.setLongitude(val.getLongitude());
        po.setLatitude(val.getLatitude());
        po.setProvince(val.getProvince());
        po.setCity(val.getCity());
        po.setDistrict(val.getDistrict());
        po.setTownship(val.getTownship());
        return po;
    }

    @Override
    public void updateScheduleLocation(Long scheduleId, LocationInfoVal locationInfo) {
        scheduleLocationMapper.delete(
                new LambdaQueryWrapper<ScheduleLocationPO>().eq(ScheduleLocationPO::getScheduleId, scheduleId));
        if (locationInfo != null) {
            ScheduleLocationPO po = buildScheduleLocationPO(scheduleId, locationInfo);
            po.setId(snowflake.nextId());
            scheduleLocationMapper.insert(po);
        }
    }

    @Override
    public void updateRuleAndScheduleLocations(ScheduleRulePO rulePO, List<SchedulePO> scheduleList, LocationInfoVal locationInfo) {
        ruleLocationMapper.delete(
                new LambdaQueryWrapper<RuleLocationPO>().eq(RuleLocationPO::getRuleId, rulePO.getId()));
        rulePO.setLocationId(null);

        if (locationInfo != null) {
            RuleLocationPO ruleLocationPO = buildRuleLocationPO(rulePO.getId(), locationInfo);
            ruleLocationPO.setId(snowflake.nextId());
            ruleLocationMapper.insert(ruleLocationPO);
            rulePO.setLocationId(ruleLocationPO.getId());
        }
        scheduleRuleMapper.updateById(rulePO);

        if (CollectionUtils.isNotEmpty(scheduleList)) {
            List<Long> scheduleIds = scheduleList.stream().map(SchedulePO::getId).collect(Collectors.toList());
            scheduleLocationMapper.delete(
                    new LambdaQueryWrapper<ScheduleLocationPO>().in(ScheduleLocationPO::getScheduleId, scheduleIds));
            copyRuleLocationToSchedules(rulePO, scheduleList);
        }
    }

    @Override
    public void deleteScheduleLocations(List<Long> scheduleIdList) {
        if (CollectionUtils.isEmpty(scheduleIdList)) {
            return;
        }
        scheduleLocationMapper.delete(
                new LambdaQueryWrapper<ScheduleLocationPO>().in(ScheduleLocationPO::getScheduleId, scheduleIdList));
    }

    @Override
    public List<ScheduleRulePO> sysAddSchedule(String userId, List<SysScheduleVal> addScheduleList) {
        if (CollectionUtils.isEmpty(addScheduleList)) {
            return null;
        }

        if (CollectionUtils.isEmpty(addScheduleList)) {
            return null;
        }

        List<ScheduleRulePO> scheduleRulePOList = addScheduleList.stream()
                .map(schedule -> ScheduleRuleAssembler.buildBaseScheduleRulePO(userId, schedule))
                .toList();

        scheduleRuleMapper.insert(scheduleRulePOList);

        return scheduleRulePOList;
    }

    @Override
    public void removeRepeatSchedule(String userId, List<SysScheduleVal> addScheduleList) {
        // 根据 addScheduleList.localSystemId 集合查询已存在的 scheduleRuleList（sysId 对应 localSystemId）
        List<String> localSystemIds = addScheduleList.stream()
                .map(SysScheduleVal::getLocalSystemId)
                .filter(StringUtils::hasText)
                .distinct()
                .collect(Collectors.toList());

        if (CollectionUtils.isEmpty(localSystemIds)) {
            return;
        }

        List<ScheduleRulePO> scheduleRuleList = scheduleRuleMapper.selectList(
                new LambdaQueryWrapper<ScheduleRulePO>()
                        .eq(ScheduleRulePO::getUserId, userId)
                        .eq(ScheduleRulePO::getIsDelete, Boolean.FALSE)
                        .in(ScheduleRulePO::getSysId, localSystemIds));

        AtomicReference<List<String>> sysIdList = new AtomicReference<>(Collections.emptyList());

        Optional.ofNullable(scheduleRuleList)
                .ifPresent(scheduleRules -> sysIdList.set(scheduleRules.stream().map(ScheduleRulePO::getSysId).toList()));

        addScheduleList.removeIf(schedule -> sysIdList.get().contains(schedule.getLocalSystemId()));
    }

    @Override
    public List<SchedulePO> sysUpdateSchedule(String userId, List<SysScheduleVal> updateScheduleList) {
        if (CollectionUtils.isEmpty(updateScheduleList) || !StringUtils.hasText(userId)) {
            return null;
        }
        // 1. 获取 updateScheduleList 中 cloudEventId 集合为 ruleIdList
        List<String> ruleIdList = updateScheduleList.stream()
                .map(SysScheduleVal::getCloudEventId)
                .filter(StringUtils::hasText)
                .distinct()
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(ruleIdList)) {
            return null;
        }
        // 2. 根据 ruleIdList 获取 scheduleRuleList（sys_id 对应 cloudEventId）
        List<ScheduleRulePO> scheduleRuleList = scheduleRuleMapper.selectList(
                new LambdaQueryWrapper<ScheduleRulePO>()
                        .eq(ScheduleRulePO::getUserId, userId)
                        .eq(ScheduleRulePO::getIsDelete, Boolean.FALSE)
                        .in(ScheduleRulePO::getId, ruleIdList));
        if (CollectionUtils.isEmpty(scheduleRuleList)) {
            return null;
        }
        // 3. 将 updateScheduleList 中的值赋值到 scheduleRuleList 中
        Map<String, SysScheduleVal> cloudEventIdToVal = updateScheduleList.stream()
                .filter(v -> StringUtils.hasText(v.getCloudEventId()))
                .collect(Collectors.toMap(SysScheduleVal::getCloudEventId, v -> v, (a, b) -> a));
        for (ScheduleRulePO rule : scheduleRuleList) {
            SysScheduleVal sysVal = cloudEventIdToVal.get(String.valueOf(rule.getId()));
            if (sysVal != null) {
                ScheduleRulePO base = ScheduleRuleAssembler.buildBaseScheduleRulePO(userId, sysVal);
                ScheduleRuleAssembler.copyRuleFieldsFrom(rule, base);
                scheduleRuleMapper.updateById(rule);
            }
        }
        // 4. 根据 ruleIdList 获取到 schedulePOList（按规则 id 查）
        List<Long> ruleIds = scheduleRuleList.stream().map(ScheduleRulePO::getId).collect(Collectors.toList());
        List<SchedulePO> schedulePOList = scheduleMapper.selectList(
                new LambdaQueryWrapper<SchedulePO>()
                        .eq(SchedulePO::getUserId, userId)
                        .eq(SchedulePO::getIsDelete, Boolean.FALSE)
                        .in(SchedulePO::getRuleId, ruleIds));
        if (CollectionUtils.isEmpty(schedulePOList)) {
            return null;
        }
        // 5. 根据更新后的 scheduleRuleList 更新 schedulePOList
        Map<Long, ScheduleRulePO> ruleIdToRule = scheduleRuleList.stream()
                .collect(Collectors.toMap(ScheduleRulePO::getId, r -> r, (a, b) -> a));
        for (SchedulePO schedule : schedulePOList) {
            ScheduleRulePO rule = ruleIdToRule.get(schedule.getRuleId());
            if (rule != null) {
                ScheduleAssembler.updateSchedulePOFromRule(schedule, rule);
                scheduleMapper.updateById(schedule);
            }
        }

        return schedulePOList;
    }

    @Override
    public void sysDelSchedule(String userId, List<SysScheduleVal> deleteScheduleList) {
        if (CollectionUtils.isEmpty(deleteScheduleList) || !StringUtils.hasText(userId)) {
            return;
        }
        // 1. 获取 deleteScheduleList 中 cloudEventId 集合为 ruleIdList
        List<String> ruleIdList = deleteScheduleList.stream()
                .map(SysScheduleVal::getCloudEventId)
                .filter(StringUtils::hasText)
                .distinct()
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(ruleIdList)) {
            return;
        }
        // 2. 根据 ruleIdList 获取 scheduleRuleList 和 schedulePOList
        List<ScheduleRulePO> scheduleRuleList = scheduleRuleMapper.selectList(
                new LambdaQueryWrapper<ScheduleRulePO>()
                        .eq(ScheduleRulePO::getUserId, userId)
                        .eq(ScheduleRulePO::getIsDelete, Boolean.FALSE)
                        .in(ScheduleRulePO::getId, ruleIdList));

        List<Long> ruleIds = scheduleRuleList.stream().map(ScheduleRulePO::getId).collect(Collectors.toList());

        List<SchedulePO> schedulePOList = Collections.emptyList();
        if (CollectionUtils.isNotEmpty(ruleIds)) {
            schedulePOList = scheduleMapper.selectList(
                    new LambdaQueryWrapper<SchedulePO>()
                            .eq(SchedulePO::getUserId, userId)
                            .eq(SchedulePO::getIsDelete, Boolean.FALSE)
                            .in(SchedulePO::getRuleId, ruleIds));
        }

        // 3. 更新 scheduleRuleList 和 schedulePOList 中的 isDelete 为 true
        scheduleRuleMapper.deleteByIds(scheduleRuleList);

        scheduleMapper.deleteByIds(schedulePOList);
    }

    @Override
    public Response<SysScheduleResponseDTO> getSysUpdateScheduleList(String userId, SysScheduleRequestDTO requestDTO) {
        SysScheduleResponseDTO responseDTO = new SysScheduleResponseDTO();
        responseDTO.setUpdateScheduleList(new ArrayList<>());
        responseDTO.setDeleteScheduleList(new ArrayList<>());

        // sysMappingScheduleList：sysId = addScheduleList.localSystemId，serverId = addScheduleList.cloudEventId
        if (CollectionUtils.isNotEmpty(requestDTO.getAddScheduleList())) {
            List<SysMappingScheduleVal> mappingList = new ArrayList<>();
            for (SysScheduleVal val : requestDTO.getAddScheduleList()) {
                SysMappingScheduleVal mapping = new SysMappingScheduleVal();
                mapping.setSysId(val.getLocalSystemId());
                mapping.setServerId(val.getCloudEventId());
                mappingList.add(mapping);
            }
            responseDTO.setSysMappingScheduleList(mappingList);
        }

        if (!StringUtils.hasText(userId)) {
            return Response.newInstance(ResponseStatusBasic.OK, responseDTO);
        }
        // 1. 获取 addScheduleList、updateSchedulList 中的 cloudEventId 集合，和 delScheduleIdList 合并成 sysSyncRuleIdList（规则 id 集合）
        List<String> cloudEventIds = Stream.of(
                        requestDTO.getAddScheduleList(),
                        requestDTO.getUpdateScheduleList())
                .filter(CollectionUtils::isNotEmpty)
                .flatMap(List::stream)
                .map(SysScheduleVal::getCloudEventId)
                .filter(StringUtils::hasText)
                .distinct()
                .collect(Collectors.toList());

        List<String> sysSyncRuleIdList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(requestDTO.getDelScheduleIdList())) {
            sysSyncRuleIdList.addAll(requestDTO.getDelScheduleIdList());
        }
        if (CollectionUtils.isNotEmpty(cloudEventIds)) {
            List<ScheduleRulePO> rulesBySysId = scheduleRuleMapper.selectList(
                    new LambdaQueryWrapper<ScheduleRulePO>()
                            .eq(ScheduleRulePO::getUserId, userId)
                            .eq(ScheduleRulePO::getIsDelete, Boolean.FALSE)
                            .in(ScheduleRulePO::getId, cloudEventIds));
            for (ScheduleRulePO r : rulesBySysId) {
                if (r.getId() != null && !sysSyncRuleIdList.contains(String.valueOf(r.getId()))) {
                    sysSyncRuleIdList.add(String.valueOf(r.getId()));
                }
            }
        }

        // 2. 查询 ScheduleRulePO：isSysSync 为 true，且 id 不在 sysSyncRuleIdList 中
        LambdaQueryWrapper<ScheduleRulePO> ruleQuery = new LambdaQueryWrapper<ScheduleRulePO>()
                .eq(ScheduleRulePO::getUserId, userId)
                .eq(ScheduleRulePO::getIsDelete, Boolean.FALSE)
                .eq(ScheduleRulePO::getIsSysSync, 1);
        if (CollectionUtils.isNotEmpty(sysSyncRuleIdList)) {
            ruleQuery.notIn(ScheduleRulePO::getId, sysSyncRuleIdList);
        }
        List<ScheduleRulePO> ruleList = scheduleRuleMapper.selectList(ruleQuery);
        if (CollectionUtils.isEmpty(ruleList)) {
            return Response.newInstance(ResponseStatusBasic.OK, responseDTO);
        }

        // 3. 封装：isDelete 不为 true 的封装为 SysScheduleVal 加入 updateScheduleList；isDelete 为 true 的 id 加入 deleteScheduleList
        for (ScheduleRulePO rule : ruleList) {
            if (Boolean.TRUE.equals(rule.getIsDelete())) {
                responseDTO.getDeleteScheduleList().add(String.valueOf(rule.getId()));
            } else {
                SysScheduleVal val = SysScheduleValAssembler.buildScheduleVal(rule);
                if (val != null) {
                    responseDTO.getUpdateScheduleList().add(val);
                }
            }
        }

        responseDTO.setLastSyncTime(LocalDateTime.now().toInstant(ZoneOffset.UTC).toEpochMilli());

        return Response.newInstance(ResponseStatusBasic.OK, responseDTO);
    }

    @Override
    public ScheduleRulePO getScheduleRuleById(Long ruleId) {
        if (ruleId == null) {
            return null;
        }
        return scheduleRuleMapper.selectById(ruleId);
    }

    @Override
    public void updateScheduleRule(ScheduleRulePO rule) {
        if (rule != null && rule.getId() != null) {
            scheduleRuleMapper.updateById(rule);
        }
    }

    @Override
    public void batchUpdateScheduleRules(List<ScheduleRulePO> rules) {
        if (CollectionUtils.isNotEmpty(rules)) {
            scheduleRuleMapper.updateById(rules);
        }
    }

    /**
     * 根据 repeatTime、repeatUnit 生成从 from 到 to 的日程日期列表。
     * repeatUnit: D-日，W-周，M-月。repeatTime 示例：D 时为 "1"(每1天) 或 "1,3,5"(每月1/3/5号)；W 时为 "1,3,5"(周一二三或周一三五)；M 时为 "1,15"(每月1号和15号)。
     */
    private List<LocalDate> generateRepeatDates(LocalDate from, LocalDate to, String repeatTime, String repeatUnit) {
        if (!StringUtils.hasText(repeatTime) || !StringUtils.hasText(repeatUnit)) {
            return null;
        }
        String unit = repeatUnit.trim().toUpperCase();
        List<Integer> numbers = parseRepeatTimeNumbers(repeatTime, from, unit);

        if ("D".equals(unit)) {
            if (numbers.isEmpty()) {
                return null;
            }
            int step = numbers.get(0);
            if (numbers.size() == 1) {
                List<LocalDate> list = new ArrayList<>();
                for (LocalDate d = from; !d.isAfter(to); d = d.plusDays(step)) {
                    list.add(d);
                }
                return list;
            }
            List<LocalDate> list = new ArrayList<>();
            YearMonth ymFrom = YearMonth.from(from);
            YearMonth ymTo = YearMonth.from(to);
            for (YearMonth ym = ymFrom; !ym.isAfter(ymTo); ym = ym.plusMonths(1)) {
                for (int dayOfMonth : numbers) {
                    if (dayOfMonth >= 1 && dayOfMonth <= ym.lengthOfMonth()) {
                        LocalDate d = ym.atDay(dayOfMonth);
                        if (!d.isBefore(from) && !d.isAfter(to)) {
                            list.add(d);
                        }
                    }
                }
            }
            return list.stream().sorted().distinct().collect(Collectors.toList());
        }

        if ("W".equals(unit)) {
            if (numbers.isEmpty()) {
                return null;
            }
            List<LocalDate> list = new ArrayList<>();
            for (LocalDate d = from; !d.isAfter(to); d = d.plusWeeks(1)) {
                int dow = d.getDayOfWeek().getValue();
                if (numbers.contains(dow)) {
                    list.add(d);
                }
            }
            return list;
        }

        if ("M".equals(unit)) {
            if (numbers.isEmpty()) {
                return null;
            }
            List<LocalDate> list = new ArrayList<>();
            YearMonth ymFrom = YearMonth.from(from);
            YearMonth ymTo = YearMonth.from(to);
            for (YearMonth ym = ymFrom; !ym.isAfter(ymTo); ym = ym.plusMonths(1)) {
                for (int dayOfMonth : numbers) {
                    if (dayOfMonth < 1) {
                        continue;
                    }
                    int actualDay = Math.min(dayOfMonth, ym.lengthOfMonth());
                    LocalDate candidate = ym.atDay(actualDay);
                    if (!candidate.isBefore(from) && !candidate.isAfter(to)) {
                        list.add(candidate);
                    }
                }
            }
            return list.stream().sorted().distinct().collect(Collectors.toList());
        }

        return Collections.singletonList(from);
    }

    private List<Integer> parseRepeatTimeNumbers(String repeatTime, LocalDate from, String repeatUnit) {
        if (!StringUtils.hasText(repeatUnit)) {
            return Collections.emptyList();
        }
        String unit = repeatUnit.trim().toUpperCase();
        if ("D".equals(unit)) {
            return Collections.singletonList(1);
        }
        if ("W".equals(unit)) {
            int dow = from != null ? from.getDayOfWeek().getValue() : 1;
            return Collections.singletonList(dow);
        }
        if ("M".equals(unit)) {
            int dom = from != null ? from.getDayOfMonth() : 1;
            return Collections.singletonList(dom);
        }
        if ("Y".equals(unit)) {
            return Collections.singletonList(1);
        }
        if (!StringUtils.hasText(repeatTime)) {
            return Collections.emptyList();
        }
        List<Integer> list = new ArrayList<>();
        for (String s : repeatTime.split("[,，\\s]+")) {
            String t = s.trim();
            if (!t.isEmpty()) {
                try {
                    list.add(Integer.parseInt(t));
                } catch (NumberFormatException ignored) {
                }
            }
        }
        return list;
    }

}
