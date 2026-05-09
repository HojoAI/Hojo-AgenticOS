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
package com.hojoai.omniasst.management.dao.message;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hojoai.omniasst.management.dao.message.po.PaMessageTask;

import java.util.List;

/**
 * 消息中心 - 进程任务 DAO
 *
 * @author David
 * @version 1.0, 2026/03/17
 * @since hojoai-webmvc-omniasst-management 1.0.0
 */
public interface PaMessageTaskDAO {

    void insert(PaMessageTask task);

    PaMessageTask selectByTaskId(String taskId);

    /**
     * 查询用户进行中的任务列表，按创建时间倒序
     */
    List<PaMessageTask> selectRunningByUserId(String userId);

    /**
     * 分页查询用户进行中的任务列表，按创建时间倒序
     */
    Page<PaMessageTask> selectRunningPageByUserId(Page<PaMessageTask> page, String userId);

    /**
     * 统计用户进行中的任务数
     */
    long countRunningByUserId(String userId);

    /**
     * 删除指定用户的全部任务
     *
     * @return 影响行数
     */
    int deleteAllByUserId(String userId);

    /**
     * 完成任务：将 status 置为 completed，并设置完成时间
     *
     * @return 影响行数
     */
    int completeTask(String taskId);
}
