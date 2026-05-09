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
package com.hojoai.omniasst.management.redis;

import com.hojoai.common.asserts.Asserts;
import com.hojoai.common.jackson.JacksonUtil;
import com.hojoai.omniasst.management.service.meeting.MeetingSegmentPersistenceService;
import com.hojoai.omniasst.management.service.meeting.bean.MeetingSegmentMessage;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * The handler for meeting segment channel.
 *
 * @author zhangxuepeng
 * @version 1.0, 2025/12/25
 * @since hojoai-webmvc-omniasst-management 1.0.0
 */
@Component
public class MeetingSegmentHandler {

    private static final Logger logger = LoggerFactory.getLogger(MeetingSegmentHandler.class);

    @Resource
    private MeetingSegmentPersistenceService meetingSegmentPersistenceService;

    /**
     * Handle meeting segment message json.
     *
     * @param messageJson raw json string from Redis channel
     */
    public void handle(String messageJson) {
        logger.info("MeetingSegmentHandler.handle called from thread: {}", Thread.currentThread().getName());
        Asserts.notBlank(messageJson, "messageJson");
        MeetingSegmentMessage message = JacksonUtil.json2Bean(messageJson, MeetingSegmentMessage.class);
        logger.info("Parsed segment message: meetingId={}, segmentId={}", 
                message.getMeetingId(), message.getSegment() != null ? message.getSegment().getSegmentId() : "null");
        meetingSegmentPersistenceService.persistSegment(message);
    }
}

