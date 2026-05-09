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
import com.hojoai.common.response.Response;
import com.hojoai.omniasst.management.common.RedisConstants;
import com.hojoai.omniasst.management.common.VoiceProviderEnum;
import com.hojoai.omniasst.management.service.meeting.MeetingPrepareAudioStore;
import com.hojoai.omniasst.management.service.meeting.MeetingVoiceprintRegisterService;
import com.hojoai.omniasst.management.service.meeting.bean.MeetingVoicePrepareCache;
import com.hojoai.omniasst.management.service.meeting.bean.MeetingVoiceprintCommitRequest;
import com.hojoai.omniasst.management.service.voice.VoiceCloneService;
import com.hojoai.omniasst.management.service.voice.bean.VoiceCloneRequest;
import com.hojoai.omniasst.management.service.voice.bean.VoiceCloneResponse;
import com.hojoai.omniasst.management.service.voice.bean.VoicePrintResponse;
import com.hojoai.omniasst.management.service.voice.client.VoiceServiceClient;
import com.hojoai.omniasst.management.service.voiceprint.VoicePrintVectorService;
import com.hojoai.omniasst.management.utils.InMemoryMultipartFile;
import jakarta.annotation.Resource;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * 会议侧声纹注册流程：准备音频缓存、克隆音色、提交声纹与昵称等。
 */
@Service
public class MeetingVoiceprintRegisterServiceImpl implements MeetingVoiceprintRegisterService {

    private static final Logger logger = LoggerFactory.getLogger(MeetingVoiceprintRegisterServiceImpl.class);

    private static final int PREPARE_CACHE_TTL_MINUTES = 30;

    @Resource
    private VoiceCloneService voiceCloneService;

    @Resource
    private VoiceServiceClient voiceServiceClient;

    @Resource
    private VoicePrintVectorService voicePrintVectorService;

    @Resource
    private RedissonClient redissonClient;

    @Resource
    private MeetingPrepareAudioStore meetingPrepareAudioStore;

    @Override
    public VoiceCloneResponse prepare(String userId, MultipartFile file, String voiceName, Integer type, boolean needSave, String provider) {
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("用户ID不能为空");
        }
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("音频文件不能为空");
        }
        if (provider == null || provider.isBlank()) {
            throw new IllegalArgumentException("provider 不能为空");
        }
        String p = provider.trim();
        if (!VoiceProviderEnum.isValid(p)) {
            throw new IllegalArgumentException("暂不支持的厂商: " + provider);
        }
        String canonicalProvider = VoiceProviderEnum.normalizeCode(p);
        if (canonicalProvider == null) {
            throw new IllegalArgumentException("暂不支持的厂商: " + provider);
        }

        int t = type == null ? 0 : type;
        if (t != 0 && t != 1) {
            throw new IllegalArgumentException("type 参数非法");
        }

        byte[] audioBytes;
        try {
            audioBytes = file.getBytes();
        } catch (IOException e) {
            throw new RuntimeException("读取上传文件失败", e);
        }
        String fn = file.getOriginalFilename();
        String ct = file.getContentType();
        MultipartFile cloneFile = new InMemoryMultipartFile("file", fn, ct, audioBytes);
        MultipartFile printFile = new InMemoryMultipartFile("file", fn, ct, audioBytes);

        VoiceCloneResponse cloned = voiceCloneService.cloneVoice(userId.trim(), voiceName, canonicalProvider, needSave, cloneFile);

        meetingPrepareAudioStore.put(userId.trim(), cloned.getVoiceId(), audioBytes, fn, ct);

        Response<VoicePrintResponse> vpRes = voiceServiceClient.recognizeFromFile(printFile);
        if (vpRes == null || vpRes.getResContent() == null) {
            String msg = vpRes != null && vpRes.getResMessage() != null ? vpRes.getResMessage() : "声纹识别服务暂时不可用";
            throw new RuntimeException("声纹提取失败：" + msg);
        }
        double[] voiceprint = vpRes.getResContent().getVoicePrint();
        if (voiceprint == null || voiceprint.length == 0) {
            throw new RuntimeException("声纹向量为空");
        }

        MeetingVoicePrepareCache cache = new MeetingVoicePrepareCache();
        cache.setUserId(userId.trim());
        cache.setVoiceId(cloned.getVoiceId());
        cache.setVoiceprint(voiceprint);
        cache.setProvider(canonicalProvider);
        cache.setType(t);
        cache.setNeedSave(needSave);

        String key = RedisConstants.KEY_MEETING_VOICEPRINT_PREPARE + userId.trim();
        RBucket<String> bucket = redissonClient.getBucket(key);
        bucket.set(JacksonUtil.bean2Json(cache), PREPARE_CACHE_TTL_MINUTES, TimeUnit.MINUTES);
        logger.info("Meeting voiceprint prepare cached, userId: {}, voiceId: {}, provider: {}", userId.trim(), cloned.getVoiceId(), canonicalProvider);

        return cloned;
    }

    @Override
    public VoiceCloneResponse commit(MeetingVoiceprintCommitRequest request) {
        if (request == null || request.getUserId() == null || request.getUserId().isBlank()) {
            throw new IllegalArgumentException("用户ID不能为空");
        }
        if (request.getProvider() == null || request.getProvider().isBlank()) {
            throw new IllegalArgumentException("provider 不能为空");
        }
        String userId = request.getUserId();
        String prov = request.getProvider();
        if (!VoiceProviderEnum.isValid(prov)) {
            throw new IllegalArgumentException("暂不支持的厂商: " + prov);
        }
        String canonicalCommit = VoiceProviderEnum.normalizeCode(prov);
        if (canonicalCommit == null) {
            throw new IllegalArgumentException("暂不支持的厂商: " + prov);
        }

        String key = RedisConstants.KEY_MEETING_VOICEPRINT_PREPARE + userId;
        RBucket<String> bucket = redissonClient.getBucket(key);
        String json = bucket.get();
        if (json == null || json.isBlank()) {
            throw new IllegalArgumentException("未找到准备阶段数据，请先调用 prepare 或数据已过期");
        }
        MeetingVoicePrepareCache cache = JacksonUtil.json2Bean(json, MeetingVoicePrepareCache.class);
        if (cache == null || cache.getVoiceId() == null || cache.getVoiceprint() == null) {
            throw new IllegalArgumentException("准备数据无效，请重新调用 prepare");
        }
        if (!canonicalCommit.equalsIgnoreCase(cache.getProvider())) {
            throw new IllegalArgumentException("provider 与准备阶段不一致");
        }

        String nick = request.getNickname();
        String vName = request.getVoiceName();
        String effectiveVoiceName = vName != null && !vName.isBlank() ? vName : nick;
        String effectiveNickname = nick != null && !nick.isBlank() ? nick : effectiveVoiceName;

        VoiceCloneRequest saveReq = new VoiceCloneRequest(
                userId, null,
                effectiveVoiceName, null,
                effectiveNickname, null,
                canonicalCommit,
                cache.getVoiceId(), null
        );
        voiceCloneService.saveUserVoiceProfile(saveReq);

        String displayName = nick != null && !nick.isBlank() ? nick
                : (vName != null && !vName.isBlank() ? vName : cache.getVoiceId());
        voicePrintVectorService.saveUserVoicePrint(userId, displayName, cache.getVoiceprint());

        bucket.delete();
        meetingPrepareAudioStore.delete(userId);
        logger.info("Meeting voiceprint commit done, userId: {}, voiceId: {}", userId, cache.getVoiceId());
        return new VoiceCloneResponse(cache.getVoiceId());
    }
}
