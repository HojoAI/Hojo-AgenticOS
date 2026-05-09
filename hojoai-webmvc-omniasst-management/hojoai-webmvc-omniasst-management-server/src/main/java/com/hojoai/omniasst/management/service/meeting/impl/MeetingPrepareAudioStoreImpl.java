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

import com.hojoai.common.jackson.JacksonUtil;
import com.hojoai.omniasst.management.common.RedisConstants;
import com.hojoai.omniasst.management.service.meeting.MeetingPrepareAudioStore;
import com.hojoai.omniasst.management.service.meeting.bean.MeetingPrepareAudioPayload;
import jakarta.annotation.Resource;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import java.util.Base64;
import java.util.concurrent.TimeUnit;

/**
 * 会议声纹准备阶段音频在 Redis 中的临时存储。
 */
@Service
public class MeetingPrepareAudioStoreImpl implements MeetingPrepareAudioStore {

    private static final int TTL_MINUTES = 30;

    @Resource
    private RedissonClient redissonClient;

    @Override
    public void put(String userId, String voiceId, byte[] audioBytes, String originalFilename, String contentType) {
        if (userId == null || userId.isBlank() || audioBytes == null || audioBytes.length == 0) {
            return;
        }
        MeetingPrepareAudioPayload p = new MeetingPrepareAudioPayload();
        p.setVoiceId(voiceId != null ? voiceId.trim() : "");
        p.setAudioBase64(Base64.getEncoder().encodeToString(audioBytes));
        p.setOriginalFilename(originalFilename != null && !originalFilename.isBlank() ? originalFilename.trim() : null);
        p.setContentType(contentType != null && !contentType.isBlank() ? contentType.trim() : null);
        String key = RedisConstants.KEY_MEETING_VOICEPRINT_PREPARE_AUDIO + userId.trim();
        RBucket<String> bucket = redissonClient.getBucket(key);
        bucket.set(JacksonUtil.bean2Json(p), TTL_MINUTES, TimeUnit.MINUTES);
    }

    @Override
    public ConsumedAudio loadIfVoiceIdMatches(String userId, String expectedVoiceId) {
        if (userId == null || userId.isBlank() || expectedVoiceId == null || expectedVoiceId.isBlank()) {
            return null;
        }
        String key = RedisConstants.KEY_MEETING_VOICEPRINT_PREPARE_AUDIO + userId.trim();
        RBucket<String> bucket = redissonClient.getBucket(key);
        String json = bucket.get();
        if (json == null || json.isBlank()) {
            return null;
        }
        MeetingPrepareAudioPayload p = JacksonUtil.json2Bean(json, MeetingPrepareAudioPayload.class);
        if (p == null || p.getAudioBase64() == null || p.getAudioBase64().isBlank()) {
            return null;
        }
        String want = expectedVoiceId.trim();
        String got = p.getVoiceId() != null ? p.getVoiceId().trim() : "";
        if (!want.equals(got)) {
            return null;
        }
        byte[] raw;
        try {
            raw = Base64.getDecoder().decode(p.getAudioBase64());
        } catch (IllegalArgumentException e) {
            return null;
        }
        if (raw.length == 0) {
            return null;
        }
        String fn = p.getOriginalFilename() != null ? p.getOriginalFilename() : "audio.wav";
        String ct = p.getContentType() != null ? p.getContentType() : "application/octet-stream";
        return new ConsumedAudio(raw, fn, ct);
    }

    @Override
    public void delete(String userId) {
        if (userId == null || userId.isBlank()) {
            return;
        }
        String key = RedisConstants.KEY_MEETING_VOICEPRINT_PREPARE_AUDIO + userId.trim();
        redissonClient.getBucket(key).delete();
    }
}
