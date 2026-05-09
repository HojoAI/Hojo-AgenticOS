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
import com.hojoai.omniasst.management.dao.meeting.MeetingDAO;
import com.hojoai.omniasst.management.dao.meeting.MeetingSegmentDAO;
import com.hojoai.omniasst.management.dao.meeting.po.Meeting;
import com.hojoai.omniasst.management.dao.meeting.po.MeetingSegment;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 完整的会议流程集成测试
 * 测试从段消息到结果消息的完整流程
 *
 * @author zhangxuepeng
 * @version 1.0, 2025/12/25
 * @since hojoai-webmvc-omniasst-management 1.0.0
 */
@SpringBootTest(classes = ISpringApplication.class)
public class MeetingFullFlowTest {

    @Resource
    private RedissonClient redissonClient;

    @Resource
    private MeetingDAO meetingDAO;

    @Resource
    private MeetingSegmentDAO meetingSegmentDAO;

    /**
     * 测试完整流程：发布段消息 -> 发布结果消息(needSave=1) -> 验证数据库
     */
    @Test
    public void testFullFlowWithSave() throws InterruptedException {
        String meetingId = "meeting-full-test-" + System.currentTimeMillis();
        String userId = "user-test-" + System.currentTimeMillis();
        long startTime = System.currentTimeMillis();

        System.out.println("\n========== 开始完整流程测试 (needSave=1) ==========");
        System.out.println("Meeting ID: " + meetingId);
        System.out.println("User ID: " + userId);

        // 步骤1: 发布多个段消息
        System.out.println("\n--- 步骤1: 发布段消息 ---");
        publishSegment(meetingId, "seg-1", "大家好，欢迎参加今天的会议", "speaker-001", 0L, 2000L);
        Thread.sleep(200);

        publishSegment(meetingId, "seg-2", "今天我们讨论一下项目进度", "speaker-001", 2000L, 5000L);
        Thread.sleep(200);

        publishSegment(meetingId, "seg-3", "好的，我先汇报一下我这边的情况", "speaker-002", 5000L, 8000L);
        Thread.sleep(200);

        publishSegment(meetingId, "seg-4", "目前项目进展顺利，预计下周完成", "speaker-002", 8000L, 12000L);
        Thread.sleep(200);

        publishSegment(meetingId, "seg-5", "很好，大家继续保持", "speaker-001", 12000L, 14000L);
        Thread.sleep(1000); // 等待段消息处理

        // 步骤2: 验证段数据已存储
        System.out.println("\n--- 步骤2: 验证段数据 ---");
        List<MeetingSegment> segments = meetingSegmentDAO.selectByMeetingId(meetingId);
        System.out.println("已存储的段数量: " + (segments != null ? segments.size() : 0));
        if (segments != null) {
            segments.forEach(seg -> System.out.println("  - " + seg.getSegmentId() + ": " + seg.getText()));
        }

        // 步骤3: 发布会议结束消息 (needSave=1)
        System.out.println("\n--- 步骤3: 发布会议结束消息 (needSave=1) ---");
        long endTime = System.currentTimeMillis();
        publishMeetingResult(meetingId, userId, startTime, endTime, 1);
        Thread.sleep(2000); // 等待结果消息处理

        // 步骤4: 验证会议记录已创建
        System.out.println("\n--- 步骤4: 验证会议记录 ---");
        Meeting meeting = meetingDAO.selectByMeetingId(meetingId);
        if (meeting != null) {
            System.out.println("✓ 会议记录已创建:");
            System.out.println("  - Meeting ID: " + meeting.getMeetingId());
            System.out.println("  - User ID: " + meeting.getUserId());
            System.out.println("  - Title: " + meeting.getTitle());
            System.out.println("  - Start Time: " + meeting.getStartTime());
            System.out.println("  - End Time: " + meeting.getEndTime());
            System.out.println("  - Total Duration: " + meeting.getTotalDuration() + "ms");
            System.out.println("  - OSS URL: " + meeting.getOssUrl());
            System.out.println("  - Segments JSON: " + (meeting.getSegmentsJson() != null ? 
                meeting.getSegmentsJson().substring(0, Math.min(100, meeting.getSegmentsJson().length())) + "..." : "null"));
        } else {
            System.out.println("✗ 会议记录未找到");
        }

        // 步骤5: 验证段数据仍然保留
        System.out.println("\n--- 步骤5: 验证段数据保留 ---");
        List<MeetingSegment> finalSegments = meetingSegmentDAO.selectByMeetingId(meetingId);
        System.out.println("保留的段数量: " + (finalSegments != null ? finalSegments.size() : 0));

        System.out.println("\n========== 完整流程测试完成 ==========\n");
    }

    /**
     * 测试完整流程：发布段消息 -> 发布结果消息(needSave=0) -> 验证段被删除
     */
    @Test
    public void testFullFlowWithoutSave() throws InterruptedException {
        String meetingId = "meeting-nosave-test-" + System.currentTimeMillis();
        String userId = "user-test-" + System.currentTimeMillis();
        long startTime = System.currentTimeMillis();

        System.out.println("\n========== 开始完整流程测试 (needSave=0) ==========");
        System.out.println("Meeting ID: " + meetingId);
        System.out.println("User ID: " + userId);

        // 步骤1: 发布段消息
        System.out.println("\n--- 步骤1: 发布段消息 ---");
        publishSegment(meetingId, "seg-1", "测试消息1", "speaker-001", 0L, 1000L);
        Thread.sleep(200);

        publishSegment(meetingId, "seg-2", "测试消息2", "speaker-002", 1000L, 2000L);
        Thread.sleep(1000); // 等待段消息处理

        // 步骤2: 验证段数据已存储
        System.out.println("\n--- 步骤2: 验证段数据 ---");
        List<MeetingSegment> segments = meetingSegmentDAO.selectByMeetingId(meetingId);
        System.out.println("已存储的段数量: " + (segments != null ? segments.size() : 0));

        // 步骤3: 发布会议结束消息 (needSave=0)
        System.out.println("\n--- 步骤3: 发布会议结束消息 (needSave=0) ---");
        long endTime = System.currentTimeMillis();
        publishMeetingResult(meetingId, userId, startTime, endTime, 0);
        Thread.sleep(2000); // 等待结果消息处理

        // 步骤4: 验证会议记录未创建
        System.out.println("\n--- 步骤4: 验证会议记录 ---");
        Meeting meeting = meetingDAO.selectByMeetingId(meetingId);
        if (meeting == null) {
            System.out.println("✓ 会议记录未创建（符合预期）");
        } else {
            System.out.println("✗ 会议记录已创建（不符合预期）");
        }

        // 步骤5: 验证段数据已删除
        System.out.println("\n--- 步骤5: 验证段数据删除 ---");
        List<MeetingSegment> finalSegments = meetingSegmentDAO.selectByMeetingId(meetingId);
        int segmentCount = (finalSegments != null ? finalSegments.size() : 0);
        if (segmentCount == 0) {
            System.out.println("✓ 段数据已删除（符合预期）");
        } else {
            System.out.println("✗ 段数据未删除，剩余: " + segmentCount + "（不符合预期）");
        }

        System.out.println("\n========== 完整流程测试完成 ==========\n");
    }

    /**
     * 测试幂等性：重复发送相同的段消息
     */
    @Test
    public void testSegmentIdempotency() throws InterruptedException {
        String meetingId = "meeting-idempotency-test-" + System.currentTimeMillis();

        System.out.println("\n========== 测试段消息幂等性 ==========");
        System.out.println("Meeting ID: " + meetingId);

        // 发送相同的段消息3次
        System.out.println("\n--- 发送相同段消息3次 ---");
        String segmentId = "seg-duplicate-" + System.currentTimeMillis();
        for (int i = 0; i < 3; i++) {
            publishSegment(meetingId, segmentId, "重复的测试消息", "speaker-001", 0L, 1000L);
            Thread.sleep(200);
        }

        Thread.sleep(1000); // 等待处理

        // 验证只存储了一条
        System.out.println("\n--- 验证存储结果 ---");
        List<MeetingSegment> segments = meetingSegmentDAO.selectByMeetingId(meetingId);
        int count = (segments != null ? segments.size() : 0);
        System.out.println("存储的段数量: " + count);
        if (count == 1) {
            System.out.println("✓ 幂等性测试通过（只存储了1条）");
        } else {
            System.out.println("✗ 幂等性测试失败（存储了" + count + "条）");
        }

        System.out.println("\n========== 幂等性测试完成 ==========\n");
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
        RTopic topic = redissonClient.getTopic(RedisConstants.CHANNEL_MEETING_SEGMENT);
        topic.publish(json);
    }

    private void publishMeetingResult(String meetingId, String userId, 
                                       Long startTime, Long endTime, Integer needSave) {
        Map<String, Object> payload = new HashMap<>(8);
        payload.put("meetingId", meetingId);
        payload.put("userId", userId);
        payload.put("ossUrl", "https://oss.example.com/meetings/" + meetingId + "/audio.mp3");
        payload.put("totalDuration", endTime - startTime);
        payload.put("startTime", startTime);
        payload.put("endTime", endTime);
        payload.put("needSave", needSave);
        payload.put("timestamp", System.currentTimeMillis());

        String json = JacksonUtil.bean2Json(payload);
        RTopic topic = redissonClient.getTopic(RedisConstants.CHANNEL_MEETING_RESULT);
        topic.publish(json);
    }
}

