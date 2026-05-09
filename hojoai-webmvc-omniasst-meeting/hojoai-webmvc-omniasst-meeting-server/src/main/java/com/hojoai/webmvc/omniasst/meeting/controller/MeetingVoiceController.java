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

import com.hojoai.common.asserts.Asserts;
import com.hojoai.common.jackson.JacksonUtil;
import com.hojoai.common.response.Response;
import com.hojoai.common.response.ResponseStatusBasic;
import com.hojoai.webmvc.omniasst.meeting.api.ManagementVoiceHttpClient;
import com.hojoai.webmvc.omniasst.meeting.api.dto.VoiceCloneResContent;
import com.hojoai.webmvc.omniasst.meeting.api.dto.VoiceProfileResponse;
import com.hojoai.webmvc.omniasst.meeting.service.bean.MeetingVoiceSaveRequest;
import com.hojoai.webmvc.omniasst.meeting.service.bean.VoiceReplicateTypeSupport;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * 音色克隆/保存 BFF：路径与会议侧其它接口一致（{@code /v1/meeting/...}、{@link Response}），转发 management {@code /v0/voice/clone}、{@code /v0/voice/save}。
 * <p>
 * 个人声纹注册（同一段录音既要声纹又要音色）请使用 {@code POST .../voiceprint/register/prepare} + {@code .../commit}；内部已串联 management 音色 clone/save 与声纹。
 * 本控制器仍保留，供仅需音色、或与旧版前端兼容的场景。
 */
@RestController
@RequestMapping("/v1/meeting/voice")
public class MeetingVoiceController {

    @Resource
    private ManagementVoiceHttpClient managementVoiceHttpClient;

    /**
     * 查询用户音色/声纹资料：type=0 或省略为声音（默认），type=1 为声纹。
     */
    @GetMapping("/profile")
    public Response<VoiceProfileResponse> profile(
            @RequestParam String userId,
            @RequestParam(required = false) String provider,
            @RequestParam(required = false) Integer type) {
        Asserts.notBlank(userId, "userId");
        if (type != null && type != 0 && type != 1) {
            throw new IllegalArgumentException("type 仅支持 0（声音，默认）或 1（声纹）");
        }
        String prov = StringUtils.isNotBlank(provider) ? provider.trim() : "hojo";
        // 会议侧 null/0=声音、1=声纹；management GET /profile 需 ?type=1 查音色，不传 type 查声纹
        int managementWireType = (type == null || type == 0) ? 1 : 0;

        Response<VoiceProfileResponse> remote = managementVoiceHttpClient.getVoiceProfile(userId.trim(), prov, managementWireType);
        if (!ManagementVoiceHttpClient.isManagementOk(remote)) {
            // management 业务失败（如「用户音色不存在」）应原样透出，避免被包装成 HTTP 500
            return forwardManagementVoiceProfileResponse(remote);
        }
        return Response.newInstance(ResponseStatusBasic.OK, remote.getResContent());
    }

    /**
     * 将 management 查询资料响应转为会议侧 {@link Response}：成功码由调用方判断；此处保留对方 {@code resCode}/{@code resMessage}/{@code resContent}。
     */
    private static Response<VoiceProfileResponse> forwardManagementVoiceProfileResponse(
            Response<VoiceProfileResponse> remote) {
        VoiceProfileResponse content = remote != null ? remote.getResContent() : null;
        Response<VoiceProfileResponse> out = Response.newInstance(ResponseStatusBasic.OK, content);
        if (remote == null) {
            applyResFields(out, "GENERAL_ERROR", "远程服务无有效响应");
            return out;
        }
        applyResFields(out, remote.getResCode(), remote.getResMessage());
        return out;
    }

    private static void applyResFields(Response<?> target, String resCode, String resMessage) {
        BeanWrapper w = new BeanWrapperImpl(target);
        if (StringUtils.isNotBlank(resCode) && w.isWritableProperty("resCode")) {
            w.setPropertyValue("resCode", resCode.trim());
        }
        if (resMessage != null && w.isWritableProperty("resMessage")) {
            w.setPropertyValue("resMessage", resMessage);
        }
    }

    /**
     * multipart/form-data，与 management 文档字段对齐；{@code needSave}、{@code provider}、{@code voiceName} 可省略时使用默认值（见参数说明）。
     * 可选 {@code replicateType}：未传或 {@code VOICE} 表示声音复刻（默认）；传 {@code VOICEPRINT} 将拒绝并提示使用声纹接口。
     */
    @PostMapping(value = "/clone", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Response<VoiceCloneResContent> clone(
            @RequestParam String userId,
            @RequestParam(required = false) String voiceName,
            @RequestParam(required = false) Boolean needSave,
            @RequestParam(required = false) String provider,
            @RequestParam(required = false) String replicateType,
            @RequestPart("file") MultipartFile file) throws IOException {
        VoiceReplicateTypeSupport.validateForVoiceEndpoints(replicateType);
        Asserts.notBlank(userId, "userId");
        Asserts.notNull(file, "file");
        if (file.isEmpty()) {
            throw new IllegalArgumentException("file 不能为空");
        }
        boolean saveFlag = needSave != null && needSave;
        String prov = StringUtils.isNotBlank(provider) ? provider.trim() : "hojo";

        Response<VoiceCloneResContent> remote = managementVoiceHttpClient.cloneVoice(
                userId.trim(),
                voiceName,
                saveFlag,
                prov,
                file.getBytes(),
                file.getOriginalFilename());

        if (!ManagementVoiceHttpClient.isManagementOk(remote)
                || remote.getResContent() == null
                || StringUtils.isBlank(remote.getResContent().getVoiceId())) {
            String detail = remote != null ? JacksonUtil.bean2Json(remote) : "null";
            throw new IllegalStateException("音色克隆未成功: " + detail);
        }
        return Response.newInstance(ResponseStatusBasic.OK, remote.getResContent());
    }

    /**
     * JSON，与 management {@code /v0/voice/save} 一致；{@code provider} 可省略时默认 {@code hojo}，{@code voiceName} 可选。
     */
    @PostMapping(value = "/save", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Response<String> save(@RequestBody MeetingVoiceSaveRequest request) {
        Asserts.notNull(request, "request");
        VoiceReplicateTypeSupport.validateForVoiceEndpoints(request.getReplicateType());
        Asserts.notBlank(request.getUserId(), "userId");
        Asserts.notBlank(request.getVoiceId(), "voiceId");
        if (StringUtils.isBlank(request.getProvider())) {
            request.setProvider("hojo");
        } else {
            request.setProvider(request.getProvider().trim());
        }

        Response<String> remote = managementVoiceHttpClient.saveVoice(request);
        if (!ManagementVoiceHttpClient.isManagementOk(remote)) {
            throw new IllegalStateException("音色保存未成功: " + JacksonUtil.bean2Json(remote));
        }
        return Response.newInstance(ResponseStatusBasic.OK, remote.getResContent());
    }
}
