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

import com.hojoai.common.response.Response;
import com.hojoai.omniasst.management.dao.voice.UserVoiceProfileDAO;
import com.hojoai.omniasst.management.dao.voice.po.UserVoiceProfile;
import com.hojoai.omniasst.management.service.meeting.MeetingPrepareAudioStore;
import com.hojoai.omniasst.management.service.voice.VoiceCloneService;
import com.hojoai.omniasst.management.service.voice.bean.UserVoiceProfileResponse;
import com.hojoai.omniasst.management.service.voice.client.VoiceServiceClient;
import com.hojoai.omniasst.management.service.voice.bean.VoicePrintResponse;
import com.hojoai.omniasst.management.service.voice.bean.VoicePrintQueryRequest;
import com.hojoai.omniasst.management.service.voice.bean.VoicePrintPageResult;
import com.hojoai.omniasst.management.service.voiceprint.VoicePrintService;
import com.hojoai.omniasst.management.service.voiceprint.VoicePrintVectorService;
import com.hojoai.omniasst.management.service.voiceprint.VoiceprintPendingSnapshot;
import com.hojoai.omniasst.management.service.voiceprint.VoiceprintPendingStore;
import com.hojoai.omniasst.management.utils.InMemoryMultipartFile;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Objects;

/**
 * 声纹识别与列表查询等业务实现，协调语音服务与向量存储。
 */
@Service
public class VoicePrintServiceImpl implements VoicePrintService {
    private static final Logger logger = LoggerFactory.getLogger(VoicePrintServiceImpl.class);

    @Resource
    private VoiceServiceClient voiceServiceClient;

    @Resource
    private VoicePrintVectorService voicePrintVectorService;

    @Resource
    private UserVoiceProfileDAO userVoiceProfileDAO;

    @Resource
    private VoiceprintPendingStore voiceprintPendingStore;

    @Resource
    private VoiceCloneService voiceCloneService;

    @Resource
    private MeetingPrepareAudioStore meetingPrepareAudioStore;

    @Override
    public void registerVoicePrintFromPending(String userId, String userName) {
        if (userId == null || userId.trim().isEmpty()) {
            throw new IllegalArgumentException("用户ID不能为空");
        }
        if (userName == null || userName.trim().isEmpty()) {
            userName = userId.trim();
        }
        VoiceprintPendingSnapshot snap = voiceprintPendingStore.take(userId.trim());
        if (snap == null || snap.getVoiceprint() == null || snap.getVoiceprint().length == 0) {
            throw new IllegalArgumentException("未找到与本次克隆关联的声纹暂存，请先调用音色克隆或在请求中上传音频文件 file");
        }
        double[] voicePrint = snap.getVoiceprint();
        String displayName = userName.trim();
        try {
            UserVoiceProfile profile = userVoiceProfileDAO.selectByUserId(userId);
            if (profile != null && profile.getVoiceName() != null && !profile.getVoiceName().trim().isEmpty()) {
                displayName = profile.getVoiceName().trim();
            }
        } catch (Exception e) {
            logger.debug("voice profile lookup for display name skipped, userId: {}", userId, e);
        }
        voicePrintVectorService.saveUserVoicePrint(userId.trim(), displayName, voicePrint);
        logger.info("Voiceprint registered from pending cache, userId: {}", userId);
    }

    @Override
    public void registerVoicePrint(String userId, String userName, MultipartFile audioFile) {
        if (userId == null || userId.trim().isEmpty()) {
            throw new IllegalArgumentException("用户ID不能为空");
        }
        if (userName == null || userName.trim().isEmpty()) {
            userName = userId.trim();
        }
        if (Objects.isNull(audioFile) || audioFile.isEmpty()) {
            throw new IllegalArgumentException("音频文件不能为空");
        }
        logger.info("Voiceprint register request, userId: {}, userName: {}", userId, userName);
        Response<VoicePrintResponse> voiceResponse = voiceServiceClient.recognizeFromFile(audioFile);
        if (Objects.isNull(voiceResponse) || Objects.isNull(voiceResponse.getResContent())) {
            String errorMsg = voiceResponse != null && voiceResponse.getResMessage() != null ? voiceResponse.getResMessage() : "声纹识别服务暂时不可用，请稍后重试";
            throw new RuntimeException("声纹识别失败：" + errorMsg);
        }
        VoicePrintResponse resContent = voiceResponse.getResContent();
        double[] voicePrint = resContent.getVoicePrint();
        if (Objects.isNull(voicePrint) || voicePrint.length == 0) {
            logger.error("Voiceprint is empty, userId: {}, userName: {}", userId, userName);
            throw new RuntimeException("声纹向量为空");
        }
        String displayName = userName.trim();
        try {
            UserVoiceProfile profile = userVoiceProfileDAO.selectByUserId(userId);
            if (profile != null && profile.getVoiceName() != null && !profile.getVoiceName().trim().isEmpty()) {
                displayName = profile.getVoiceName().trim();
            }
        } catch (Exception e) {
            logger.debug("voice profile lookup for display name skipped, userId: {}", userId, e);
        }
        voicePrintVectorService.saveUserVoicePrint(userId, displayName, voicePrint);
    }

    @Override
    public VoicePrintPageResult queryUserVoicePrints(VoicePrintQueryRequest request) {
        if (request == null) {
            request = new VoicePrintQueryRequest();
        }
        int page = request.getPage() != null && request.getPage() > 0 ? request.getPage() : 1;
        int pageSize = request.getPageSize() != null && request.getPageSize() > 0 ? request.getPageSize() : 20;
        return voicePrintVectorService.queryUserVoicePrints(request.getUserName(), page, pageSize);
    }

    @Override
    public void deleteUserVoicePrint(String userId) {
        if (userId == null || userId.trim().isEmpty()) {
            throw new IllegalArgumentException("用户ID不能为空");
        }
        int rows = voicePrintVectorService.deleteUserVoicePrint(userId);
        if (rows == 0) {
            throw new IllegalArgumentException("用户声纹不存在");
        }
    }

    @Override
    public void deleteUserVoicePrintIfPresent(String userId) {
        if (userId == null || userId.trim().isEmpty()) {
            return;
        }
        voicePrintVectorService.deleteUserVoicePrint(userId.trim());
    }

    @Override
    public void copyVoiceProfileToVoiceprint(String userId) {
        if (userId == null || userId.trim().isEmpty()) {
            throw new IllegalArgumentException("用户ID不能为空");
        }
        String uid = userId.trim();
        UserVoiceProfileResponse profile = voiceCloneService.getUserVoiceProfile(uid, null);
        if (profile == null) {
            throw new IllegalArgumentException("用户音色不存在");
        }
        String voiceId = profile.getVoiceId();
        if (voiceId == null || voiceId.isBlank()) {
            throw new IllegalArgumentException("音色档案缺少 voiceId，无法关联声纹");
        }
        String displayName = displayNameFromProfile(uid, profile);
        String vid = voiceId.trim();

        MeetingPrepareAudioStore.ConsumedAudio prepared = meetingPrepareAudioStore.loadIfVoiceIdMatches(uid, vid);
        if (prepared != null) {
            InMemoryMultipartFile mf = new InMemoryMultipartFile(
                    "file", prepared.getOriginalFilename(), prepared.getContentType(), prepared.getBytes());
            Response<VoicePrintResponse> voiceResponse = voiceServiceClient.recognizeFromFile(mf);
            if (Objects.isNull(voiceResponse) || Objects.isNull(voiceResponse.getResContent())) {
                String errorMsg = voiceResponse != null && voiceResponse.getResMessage() != null
                        ? voiceResponse.getResMessage() : "声纹识别服务暂时不可用，请稍后重试";
                throw new RuntimeException("声纹识别失败：" + errorMsg);
            }
            double[] voicePrint = voiceResponse.getResContent().getVoicePrint();
            if (voicePrint == null || voicePrint.length == 0) {
                throw new RuntimeException("声纹向量为空");
            }
            voicePrintVectorService.saveUserVoicePrint(uid, displayName, voicePrint);
            meetingPrepareAudioStore.delete(uid);
            logger.info("Voiceprint saved from meeting prepare audio (copy-voice), userId: {}", uid);
            return;
        }

        VoiceprintPendingSnapshot snap = voiceprintPendingStore.peekIfVoiceIdMatches(uid, vid);
        if (snap != null) {
            voicePrintVectorService.saveUserVoicePrint(uid, displayName, snap.getVoiceprint());
            voiceprintPendingStore.removePending(uid);
            logger.info("Voiceprint saved from clone pending (copy-voice), userId: {}, voiceId: {}", uid, vid);
            return;
        }

        throw new IllegalArgumentException(
                "无法生成声纹：未找到会议 prepare 暂存音频（voiceId 须与当前音色一致），且无匹配的音色克隆暂存；请在有效期内先调用会议 prepare 或完成音色克隆");
    }

    private static String displayNameFromProfile(String userId, UserVoiceProfileResponse profile) {
        if (profile.getNickname() != null && !profile.getNickname().isBlank()) {
            return profile.getNickname().trim();
        }
        if (profile.getVoiceName() != null && !profile.getVoiceName().isBlank()) {
            return profile.getVoiceName().trim();
        }
        return userId;
    }
}
