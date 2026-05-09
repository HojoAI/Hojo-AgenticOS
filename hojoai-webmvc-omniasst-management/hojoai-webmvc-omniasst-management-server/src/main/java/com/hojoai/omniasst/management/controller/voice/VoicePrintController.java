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

import com.hojoai.common.asserts.Asserts;
import com.hojoai.common.response.Response;
import com.hojoai.omniasst.management.service.voice.bean.VoiceprintDisplayNameRequest;
import com.hojoai.omniasst.management.service.voice.bean.VoicePrintMatchRequest;
import com.hojoai.omniasst.management.service.voice.bean.VoicePrintQueryRequest;
import com.hojoai.omniasst.management.service.voice.bean.UserMatchResult;
import com.hojoai.omniasst.management.service.voice.bean.VoicePrintPageResult;
import com.hojoai.omniasst.management.service.voiceprint.VoicePrintService;
import com.hojoai.omniasst.management.service.voiceprint.VoicePrintVectorService;
import com.hojoai.omniasst.management.utils.ResultUtils;
import com.hojoai.omniasst.management.utils.VoiceMultipartResolver;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import java.util.Objects;

/**
 * 声纹匹配、分页查询与展示名等接口（v0）。
 */
@RestController
@RequestMapping("/v0/voiceprint")
public class VoicePrintController {

    @Resource
    private VoicePrintVectorService voicePrintVectorService;

    @Resource
    private VoicePrintService voicePrintService;

    @PostMapping("/match")
    public Response<UserMatchResult> matchVoicePrint(@RequestBody VoicePrintMatchRequest voicePrintMatchRequest) {
        Asserts.notNull(voicePrintMatchRequest, "voicePrintMatchRequest");
        Asserts.notNull(voicePrintMatchRequest.getVoiceprint(), "voiceprint");
        UserMatchResult result = voicePrintVectorService.matchUserVoicePrint(voicePrintMatchRequest.getVoiceprint(), voicePrintMatchRequest.getThreshold());
        return ResultUtils.Ok(result);
    }

    @PostMapping(value = "/register", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Response<String> registerVoicePrint(
            HttpServletRequest request,
            @RequestParam("userId") String userId,
            @RequestParam(value = "userName", required = false) String userName) {
        if (userId == null || userId.isBlank()) {
            return ResultUtils.Error("用户ID不能为空");
        }
        if (!(request instanceof MultipartHttpServletRequest mreq)) {
            return ResultUtils.Error("请使用 Content-Type: multipart/form-data 上传");
        }
        MultipartFile part = VoiceMultipartResolver.resolveAudioMultipart(mreq);
        String effectiveUserName = (userName == null || userName.isBlank()) ? userId.trim() : userName.trim();
        if (Objects.isNull(part) || part.isEmpty()) {
            try {
                voicePrintService.registerVoicePrintFromPending(userId.trim(), effectiveUserName);
                return ResultUtils.Ok("声纹录入成功");
            } catch (IllegalArgumentException e) {
                return ResultUtils.Error(e.getMessage());
            } catch (Exception e) {
                return ResultUtils.Error("声纹录入失败: " + e.getMessage());
            }
        }
        try {
            voicePrintService.registerVoicePrint(userId.trim(), effectiveUserName, part);
            return ResultUtils.Ok("声纹录入成功");
        } catch (IllegalArgumentException e) {
            return ResultUtils.Error(e.getMessage());
        } catch (Exception e) {
            return ResultUtils.Error("声纹录入失败: " + e.getMessage());
        }
    }

    @PostMapping("/list")
    public Response<VoicePrintPageResult> queryUserVoicePrints(@RequestBody VoicePrintQueryRequest request) {
        try {
            VoicePrintPageResult result = voicePrintService.queryUserVoicePrints(request);
            return ResultUtils.Ok(result);
        } catch (Exception e) {
            return ResultUtils.Error("查询失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/{userId}")
    public Response<String> deleteUserVoicePrint(@PathVariable("userId") String userId) {
        try {
            voicePrintService.deleteUserVoicePrint(userId);
            return ResultUtils.Ok("删除成功");
        } catch (IllegalArgumentException e) {
            return ResultUtils.Error(e.getMessage());
        } catch (Exception e) {
            return ResultUtils.Error("删除失败: " + e.getMessage());
        }
    }

    @GetMapping("/username")
    public Response<String> getUserName(@RequestParam("userId") String userId) {
        String userName = voicePrintVectorService.getUserNameByUserId(userId);
        if (userName == null) {
            return ResultUtils.Error("用户声纹不存在");
        }
        return ResultUtils.Ok(userName);
    }

    /**
     * 仅更新声纹库中的展示名（{@code user_voiceprints.user_name}），不修改向量。
     */
    @PostMapping("/display-name")
    public Response<String> updateDisplayName(@RequestBody VoiceprintDisplayNameRequest request) {
        if (request == null || request.getUserId() == null || request.getUserId().isBlank()) {
            return ResultUtils.Error("用户ID不能为空");
        }
        try {
            int rows = voicePrintVectorService.updateUserName(request.getUserId(), request.getUserName());
            if (rows == 0) {
                return ResultUtils.Error("用户声纹不存在");
            }
            return ResultUtils.Ok("更新成功");
        } catch (Exception e) {
            return ResultUtils.Error("更新失败: " + e.getMessage());
        }
    }
}
