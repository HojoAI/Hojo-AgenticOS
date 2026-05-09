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
package com.hojoai.omniasst.management.controller.voice;

import com.hojoai.common.response.Response;
import com.hojoai.omniasst.management.common.VoiceProviderEnum;
import com.hojoai.omniasst.management.service.voice.VoiceCloneService;
import com.hojoai.omniasst.management.service.voice.bean.UserVoiceProfileResponse;
import com.hojoai.omniasst.management.service.voice.bean.UserVoicePrintProfileResponse;
import com.hojoai.omniasst.management.service.voice.bean.VoiceProfileResponse;
import com.hojoai.omniasst.management.service.voiceprint.VoicePrintService;
import com.hojoai.omniasst.management.service.voiceprint.VoicePrintVectorService;
import com.hojoai.omniasst.management.utils.ResultUtils;
import jakarta.annotation.Resource;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

/**
 * 用户音色与声纹档案相关接口（v1）。
 */
@RestController
@RequestMapping("/v1/voice")
public class VoiceProfileV1Controller {

    @Resource
    private VoiceCloneService voiceCloneService;

    @Resource
    private VoicePrintVectorService voicePrintVectorService;

    @Resource
    private VoicePrintService voicePrintService;

    /**
     * 查询声纹/音色档案
     *
     * type=0 音色（默认）；type=1 声纹
     */
    @GetMapping("/profile")
    public Response<VoiceProfileResponse> getVoiceProfile(
            @RequestParam("userId") String userId,
            @RequestParam(value = "provider", required = false) String provider,
            @RequestParam(value = "type", required = false) Integer type
    ) {
        try {
            if (userId == null || userId.trim().isEmpty()) {
                return ResultUtils.Error("用户ID不能为空");
            }
            int finalType = (type == null) ? 0 : type;
            if (finalType != 0 && finalType != 1) {
                return ResultUtils.Error("type参数非法");
            }

            if (finalType == 1) {
                String echoProvider = (provider == null || provider.trim().isEmpty())
                        ? VoiceProviderEnum.HOJO.getCode()
                        : provider.trim();
                UserVoicePrintProfileResponse voicePrintProfile = voicePrintVectorService.getUserVoicePrintProfile(userId.trim());
                if (Objects.isNull(voicePrintProfile)) {
                    return ResultUtils.Error("用户声纹不存在");
                }
                VoiceProfileResponse res = new VoiceProfileResponse();
                res.setId(voicePrintProfile.getId());
                res.setUserId(voicePrintProfile.getUserId());
                res.setVoiceId(voicePrintProfile.getId() == null ? null : String.valueOf(voicePrintProfile.getId()));
                res.setVoiceName(null);
                res.setProvider(echoProvider);
                res.setCreatedAt(voicePrintProfile.getCreatedAt());
                res.setUpdatedAt(voicePrintProfile.getUpdatedAt());
                res.setNickname(voicePrintProfile.getUserName());
                return ResultUtils.Ok(res);
            }

            String finalProvider = (provider == null || provider.trim().isEmpty()) ? VoiceProviderEnum.HOJO.getCode() : provider.trim();
            if (!VoiceProviderEnum.isValid(finalProvider)) {
                return ResultUtils.Error("暂不支持的厂商: " + finalProvider);
            }
            String normalized = VoiceProviderEnum.normalizeCode(finalProvider);
            if (normalized != null) {
                finalProvider = normalized;
            }
            UserVoiceProfileResponse profile = voiceCloneService.getUserVoiceProfile(userId.trim(), finalProvider);
            if (Objects.isNull(profile)) {
                return ResultUtils.Error("用户音色不存在");
            }
            VoiceProfileResponse res = new VoiceProfileResponse();
            res.setId(profile.getId());
            res.setUserId(profile.getUserId());
            res.setVoiceId(profile.getVoiceId());
            res.setVoiceName(profile.getVoiceName());
            res.setProvider(profile.getProvider());
            res.setCreatedAt(profile.getCreatedAt());
            res.setUpdatedAt(profile.getUpdatedAt());
            res.setNickname(blankNickname(profile.getNickname()));
            return ResultUtils.Ok(res);
        } catch (IllegalArgumentException e) {
            return ResultUtils.Error(e.getMessage());
        } catch (Exception e) {
            return ResultUtils.Error("查询失败: " + e.getMessage());
        }
    }

    /**
     * 删除声音/声纹档案
     *
     * type=0 音色（默认）：查找逻辑与 {@link #getVoiceProfile} 一致（含按 userId 回退）；按查到的档案实际 {@code provider} 删除该行，并同时删除该 userId 在声纹表中的记录（若存在）。
     * type=1 仅删除声纹。
     */
    @DeleteMapping(value = "/deregister", produces = MediaType.APPLICATION_JSON_VALUE)
    public Response<VoiceProfileResponse> deregister(
            @RequestParam("userId") String userId,
            @RequestParam(value = "provider", required = false) String provider,
            @RequestParam(value = "type", required = false) Integer type
    ) {
        try {
            if (userId == null || userId.trim().isEmpty()) {
                return ResultUtils.Error("用户ID不能为空");
            }
            int finalType = (type == null) ? 0 : type;
            if (finalType != 0 && finalType != 1) {
                return ResultUtils.Error("type参数非法");
            }

            if (finalType == 1) {
                String echoProvider = (provider == null || provider.trim().isEmpty())
                        ? VoiceProviderEnum.HOJO.getCode()
                        : provider.trim();
                UserVoicePrintProfileResponse voicePrintProfile = voicePrintVectorService.getUserVoicePrintProfile(userId.trim());
                if (Objects.isNull(voicePrintProfile)) {
                    return ResultUtils.Error("用户声纹不存在");
                }
                voicePrintService.deleteUserVoicePrint(userId.trim());

                VoiceProfileResponse res = new VoiceProfileResponse();
                res.setId(voicePrintProfile.getId());
                res.setUserId(voicePrintProfile.getUserId());
                res.setVoiceId(voicePrintProfile.getId() == null ? null : String.valueOf(voicePrintProfile.getId()));
                res.setVoiceName(null);
                res.setProvider(echoProvider);
                res.setCreatedAt(voicePrintProfile.getCreatedAt());
                res.setUpdatedAt(voicePrintProfile.getUpdatedAt());
                res.setNickname(voicePrintProfile.getUserName());
                return ResultUtils.Ok(res);
            }

            String finalProvider = (provider == null || provider.trim().isEmpty()) ? VoiceProviderEnum.HOJO.getCode() : provider.trim();
            if (!VoiceProviderEnum.isValid(finalProvider)) {
                return ResultUtils.Error("暂不支持的厂商: " + finalProvider);
            }
            String normalized = VoiceProviderEnum.normalizeCode(finalProvider);
            if (normalized != null) {
                finalProvider = normalized;
            }
            // 与 GET /profile 一致：先按 provider 查，无则按 userId 回退，避免「查询能查到、hojo 档案却用 minimax 删失败」
            UserVoiceProfileResponse profile = voiceCloneService.getUserVoiceProfile(userId.trim(), finalProvider);
            if (Objects.isNull(profile)) {
                return ResultUtils.Error("用户音色不存在");
            }
            voiceCloneService.deleteUserVoiceProfile(userId.trim(), profile.getProvider());
            voicePrintService.deleteUserVoicePrintIfPresent(userId.trim());

            VoiceProfileResponse res = new VoiceProfileResponse();
            res.setId(profile.getId());
            res.setUserId(profile.getUserId());
            res.setVoiceId(profile.getVoiceId());
            res.setVoiceName(profile.getVoiceName());
            res.setProvider(profile.getProvider());
            res.setCreatedAt(profile.getCreatedAt());
            res.setUpdatedAt(profile.getUpdatedAt());
            res.setNickname(blankNickname(profile.getNickname()));
            return ResultUtils.Ok(res);
        } catch (IllegalArgumentException e) {
            return ResultUtils.Error(e.getMessage());
        } catch (Exception e) {
            return ResultUtils.Error("删除失败: " + e.getMessage());
        }
    }

    /** nickName：仅展示真实昵称；空串为 null，不用 voiceId/voiceName 兜底 */
    private static String blankNickname(String nickname) {
        if (nickname == null || nickname.trim().isEmpty()) {
            return null;
        }
        return nickname.trim();
    }
}

