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
import com.hojoai.omniasst.management.service.meeting.MeetingPersistenceService;
import com.hojoai.omniasst.management.service.meeting.bean.MeetingResultMessage;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * The handler for meeting result channel.
 *
 * @author zhangxuepeng
 * @version 1.0, 2025/12/16
 * @since hojoai-webmvc-omniasst-management 1.0.0
 */
@Component
public class MeetingResultHandler {

    private static final Logger logger = LoggerFactory.getLogger(MeetingResultHandler.class);

    @Resource
    private MeetingPersistenceService meetingPersistenceService;

    /**
     * Handle meeting result message json.
     *
     * @param messageJson raw json string from Redis channel
     */
    public void handle(String messageJson) {
        Asserts.notBlank(messageJson, "messageJson");
        MeetingResultMessage message = JacksonUtil.json2Bean(messageJson, MeetingResultMessage.class);
        meetingPersistenceService.persistMeetingResult(message);
    }
}

