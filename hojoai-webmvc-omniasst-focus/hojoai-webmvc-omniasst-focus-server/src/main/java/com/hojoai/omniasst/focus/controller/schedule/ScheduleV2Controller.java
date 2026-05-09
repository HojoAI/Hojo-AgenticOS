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
package com.hojoai.omniasst.focus.controller.schedule;

import com.hojoai.common.jackson.JacksonUtil;
import com.hojoai.common.response.Response;
import com.hojoai.common.response.ResponseStatusBasic;
import com.hojoai.omniasst.focus.dto.request.DeleteScheduleRequestDTO;
import com.hojoai.omniasst.focus.dto.request.ManualModifyScheduleRequestDTO;
import com.hojoai.omniasst.focus.dto.request.ManualScheduleRequestDTO;
import com.hojoai.omniasst.focus.dto.request.SysScheduleRequestDTO;
import com.hojoai.omniasst.focus.dto.response.SysModifyScheduleResponseDTO;
import com.hojoai.omniasst.focus.dto.response.SysScheduleResponseDTO;
import com.hojoai.omniasst.focus.repository.po.SchedulePO;
import com.hojoai.omniasst.focus.repository.po.ScheduleRulePO;
import com.hojoai.omniasst.focus.service.ScheduleRuleService;
import com.hojoai.omniasst.focus.service.ScheduleService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import xyz.downgoon.snowflake.Snowflake;

import javax.annotation.Resource;
import java.util.List;

/**
 * 日程 API v2：手动创建/更新/删除、系统同步等（与 v1 并存，按客户端约定选用）。
 */
@RestController
@RequestMapping("/api/v2")
public class ScheduleV2Controller {

    private static final Logger logger = LoggerFactory.getLogger(ScheduleV2Controller.class);

    @Resource
    private ScheduleRuleService scheduleRuleService;

    @Resource
    private Snowflake snowflake;

    @Resource
    private ScheduleService scheduleService;

    /**
     * Delete schedule
     * DELETE /api/v2/schedule/{scheduleId}?deleteType=1|2|3
     * Body: userId（必填）。2/3 的规则从当前日程的 rule_id 带出，无需传 scheduleRuleId。
     *
     * @param scheduleId 日程 ID
     * @param deleteType 1-当前日程；2-当前及后续日程；3-所有日程（同规则下）
     * @param userId    用户id：userId
     * @param scheduleRuleId    日程规则id：scheduleRuleId
     * @return 统一响应，resContent 为删除结果说明
     */
    @DeleteMapping(value = "/schedule/{scheduleId}")
    public Response<String> deleteSchedule(
            @PathVariable("scheduleId") String scheduleId,
            @RequestParam("deleteType") String deleteType,
            @RequestParam("userId") String userId,
            @RequestParam("scheduleRuleId") String scheduleRuleId) {
        try {
            if (StringUtils.isBlank(scheduleId) || StringUtils.isBlank(deleteType) || StringUtils.isBlank(userId)) {
                logger.warn("deleteSchedule: scheduleId 为空 或 deleteType 为空 或 userId为空");
                return Response.newInstance(ResponseStatusBasic.ILLEGAL_ARGUMENT, null);
            }

            DeleteScheduleRequestDTO request = new DeleteScheduleRequestDTO();
            request.setUserId(userId);
            request.setScheduleRuleId(scheduleRuleId);

            String delScheduleRuleId = scheduleService.deleteSchedule(Long.valueOf(scheduleId), deleteType, request);
            return Response.newInstance(ResponseStatusBasic.OK, delScheduleRuleId);
        } catch (Exception e) {
            logger.error("deleteSchedule 失败, scheduleId={}, deleteType={}", scheduleId, deleteType, e);
            return Response.newInstance(ResponseStatusBasic.GENERAL_ERROR, null);
        }
    }

    /**
     * 创建日程（支持附件）
     * Content-Type: multipart/form-data，part「data」为 JSON 字符串，part「file」为附件（可多个）
     */
    @PostMapping(value = "/schedule/{userId}")
    public Response<String> manualCreateSchedule(
            @PathVariable("userId") String userId,
            @RequestBody ManualScheduleRequestDTO requestDTO) {
        try {
            if (StringUtils.isBlank(userId)) {
                logger.warn("manualCreateSchedule: userId 为空");
                return Response.newInstance(ResponseStatusBasic.ILLEGAL_ARGUMENT, null);
            }
            if (requestDTO.isInvalid()) {
                logger.warn("manualCreateSchedule param error");
                return Response.newInstance(ResponseStatusBasic.ILLEGAL_ARGUMENT, null);
            }
            Pair<ScheduleRulePO, List<SchedulePO>> pair = scheduleRuleService.manualCreateSchedule(userId, requestDTO);
            return Response.newInstance(ResponseStatusBasic.OK, String.valueOf(pair.getLeft().getId()));
        } catch (Exception e) {
            logger.error("manualCreateSchedule 失败, userId={}, type={}", userId, requestDTO != null ? requestDTO.getType() : null, e);
            return Response.newInstance(ResponseStatusBasic.GENERAL_ERROR, null);
        }
    }

    /**
     * 修改日程（支持附件）
     * Content-Type: multipart/form-data，part「data」为 JSON 字符串，part「file」为附件（可多个）
     */
    @PutMapping(value = "/schedule/{scheduleId}")
    public Response<SysModifyScheduleResponseDTO> manualModifySchedule(
            @PathVariable("scheduleId") String scheduleId,
            @RequestParam("modifyType") String modifyType,
            @RequestBody ManualModifyScheduleRequestDTO requestDTO) {
        try {
            if (StringUtils.isBlank(scheduleId)) {
                logger.warn("manualModifySchedule: scheduleId 为空");
                return Response.newInstance(ResponseStatusBasic.ILLEGAL_ARGUMENT, null);
            }
            if (requestDTO.isInvalid()) {
                logger.warn("manualModifySchedule param error");
                return Response.newInstance(ResponseStatusBasic.ILLEGAL_ARGUMENT, null);
            }
            return scheduleRuleService.manualModifySchedule(scheduleId, modifyType, requestDTO);
        } catch (Exception e) {
            logger.error("manualModifySchedule 失败, scheduleId={}, type={}", scheduleId, requestDTO != null ? requestDTO.getType() : null, e);
            return Response.newInstance(ResponseStatusBasic.GENERAL_ERROR, null);
        }
    }

    @PostMapping(value = "/schedule/sys-sync/{userId}")
    public Response<SysScheduleResponseDTO> sysSyncSchedule(@PathVariable("userId") String userId, @RequestBody SysScheduleRequestDTO requestDTO) {
        logger.info("ScheduleV2Controller.sysSyncSchedule.request userId is -> {}, requestDTO is -> {}", userId, JacksonUtil.bean2Json(requestDTO));
        try {
            if (StringUtils.isBlank(userId)) {
                logger.warn("sysSyncSchedule: userId 为空");
                return Response.newInstance(ResponseStatusBasic.ILLEGAL_ARGUMENT, null);
            }

            scheduleRuleService.removeRepeatScheduleList(userId, requestDTO.getAddScheduleList());

            // 根据 addScheduleList 数量用 Snowflake 生成 id，并写入各条 cloudEventId
            if (CollectionUtils.isNotEmpty(requestDTO.getAddScheduleList())) {
                requestDTO.getAddScheduleList().forEach(scheduleVal -> scheduleVal.setCloudEventId(String.valueOf(snowflake.nextId())));
            }

            scheduleRuleService.sysAsyncScheduleHandle(userId, requestDTO);

            return scheduleRuleService.sysSyncScheduleHandle(userId, requestDTO);
        } catch (Exception e) {
            logger.error("sysSyncSchedule 失败, userId={}", userId, e);
            return Response.newInstance(ResponseStatusBasic.GENERAL_ERROR, null);
        }

    }

}
