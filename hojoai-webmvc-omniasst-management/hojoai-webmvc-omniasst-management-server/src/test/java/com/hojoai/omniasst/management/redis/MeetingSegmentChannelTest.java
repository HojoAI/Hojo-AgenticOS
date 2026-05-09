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

import com.hojoai.common.jackson.JacksonUtil;
import com.hojoai.omniasst.management.ISpringApplication;
import com.hojoai.omniasst.management.common.RedisConstants;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashMap;
import java.util.Map;

/**
 * The test for publishing meeting segment message to Redis channel.
 *
 * @author zhangxuepeng
 * @version 1.0, 2025/12/25
 * @since hojoai-webmvc-omniasst-management 1.0.0
 */
@SpringBootTest(classes = ISpringApplication.class)
public class MeetingSegmentChannelTest {

    @Resource
    private RedissonClient redissonClient;

    /**
     * 测试发布单个会议段消息
     */
    @Test
    public void publishMeetingSegmentSample() {
        String meetingId = "meeting-test-" + System.currentTimeMillis();

        Map<String, Object> segment = new HashMap<>(8);
        segment.put("segmentId", "seg-" + System.currentTimeMillis());
        segment.put("text", "你好，这是一段测试文本");
        segment.put("speakerId", "speaker-001");
        segment.put("startTime", 0L);
        segment.put("endTime", 2000L);
        segment.put("duration", 2000L);

        Map<String, Object> payload = new HashMap<>(4);
        payload.put("meetingId", meetingId);
        payload.put("segment", segment);
        payload.put("timestamp", System.currentTimeMillis());

        String json = JacksonUtil.bean2Json(payload);
        System.out.println("Publishing segment message: " + json);

        RTopic topic = redissonClient.getTopic(RedisConstants.CHANNEL_MEETING_SEGMENT);
        topic.publish(json);

        System.out.println("Segment message published successfully for meetingId: " + meetingId);
    }

    /**
     * 测试发布多个会议段消息（模拟实时流式传输）
     */
    @Test
    public void publishMultipleMeetingSegments() throws InterruptedException {
        String meetingId = "meeting-test-" + System.currentTimeMillis();
        System.out.println("Testing multiple segments for meetingId: " + meetingId);

        // 发布第一个段
        publishSegment(meetingId, "seg-1", "大家好，欢迎参加今天的会议", "speaker-001", 0L, 2000L);
        Thread.sleep(500);

        // 发布第二个段
        publishSegment(meetingId, "seg-2", "今天我们讨论一下项目进度", "speaker-001", 2000L, 5000L);
        Thread.sleep(500);

        // 发布第三个段
        publishSegment(meetingId, "seg-3", "好的，我先汇报一下我这边的情况", "speaker-002", 5000L, 8000L);
        Thread.sleep(500);

        // 发布第四个段
        publishSegment(meetingId, "seg-4", "目前项目进展顺利，预计下周完成", "speaker-002", 8000L, 12000L);

        System.out.println("All segments published successfully for meetingId: " + meetingId);
    }

    private void publishSegment(String meetingId, String segmentId, String text, 
                                 String speakerId, Long startTime, Long endTime) {
        Map<String, Object> segment = new HashMap<>(8);
        segment.put("segmentId", segmentId);
        segment.put("text", text);
        segment.put("speakerId", speakerId);
        segment.put("startTime", startTime);
        segment.put("endTime", endTime);
        segment.put("duration", endTime - startTime);

        Map<String, Object> payload = new HashMap<>(4);
        payload.put("meetingId", meetingId);
        payload.put("segment", segment);
        payload.put("timestamp", System.currentTimeMillis());

        String json = JacksonUtil.bean2Json(payload);
        System.out.println("Publishing segment: " + segmentId + " - " + text);

        RTopic topic = redissonClient.getTopic(RedisConstants.CHANNEL_MEETING_SEGMENT);
        topic.publish(json);
    }
}

