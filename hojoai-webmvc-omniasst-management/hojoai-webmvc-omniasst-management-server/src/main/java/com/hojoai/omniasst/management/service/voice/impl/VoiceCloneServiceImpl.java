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
package com.hojoai.omniasst.management.service.voice.impl;

import com.hojoai.common.response.Response;
import com.hojoai.omniasst.management.common.VoiceProviderEnum;
import com.hojoai.omniasst.management.dao.voice.UserVoiceProfileDAO;
import com.hojoai.omniasst.management.dao.voice.po.UserVoiceProfile;
import com.hojoai.omniasst.management.service.voice.VoiceCloneService;
import com.hojoai.omniasst.management.service.voice.bean.UserVoiceProfileResponse;
import com.hojoai.omniasst.management.service.voice.bean.VoiceCloneRequest;
import com.hojoai.omniasst.management.service.voice.client.VoiceServiceClient;
import com.hojoai.omniasst.management.service.voice.bean.VoiceCloneResponse;
import com.hojoai.omniasst.management.service.voice.bean.VoicePrintResponse;
import com.hojoai.omniasst.management.service.voiceprint.VoiceprintPendingStore;
import com.hojoai.omniasst.management.utils.InMemoryMultipartFile;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Date;
import java.util.Objects;

/**
 * 音色克隆业务实现：调用语音服务 Feign、维护用户音色档案与待注册声纹缓存。
 */
@Service
public class VoiceCloneServiceImpl implements VoiceCloneService {

    private static final Logger logger = LoggerFactory.getLogger(VoiceCloneServiceImpl.class);

    @Resource
    private VoiceServiceClient voiceServiceClient;

    @Resource
    private UserVoiceProfileDAO userVoiceProfileDAO;

    @Resource
    private VoiceprintPendingStore voiceprintPendingStore;

    @Override
    public VoiceCloneResponse cloneVoice(String userId, String voiceName, String provider, Boolean needSave, MultipartFile audioFile) {
        if (!VoiceProviderEnum.isValid(provider)) {
            throw new IllegalArgumentException("暂不支持的厂商: " + provider);
        }
        VoiceProviderEnum providerEnum = VoiceProviderEnum.fromCode(provider);
        final byte[] raw;
        try {
            raw = audioFile.getBytes();
        } catch (IOException e) {
            throw new RuntimeException("读取音频失败", e);
        }
        String fn = audioFile.getOriginalFilename();
        String ct = audioFile.getContentType();
        InMemoryMultipartFile cloneFile = new InMemoryMultipartFile("file", fn, ct, raw);
        String effectiveVoiceName = (voiceName == null || voiceName.isBlank())
            ? defaultVoiceNameFromAudioFile(cloneFile)
            : voiceName.trim();
        logger.info("Voice clone request, userId: {}, voiceName: {}, provider: {}", userId, effectiveVoiceName, providerEnum.getCode());

        // 获取文件类型，直接从文件后缀获取，没有后缀则默认 wav
        String fileType = "wav"; // 默认值
        String originalFilename = cloneFile.getOriginalFilename();
        if (originalFilename != null && originalFilename.contains(".")) {
            fileType = originalFilename.substring(originalFilename.lastIndexOf(".") + 1).toLowerCase();
        }

        Response<VoiceCloneResponse> voiceResponse = voiceServiceClient.cloneVoice(cloneFile, fileType, providerEnum.getCode());
        if (voiceResponse == null || voiceResponse.getResContent() == null) {
            String errorMsg = voiceResponse != null && voiceResponse.getResMessage() != null ? voiceResponse.getResMessage() : "音色复刻服务暂时不可用，请稍后重试";
            logger.error("Voice clone failed, userId: {}, error: {}", userId, errorMsg);
            throw new RuntimeException("音色复刻失败：" + errorMsg);
        }
        String voiceId = voiceResponse.getResContent().getVoiceId();
        logger.info("Voice clone success, userId: {}, voiceId: {}, provider: {}", userId, voiceId, providerEnum.getCode());
        if (needSave) {
            saveUserVoice(userId, effectiveVoiceName, null, providerEnum.getCode(), voiceId);
        }
        try {
            InMemoryMultipartFile printFile = new InMemoryMultipartFile("file", fn, ct, raw);
            Response<VoicePrintResponse> vpRes = voiceServiceClient.recognizeFromFile(printFile);
            if (vpRes != null && vpRes.getResContent() != null) {
                double[] vec = vpRes.getResContent().getVoicePrint();
                if (vec != null && vec.length > 0) {
                    voiceprintPendingStore.putAfterClone(userId.trim(), voiceId, providerEnum.getCode(), vec);
                }
            }
        } catch (Exception ex) {
            logger.warn("克隆后声纹暂存失败（无 file 注册将不可用），userId: {}", userId, ex);
        }
        return new VoiceCloneResponse(voiceId);
    }

    private static String defaultVoiceNameFromAudioFile(MultipartFile audioFile) {
        if (audioFile == null) {
            return null;
        }
        String fn = audioFile.getOriginalFilename();
        if (fn == null || fn.isBlank()) {
            return null;
        }
        String base = fn.trim();
        int dot = base.lastIndexOf('.');
        if (dot > 0) {
            base = base.substring(0, dot);
        }
        return base.isBlank() ? null : base.trim();
    }

    @Override
    public UserVoiceProfileResponse getUserVoiceProfile(String userId, String provider) {
        if (Objects.isNull(userId) || userId.trim().isEmpty()) {
            throw new IllegalArgumentException("用户ID不能为空");
        }
        String p = (Objects.isNull(provider) || provider.trim().isEmpty()) ? VoiceProviderEnum.HOJO.getCode() : provider.trim();
        String normalized = VoiceProviderEnum.normalizeCode(p);
        if (normalized != null) {
            p = normalized;
        }
        try {
            UserVoiceProfile profile = userVoiceProfileDAO.selectByUserIdAndProvider(userId, p);
            if (Objects.isNull(profile)) {
                profile = userVoiceProfileDAO.selectByUserId(userId);
            }
            if (Objects.isNull(profile)) {
                return null;
            }
            UserVoiceProfileResponse result = new UserVoiceProfileResponse();
            result.setId(profile.getId());
            result.setUserId(profile.getUserId());
            result.setVoiceId(profile.getVoiceId());
            result.setVoiceName(profile.getVoiceName());
            result.setNickname(profile.getNickname());
            result.setProvider(profile.getProvider());
            result.setCreatedAt(profile.getCreatedAt());
            result.setUpdatedAt(profile.getUpdatedAt());
            return result;
        } catch (Exception e) {
            logger.error("Error querying voice profile, userId: {}, provider: {}", userId, p, e);
            throw new RuntimeException("查询失败，请稍后重试");
        }
    }

    @Override
    public UserVoiceProfileResponse getUserVoiceProfileStrict(String userId, String provider) {
        if (Objects.isNull(userId) || userId.trim().isEmpty()) {
            throw new IllegalArgumentException("用户ID不能为空");
        }
        String p = (Objects.isNull(provider) || provider.trim().isEmpty()) ? VoiceProviderEnum.HOJO.getCode() : provider.trim();
        String normalized = VoiceProviderEnum.normalizeCode(p);
        if (normalized != null) {
            p = normalized;
        }
        try {
            UserVoiceProfile profile = userVoiceProfileDAO.selectByUserIdAndProvider(userId, p);
            if (Objects.isNull(profile)) {
                return null;
            }
            UserVoiceProfileResponse result = new UserVoiceProfileResponse();
            result.setId(profile.getId());
            result.setUserId(profile.getUserId());
            result.setVoiceId(profile.getVoiceId());
            result.setVoiceName(profile.getVoiceName());
            result.setNickname(profile.getNickname());
            result.setProvider(profile.getProvider());
            result.setCreatedAt(profile.getCreatedAt());
            result.setUpdatedAt(profile.getUpdatedAt());
            return result;
        } catch (Exception e) {
            logger.error("Error querying voice profile (strict), userId: {}, provider: {}", userId, p, e);
            throw new RuntimeException("查询失败，请稍后重试");
        }
    }

    @Override
    public void deleteUserVoiceProfile(String userId, String provider) {
        if (Objects.isNull(userId) || userId.trim().isEmpty()) {
            throw new IllegalArgumentException("用户ID不能为空");
        }
        String p = provider == null || provider.isBlank() ? VoiceProviderEnum.HOJO.getCode() : provider.trim();
        String normalized = VoiceProviderEnum.normalizeCode(p);
        if (normalized != null) {
            p = normalized;
        }
        try {
            UserVoiceProfile profile = userVoiceProfileDAO.selectByUserIdAndProvider(userId, p);
            if (Objects.isNull(profile)) {
                throw new IllegalArgumentException("用户音色不存在");
            }
            int rows = userVoiceProfileDAO.deleteByUserId(userId, p);
            if (rows == 0) {
                throw new IllegalArgumentException("用户音色不存在");
            }
            logger.info("Voice profile deleted, userId: {}, provider: {}", userId, p);
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error deleting voice profile, userId: {}", userId, e);
            throw new RuntimeException("删除失败，请稍后重试");
        }
    }

    @Override
    public void saveUserVoiceProfile(VoiceCloneRequest request) {
        this.saveUserVoice(request.getUserId(), request.getVoiceName(), request.getNickname(), request.getProvider(), request.getVoiceId());
    }

    private void saveUserVoice(String userId, String voiceName, String nickname, String provider, String voiceId) {
        String p = provider == null || provider.isBlank() ? VoiceProviderEnum.HOJO.getCode() : provider.trim();
        if (!VoiceProviderEnum.isValid(p)) {
            throw new RuntimeException("暂不支持的厂商:" + provider);
        }
        VoiceProviderEnum providerEnum = VoiceProviderEnum.fromCode(p);
        String canonicalProvider = providerEnum.getCode();
        UserVoiceProfile existingProfile = userVoiceProfileDAO.selectByUserId(userId);
        if (Objects.nonNull(existingProfile)) {
            userVoiceProfileDAO.deleteByUserId(userId, canonicalProvider);
        }
        Date now = new Date();
        String effectiveVoiceName = (voiceName == null || voiceName.trim().isEmpty())
                ? (voiceId != null && !voiceId.isBlank() ? voiceId.trim() : null)
                : voiceName.trim();
        String effectiveNickname = (nickname == null || nickname.trim().isEmpty()) ? null : nickname.trim();

        UserVoiceProfile newProfile = new UserVoiceProfile();
        newProfile.setUserId(userId);
        newProfile.setVoiceId(voiceId);
        newProfile.setVoiceName(effectiveVoiceName);
        newProfile.setNickname(effectiveNickname);
        newProfile.setProvider(providerEnum.getCode());
        newProfile.setCreatedAt(now);
        newProfile.setUpdatedAt(now);
        userVoiceProfileDAO.insert(newProfile);
        logger.info("Voice profile saved, userId: {}, voiceId: {}, voiceName: {}, nickname: {}, provider: {}",
                userId, voiceId, effectiveVoiceName, effectiveNickname, providerEnum.getCode());
    }

}
