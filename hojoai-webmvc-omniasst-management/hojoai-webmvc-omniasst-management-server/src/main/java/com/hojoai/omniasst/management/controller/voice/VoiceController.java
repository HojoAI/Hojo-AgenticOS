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
import com.hojoai.omniasst.management.service.voice.VoiceCloneService;
import com.hojoai.omniasst.management.service.voice.bean.UserVoiceProfileResponse;
import com.hojoai.omniasst.management.service.voice.bean.VoiceCloneRequest;
import com.hojoai.omniasst.management.service.voice.bean.VoiceCloneResponse;
import com.hojoai.omniasst.management.utils.ResultUtils;
import jakarta.annotation.Resource;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Objects;

/**
 * 音色克隆相关 HTTP 接口（v0）。
 */
@RestController
@RequestMapping("/v0/voice")
public class VoiceController {

    @Resource
    private VoiceCloneService voiceCloneService;

    @PostMapping(value = "/clone", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Response<VoiceCloneResponse> cloneVoice(
            @RequestParam("userId") String userId,
            @RequestParam(value = "voiceName", required = false) String voiceName,
            @RequestParam("provider") String provider,
            @RequestParam("needSave") Boolean needSave,
            @RequestPart("file") MultipartFile file
    ) {
        try {
            VoiceCloneResponse response = voiceCloneService.cloneVoice(userId, voiceName, provider, needSave, file);
            return ResultUtils.Ok(response);
        } catch (IllegalArgumentException e) {
            return ResultUtils.Error(e.getMessage());
        } catch (Exception e) {
            return ResultUtils.Error("音色复刻失败：" + e.getMessage());
        }
    }

    @GetMapping("/profile")
    public Response<UserVoiceProfileResponse> getUserVoiceProfile(
            @RequestParam("userId") String userId,
            @RequestParam("provider") String provider) {
        UserVoiceProfileResponse profile = voiceCloneService.getUserVoiceProfile(userId, provider);
        if (Objects.isNull(profile)) {
            return ResultUtils.Error("用户音色不存在");
        }
        return ResultUtils.Ok(profile);
    }

    @DeleteMapping("/profile")
    public Response<String> deleteUserVoiceProfile(@RequestParam("userId") String userId, @RequestParam("provider") String provider) {
        try {
            voiceCloneService.deleteUserVoiceProfile(userId, provider);
            return ResultUtils.Ok("删除成功");
        } catch (IllegalArgumentException e) {
            return ResultUtils.Error(e.getMessage());
        } catch (Exception e) {
            return ResultUtils.Error("删除失败：" + e.getMessage());
        }
    }

    @PostMapping("/save")
    public Response<String> saveUserVoiceProfile(@RequestBody VoiceCloneRequest request) {
        voiceCloneService.saveUserVoiceProfile(request);
        return ResultUtils.Ok("保存成功");
    }

}
