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
package com.hojoai.webmvc.omniasst.meeting.service;

import com.hojoai.common.page.IPage;
import com.hojoai.webmvc.omniasst.meeting.service.bean.MeetingDetailMarkRequest;
import com.hojoai.webmvc.omniasst.meeting.service.bean.MeetingDetailMarkResponse;
import com.hojoai.webmvc.omniasst.meeting.service.bean.MeetingDetailResponse;
import com.hojoai.webmvc.omniasst.meeting.service.bean.MeetingListRequest;
import com.hojoai.webmvc.omniasst.meeting.service.bean.MeetingListResponse;
import com.hojoai.webmvc.omniasst.meeting.service.bean.MeetingSpeakerRenameRequest;
import com.hojoai.webmvc.omniasst.meeting.service.bean.TodoStatusUpdateRequest;

public interface MeetingService {

    /**
     * 分页查询用户的会议列表。
     *
     * @param request 查询条件（含 userId、分页等）
     * @return 分页结果
     */
    IPage<MeetingListResponse> listMeetings(MeetingListRequest request);

    /**
     * 按会议 ID 查询详情（含段落、待办等聚合信息）。
     *
     * @param meetingId 会议 ID
     * @return 详情；不存在时由实现层决定是否为 null 或抛错
     */
    MeetingDetailResponse getMeetingDetail(String meetingId);

    /**
     * 更新会议待办项状态（非完成态切换）。
     *
     * @param request 会议 ID、待办 ID、目标状态等
     */
    void updateTodoStatus(TodoStatusUpdateRequest request);

    /**
     * 将待办标记为完成或取消完成。
     *
     * @param request 含 userId、meetingId、todoId、completed（0/1）
     */
    void completeTodo(TodoStatusUpdateRequest request);

    /** 修改某说话人在全会议段上的展示名，并刷新 segments_json、attendees */
    void renameSpeaker(MeetingSpeakerRenameRequest request);

    /** 会议详情页打标：写 mark_json 并刷新 meetings.segments_json、attendees */
    MeetingDetailMarkResponse markOnMeetingDetail(MeetingDetailMarkRequest request);
}
