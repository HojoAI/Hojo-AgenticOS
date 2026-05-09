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
import com.hojoai.common.response.Response;
import com.hojoai.common.response.ResponseStatusBasic;
import com.hojoai.webmvc.omniasst.meeting.service.MeetingService;
import com.hojoai.webmvc.omniasst.meeting.service.bean.MeetingDetailMarkRequest;
import com.hojoai.webmvc.omniasst.meeting.service.bean.MeetingDetailMarkResponse;
import com.hojoai.webmvc.omniasst.meeting.service.bean.MeetingSpeakerRenameRequest;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 会议 REST 接口（v1）：详情打标、说话人重命名等。
 * 依赖 {@link com.hojoai.webmvc.omniasst.meeting.service.MeetingService}。
 */
@RestController
@RequestMapping("/v1/meeting")
public class MeetingV1Controller {

    @Resource
    private MeetingService meetingService;

    /**
     * 会议详情时间轴打标（{@code /detail/mark} 与 {@code /segment/mark} 等价路由）。
     *
     * @param request 会议、用户、段落、offset 等
     * @return 打标结果
     */
    @PostMapping({"/detail/mark", "/segment/mark"})
    public Response<MeetingDetailMarkResponse> markOnMeetingDetail(@RequestBody MeetingDetailMarkRequest request) {
        Asserts.notNull(request, "request");
        Asserts.notBlank(request.getMeetingId(), "meetingId");
        Asserts.notBlank(request.getUserId(), "userId");
        Asserts.notBlank(request.getSegmentId(), "segmentId");
        Asserts.notNull(request.getOffsetMs(), "offsetMs");
        MeetingDetailMarkResponse result = meetingService.markOnMeetingDetail(request);
        return Response.newInstance(ResponseStatusBasic.OK, result);
    }

    /**
     * 重命名某一说话人在全会议段落上的展示名。
     *
     * @param request 会议、用户、speakerId、新名称
     * @return 成功文案
     */
    @PostMapping("/speaker/rename")
    public Response<String> renameSpeaker(@RequestBody MeetingSpeakerRenameRequest request) {
        Asserts.notNull(request, "request");
        Asserts.notBlank(request.getMeetingId(), "meetingId");
        Asserts.notBlank(request.getUserId(), "userId");
        Asserts.notBlank(request.getSpeakerId(), "speakerId");
        Asserts.notBlank(request.getSpeakerName(), "speakerName");
        meetingService.renameSpeaker(request);
        return Response.newInstance(ResponseStatusBasic.OK, "success");
    }
}
