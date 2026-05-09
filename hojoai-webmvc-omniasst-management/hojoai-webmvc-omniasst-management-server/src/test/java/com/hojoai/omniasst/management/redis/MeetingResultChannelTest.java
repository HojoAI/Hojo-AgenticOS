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
 * The test for publishing meeting result message to Redis channel.
 *
 * @author zhangxuepeng
 * @version 1.0, 2025/12/25
 * @since hojoai-webmvc-omniasst-management 1.0.0
 */
@SpringBootTest(classes = ISpringApplication.class)
public class MeetingResultChannelTest {

    @Resource
    private RedissonClient redissonClient;

    /**
     * 测试发布会议结束消息 - needSave=1（保存会议）
     */
    @Test
    public void publishMeetingResultWithSave() {
        String meetingId = "meeting-test-" + System.currentTimeMillis();
        long startTime = System.currentTimeMillis() - 60000; // 1分钟前开始
        long endTime = System.currentTimeMillis();

        Map<String, Object> payload = new HashMap<>(8);
        payload.put("meetingId", meetingId);
        payload.put("userId", "user-test-123");
        payload.put("ossUrl", "https://oss.example.com/meetings/" + meetingId + "/audio.mp3");
        payload.put("totalDuration", endTime - startTime);
        payload.put("startTime", startTime);
        payload.put("endTime", endTime);
        payload.put("needSave", 1); // 保存会议
        payload.put("timestamp", System.currentTimeMillis());

        String json = JacksonUtil.bean2Json(payload);
        System.out.println("Publishing meeting result (needSave=1): " + json);

        RTopic topic = redissonClient.getTopic(RedisConstants.CHANNEL_MEETING_RESULT);
        topic.publish(json);

        System.out.println("Meeting result published successfully for meetingId: " + meetingId);
    }

    /**
     * 测试发布会议结束消息 - needSave=0（不保存会议，删除段）
     */
    @Test
    public void publishMeetingResultWithoutSave() {
        String meetingId = "meeting-test-" + System.currentTimeMillis();
        long startTime = System.currentTimeMillis() - 30000; // 30秒前开始
        long endTime = System.currentTimeMillis();

        Map<String, Object> payload = new HashMap<>(8);
        payload.put("meetingId", meetingId);
        payload.put("userId", "user-test-456");
        payload.put("ossUrl", "https://oss.example.com/meetings/" + meetingId + "/audio.mp3");
        payload.put("totalDuration", endTime - startTime);
        payload.put("startTime", startTime);
        payload.put("endTime", endTime);
        payload.put("needSave", 0); // 不保存会议
        payload.put("timestamp", System.currentTimeMillis());

        String json = JacksonUtil.bean2Json(payload);
        System.out.println("Publishing meeting result (needSave=0): " + json);

        RTopic topic = redissonClient.getTopic(RedisConstants.CHANNEL_MEETING_RESULT);
        topic.publish(json);

        System.out.println("Meeting result published successfully for meetingId: " + meetingId);
    }
}
