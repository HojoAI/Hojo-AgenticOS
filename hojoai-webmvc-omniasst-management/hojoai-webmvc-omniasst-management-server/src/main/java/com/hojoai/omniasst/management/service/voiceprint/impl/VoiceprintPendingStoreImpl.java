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
package com.hojoai.omniasst.management.service.voiceprint.impl;

import com.hojoai.common.jackson.JacksonUtil;
import com.hojoai.omniasst.management.common.RedisConstants;
import com.hojoai.omniasst.management.service.voiceprint.VoiceprintPendingSnapshot;
import com.hojoai.omniasst.management.service.voiceprint.VoiceprintPendingStore;
import jakarta.annotation.Resource;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 声纹待提交快照的 Redis 存储实现。
 */
@Service
public class VoiceprintPendingStoreImpl implements VoiceprintPendingStore {

    private static final int TTL_MINUTES = 30;

    @Resource
    private RedissonClient redissonClient;

    @Override
    public void putAfterClone(String userId, String voiceId, String provider, double[] voiceprint) {
        if (userId == null || userId.isBlank() || voiceprint == null || voiceprint.length == 0) {
            return;
        }
        Map<String, Object> m = new HashMap<>(4);
        m.put("userId", userId.trim());
        m.put("voiceId", voiceId != null ? voiceId : "");
        m.put("provider", provider != null ? provider : "");
        m.put("voiceprint", voiceprint);
        String key = RedisConstants.KEY_VOICEPRINT_AFTER_CLONE + userId.trim();
        RBucket<String> bucket = redissonClient.getBucket(key);
        bucket.set(JacksonUtil.bean2Json(m), TTL_MINUTES, TimeUnit.MINUTES);
    }

    @Override
    public VoiceprintPendingSnapshot take(String userId) {
        if (userId == null || userId.isBlank()) {
            return null;
        }
        String key = RedisConstants.KEY_VOICEPRINT_AFTER_CLONE + userId.trim();
        RBucket<String> bucket = redissonClient.getBucket(key);
        String json = bucket.get();
        if (json == null || json.isBlank()) {
            return null;
        }
        bucket.delete();
        return parseSnapshotJson(json);
    }

    @Override
    public VoiceprintPendingSnapshot peekIfVoiceIdMatches(String userId, String expectedVoiceId) {
        if (userId == null || userId.isBlank() || expectedVoiceId == null || expectedVoiceId.isBlank()) {
            return null;
        }
        String key = RedisConstants.KEY_VOICEPRINT_AFTER_CLONE + userId.trim();
        RBucket<String> bucket = redissonClient.getBucket(key);
        String json = bucket.get();
        if (json == null || json.isBlank()) {
            return null;
        }
        VoiceprintPendingSnapshot snap = parseSnapshotJson(json);
        if (snap == null || snap.getVoiceprint() == null || snap.getVoiceprint().length == 0) {
            return null;
        }
        String want = expectedVoiceId.trim();
        String got = snap.getVoiceId() != null ? snap.getVoiceId().trim() : "";
        if (!want.equals(got)) {
            return null;
        }
        return snap;
    }

    @Override
    public void removePending(String userId) {
        if (userId == null || userId.isBlank()) {
            return;
        }
        String key = RedisConstants.KEY_VOICEPRINT_AFTER_CLONE + userId.trim();
        redissonClient.getBucket(key).delete();
    }

    @SuppressWarnings("unchecked")
    private static VoiceprintPendingSnapshot parseSnapshotJson(String json) {
        Map<String, Object> map = JacksonUtil.json2Bean(json, Map.class);
        if (map == null) {
            return null;
        }
        String uid = String.valueOf(map.get("userId"));
        String vid = map.get("voiceId") != null ? String.valueOf(map.get("voiceId")) : "";
        String prov = map.get("provider") != null ? String.valueOf(map.get("provider")) : "";
        Object vpObj = map.get("voiceprint");
        double[] vp = parseVoiceprint(vpObj);
        if (vp == null || vp.length == 0) {
            return null;
        }
        return new VoiceprintPendingSnapshot(uid, vid, prov, vp);
    }

    private static double[] parseVoiceprint(Object vpObj) {
        if (vpObj instanceof double[] d) {
            return d;
        }
        if (vpObj instanceof List<?> list) {
            double[] out = new double[list.size()];
            for (int i = 0; i < list.size(); i++) {
                Object x = list.get(i);
                if (x instanceof Number n) {
                    out[i] = n.doubleValue();
                } else {
                    out[i] = Double.parseDouble(String.valueOf(x));
                }
            }
            return out;
        }
        return null;
    }
}
