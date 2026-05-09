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
package com.hojoai.webmvc.omniasst.meeting.controller;

import com.hojoai.common.response.Response;
import com.hojoai.common.response.ResponseStatusBasic;
import com.hojoai.webmvc.omniasst.meeting.api.ManagementServiceClient;
import com.hojoai.webmvc.omniasst.meeting.api.ManagementVoiceHttpClient;
import com.hojoai.webmvc.omniasst.meeting.api.dto.VoiceRegisterCommitRequest;
import com.hojoai.webmvc.omniasst.meeting.api.dto.VoiceRegisterCommitResult;
import com.hojoai.webmvc.omniasst.meeting.api.dto.VoiceRegisterPrepareRequest;
import com.hojoai.webmvc.omniasst.meeting.api.dto.VoiceRegisterPrepareResult;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 会议侧声纹/音色注册 BFF，转发到 management v1 接口。
 */
@RestController
@RequestMapping("/v1/meeting/voice/register")
public class VoiceRegisterBffController {

    @Resource
    private ManagementServiceClient managementServiceClient;

    @PostMapping(value = "/prepare", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Response<VoiceRegisterPrepareResult> prepare(@RequestBody VoiceRegisterPrepareRequest request) {
        if (request == null || StringUtils.isBlank(request.getUserId())) {
            throw new IllegalArgumentException("userId 不能为空");
        }
        if (StringUtils.isBlank(request.getAudioBase64())) {
            throw new IllegalArgumentException("audioBase64 不能为空");
        }
        Response<VoiceRegisterPrepareResult> remote = managementServiceClient.voiceRegisterPrepare(request);
        if (!ManagementVoiceHttpClient.isManagementOk(remote)) {
            throw new IllegalStateException("prepare 失败: " + (remote != null ? remote.getResMessage() : "无响应"));
        }
        return Response.newInstance(ResponseStatusBasic.OK, remote.getResContent());
    }

    @PostMapping(value = "/commit", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Response<VoiceRegisterCommitResult> commit(@RequestBody VoiceRegisterCommitRequest request) {
        if (request == null || StringUtils.isBlank(request.getUserId())) {
            throw new IllegalArgumentException("userId 不能为空");
        }
        Response<VoiceRegisterCommitResult> remote = managementServiceClient.voiceRegisterCommit(request);
        if (!ManagementVoiceHttpClient.isManagementOk(remote)) {
            throw new IllegalStateException("commit 失败: " + (remote != null ? remote.getResMessage() : "无响应"));
        }
        return Response.newInstance(ResponseStatusBasic.OK, remote.getResContent());
    }
}
