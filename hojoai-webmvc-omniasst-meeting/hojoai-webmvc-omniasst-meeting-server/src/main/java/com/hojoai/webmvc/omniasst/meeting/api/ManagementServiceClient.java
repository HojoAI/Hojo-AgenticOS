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
package com.hojoai.webmvc.omniasst.meeting.api;

import com.hojoai.common.response.Response;
import com.hojoai.omniasst.common.api.conversation.ConversationPushRequest;
import com.hojoai.webmvc.omniasst.meeting.api.dto.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;

@FeignClient(name = "hojoai-webmvc-omniasst-management", path = "/omniasst-management/api")
public interface ManagementServiceClient {

    @GetMapping("/v0/storage/presign")
    Response<PreSignUrlResponse> getPreSignUrl(@RequestParam("path") String path,
                                               @RequestParam(value = "contentType", required = false, defaultValue = "") String contentType,
                                               @RequestParam(value = "style", required = false, defaultValue = "") String style);

    @GetMapping("/v0/storage/append-upload")
    Response<AppendUploadResponse> generateAppendUploadUrl(@RequestParam("path") String path,
                                                            @RequestParam(value = "position", required = false, defaultValue = "0") Long position,
                                                            @RequestParam(value = "contentType", required = false, defaultValue = "") String contentType);
    @PostMapping("/v0/voiceprint/match")
    Response<UserMatchResult> matchVoiceprint(@RequestBody VoiceprintMatchRequest voiceprintMatchRequest);

    @PostMapping("/v0/push")
    Response<String> push(@RequestBody PAPushRequestDTO request);

    @PostMapping("/v0/message-center/tasks/create")
    Response<String> createMessageCenterTask(@RequestBody MessageCenterCreateTaskRequest request);

    @PostMapping("/v0/message-center/tasks/{taskId}/complete")
    Response<Void> completeMessageCenterTask(@PathVariable("taskId") String taskId);

    @PostMapping("/v0/conversations/conversation/message/push")
    Response<ConversationMessageResponse> pushMessage(@RequestBody ConversationPushRequest request);

    @GetMapping("/v0/voiceprint/username")
    Response<String> getUserName(@RequestParam("userId") String userId);

    /**
     * 用户头像 OSS 路径或完整 URL；与 {@link #getUserName} 同属 management 用户侧能力。
     * 未部署时调用方应捕获异常并回退（如使用段落内已存头像）。
     */
    @GetMapping("/v0/voiceprint/user-avatar")
    Response<String> getUserAvatar(@RequestParam("userId") String userId);

    /**
     * 会议声纹注册阶段一：上传音频做克隆/预处理，不写入用户正式声纹关联。
     * 由 management 实现；未部署时可关闭 {@code meeting.voiceprint.register.remote-prepare-enabled}。
     */
    @PostMapping("/v0/voiceprint/meeting/register/prepare")
    Response<MeetingVoiceprintPrepareRemoteResponse> registerVoiceprintPrepare(@RequestBody MeetingVoiceprintPrepareRemoteRequest request);

    /**
     * multipart/form-data：userId、userName、audioFile（与 management 侧字段名一致）。
     * 调用方传入带文件名的 {@link org.springframework.core.io.ByteArrayResource} 等 {@link Resource}。
     */
    @PostMapping(value = "/v0/voiceprint/register", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    Response<String> registerVoicePrint(@RequestPart("userId") String userId,
                                        @RequestPart("userName") String userName,
                                        @RequestPart("audioFile") Resource audioFile);

    /**
     * 删除用户声纹；{@code resContent} 在 management 侧可能为对象或纯字符串，需调用方按需解析。
     */
    @DeleteMapping("/v0/voiceprint/{userId}")
    Response<Object> deleteVoiceprint(@PathVariable("userId") String userId);

    // ==================== v1 voice register ====================

    @PostMapping(value = "/v1/voice/register/prepare", consumes = MediaType.APPLICATION_JSON_VALUE)
    Response<VoiceRegisterPrepareResult> voiceRegisterPrepare(@RequestBody VoiceRegisterPrepareRequest request);

    @PostMapping(value = "/v1/voice/register/commit", consumes = MediaType.APPLICATION_JSON_VALUE)
    Response<VoiceRegisterCommitResult> voiceRegisterCommit(@RequestBody VoiceRegisterCommitRequest request);

}

