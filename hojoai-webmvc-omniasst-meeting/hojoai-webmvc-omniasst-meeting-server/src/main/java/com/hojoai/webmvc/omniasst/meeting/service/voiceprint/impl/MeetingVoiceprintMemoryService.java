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
package com.hojoai.webmvc.omniasst.meeting.service.voiceprint.impl;

import com.hojoai.webmvc.omniasst.meeting.api.dto.UserMatchResult;
import com.hojoai.webmvc.omniasst.meeting.service.voiceprint.bean.MeetingVoiceprintData;
import com.hojoai.webmvc.omniasst.meeting.service.voiceprint.util.VectorSimilarityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 会议内声纹向量内存索引：按 meetingId、speakerId 缓存，用于实时相似度匹配。
 */
@Service
public class MeetingVoiceprintMemoryService {

    private static final Logger logger = LoggerFactory.getLogger(MeetingVoiceprintMemoryService.class);

    // key=meetingId, value = Map<speakerId, MeetingVoiceprintData>
    private final Map<String, Map<String, MeetingVoiceprintData>> voiceprintStore = new ConcurrentHashMap<>();

    public UserMatchResult match(String meetingId, double[] voiceprint, double threshold) {
        if (voiceprint == null || voiceprint.length == 0 || meetingId == null || meetingId.trim().isEmpty()) {
            return null;
        }
        Map<String, MeetingVoiceprintData> meetingData = voiceprintStore.get(meetingId);
        if (meetingData == null || meetingData.isEmpty()) {
            return null;
        }
        double maxSimilarity = 0.0;
        MeetingVoiceprintData matched = null;
        for (MeetingVoiceprintData data : meetingData.values()) {
            if (data != null && data.getVoiceprint() != null) {
                double similarity = VectorSimilarityUtils.cosineSimilarity(voiceprint, data.getVoiceprint());
                if (similarity > maxSimilarity) {
                    maxSimilarity = similarity;
                    matched = data;
                }
            }
        }
        if (matched != null && maxSimilarity >= threshold) {
            logger.info("[声纹匹配成功-临时表] meetingId: {}, speakerId: {}, speakerName: {}, similarity: {}, threshold: {}", meetingId, matched.getSpeakerId(), matched.getSpeakerName(), maxSimilarity, threshold);
            return new UserMatchResult(matched.getSpeakerId(), matched.getSpeakerName(), "");
        }
        if (matched != null) {
            logger.info("[声纹匹配失败-临时表] meetingId: {}, speakerId: {}, speakerName: {}, similarity: {}, threshold: {}, 未达到阈值", meetingId, matched.getSpeakerId(), matched.getSpeakerName(), maxSimilarity, threshold);
        } else {
            logger.info("[声纹匹配失败-临时表] meetingId: {}, maxSimilarity: {}, threshold: {}, 未找到匹配的说话人", meetingId, maxSimilarity, threshold);
        }
        return null;
    }

    public void save(String meetingId, String speakerId, String speakerName, double[] voiceprint) {
        if (meetingId == null || meetingId.trim().isEmpty() || speakerId == null || speakerId.trim().isEmpty() || voiceprint == null || voiceprint.length == 0) {
            logger.warn("Invalid parameters for saving voiceprint, meetingId: {}, speakerId: {}", meetingId, speakerId);
            return;
        }
        Map<String, MeetingVoiceprintData> meetingData = voiceprintStore.computeIfAbsent(meetingId, k -> new ConcurrentHashMap<>());
        if (meetingData.containsKey(speakerId)) {
            logger.debug("Voiceprint already exists, skipped, meetingId: {}, speakerId: {}", meetingId, speakerId);
            return;
        }
        MeetingVoiceprintData data = new MeetingVoiceprintData(speakerId, speakerName, voiceprint);
        meetingData.put(speakerId, data);
        logger.info("Voiceprint saved to memory, meetingId: {}, speakerId: {}, speakerName: {}", meetingId, speakerId, speakerName);
    }

    public int delete(String meetingId) {
        if (meetingId == null || meetingId.trim().isEmpty()) {
            return 0;
        }
        Map<String, MeetingVoiceprintData> meetingData = voiceprintStore.remove(meetingId);
        int size = meetingData != null ? meetingData.size() : 0;
        logger.info("Voiceprints deleted from memory, meetingId: {}, count: {}", meetingId, size);
        return size;
    }
}
