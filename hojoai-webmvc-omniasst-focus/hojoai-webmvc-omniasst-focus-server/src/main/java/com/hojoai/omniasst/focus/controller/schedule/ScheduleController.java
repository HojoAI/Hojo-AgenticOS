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
import com.hojoai.omniasst.focus.common.ResponseStatusWithMessage;
import com.hojoai.omniasst.focus.dto.request.*;
import com.hojoai.omniasst.focus.dto.response.CreateScheduleResponseDTO;
import com.hojoai.omniasst.focus.dto.response.ScheduleDetailResponseDTO;
import com.hojoai.omniasst.focus.dto.response.ScheduleResponseDTO;
import com.hojoai.omniasst.focus.repository.po.SchedulePO;
import com.hojoai.omniasst.focus.service.ScheduleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Objects;

/**
 * Schedule Controller
 * Responsibility: Receive HTTP requests, validate parameters, call Service layer
 *
 * @author My
 * @version 1.0 2024/12/29
 * @since hojoai-webmvc-omniasst.focus 1.0.0
 */
@RestController
@RequestMapping("/api/v1")
public class ScheduleController {

    private static final Logger logger = LoggerFactory.getLogger(ScheduleController.class);

    @Resource
    private ScheduleService scheduleService;

    /**
     * Create schedule from AI (natural language) - Stream Response
     * POST /api/v1/schedule/{userId}
     * 使用AI解析用户的自然语言描述，自动创建日程
     * 使用流式响应返回AI生成的内容
     *
     * @param userId 用户ID
     * @param requestDTO 对话创建日程请求
     * @return AI流式响应 (Server-Sent Events)
     */
    @PostMapping(value = "/schedule/conversation/{userId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter conversationCreateSchedule(
            @PathVariable("userId") String userId,
            @RequestBody ConversationCreateScheduleRequestDTO requestDTO) {
        
        logger.info("收到AI创建日程请求 - userId: {}, request: {}", userId, JacksonUtil.bean2Json(requestDTO));
        
        // 创建 SseEmitter，设置超时时间为30分钟
        SseEmitter emitter = new SseEmitter(30 * 60 * 1000L);
        
        try {
            // 参数验证
            if (userId == null || userId.trim().isEmpty()) {
                logger.warn("创建日程请求失败: userId为空");
                emitter.completeWithError(new IllegalArgumentException("userId不能为空"));
                return emitter;
            }
            if (requestDTO.getQueryStr() == null || requestDTO.getQueryStr().trim().isEmpty()) {
                logger.warn("创建日程请求失败: queryStr为空");
                emitter.completeWithError(new IllegalArgumentException("queryStr不能为空"));
                return emitter;
            }
            
            // 异步调用Service层进行领域编排
            scheduleService.createScheduleFromAI(userId, requestDTO.getConversationId(), requestDTO.getQueryStr(), emitter);
            
        } catch (Exception e) {
            logger.error("AI创建日程失败 - userId: {}, talkId: {}, error: {}", 
                    userId, requestDTO.getConversationId(), e.getMessage(), e);
            emitter.completeWithError(e);
        }
        
        return emitter;
    }

    /**
     * Create schedule from meeting AI (natural language) - Non-stream Response
     * POST /api/v1/schedule/meeting/{userId}
     * 使用AI解析会议相关的自然语言描述，自动创建日程
     * 
     * @param userId 用户ID
     * @param requestDTO 会议创建日程请求
     * @return 创建结果消息
     */
    @PostMapping(value = "/schedule/meeting/{userId}")
    public Response<String> meetingCreateSchedule(
            @PathVariable("userId") String userId,
            @RequestBody MeetingCreateScheduleRequestDTO requestDTO) {
        
        logger.info("收到Meeting AI创建日程请求 - userId: {}, request: {}", userId, JacksonUtil.bean2Json(requestDTO));
        
        try {
            // 参数验证
            if (userId == null || userId.trim().isEmpty()) {
                logger.warn("创建日程请求失败: userId为空");
                return Response.newInstance(ResponseStatusBasic.ILLEGAL_ARGUMENT, null);
            }
            if (requestDTO.getQueryStr() == null || requestDTO.getQueryStr().trim().isEmpty()) {
                logger.warn("创建日程请求失败: queryStr为空");
                return Response.newInstance(ResponseStatusBasic.ILLEGAL_ARGUMENT, null);
            }

            SchedulePO schedulePO = scheduleService.getScheduleByUserIdAndMeetingId(userId, requestDTO.getMeetingId());

            if (Objects.nonNull(schedulePO)) {
                logger.warn("创建日程请求失败: 该会议已创建日程");
                return Response.newInstance(ResponseStatusBasic.GENERAL_ERROR, null);
            }

            // 调用Service层进行领域编排
            String scheduleId = scheduleService.meetingCreateScheduleFromAI(userId, requestDTO);
            
            logger.info("Meeting AI创建日程成功 - userId: {}, result: {}", userId, scheduleId);
            return Response.newInstance(ResponseStatusBasic.OK, scheduleId);
            
        } catch (Exception e) {
            logger.error("Meeting AI创建日程失败 - userId: {}, meetingId: {}, error: {}", 
                    userId, requestDTO.getMeetingId(), e.getMessage(), e);
            return Response.newInstance(ResponseStatusBasic.GENERAL_ERROR, null);
        }
    }

    /**
     * Create schedule
     * POST /api/v1/schedule
     * Automatically detect time conflicts, generate conflict suggestions, create reminder records
     *
     * @param request Create schedule request
     * @return Created schedule list with ID, title, and status
     */
    @PostMapping("/schedule")
    public Response<List<CreateScheduleResponseDTO>> createSchedule(@RequestBody CreateScheduleRequestDTO request) {
        try {
            List<CreateScheduleResponseDTO> result = scheduleService.createSchedule(request);
            return Response.newInstance(ResponseStatusBasic.OK, result);
        } catch (Exception e) {
            logger.error("Create schedule failed, userId: {}", request.getUserId(), e);
            return Response.newInstance(ResponseStatusBasic.GENERAL_ERROR, null);
        }
    }

    /**
     * Card create schedules
     * POST /api/v1/card/schedule
     * Update isCreate from false to true for specified schedules
     *
     * @param userId User ID from path variable
     * @param request Card create request with scheduleIdList
     * @return Created schedule list with ID, title, and status
     */
    @PostMapping("/schedule/card/{userId}")
    public Response<List<CreateScheduleResponseDTO>> cardCreateSchedule(@PathVariable("userId") String userId,
                                                                        @RequestBody CardCreateScheduleRequestDTO request) {
        try {
            List<CreateScheduleResponseDTO> result = scheduleService.cardCreateSchedule(userId, request);
            return Response.newInstance(ResponseStatusBasic.OK, result);
        } catch (Exception e) {
            logger.error("Card create schedules failed, userId: {}", userId, e);
            return Response.newInstance(ResponseStatusBasic.GENERAL_ERROR, null);
        }
    }

    /**
     *Query ScheduleList
     * GET /api/v1/schedules/{userId}?date=xxx
     * @param userId 用户ID（路径变量）
     * @param date   日期，必填，格式 yyyy-MM-dd
     * @return resContent.dailyBriefing + resContent.scheduleList（ScheduleVal 列表）
     */
    @GetMapping("/schedules/{userId}")
    public Response<ScheduleResponseDTO> queryScheduleList(
            @PathVariable("userId") String userId,
            @RequestParam(value = "date") String date,
            @RequestParam(value = "isShowSysSync") String isShowSysSync) {
        logger.info("Query schedule list request: userId={}, date={}", userId, date);
        try {
            if (userId == null || userId.trim().isEmpty()) {
                logger.warn("Query schedule list: userId is empty");
                return Response.newInstance(ResponseStatusBasic.ILLEGAL_ARGUMENT, null);
            }
            if (date == null || date.trim().isEmpty()) {
                logger.warn("Query schedule list: date is empty");
                return Response.newInstance(ResponseStatusBasic.ILLEGAL_ARGUMENT, null);
            }
            LocalDate localDate;
            try {
                localDate = LocalDate.parse(date, DateTimeFormatter.ISO_LOCAL_DATE);
            } catch (DateTimeParseException e) {
                logger.warn("Query schedule list: invalid date format, date={}", date);
                return Response.newInstance(ResponseStatusBasic.ILLEGAL_ARGUMENT, null);
            }
            long dateTs = localDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
            QueryScheduleByDateRequestDTO request = new QueryScheduleByDateRequestDTO();
            request.setUserId(userId);
            request.setDate(dateTs);
            request.setIsShowSysSync(isShowSysSync);
            ScheduleResponseDTO result = scheduleService.queryScheduleByDate(request);
            return Response.newInstance(ResponseStatusBasic.OK, result);
        } catch (Exception e) {
            logger.error("Query schedule list failed, userId: {}, date: {}, error: {}", userId, date, e.getMessage(), e);
            String msg = e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
            return Response.newInstance(new ResponseStatusWithMessage(ResponseStatusBasic.GENERAL_ERROR.getResCode(), msg), null);
        }
    }

    /**
     * Query schedule detail
     * GET /api/v1/schedule/{userId}?scheduleId=xxx
     * Content-Type: application/x-www-form-urlencoded（GET 无 body，参数通过 Query 传递）
     * 根据用户 ID 和日程 ID 查询日程详情（含航班/火车等附属信息）
     *
     * @param userId     用户 ID（路径变量）
     * @param scheduleId 日程 ID（查询参数，必填）
     * @return 日程详情
     */
    @GetMapping("/schedule/{userId}")
    public Response<ScheduleDetailResponseDTO> queryScheduleDetail(
            @PathVariable("userId") String userId,
            @RequestParam("scheduleId") String scheduleId) {
        try {
            if (userId == null || userId.trim().isEmpty()) {
                logger.warn("Query schedule detail: userId is empty");
                return Response.newInstance(ResponseStatusBasic.ILLEGAL_ARGUMENT, null);
            }
            if (scheduleId == null || scheduleId.trim().isEmpty()) {
                logger.warn("Query schedule detail: scheduleId is empty");
                return Response.newInstance(ResponseStatusBasic.ILLEGAL_ARGUMENT, null);
            }
            QueryScheduleDetailRequestDTO request = new QueryScheduleDetailRequestDTO();
            request.setUserId(userId);
            request.setScheduleId(scheduleId);
            ScheduleDetailResponseDTO result = scheduleService.queryScheduleDetail(request);
            if (result == null) {
                return Response.newInstance(new ResponseStatusWithMessage(ResponseStatusBasic.GENERAL_ERROR.getResCode(), "日程不存在"), null);
            }
            return Response.newInstance(ResponseStatusBasic.OK, result);
        } catch (IllegalArgumentException e) {
            logger.warn("Query schedule detail invalid param, userId: {}, scheduleId: {}, error: {}", userId, scheduleId, e.getMessage());
            return Response.newInstance(ResponseStatusBasic.ILLEGAL_ARGUMENT, null);
        } catch (Exception e) {
            logger.error("Query schedule detail failed, userId: {}, scheduleId: {}", userId, scheduleId, e);
            return Response.newInstance(ResponseStatusBasic.GENERAL_ERROR, null);
        }
    }

    /**
     * Update schedule (PATCH)
     * PATCH /api/v1/schedule/{userId}/{scheduleId}
     * Update schedule information
     *
     * @param userId     User ID from path variable
     * @param scheduleId Schedule ID from path variable
     * @param request    Update request body
     * @return Update result
     */
    @PatchMapping("/schedule/{userId}/{scheduleId}")
    public Response<Boolean> updateSchedule(
            @PathVariable("userId") String userId,
            @PathVariable("scheduleId") String scheduleId,
            @RequestBody UpdateScheduleRequestDTO request) {
        try {
            // Set userId from path variable
            request.setUserId(userId);
            
            // Call service
            Boolean result = scheduleService.updateSchedule(scheduleId, request);
            return Response.newInstance(ResponseStatusBasic.OK, result);
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid request for update schedule: userId={}, scheduleId={}, error={}", 
                    userId, scheduleId, e.getMessage());
            return Response.newInstance(ResponseStatusBasic.ILLEGAL_ARGUMENT, null);
        } catch (Exception e) {
            logger.error("Update schedule failed, userId: {}, scheduleId: {}", userId, scheduleId, e);
            return Response.newInstance(ResponseStatusBasic.GENERAL_ERROR, null);
        }
    }

}
