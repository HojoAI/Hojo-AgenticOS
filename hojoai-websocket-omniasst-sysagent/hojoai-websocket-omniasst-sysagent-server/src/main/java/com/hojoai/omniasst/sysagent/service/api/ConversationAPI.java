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
package com.hojoai.omniasst.sysagent.service.api;

import com.hojoai.omniasst.common.api.avatar.AvatarItem;
import com.hojoai.common.response.Response;
import com.hojoai.omniasst.common.api.conversation.ConversationAddMessageResponse;
import com.hojoai.omniasst.common.api.conversation.ConversationAddMessageRequest;
import com.hojoai.omniasst.sysagent.service.conversation.bean.DeleteChatRequest;
import com.hojoai.omniasst.sysagent.service.conversation.bean.CreateConversationIdResponse;
import com.hojoai.omniasst.sysagent.service.conversation.bean.CreateConversationIdRequest;
import com.hojoai.omniasst.sysagent.service.conversation.bean.ManagementConversationMessageQueryRequest;
import com.hojoai.omniasst.sysagent.service.conversation.bean.ReplaceMessageRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

/**
 * 会话与消息管理服务的 Feign 客户端，对应 {@code hojoai-webmvc-omniasst-management}。
 * <p>用于创建会话、持久化消息、查询/替换/删除消息及更新标题、查询数字人形象等。</p>
 */
@FeignClient(name = "hojoai-webmvc-omniasst-management")
public interface ConversationAPI {

    /**
     * 创建或绑定会话 ID。
     *
     * @param request 创建会话请求
     * @return management 统一响应，body 含新会话标识信息
     */
    @PostMapping("/omniasst-management/api/v0/conversations/id")
    ResponseEntity<Response<CreateConversationIdResponse>> createConversationById(@RequestBody CreateConversationIdRequest request);

    /**
     * 批量保存会话消息（多轮对话落库）。
     *
     * @param messages       待持久化的消息列表
     * @param headphoneMode  是否耳机模式（management 侧会话/落库扩展用）
     * @return 每条消息的保存结果列表
     */
    @PostMapping("/omniasst-management/api/v0/conversations/messages")
    ResponseEntity<Response<List<ConversationAddMessageResponse>>> saveConversationMessages(
            @RequestBody List<ConversationAddMessageRequest> messages,
            @RequestParam("headphoneMode") boolean headphoneMode);

    /**
     * 按条件查询管理侧会话消息列表。
     *
     * @param request 查询条件（会话、分页等）
     * @return 消息集合，结构由 management 定义（原始 {@link Map} 与对端契约一致）
     */
    @PostMapping("/omniasst-management/api/v0/conversations/messages/list")
    ResponseEntity<Response<Map>> getManagementConversationMessageById(@RequestBody ManagementConversationMessageQueryRequest request);

    /**
     * 替换单条已存消息内容。
     *
     * @param request 目标消息与替换内容
     * @return 无业务体时仅状态码
     */
    @PostMapping("/omniasst-management/api/v0/conversations/message/replace")
    ResponseEntity<Response<Void>> replaceConversationMessage(@RequestBody ReplaceMessageRequest request);

    /**
     * 删除单条消息。
     *
     * @param request 会话与消息标识
     * @return 操作结果
     */
    @PostMapping("/omniasst-management/api/v0/conversations/message/delete")
    ResponseEntity<Response<Void>> deleteConversationMessage(@RequestBody DeleteChatRequest request);

    /**
     * 删除整个会话。
     *
     * @param request 会话标识
     * @return 操作结果
     */
    @PostMapping("/omniasst-management/api/v0/conversations/conversation/delete")
    ResponseEntity<Response<Void>> deleteConversation(@RequestBody DeleteChatRequest request);

    /**
     * 更新会话标题。
     *
     * @param conversationId 会话 ID
     * @param title          新标题
     * @return 操作结果
     */
    @GetMapping("/omniasst-management/api/v0/conversations/conversation/title")
    ResponseEntity<Response<Void>> updateConversationTitle(@RequestParam("conversationId") String conversationId, @RequestParam("title") String title);

    /**
     * 按 ID 查询数字人/形象配置。
     *
     * @param avatarId 形象 ID
     * @return 形象详情
     */
    @GetMapping("/omniasst-management/api/v0/avatars/{avatarId}")
    ResponseEntity<Response<AvatarItem>> getAvatarById(@PathVariable("avatarId") String avatarId);
}