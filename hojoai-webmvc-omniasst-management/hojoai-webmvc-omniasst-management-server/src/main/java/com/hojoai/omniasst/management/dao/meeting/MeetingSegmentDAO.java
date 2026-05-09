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
package com.hojoai.omniasst.management.dao.meeting;

import com.hojoai.omniasst.management.dao.meeting.po.MeetingSegment;

import java.util.List;

/**
 * The DAO interface for meeting segment.
 *
 * @author zhangxuepeng
 * @version 1.0, 2025/12/25
 * @since hojoai-webmvc-omniasst-management 1.0.0
 */
public interface MeetingSegmentDAO {

    /**
     * Insert meeting segment.
     *
     * @param segment meeting segment entity
     * @return affected rows
     */
    int insertSegment(MeetingSegment segment);

    /**
     * Select all segments by meeting id, ordered by timestamp ASC.
     *
     * @param meetingId meeting id
     * @return list of segments
     */
    List<MeetingSegment> selectByMeetingId(String meetingId);

    /**
     * Delete all segments by meeting id.
     *
     * @param meetingId meeting id
     * @return affected rows
     */
    int deleteByMeetingId(String meetingId);
}

