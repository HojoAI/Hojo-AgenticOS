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
package com.hojoai.omniasst.focus.service;

import com.hojoai.omniasst.focus.dto.request.*;
import com.hojoai.omniasst.focus.dto.response.CreateScheduleResponseDTO;
import com.hojoai.omniasst.focus.dto.response.ScheduleDetailResponseDTO;
import com.hojoai.omniasst.focus.dto.response.ScheduleResponseDTO;
import com.hojoai.omniasst.focus.repository.po.SchedulePO;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

/**
 * Schedule Service Interface
 *
 * @author My
 * @version 1.0 2024/12/29
 * @since hojoai-webmvc-omniasst.focus 1.0.0
 */
public interface ScheduleService {

    /**
     * Query schedule list by user ID and date
     *
     * @param request Query request
     * @return Schedule response with daily briefing and schedule list
     */
    ScheduleResponseDTO queryScheduleByDate(QueryScheduleByDateRequestDTO request) throws Exception;

    /**
     * Query schedule detail by user ID and schedule ID
     *
     * @param request Query request
     * @return Schedule detail
     */
    ScheduleDetailResponseDTO queryScheduleDetail(QueryScheduleDetailRequestDTO request) throws Exception;

    /**
     * Update schedule
     *
     * @param scheduleId Schedule ID
     * @param request    Update request
     * @return Update result
     */
    Boolean updateSchedule(String scheduleId, UpdateScheduleRequestDTO request) throws Exception;

    /**
     * Create schedule
     *
     * @param request Create request
     * @return Created schedule list with ID, title, and status
     */
    List<CreateScheduleResponseDTO> createSchedule(CreateScheduleRequestDTO request) throws Exception;

    /**
     * Card create schedules (update isCreate from false to true)
     *
     * @param userId User ID
     * @param request Card create request
     * @return Created schedule list with ID, title, and status
     */
    List<CreateScheduleResponseDTO> cardCreateSchedule(String userId, CardCreateScheduleRequestDTO request) throws Exception;

    /**
     * Create schedule from AI (natural language) - Stream Response
     * 领域编排：调用AI解析Domain + 调用Schedule创建Domain
     *
     * @param userId User ID
     * @param conversationId Talk ID
     * @param queryStr Natural language description
     * @param emitter SSE emitter for streaming response
     */
    void createScheduleFromAI(String userId, String conversationId, String queryStr, SseEmitter emitter) throws Exception;

    /**
     * Create schedule from meeting AI (natural language) - Non-stream Response
     * 领域编排：调用AI解析Domain + 调用Schedule创建Domain
     *
     * @param userId User ID
     * @param request Meeting create schedule request
     * @return Success message
     * @throws Exception If creation fails
     */
    String meetingCreateScheduleFromAI(String userId, MeetingCreateScheduleRequestDTO request) throws Exception;

    SchedulePO getScheduleByUserIdAndMeetingId(String userId, String meetingId);

    /**
     * 根据 userId、scheduleId 及 deleteType 删除日程（330 新增，逻辑删除）
     * <p>
     * deleteType=1：当前日程；2：当前及后续日程；3：所有日程（同规则）。2/3 的 ruleId 从当前日程带出。
     * </p>
     *
     * @param scheduleId 日程 ID（路径参数）
     * @param deleteType 1-当前日程；2-当前及后续日程；3-所有日程
     * @param request    请求体：userId 必填
     * @return 系统同步日历id
     */
    String deleteSchedule(Long scheduleId, String deleteType, DeleteScheduleRequestDTO request);

}
