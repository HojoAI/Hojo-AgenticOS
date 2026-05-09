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
package com.hojoai.omniasst.management.controller.message;

import com.hojoai.common.asserts.Asserts;
import com.hojoai.common.page.IPage;
import com.hojoai.common.response.Response;
import com.hojoai.omniasst.management.service.message.MessageCenterService;
import com.hojoai.omniasst.management.service.message.bean.BatchReadRequest;
import com.hojoai.omniasst.management.service.message.bean.CreateMessageRequest;
import com.hojoai.omniasst.management.service.message.bean.CreateTaskRequest;
import com.hojoai.omniasst.management.service.message.bean.MessageItemResponse;
import com.hojoai.omniasst.management.service.message.bean.TaskCountResponse;
import com.hojoai.omniasst.management.service.message.bean.TaskItemResponse;
import com.hojoai.omniasst.management.service.message.bean.UnreadCountResponse;
import com.hojoai.omniasst.management.utils.ResultUtils;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 消息中心控制器，提供消息列表、未读/已读、进程任务等能力。
 *
 * @author David
 * @version 1.0, 2026/03/17
 * @since hojoai-webmvc-omniasst-management 1.0.0
 */
@RestController
@RequestMapping("/v0/message-center")
public class MessageCenterController {

    private static final Logger logger = LoggerFactory.getLogger(MessageCenterController.class);

    @Resource
    private MessageCenterService messageCenterService;

    // ==================== 消息相关（面向客户端） ====================

    /**
     * 消息列表（分页）
     */
    @GetMapping("/messages")
    public Response<IPage<MessageItemResponse>> listMessages(
            @RequestHeader("X-User-Id") String userId,
            @RequestParam(value = "pageId", defaultValue = "1") long pageId,
            @RequestParam(value = "pageSize", defaultValue = "20") long pageSize) {
        Asserts.notBlank(userId, "userId");
        IPage<MessageItemResponse> result = messageCenterService.listMessages(userId, pageId, pageSize);
        return ResultUtils.Ok(result);
    }

    /**
     * 未读消息数
     */
    @GetMapping("/messages/unread-count")
    public Response<UnreadCountResponse> getUnreadCount(@RequestHeader("X-User-Id") String userId) {
        Asserts.notBlank(userId, "userId");
        UnreadCountResponse result = messageCenterService.getUnreadCount(userId);
        return ResultUtils.Ok(result);
    }

    /**
     * 单条消息标记已读
     */
    @PostMapping("/messages/{messageId}/read")
    public Response markAsRead(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable("messageId") String messageId) {
        Asserts.notBlank(userId, "userId");
        Asserts.notBlank(messageId, "messageId");
        messageCenterService.markAsRead(userId, messageId);
        return ResultUtils.Ok();
    }

    /**
     * 批量消息标记已读
     */
    @PostMapping("/messages/read")
    public Response batchMarkAsRead(
            @RequestHeader("X-User-Id") String userId,
            @RequestBody BatchReadRequest request) {
        Asserts.notBlank(userId, "userId");
        Asserts.notNull(request, "request");
        Asserts.notNull(request.getMessageIds(), "messageIds");
        messageCenterService.batchMarkAsRead(userId, request.getMessageIds());
        return ResultUtils.Ok();
    }

    /**
     * 清空当前用户的所有消息及进程任务
     */
    @PostMapping("/messages/clear")
    public Response clearAllMessages(@RequestHeader("X-User-Id") String userId) {
        Asserts.notBlank(userId, "userId");
        messageCenterService.clearAllMessages(userId);
        return ResultUtils.Ok();
    }

    // ==================== 进程任务相关（面向客户端） ====================

    /**
     * 进行中任务列表（分页）
     */
    @GetMapping("/tasks/in-progress")
    public Response<IPage<TaskItemResponse>> listRunningTasks(
            @RequestHeader("X-User-Id") String userId,
            @RequestParam(value = "pageId", defaultValue = "1") long pageId,
            @RequestParam(value = "pageSize", defaultValue = "20") long pageSize) {
        Asserts.notBlank(userId, "userId");
        IPage<TaskItemResponse> result = messageCenterService.listRunningTasks(userId, pageId, pageSize);
        return ResultUtils.Ok(result);
    }

    /**
     * 进行中任务数量
     */
    @GetMapping("/tasks/in-progress/count")
    public Response<TaskCountResponse> getRunningTaskCount(@RequestHeader("X-User-Id") String userId) {
        Asserts.notBlank(userId, "userId");
        TaskCountResponse result = messageCenterService.getRunningTaskCount(userId);
        return ResultUtils.Ok(result);
    }

    // ==================== 内部接口（供业务模块调用） ====================

    /**
     * 创建消息
     */
    @PostMapping("/messages/create")
    public Response<String> createMessage(@RequestBody CreateMessageRequest request) {
        Asserts.notNull(request, "request");
        Asserts.notBlank(request.getUserId(), "userId");
        Asserts.notBlank(request.getType(), "type");
        Asserts.notBlank(request.getTitle(), "title");
        String messageId = messageCenterService.createMessage(request);
        return ResultUtils.Ok(messageId);
    }

    /**
     * 创建进程任务
     */
    @PostMapping("/tasks/create")
    public Response<String> createTask(@RequestBody CreateTaskRequest request) {
        Asserts.notNull(request, "request");
        Asserts.notBlank(request.getUserId(), "userId");
        Asserts.notBlank(request.getType(), "type");
        Asserts.notBlank(request.getTitle(), "title");
        String taskId = messageCenterService.createTask(request);
        return ResultUtils.Ok(taskId);
    }

    /**
     * 完成进程任务
     */
    @PostMapping("/tasks/{taskId}/complete")
    public Response completeTask(@PathVariable("taskId") String taskId) {
        Asserts.notBlank(taskId, "taskId");
        messageCenterService.completeTask(taskId);
        return ResultUtils.Ok();
    }
}
