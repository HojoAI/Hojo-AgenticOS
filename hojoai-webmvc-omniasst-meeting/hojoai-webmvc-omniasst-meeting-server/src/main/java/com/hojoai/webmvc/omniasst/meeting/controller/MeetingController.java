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
package com.hojoai.webmvc.omniasst.meeting.controller;

import com.hojoai.common.asserts.Asserts;
import com.hojoai.common.page.IPage;
import com.hojoai.common.response.Response;
import com.hojoai.common.response.ResponseStatusBasic;
import com.hojoai.webmvc.omniasst.meeting.service.MeetingService;
import com.hojoai.webmvc.omniasst.meeting.service.bean.MeetingDetailResponse;
import com.hojoai.webmvc.omniasst.meeting.service.bean.MeetingListRequest;
import com.hojoai.webmvc.omniasst.meeting.service.bean.MeetingListResponse;
import com.hojoai.webmvc.omniasst.meeting.service.bean.TodoStatusUpdateRequest;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

/**
 * 会议 REST 接口（v0）：列表、详情、待办状态与完成态。
 * 依赖 {@link com.hojoai.webmvc.omniasst.meeting.service.MeetingService}，供 Web / 客户端在上下文路径 {@code /omniasst-meeting} 下调用。
 */
@RestController
@RequestMapping("/v0/meeting")
public class MeetingController {

    @Resource
    private MeetingService meetingService;

    /**
     * 分页查询会议列表。
     *
     * @param request 必须包含 userId
     * @return 分页数据
     */
    @PostMapping("/list")
    public Response<IPage<MeetingListResponse>> listMeetings(@RequestBody MeetingListRequest request) {
        Asserts.notNull(request, "request");
        Asserts.notBlank(request.getUserId(), "userId");
        IPage<MeetingListResponse> result = meetingService.listMeetings(request);
        return Response.newInstance(ResponseStatusBasic.OK, result);
    }

    /**
     * 查询单次会议详情。
     *
     * @param meetingId 会议 ID
     * @return 详情聚合
     */
    @GetMapping("/{meetingId}")
    public Response<MeetingDetailResponse> getMeetingDetail(@PathVariable("meetingId") String meetingId) {
        Asserts.notBlank(meetingId, "meetingId");
        MeetingDetailResponse detail = meetingService.getMeetingDetail(meetingId);
        return Response.newInstance(ResponseStatusBasic.OK, detail);
    }

    /**
     * 更新待办项状态（非「完成」布尔切换）。
     *
     * @param request 会议、待办与目标状态
     * @return 成功文案
     */
    @PostMapping("/todo/update-status")
    public Response<String> updateTodoStatus(@RequestBody TodoStatusUpdateRequest request) {
        Asserts.notNull(request, "request");
        Asserts.notBlank(request.getMeetingId(), "meetingId");
        Asserts.notBlank(request.getTodoId(), "todoId");
        Asserts.notNull(request.getStatus(), "status");
        meetingService.updateTodoStatus(request);
        return Response.newInstance(ResponseStatusBasic.OK, "success");
    }

    /**
     * 将待办标记为完成或取消完成（completed 为 0 或 1）。
     *
     * @param request 含 userId、meetingId、todoId、completed
     * @return 成功文案
     * @throws IllegalArgumentException 当 completed 非 0/1 时
     */
    @PostMapping("/todo/complete")
    public Response<String> completeTodo(@RequestBody TodoStatusUpdateRequest request) {
        Asserts.notNull(request, "request");
        Asserts.notBlank(request.getUserId(), "userId");
        Asserts.notBlank(request.getMeetingId(), "meetingId");
        Asserts.notBlank(request.getTodoId(), "todoId");
        Asserts.notNull(request.getCompleted(), "completed");
        if (request.getCompleted() != 0 && request.getCompleted() != 1) {
            throw new IllegalArgumentException("completed 参数值必须为 0 或 1");
        }
        meetingService.completeTodo(request);
        return Response.newInstance(ResponseStatusBasic.OK, "success");
    }
}
