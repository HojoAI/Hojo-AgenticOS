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
package com.hojoai.webmvc.omniasst.meeting.service.impl;

import com.hojoai.common.asserts.Asserts;
import com.hojoai.webmvc.omniasst.meeting.dao.meeting.MeetingSegmentDAO;
import com.hojoai.webmvc.omniasst.meeting.dao.meeting.po.MeetingSegment;
import com.hojoai.webmvc.omniasst.meeting.service.MeetingSegmentPersistenceService;
import com.hojoai.webmvc.omniasst.meeting.service.bean.MeetingSegmentMessage;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

/**
 * 将 WebSocket 侧段落消息落库，处理重复键与幂等。
 */
@Service
public class MeetingSegmentPersistenceServiceImpl implements MeetingSegmentPersistenceService {

    private static final Logger logger = LoggerFactory.getLogger(MeetingSegmentPersistenceServiceImpl.class);

    @Resource
    private MeetingSegmentDAO meetingSegmentDAO;

    @Override
    public void persistSegment(MeetingSegmentMessage message) {
        if (message == null) {
            logger.warn("[段数据持久化] message is null, ignore");
            return;
        }
        logger.info("[段数据持久化] 开始处理, meetingId: {}", message.getMeetingId());
        Asserts.notBlank(message.getMeetingId(), "meetingId");
        Asserts.notNull(message.getSegment(), "segment");
        Asserts.notNull(message.getTimestamp(), "timestamp");

        MeetingSegment segmentBean = message.getSegment();
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
                logger.info("[段数据持久化] 插入成功, meetingId: {}, segmentId: {}, text: [{}], speaker: {}", 
                        message.getMeetingId(), segmentBean.getSegmentId(), segmentBean.getText(), segmentBean.getSpeakerName());
            } else {
                logger.warn("[段数据持久化] 插入返回0行, meetingId: {}, segmentId: {}", 
                        message.getMeetingId(), segmentBean.getSegmentId());
            }
        } catch (DuplicateKeyException e) {
            logger.warn("[段数据持久化] 段已存在(重复segmentId), meetingId: {}, segmentId: {}", 
                    message.getMeetingId(), segmentBean.getSegmentId());
        } catch (Exception e) {
            logger.error("[段数据持久化] 插入失败, meetingId: {}, segmentId: {}", 
                    message.getMeetingId(), segmentBean.getSegmentId(), e);
        }
    }
}

