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
package com.hojoai.omniasst.management.dao.message.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hojoai.omniasst.management.dao.message.PaMessageTaskDAO;
import com.hojoai.omniasst.management.dao.message.PaMessageTaskMapper;
import com.hojoai.omniasst.management.dao.message.po.PaMessageTask;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

/**
 * 消息中心 - 进程任务 DAO 实现
 *
 * @author David
 * @version 1.0, 2026/03/17
 * @since hojoai-webmvc-omniasst-management 1.0.0
 */
@Repository
public class PaMessageTaskDAOImpl implements PaMessageTaskDAO {

    private static final String STATUS_RUNNING = "running";
    private static final String STATUS_COMPLETED = "completed";

    @Resource
    private PaMessageTaskMapper paMessageTaskMapper;

    @Override
    public void insert(PaMessageTask task) {
        paMessageTaskMapper.insert(task);
    }

    @Override
    public PaMessageTask selectByTaskId(String taskId) {
        LambdaQueryWrapper<PaMessageTask> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PaMessageTask::getTaskId, taskId);
        return paMessageTaskMapper.selectOne(wrapper);
    }

    @Override
    public List<PaMessageTask> selectRunningByUserId(String userId) {
        LambdaQueryWrapper<PaMessageTask> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PaMessageTask::getUserId, userId)
               .eq(PaMessageTask::getStatus, STATUS_RUNNING)
               .orderByDesc(PaMessageTask::getCreatedAt);
        return paMessageTaskMapper.selectList(wrapper);
    }

    @Override
    public long countRunningByUserId(String userId) {
        LambdaQueryWrapper<PaMessageTask> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PaMessageTask::getUserId, userId)
               .eq(PaMessageTask::getStatus, STATUS_RUNNING);
        return paMessageTaskMapper.selectCount(wrapper);
    }

    @Override
    public Page<PaMessageTask> selectRunningPageByUserId(Page<PaMessageTask> page, String userId) {
        LambdaQueryWrapper<PaMessageTask> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PaMessageTask::getUserId, userId)
               .eq(PaMessageTask::getStatus, STATUS_RUNNING)
               .orderByDesc(PaMessageTask::getCreatedAt);
        return paMessageTaskMapper.selectPage(page, wrapper);
    }

    @Override
    public int deleteAllByUserId(String userId) {
        LambdaQueryWrapper<PaMessageTask> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PaMessageTask::getUserId, userId);
        return paMessageTaskMapper.delete(wrapper);
    }

    @Override
    public int completeTask(String taskId) {
        LambdaUpdateWrapper<PaMessageTask> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(PaMessageTask::getTaskId, taskId)
               .eq(PaMessageTask::getStatus, STATUS_RUNNING)
               .set(PaMessageTask::getStatus, STATUS_COMPLETED)
               .set(PaMessageTask::getFinishedAt, new Date());
        return paMessageTaskMapper.update(null, wrapper);
    }
}
