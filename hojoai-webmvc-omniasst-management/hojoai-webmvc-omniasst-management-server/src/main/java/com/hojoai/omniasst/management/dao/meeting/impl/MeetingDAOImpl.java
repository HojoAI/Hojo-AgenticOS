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
package com.hojoai.omniasst.management.dao.meeting.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hojoai.omniasst.management.dao.meeting.MeetingDAO;
import com.hojoai.omniasst.management.dao.meeting.MeetingMapper;
import com.hojoai.omniasst.management.dao.meeting.po.Meeting;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Repository;

/**
 * The DAO implementation for meeting.
 *
 * @author zhangxuepeng
 * @version 1.0, 2025/12/25
 * @since hojoai-webmvc-omniasst-management 1.0.0
 */
@Repository
public class MeetingDAOImpl implements MeetingDAO {

    @Resource
    private MeetingMapper meetingMapper;

    @Override
    public Meeting selectByMeetingId(String meetingId) {
        LambdaQueryWrapper<Meeting> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Meeting::getMeetingId, meetingId);
        return meetingMapper.selectOne(wrapper);
    }

    @Override
    public int insertMeeting(Meeting meeting) {
        return meetingMapper.insert(meeting);
    }

    @Override
    public int updateMeetingByMeetingId(Meeting meeting) {
        LambdaUpdateWrapper<Meeting> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(Meeting::getMeetingId, meeting.getMeetingId());
        return meetingMapper.update(meeting, wrapper);
    }

    @Override
    public Page<Meeting> selectMeetingPage(Page<Meeting> page, LambdaQueryWrapper<Meeting> wrapper) {
        return meetingMapper.selectPage(page, wrapper);
    }
}

