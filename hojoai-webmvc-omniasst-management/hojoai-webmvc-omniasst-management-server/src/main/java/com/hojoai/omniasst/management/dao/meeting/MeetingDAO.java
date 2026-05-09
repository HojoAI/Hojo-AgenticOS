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

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hojoai.omniasst.management.dao.meeting.po.Meeting;

/**
 * The DAO interface for meeting.
 *
 * @author zhangxuepeng
 * @version 1.0, 2025/12/25
 * @since hojoai-webmvc-omniasst-management 1.0.0
 */
public interface MeetingDAO {

    /**
     * Select meeting by meeting id.
     *
     * @param meetingId meeting id
     * @return meeting or null if not exists
     */
    Meeting selectByMeetingId(String meetingId);

    /**
     * Insert meeting.
     *
     * @param meeting meeting entity
     * @return affected rows
     */
    int insertMeeting(Meeting meeting);

    /**
     * Update meeting by meeting id.
     *
     * @param meeting meeting entity
     * @return affected rows
     */
    int updateMeetingByMeetingId(Meeting meeting);

    /**
     * Select meetings page by wrapper.
     *
     * @param page    page object
     * @param wrapper query wrapper
     * @return page result
     */
    Page<Meeting> selectMeetingPage(Page<Meeting> page, LambdaQueryWrapper<Meeting> wrapper);
}

