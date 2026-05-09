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
package com.hojoai.webmvc.omniasst.meeting.dao.meeting.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.hojoai.webmvc.omniasst.meeting.dao.meeting.MeetingSegmentDAO;
import com.hojoai.webmvc.omniasst.meeting.dao.meeting.MeetingSegmentMapper;
import com.hojoai.webmvc.omniasst.meeting.dao.meeting.po.MeetingSegment;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 会议段落表 {@link com.hojoai.webmvc.omniasst.meeting.dao.meeting.po.MeetingSegment} 的持久化实现。
 */
@Repository
public class MeetingSegmentDAOImpl implements MeetingSegmentDAO {

    @Resource
    private MeetingSegmentMapper meetingSegmentMapper;

    @Override
    public int insertSegment(MeetingSegment segment) {
        return meetingSegmentMapper.insert(segment);
    }

    @Override
    public List<MeetingSegment> selectByMeetingId(String meetingId) {
        LambdaQueryWrapper<MeetingSegment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MeetingSegment::getMeetingId, meetingId)
                .orderByAsc(MeetingSegment::getTimestamp);
        return meetingSegmentMapper.selectList(wrapper);
    }

    @Override
    public int deleteByMeetingId(String meetingId) {
        LambdaQueryWrapper<MeetingSegment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MeetingSegment::getMeetingId, meetingId);
        return meetingSegmentMapper.delete(wrapper);
    }

    @Override
    public int updateSpeakerNameByMeetingIdAndSpeakerId(String meetingId, String speakerId, String speakerName) {
        LambdaUpdateWrapper<MeetingSegment> uw = new LambdaUpdateWrapper<>();
        uw.set(MeetingSegment::getSpeakerName, speakerName)
                .eq(MeetingSegment::getMeetingId, meetingId)
                .eq(MeetingSegment::getSpeakerId, speakerId);
        return meetingSegmentMapper.update(null, uw);
    }

    @Override
    public int updateMarkJsonByMeetingIdAndSegmentId(String meetingId, String segmentId, String markJson) {
        LambdaUpdateWrapper<MeetingSegment> uw = new LambdaUpdateWrapper<>();
        uw.set(MeetingSegment::getMarkJson, markJson)
                .eq(MeetingSegment::getMeetingId, meetingId)
                .eq(MeetingSegment::getSegmentId, segmentId);
        return meetingSegmentMapper.update(null, uw);
    }
}

