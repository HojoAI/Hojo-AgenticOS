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
package com.hojoai.webmvc.omniasst.meeting.dao.meeting;

import com.hojoai.webmvc.omniasst.meeting.dao.meeting.po.MeetingSegment;

import java.util.List;

public interface MeetingSegmentDAO {

    int insertSegment(MeetingSegment segment);

    List<MeetingSegment> selectByMeetingId(String meetingId);

    int deleteByMeetingId(String meetingId);

    /**
     * 将指定会议下某 speakerId 的所有段的展示名批量更新（用户修改发言人）。
     */
    int updateSpeakerNameByMeetingIdAndSpeakerId(String meetingId, String speakerId, String speakerName);

    /**
     * 更新某段的打标 JSON（段已落库后调用）。
     */
    int updateMarkJsonByMeetingIdAndSegmentId(String meetingId, String segmentId, String markJson);
}

