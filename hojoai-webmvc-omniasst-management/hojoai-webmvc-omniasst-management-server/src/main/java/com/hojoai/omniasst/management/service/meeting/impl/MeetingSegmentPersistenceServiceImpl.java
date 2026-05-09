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
package com.hojoai.omniasst.management.service.meeting.impl;

import com.hojoai.common.asserts.Asserts;
import com.hojoai.omniasst.management.dao.meeting.MeetingSegmentDAO;
import com.hojoai.omniasst.management.dao.meeting.po.MeetingSegment;
import com.hojoai.omniasst.management.service.meeting.MeetingSegmentPersistenceService;
import com.hojoai.omniasst.management.service.meeting.bean.MeetingSegmentMessage;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

/**
 * The service implementation for meeting segment persistence.
 *
 * @author zhangxuepeng
 * @version 1.0, 2025/12/25
 * @since hojoai-webmvc-omniasst-management 1.0.0
 */
@Service
public class MeetingSegmentPersistenceServiceImpl implements MeetingSegmentPersistenceService {

    private static final Logger logger = LoggerFactory.getLogger(MeetingSegmentPersistenceServiceImpl.class);

    @Resource
    private MeetingSegmentDAO meetingSegmentDAO;

    @Override
    public void persistSegment(MeetingSegmentMessage message) {
        if (message == null) {
            logger.warn("Meeting segment message is null, ignore.");
            return;
        }

        Asserts.notBlank(message.getMeetingId(), "meetingId");
        Asserts.notNull(message.getSegment(), "segment");
        Asserts.notNull(message.getTimestamp(), "timestamp");

        com.hojoai.omniasst.management.service.meeting.bean.MeetingSegment segmentBean = message.getSegment();
        Asserts.notBlank(segmentBean.getSegmentId(), "segment.segmentId");
        Asserts.notBlank(segmentBean.getText(), "segment.text");
        Asserts.notNull(segmentBean.getStartTime(), "segment.startTime");
        Asserts.notNull(segmentBean.getEndTime(), "segment.endTime");
        Asserts.notNull(segmentBean.getDuration(), "segment.duration");

        MeetingSegment segment = new MeetingSegment();
        segment.setMeetingId(message.getMeetingId());
        segment.setSegmentId(segmentBean.getSegmentId());
        segment.setText(segmentBean.getText());
        segment.setSpeakerId(segmentBean.getSpeakerId());
        segment.setSpeakerName(segmentBean.getSpeakerName());
        segment.setSpeakerAvatar(segmentBean.getSpeakerAvatar());
        segment.setStartTime(segmentBean.getStartTime());
        segment.setEndTime(segmentBean.getEndTime());
        segment.setDuration(segmentBean.getDuration());
        segment.setTimestamp(message.getTimestamp());

        try {
            int rows = meetingSegmentDAO.insertSegment(segment);
            if (rows > 0) {
                logger.info("Insert meeting segment success, meetingId: {}, segmentId: {}, timestamp: {}", 
                        message.getMeetingId(), segmentBean.getSegmentId(), message.getTimestamp());
            } else {
                logger.warn("Insert meeting segment returned 0 rows, meetingId: {}, segmentId: {}", 
                        message.getMeetingId(), segmentBean.getSegmentId());
            }
        } catch (DuplicateKeyException e) {
            logger.warn("Meeting segment already exists (duplicate segmentId), meetingId: {}, segmentId: {}", 
                    message.getMeetingId(), segmentBean.getSegmentId());
        } catch (Exception e) {
            logger.error("Failed to insert meeting segment, meetingId: {}, segmentId: {}, error: {}", 
                    message.getMeetingId(), segmentBean.getSegmentId(), e.getMessage(), e);
        }
    }
}

