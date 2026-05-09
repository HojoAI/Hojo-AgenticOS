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
package com.hojoai.omniasst.sysagent.controller.conversation;

import com.hojoai.common.response.Response;
import com.hojoai.common.response.ResponseStatusBasic;
import com.hojoai.omniasst.sysagent.service.conversation.bean.DeleteChatRequest;
import com.hojoai.omniasst.sysagent.service.conversation.bean.RechatRequest;
import com.hojoai.omniasst.sysagent.service.conversation.bean.ConversationRequest;
import com.hojoai.omniasst.sysagent.service.conversation.bean.StopRequest;
import com.hojoai.omniasst.sysagent.service.conversation.ConversationService;
import com.hojoai.omniasst.sysagent.service.conversation.memory.MemoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.concurrent.ScheduledFuture;

/**
 * 对话相关 HTTP 接口：基于 SSE 流式输出聊天/重聊，并提供删除消息、停止生成等管理能力。
 * <p>依赖 {@link ConversationService} 处理业务与心跳，请求头 {@code X-User-Id} 用于标识用户。</p>
 */
@RestController
@RequestMapping("/omniasst-sysagent/v0/conversation")
public class ConversationController {
    private static final Logger logger = LoggerFactory.getLogger(ConversationController.class);

    @Autowired
    private ConversationService conversationService;

    @Autowired
    private MemoryService memoryService;

    /**
     * 发起一轮对话，以 SSE 流式返回模型输出。
     *
     * @param request 对话请求体（会话、消息等）
     * @param userId  用户 ID，来自请求头 {@code X-User-Id}
     * @return 长连接 {@link SseEmitter}；处理异常时 {@link SseEmitter#completeWithError(Throwable)}
     */
    @PostMapping(value = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter chat(@RequestBody ConversationRequest request, 
                          @RequestHeader("X-User-Id") String userId) {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
        ScheduledFuture<?> healthCheck = conversationService.healthCheck(emitter);

        try {
            // 异步推送由 ConversationService 写入 emitter；此处仅触发启动
            conversationService.processChat(request, emitter, userId);
        } catch (Exception e) {
            emitter.completeWithError(e);
        }

        emitter.onCompletion(() -> {
            logger.info("SSE connect is onCompletion");
            healthCheck.cancel(true);
        });

        emitter.onTimeout(() -> {
            logger.warn("SSE connect is onTimeout");
            emitter.complete();
            healthCheck.cancel(true);
        });
        
        return emitter;
    }
    
    /**
     * 基于历史上下文重新生成或续聊，输出为 SSE 流。
     *
     * @param request 重聊请求
     * @param userId  用户 ID
     * @return SSE 连接对象
     */
    @PostMapping(value = "/rechat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter rechat(@RequestBody RechatRequest request,
                            @RequestHeader("X-User-Id") String userId) {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
        ScheduledFuture<?> healthCheck = conversationService.healthCheck(emitter);

        try {
            conversationService.processRechat(request, emitter, userId);
        } catch (Exception e) {
            emitter.completeWithError(e);
        }

        emitter.onCompletion(() -> {
            logger.info("SSE rechat connect is onCompletion");
            healthCheck.cancel(true);
        });

        emitter.onTimeout(() -> {
            logger.warn("SSE rechat connect is onTimeout");
            emitter.complete();
            healthCheck.cancel(true);
        });
        
        return emitter;
    }
    
    /**
     * 删除会话中的单条消息（management 侧同步）。
     *
     * @param request 含会话、消息标识等
     * @return 成功为 OK，否则为通用错误
     */
    @PostMapping("/message/delete-chat")
    public Response<Void> deleteChat(@RequestBody DeleteChatRequest request) {
        boolean success = conversationService.deleteChatMessage(request);
        
        if (success) {
            return Response.newInstance(ResponseStatusBasic.OK);
        } else {
            return Response.newInstance(ResponseStatusBasic.GENERAL_ERROR);
        }
    }
    
    /**
     * 删除整个会话及其消息。
     *
     * @param request 会话删除参数
     * @return 操作结果封装
     */
    @PostMapping("/delete-chat")
    public Response<Void> deleteChatConversation(@RequestBody DeleteChatRequest request) {
        boolean success = conversationService.deleteChatConversation(request);
        
        if (success) {
            return Response.newInstance(ResponseStatusBasic.OK);
        } else {
            return Response.newInstance(ResponseStatusBasic.GENERAL_ERROR);
        }
    }
    
    /**
     * 中止指定会话下正在进行的生成（如停止流式输出）。
     *
     * @param request 含 {@code conversationId}、{@code messageId}
     * @return 是否成功通知下游停止
     */
    @PostMapping("/chat/stop")
    public Response<Void> stopConversation(@RequestBody StopRequest request) {
        logger.info("Received stop request: {}", request);
        boolean success = conversationService.stopConversation(
            request.getConversationId(), 
            request.getMessageId()
        );
        
        if (success) {
            return Response.newInstance(ResponseStatusBasic.OK);
        } else {
            return Response.newInstance(ResponseStatusBasic.GENERAL_ERROR);
        }
    }
}