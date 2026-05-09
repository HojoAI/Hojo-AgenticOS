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
package com.hojoai.omniasst.management.service.message.impl;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hojoai.common.date.DateUtil;
import com.hojoai.common.page.IPage;
import com.hojoai.common.response.ResponseStatusBasic;
import com.hojoai.omniasst.management.common.ResponseStatusExtra;
import com.hojoai.omniasst.management.dao.message.PaMessageDAO;
import com.hojoai.omniasst.management.dao.message.PaMessageTaskDAO;
import com.hojoai.omniasst.management.dao.message.po.PaMessage;
import com.hojoai.omniasst.management.dao.message.po.PaMessageTask;
import com.hojoai.omniasst.management.service.message.MessageCenterService;
import com.hojoai.omniasst.management.service.message.bean.CreateMessageRequest;
import com.hojoai.omniasst.management.service.message.bean.CreateTaskRequest;
import com.hojoai.omniasst.management.service.message.bean.MessageItemResponse;
import com.hojoai.omniasst.management.service.message.bean.TaskCountResponse;
import com.hojoai.omniasst.management.service.message.bean.TaskItemResponse;
import com.hojoai.omniasst.management.service.message.bean.UnreadCountResponse;
import com.hojoai.omniasst.management.utils.ThrowUtils;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * 消息中心服务实现
 *
 * @author David
 * @version 1.0, 2026/03/17
 * @since hojoai-webmvc-omniasst-management 1.0.0
 */
@Service
public class MessageCenterServiceImpl implements MessageCenterService {

    private static final Logger logger = LoggerFactory.getLogger(MessageCenterServiceImpl.class);

    private static final String MESSAGE_ID_PREFIX = "MSG";
    private static final String TASK_ID_PREFIX = "TSK";

    @Resource
    private PaMessageDAO paMessageDAO;

    @Resource
    private PaMessageTaskDAO paMessageTaskDAO;

    // ==================== 消息相关 ====================

    @Override
    public IPage<MessageItemResponse> listMessages(String userId, long pageId, long pageSize) {
        ThrowUtils.ThrowIf(StringUtils.isBlank(userId), ResponseStatusBasic.ILLEGAL_ARGUMENT, "userId cannot be blank");

        Page<PaMessage> page = new Page<>(pageId, pageSize);
        Page<PaMessage> result = paMessageDAO.selectPageByUserId(page, userId);

        List<MessageItemResponse> content = result.getRecords().stream()
                .map(this::convertToMessageItem)
                .toList();

        return IPage.newInstance((int) result.getCurrent(), (int) result.getSize(), result.getTotal(), content);
    }

    @Override
    public UnreadCountResponse getUnreadCount(String userId) {
        ThrowUtils.ThrowIf(StringUtils.isBlank(userId), ResponseStatusBasic.ILLEGAL_ARGUMENT, "userId cannot be blank");

        long count = paMessageDAO.countUnreadByUserId(userId);
        return new UnreadCountResponse(count);
    }

    @Override
    public void markAsRead(String userId, String messageId) {
        ThrowUtils.ThrowIf(StringUtils.isBlank(userId), ResponseStatusBasic.ILLEGAL_ARGUMENT, "userId cannot be blank");
        ThrowUtils.ThrowIf(StringUtils.isBlank(messageId), ResponseStatusBasic.ILLEGAL_ARGUMENT, "messageId cannot be blank");

        int updated = paMessageDAO.markAsRead(userId, messageId);
        logger.info("Mark message as read, userId: {}, messageId: {}, updated: {}", userId, messageId, updated);
    }

    @Override
    public void batchMarkAsRead(String userId, List<String> messageIds) {
        ThrowUtils.ThrowIf(StringUtils.isBlank(userId), ResponseStatusBasic.ILLEGAL_ARGUMENT, "userId cannot be blank");
        ThrowUtils.ThrowIf(messageIds == null || messageIds.isEmpty(), ResponseStatusBasic.ILLEGAL_ARGUMENT, "messageIds cannot be empty");

        int updated = paMessageDAO.batchMarkAsRead(userId, messageIds);
        logger.info("Batch mark messages as read, userId: {}, count: {}, updated: {}", userId, messageIds.size(), updated);
    }

    @Override
    public void clearAllMessages(String userId) {
        ThrowUtils.ThrowIf(StringUtils.isBlank(userId), ResponseStatusBasic.ILLEGAL_ARGUMENT, "userId cannot be blank");

        int deletedMessages = paMessageDAO.deleteAllByUserId(userId);
        int deletedTasks = paMessageTaskDAO.deleteAllByUserId(userId);
        logger.info("Clear all messages, userId: {}, deletedMessages: {}, deletedTasks: {}", userId, deletedMessages, deletedTasks);
    }

    @Override
    public String createMessage(CreateMessageRequest request) {
        ThrowUtils.ThrowIf(request == null, ResponseStatusBasic.ILLEGAL_ARGUMENT, "request cannot be null");
        ThrowUtils.ThrowIf(StringUtils.isBlank(request.getUserId()), ResponseStatusBasic.ILLEGAL_ARGUMENT, "userId cannot be blank");
        ThrowUtils.ThrowIf(StringUtils.isBlank(request.getType()), ResponseStatusBasic.ILLEGAL_ARGUMENT, "type cannot be blank");
        ThrowUtils.ThrowIf(StringUtils.isBlank(request.getTitle()), ResponseStatusBasic.ILLEGAL_ARGUMENT, "title cannot be blank");

        String messageId = MESSAGE_ID_PREFIX + IdWorker.getIdStr();

        PaMessage message = new PaMessage();
        message.setMessageId(messageId);
        message.setUserId(request.getUserId());
        message.setType(request.getType());
        message.setTitle(request.getTitle());
        message.setBody(request.getBody());
        message.setRelatedId(request.getRelatedId());
        message.setRelatedTitle(request.getRelatedTitle());
        message.setLandingPage(request.getLandingPage());
        message.setCreatedAt(new Date());

        paMessageDAO.insert(message);
        logger.info("Create message, messageId: {}, userId: {}, type: {}", messageId, request.getUserId(), request.getType());

        return messageId;
    }

    // ==================== 进程任务相关 ====================

    @Override
    public IPage<TaskItemResponse> listRunningTasks(String userId, long pageId, long pageSize) {
        ThrowUtils.ThrowIf(StringUtils.isBlank(userId), ResponseStatusBasic.ILLEGAL_ARGUMENT, "userId cannot be blank");

        Page<PaMessageTask> page = new Page<>(pageId, pageSize);
        Page<PaMessageTask> result = paMessageTaskDAO.selectRunningPageByUserId(page, userId);

        List<TaskItemResponse> content = result.getRecords().stream()
                .map(this::convertToTaskItem)
                .toList();

        return IPage.newInstance((int) result.getCurrent(), (int) result.getSize(), result.getTotal(), content);
    }

    @Override
    public TaskCountResponse getRunningTaskCount(String userId) {
        ThrowUtils.ThrowIf(StringUtils.isBlank(userId), ResponseStatusBasic.ILLEGAL_ARGUMENT, "userId cannot be blank");

        long count = paMessageTaskDAO.countRunningByUserId(userId);
        return new TaskCountResponse(count);
    }

    @Override
    public String createTask(CreateTaskRequest request) {
        ThrowUtils.ThrowIf(request == null, ResponseStatusBasic.ILLEGAL_ARGUMENT, "request cannot be null");
        ThrowUtils.ThrowIf(StringUtils.isBlank(request.getUserId()), ResponseStatusBasic.ILLEGAL_ARGUMENT, "userId cannot be blank");
        ThrowUtils.ThrowIf(StringUtils.isBlank(request.getType()), ResponseStatusBasic.ILLEGAL_ARGUMENT, "type cannot be blank");
        ThrowUtils.ThrowIf(StringUtils.isBlank(request.getTitle()), ResponseStatusBasic.ILLEGAL_ARGUMENT, "title cannot be blank");

        String taskId = TASK_ID_PREFIX + IdWorker.getIdStr();

        PaMessageTask task = new PaMessageTask();
        task.setTaskId(taskId);
        task.setUserId(request.getUserId());
        task.setType(request.getType());
        task.setTitle(request.getTitle());
        task.setStatus("running");
        task.setProgressText(request.getProgressText());
        task.setEstimatedSeconds(request.getEstimatedSeconds());
        task.setRelatedId(request.getRelatedId());
        task.setCreatedAt(new Date());

        paMessageTaskDAO.insert(task);
        logger.info("Create task, taskId: {}, userId: {}, type: {}", taskId, request.getUserId(), request.getType());

        return taskId;
    }

    @Override
    public void completeTask(String taskId) {
        ThrowUtils.ThrowIf(StringUtils.isBlank(taskId), ResponseStatusBasic.ILLEGAL_ARGUMENT, "taskId cannot be blank");

        PaMessageTask task = paMessageTaskDAO.selectByTaskId(taskId);
        ThrowUtils.ThrowIf(task == null, ResponseStatusExtra.TASK_NOT_FOUND, "Task not found");

        int updated = paMessageTaskDAO.completeTask(taskId);
        logger.info("Complete task, taskId: {}, updated: {}", taskId, updated);
    }

    // ==================== 内部转换 ====================

    private MessageItemResponse convertToMessageItem(PaMessage message) {
        MessageItemResponse response = new MessageItemResponse();
        response.setMessageId(message.getMessageId());
        response.setType(message.getType());
        response.setTitle(message.getTitle());
        response.setBody(message.getBody());
        response.setRelatedId(message.getRelatedId());
        response.setRelatedTitle(message.getRelatedTitle());
        response.setLandingPage(message.getLandingPage());
        response.setCreatedAt(DateUtil.formatAsSecond(message.getCreatedAt()));
        if (message.getReadAt() != null) {
            response.setReadAt(DateUtil.formatAsSecond(message.getReadAt()));
        }
        return response;
    }

    private TaskItemResponse convertToTaskItem(PaMessageTask task) {
        TaskItemResponse response = new TaskItemResponse();
        response.setTaskId(task.getTaskId());
        response.setType(task.getType());
        response.setTitle(task.getTitle());
        response.setStatus(task.getStatus());
        response.setProgressText(task.getProgressText());
        response.setEstimatedSeconds(task.getEstimatedSeconds());
        response.setRelatedId(task.getRelatedId());
        response.setCreatedAt(DateUtil.formatAsSecond(task.getCreatedAt()));
        return response;
    }
}
