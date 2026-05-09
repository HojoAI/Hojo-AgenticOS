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
package com.hojoai.omniasst.management.service.message;

import com.hojoai.common.page.IPage;
import com.hojoai.omniasst.management.service.message.bean.CreateMessageRequest;
import com.hojoai.omniasst.management.service.message.bean.CreateTaskRequest;
import com.hojoai.omniasst.management.service.message.bean.MessageItemResponse;
import com.hojoai.omniasst.management.service.message.bean.TaskCountResponse;
import com.hojoai.omniasst.management.service.message.bean.TaskItemResponse;
import com.hojoai.omniasst.management.service.message.bean.UnreadCountResponse;

import java.util.List;

/**
 * 消息中心服务接口
 *
 * @author David
 * @version 1.0, 2026/03/17
 * @since hojoai-webmvc-omniasst-management 1.0.0
 */
public interface MessageCenterService {

    // ==================== 消息相关 ====================

    /**
     * 分页查询用户的消息列表
     *
     * @param userId   用户ID
     * @param pageId   当前页号
     * @param pageSize 页面大小
     * @return 消息分页列表
     */
    IPage<MessageItemResponse> listMessages(String userId, long pageId, long pageSize);

    /**
     * 查询用户未读消息数
     *
     * @param userId 用户ID
     * @return 未读数量
     */
    UnreadCountResponse getUnreadCount(String userId);

    /**
     * 标记单条消息已读
     *
     * @param userId    用户ID
     * @param messageId 消息ID
     */
    void markAsRead(String userId, String messageId);

    /**
     * 批量标记消息已读
     *
     * @param userId     用户ID
     * @param messageIds 消息ID列表
     */
    void batchMarkAsRead(String userId, List<String> messageIds);

    /**
     * 清空指定用户的所有消息及进程任务
     *
     * @param userId 用户ID
     */
    void clearAllMessages(String userId);

    /**
     * 创建消息（供业务模块或推送链路调用）
     *
     * @param request 创建消息请求
     * @return 生成的 messageId
     */
    String createMessage(CreateMessageRequest request);

    // ==================== 进程任务相关 ====================

    /**
     * 分页查询用户进行中的任务列表
     *
     * @param userId   用户ID
     * @param pageId   当前页号
     * @param pageSize 页面大小
     * @return 进行中任务分页列表
     */
    IPage<TaskItemResponse> listRunningTasks(String userId, long pageId, long pageSize);

    /**
     * 查询用户进行中的任务数量
     *
     * @param userId 用户ID
     * @return 进行中任务数量
     */
    TaskCountResponse getRunningTaskCount(String userId);

    /**
     * 创建进程任务（供业务模块调用）
     *
     * @param request 创建任务请求
     * @return 生成的 taskId
     */
    String createTask(CreateTaskRequest request);

    /**
     * 完成进程任务
     *
     * @param taskId 任务ID
     */
    void completeTask(String taskId);
}
