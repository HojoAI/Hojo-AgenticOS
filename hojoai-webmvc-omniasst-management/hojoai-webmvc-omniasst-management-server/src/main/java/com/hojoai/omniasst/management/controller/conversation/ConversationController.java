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
package com.hojoai.omniasst.management.controller.conversation;

import com.hojoai.common.asserts.Asserts;
import com.hojoai.common.page.IPage;
import com.hojoai.common.response.Response;
import com.hojoai.omniasst.common.api.conversation.ConversationAddMessageRequest;
import com.hojoai.omniasst.common.api.conversation.ConversationAddMessageResponse;
import com.hojoai.omniasst.common.api.conversation.ConversationPushRequest;
import com.hojoai.omniasst.common.api.conversation.UpdateMessageExtraRequest;
import com.hojoai.omniasst.management.service.conversation.ConversationMessageService;
import com.hojoai.omniasst.management.service.conversation.bean.ConversationListResponse;
import com.hojoai.omniasst.management.service.conversation.bean.ConversationIdRequest;
import com.hojoai.omniasst.management.service.conversation.bean.ConversationIdResponse;
import com.hojoai.omniasst.management.service.conversation.bean.ConversationQueryRequest;
import com.hojoai.omniasst.management.service.conversation.bean.MessageListResponse;
import com.hojoai.omniasst.management.service.conversation.bean.MessageQueryRequest;
import com.hojoai.omniasst.management.service.conversation.bean.ReplaceMessageRequest;
import com.hojoai.omniasst.management.service.conversation.bean.DeleteMessageRequest;
import com.hojoai.omniasst.management.utils.ResultUtils;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * The controller for conversation.
 *
 * @author zhangxuepeng
 * @version 1.0, 2025/12/10
 * @since hojoai-webmvc-omniasst-management 1.0.0
 */
@RestController
@RequestMapping("/v0/conversations")
public class ConversationController {

    @Resource
    private ConversationMessageService conversationService;

    /**
     * 获取新的对话ID与消息ID（预占，不落库）
     */
    @PostMapping("/id")
    public Response<ConversationIdResponse> generateConversationId(@RequestBody(required = false) ConversationIdRequest request) {
        Asserts.notNull(request, "request");
        Asserts.notBlank(request.getUserId(), "userId");
        ConversationIdResponse resp = conversationService.generateConversationIds(request);
        return ResultUtils.Ok(resp);
    }

    /**
     * 创建会话并提交首条消息 / 追加消息
     */
    @PostMapping("/message")
    public Response<ConversationAddMessageResponse> appendMessage(@RequestBody ConversationAddMessageRequest request) {
        ConversationAddMessageResponse result = conversationService.appendMessage(request);
        return ResultUtils.Ok(result);
    }

    /**
     * 批量追加消息
     */
    @PostMapping("/messages")
    public Response<List<ConversationAddMessageResponse>> appendMessages(@RequestBody List<ConversationAddMessageRequest> requests) {
        Asserts.notNull(requests, "requests");
        List<ConversationAddMessageResponse> results = conversationService.appendMessages(requests);
        return ResultUtils.Ok(results);
    }

    /**
     * 根据用户ID查询对话列表（分页）
     */
    @PostMapping("/list")
    public Response<IPage<ConversationListResponse>> listConversations(@RequestBody ConversationQueryRequest request) {
        Asserts.notNull(request, "request");
        Asserts.notBlank(request.getUserId(), "userId");
        IPage<ConversationListResponse> result = conversationService.listByUser(request);
        return ResultUtils.Ok(result);
    }

    /**
     * 根据对话ID查询消息列表（分页）
     */
    @PostMapping("/messages/list")
    public Response<IPage<MessageListResponse>> listMessages(@RequestBody MessageQueryRequest request) {
        Asserts.notNull(request, "request");
        Asserts.notBlank(request.getConversationId(), "conversationId");
        IPage<MessageListResponse> result = conversationService.listMessages(request);
        return ResultUtils.Ok(result);
    }

    /**
     * 替换消息
     */
    @PostMapping("/message/replace")
    public Response replaceMessage(@RequestBody ReplaceMessageRequest replaceMessageRequest) {
        Asserts.notNull(replaceMessageRequest, "replaceMessageRequest");
        Asserts.notBlank(replaceMessageRequest.getConversationId(), "conversationId");
        Asserts.notBlank(replaceMessageRequest.getOldMessageId(), "oldMessageId");
        Asserts.notNull(replaceMessageRequest.getNewMessageRequest(), "newMessageRequest");
        Asserts.notBlank(replaceMessageRequest.getNewMessageRequest().getMessageId(), "newMessageId");

        conversationService.replaceMessage(
            replaceMessageRequest.getConversationId(),
            replaceMessageRequest.getNewMessageRequest(),
            replaceMessageRequest.getOldMessageId()
        );
        return ResultUtils.Ok();
    }

    /**
     * 删除消息
     */
    @PostMapping("/message/delete")
    public Response deleteMessage(@RequestBody DeleteMessageRequest request) {
        Asserts.notNull(request, "request");
        Asserts.notBlank(request.getConversationId(), "conversationId");
        Asserts.notBlank(request.getMessageId(), "messageId");

        conversationService.deleteMessage(request.getConversationId(), request.getMessageId());
        return ResultUtils.Ok();
    }

    /**
     * 删除对话
     */
    @PostMapping("/conversation/delete")
    public Response deleteConversation(@RequestBody DeleteMessageRequest request) {
        Asserts.notNull(request, "request");
        Asserts.notBlank(request.getConversationId(), "conversationId");

        conversationService.deleteConversation(request.getConversationId());
        return ResultUtils.Ok();
    }

    /**
     * 更新对话标题
     */
    @GetMapping("/conversation/title")
    public Response updateConversationTitle(
            @RequestParam("conversationId") String conversationId,
            @RequestParam("title") String title) {
        Asserts.notBlank(conversationId, "conversationId");
        Asserts.notBlank(title, "title");

        conversationService.updateConversationTitle(conversationId, title);
        return ResultUtils.Ok();
    }

    /**
     * 保存推送消息
     * 如果没有conversationId则创建一个新的对话，如果有则直接添加到对应conversation中
     */
    @PostMapping("/conversation/message/push")
    public Response<ConversationAddMessageResponse> pushMessage(@RequestBody ConversationPushRequest request) {
        Asserts.notNull(request, "request");
        Asserts.notBlank(request.getUserId(), "userId");
        Asserts.notNull(request.getContent(), "content");
        Asserts.notFalse(!request.getContent().isEmpty(), "content cannot be empty");

        ConversationAddMessageResponse result = conversationService.pushMessage(request);
        return ResultUtils.Ok(result);
    }

    /**
     * 判断当前用户的所有对话是否都已读
     */
    @GetMapping("/all-read")
    public Response<Boolean> areAllConversationsRead(@RequestHeader("X-User-Id") String userId) {
        Asserts.notBlank(userId, "userId");
        boolean allRead = conversationService.areAllConversationsRead(userId);
        return ResultUtils.Ok(allRead);
    }

    /**
     * 更新消息额外信息
     */
    @PostMapping("/message/extra/update")
    public Response<Boolean> updateMessageExtra(@RequestBody UpdateMessageExtraRequest request) {
        Asserts.notNull(request, "request");
        Asserts.notBlank(request.getConversationId(), "conversationId");
        Asserts.notBlank(request.getMessageId(), "messageId");
        Asserts.notBlank(request.getContentId(), "contentId");
        Asserts.notNull(request.getExtra(), "extra");

        boolean success = conversationService.updateMessageExtra(
            request.getConversationId(),
            request.getMessageId(),
            request.getContentId(),
            request.getExtra()
        );
        return ResultUtils.Ok(success);
    }
}
