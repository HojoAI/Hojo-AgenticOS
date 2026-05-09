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
package com.hojoai.webmvc.omniasst.translate.api;

import com.hojoai.common.response.Response;
import com.hojoai.omniasst.common.api.conversation.ConversationPushRequest;
import com.hojoai.webmvc.omniasst.translate.api.dto.ConversationMessageResponse;
import com.hojoai.webmvc.omniasst.translate.api.dto.PAPushRequestDTO;
import com.hojoai.webmvc.omniasst.translate.api.dto.VoiceCloneResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

/**
 * 调用 management 服务：音色克隆、PA 推送与会话消息推送。
 */
@FeignClient(name = "hojoai-webmvc-omniasst-management", path = "/omniasst-management/api")
public interface ManagementServiceClient {

    /**
     * 上传音频进行音色克隆并可选落库。
     *
     * @param userId    用户 ID
     * @param voiceName 音色名称，可选
     * @param provider  服务商标识，如 minimax
     * @param needSave  是否保存音色
     * @param file      音频文件
     * @return 克隆结果（含 voiceId）
     */
    @PostMapping(value = "/v0/voice/clone", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    Response<VoiceCloneResponse> cloneVoice(@RequestPart("userId") String userId, @RequestPart(value = "voiceName", required = false) String voiceName, @RequestPart("provider") String provider, @RequestPart("needSave") boolean needSave, @RequestPart("file") MultipartFile file);

    /**
     * PA 通道推送（翻译卡片等）。
     *
     * @param request 推送体
     * @return 通用响应
     */
    @PostMapping("/v0/push")
    Response<String> push(@RequestBody PAPushRequestDTO request);

    /**
     * 向会话推送结构化消息（气泡 + 卡片等）。
     *
     * @param request 会话推送请求
     * @return 推送结果
     */
    @PostMapping("/v0/conversations/conversation/message/push")
    Response<ConversationMessageResponse> pushMessage(@RequestBody ConversationPushRequest request);

}
