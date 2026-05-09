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

import com.hojoai.webmvc.omniasst.meeting.service.bean.MeetingSegmentMessage;
import com.hojoai.webmvc.omniasst.meeting.websocket.bean.dto.MeetingResultMessage;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ForkJoinPool;

/**
 * 异步持久化：段落落库与纪要生成等，避免阻塞 WebSocket 线程。
 */
@Service
public class MeetingPersistenceAsyncService {

    private static final Logger logger = LoggerFactory.getLogger(MeetingPersistenceAsyncService.class);

    @Resource
    private MeetingSegmentPersistenceService meetingSegmentPersistenceService;

    @Resource
    private MeetingPersistenceService meetingPersistenceService;

    @Async
    public void persistSegmentAsync(MeetingSegmentMessage message) {
        String meetingId = message != null ? message.getMeetingId() : null;
        String segmentId = message != null && message.getSegment() != null ? message.getSegment().getSegmentId() : null;
        logger.info("[段数据入库开始] meetingId: {}, segmentId: {}", meetingId, segmentId);
        try {
            meetingSegmentPersistenceService.persistSegment(message);
            logger.info("[段数据入库完成] meetingId: {}, segmentId: {}", meetingId, segmentId);
        } catch (Exception e) {
            logger.error("[段数据入库失败] meetingId: {}, segmentId: {}", meetingId, segmentId, e);
        }
    }

    public CompletableFuture<Void> persistSegmentAsyncWithFuture(MeetingSegmentMessage message) {
        String meetingId = message != null ? message.getMeetingId() : null;
        String segmentId = message != null && message.getSegment() != null ? message.getSegment().getSegmentId() : null;
        logger.info("[段数据入库开始] meetingId: {}, segmentId: {}", meetingId, segmentId);
        
        return CompletableFuture.runAsync(() -> {
            try {
                meetingSegmentPersistenceService.persistSegment(message);
                logger.info("[段数据入库完成] meetingId: {}, segmentId: {}", meetingId, segmentId);
            } catch (Exception e) {
                logger.error("[段数据入库失败] meetingId: {}, segmentId: {}", meetingId, segmentId, e);
                throw new RuntimeException("段数据入库失败", e);
            }
        }, ForkJoinPool.commonPool());
    }

    @Async
    public void persistMeetingResultAsync(MeetingResultMessage message, String conversationId) {
        String meetingId = message != null ? message.getMeetingId() : null;
        Integer needSave = message != null ? message.getNeedSave() : null;
        logger.info("[会议结果持久化] 会议结果入库开始, meetingId: {}, needSave: {}", meetingId, needSave);
        try {
            meetingPersistenceService.persistMeetingResult(message, conversationId);
            logger.info("[会议结果持久化] 会议结果入库完成, meetingId: {}, needSave: {}", meetingId, needSave);
        } catch (Exception e) {
            logger.error("[会议结果持久化] 会议结果入库失败, meetingId: {}, needSave: {}", meetingId, needSave, e);
        }
    }

}

