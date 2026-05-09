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
package com.hojoai.omniasst.management.controller.meeting;

import com.hojoai.common.response.Response;
import com.hojoai.omniasst.management.service.meeting.MeetingVoiceprintRegisterService;
import com.hojoai.omniasst.management.service.meeting.bean.MeetingVoiceprintCommitRequest;
import com.hojoai.omniasst.management.service.voice.bean.VoiceCloneResponse;
import com.hojoai.omniasst.management.service.voice.bean.VoiceprintCopyVoiceRequest;
import com.hojoai.omniasst.management.service.voiceprint.VoicePrintService;
import com.hojoai.omniasst.management.utils.ResultUtils;
import com.hojoai.omniasst.management.utils.VoiceMultipartResolver;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import java.util.Objects;

/**
 * 会议侧录音准备 / 昵称提交（与文档 omniasst-meeting v1 meeting voiceprint register 对齐）。
 * 网关可将 {@code /omniasst-meeting/v1/meeting/...} 转发至本服务相同 path（视部署加 context-path）。
 */
@RestController
@RequestMapping("/v1/meeting/voiceprint/register")
public class MeetingVoiceprintController {

    @Resource
    private MeetingVoiceprintRegisterService meetingVoiceprintRegisterService;

    @Resource
    private VoicePrintService voicePrintService;

    /**
     * 与 {@code prepare} 配套：请求体仅 {@code userId}，用 prepare 暂存音频或克隆暂存写入声纹表。
     */
    @PostMapping(value = "/copy-voice", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Response<String> copyVoiceToVoiceprint(@RequestBody VoiceprintCopyVoiceRequest body) {
        if (body == null || body.getUserId() == null || body.getUserId().isBlank()) {
            return ResultUtils.Error("用户ID不能为空");
        }
        try {
            voicePrintService.copyVoiceProfileToVoiceprint(body.getUserId());
            return ResultUtils.Ok("声纹同步成功");
        } catch (IllegalArgumentException e) {
            return ResultUtils.Error(e.getMessage());
        } catch (Exception e) {
            return ResultUtils.Error("声纹同步失败: " + e.getMessage());
        }
    }

    @PostMapping(value = "/prepare", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Response<VoiceCloneResponse> prepare(
            HttpServletRequest request,
            @RequestParam("userId") String userId,
            @RequestParam(value = "voiceName", required = false) String voiceName,
            @RequestParam(value = "type", required = false) Integer type,
            @RequestParam("needSave") Boolean needSave,
            @RequestParam("provider") String provider) {
        if (!(request instanceof MultipartHttpServletRequest mreq)) {
            return ResultUtils.Error("请使用 Content-Type: multipart/form-data");
        }
        if (needSave == null) {
            return ResultUtils.Error("needSave 不能为空");
        }
        MultipartFile file = VoiceMultipartResolver.resolveAudioMultipart(mreq);
        if (Objects.isNull(file) || file.isEmpty()) {
            return ResultUtils.Error("file 不能为空（二进制音频，字段名通常为 file）");
        }
        try {
            VoiceCloneResponse res = meetingVoiceprintRegisterService.prepare(
                    userId, file, voiceName, type, needSave, provider);
            return ResultUtils.Ok(res);
        } catch (IllegalArgumentException e) {
            return ResultUtils.Error(e.getMessage());
        } catch (Exception e) {
            return ResultUtils.Error("准备失败: " + e.getMessage());
        }
    }

    @PostMapping(value = "/commit", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Response<VoiceCloneResponse> commit(@RequestBody MeetingVoiceprintCommitRequest body) {
        try {
            VoiceCloneResponse res = meetingVoiceprintRegisterService.commit(body);
            return ResultUtils.Ok(res);
        } catch (IllegalArgumentException e) {
            return ResultUtils.Error(e.getMessage());
        } catch (Exception e) {
            return ResultUtils.Error("提交失败: " + e.getMessage());
        }
    }
}
