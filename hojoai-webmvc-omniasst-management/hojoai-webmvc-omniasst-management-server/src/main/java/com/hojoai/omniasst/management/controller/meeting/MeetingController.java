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
package com.hojoai.omniasst.management.controller.meeting;

import com.hojoai.common.asserts.Asserts;
import com.hojoai.common.page.IPage;
import com.hojoai.common.response.Response;
import com.hojoai.omniasst.management.service.meeting.MeetingService;
import com.hojoai.omniasst.management.service.meeting.bean.MeetingDetailResponse;
import com.hojoai.omniasst.management.service.meeting.bean.MeetingListRequest;
import com.hojoai.omniasst.management.service.meeting.bean.MeetingListResponse;
import com.hojoai.omniasst.management.utils.ResultUtils;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * The controller for meeting list and detail.
 *
 * @author zhangxuepeng
 * @version 1.0, 2025/12/16
 * @since hojoai-webmvc-omniasst-management 1.0.0
 */
@RestController
@RequestMapping("/v0/meeting")
public class MeetingController {

    @Resource
    private MeetingService meetingService;

    /**
     * List meetings by user (POST).
     */
    @PostMapping("/list")
    public Response<IPage<MeetingListResponse>> listMeetings(@RequestBody MeetingListRequest request) {
        Asserts.notNull(request, "request");
        Asserts.notBlank(request.getUserId(), "userId");
        IPage<MeetingListResponse> result = meetingService.listMeetings(request);
        return ResultUtils.Ok(result);
    }

    /**
     * Get meeting detail by meeting id (GET).
     */
    @GetMapping("/{meetingId}")
    public Response<MeetingDetailResponse> getMeetingDetail(@PathVariable("meetingId") String meetingId) {
        Asserts.notBlank(meetingId, "meetingId");
        MeetingDetailResponse detail = meetingService.getMeetingDetail(meetingId);
        return ResultUtils.Ok(detail);
    }
}
