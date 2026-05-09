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
package com.hojoai.webmvc.omniasst.meeting.dao.meeting.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hojoai.webmvc.omniasst.meeting.dao.meeting.MeetingImportTaskDAO;
import com.hojoai.webmvc.omniasst.meeting.dao.meeting.MeetingImportTaskMapper;
import com.hojoai.webmvc.omniasst.meeting.dao.meeting.po.MeetingImportTask;
import com.hojoai.webmvc.omniasst.meeting.service.importtask.MeetingImportConstants;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Repository;

import java.util.Arrays;
import java.util.List;

/**
 * 会议导入任务表 {@link com.hojoai.webmvc.omniasst.meeting.dao.meeting.po.MeetingImportTask} 的查询与更新实现。
 */
@Repository
public class MeetingImportTaskDAOImpl implements MeetingImportTaskDAO {

    @Resource
    private MeetingImportTaskMapper meetingImportTaskMapper;

    @Override
    public int insert(MeetingImportTask task) {
        return meetingImportTaskMapper.insert(task);
    }

    @Override
    public MeetingImportTask selectByMeetingId(String meetingId) {
        LambdaQueryWrapper<MeetingImportTask> w = new LambdaQueryWrapper<>();
        w.eq(MeetingImportTask::getMeetingId, meetingId);
        return meetingImportTaskMapper.selectOne(w);
    }

    @Override
    public int updateByMeetingId(MeetingImportTask task) {
        LambdaUpdateWrapper<MeetingImportTask> w = new LambdaUpdateWrapper<>();
        w.eq(MeetingImportTask::getMeetingId, task.getMeetingId());
        return meetingImportTaskMapper.update(task, w);
    }

    @Override
    public List<MeetingImportTask> selectQueueByUserId(String userId, int limit) {
        int safeLimit = Math.min(Math.max(limit, 1), 200);
        LambdaQueryWrapper<MeetingImportTask> w = new LambdaQueryWrapper<>();
        w.eq(MeetingImportTask::getUserId, userId);
        w.ne(MeetingImportTask::getPipelineStatus, MeetingImportConstants.PIPELINE_COMPLETED);
        w.orderByDesc(MeetingImportTask::getCreatedAt);
        Page<MeetingImportTask> page = new Page<>(1, safeLimit);
        return meetingImportTaskMapper.selectPage(page, w).getRecords();
    }

    @Override
    public long countActiveProcessing(String userId) {
        LambdaQueryWrapper<MeetingImportTask> main = new LambdaQueryWrapper<>();
        main.eq(MeetingImportTask::getUserId, userId);
        main.in(MeetingImportTask::getPipelineStatus, Arrays.asList(
                MeetingImportConstants.PIPELINE_PENDING_UPLOAD,
                MeetingImportConstants.PIPELINE_TRANSCRIBING,
                MeetingImportConstants.PIPELINE_ANALYZING
        ));
        long c1 = meetingImportTaskMapper.selectCount(main);

        LambdaQueryWrapper<MeetingImportTask> know = new LambdaQueryWrapper<>();
        know.eq(MeetingImportTask::getUserId, userId);
        know.eq(MeetingImportTask::getPipelineStatus, MeetingImportConstants.PIPELINE_COMPLETED);
        know.eq(MeetingImportTask::getKnowledgeStatus, MeetingImportConstants.KNOWLEDGE_PROCESSING);
        long c2 = meetingImportTaskMapper.selectCount(know);

        return c1 + c2;
    }

    @Override
    public void updatePipelineFailed(String meetingId, String errorCode, String errorMessage) {
        LambdaUpdateWrapper<MeetingImportTask> w = new LambdaUpdateWrapper<>();
        w.eq(MeetingImportTask::getMeetingId, meetingId);
        w.set(MeetingImportTask::getPipelineStatus, MeetingImportConstants.PIPELINE_FAILED);
        w.set(MeetingImportTask::getErrorCode, errorCode);
        w.set(MeetingImportTask::getErrorMessage, errorMessage);
        meetingImportTaskMapper.update(null, w);
    }

    @Override
    public void updatePipelineStatus(String meetingId, String pipelineStatus) {
        LambdaUpdateWrapper<MeetingImportTask> w = new LambdaUpdateWrapper<>();
        w.eq(MeetingImportTask::getMeetingId, meetingId);
        w.set(MeetingImportTask::getPipelineStatus, pipelineStatus);
        w.set(MeetingImportTask::getErrorCode, null);
        w.set(MeetingImportTask::getErrorMessage, null);
        meetingImportTaskMapper.update(null, w);
    }

    @Override
    public int markRead(String meetingId, String userId) {
        LambdaUpdateWrapper<MeetingImportTask> w = new LambdaUpdateWrapper<>();
        w.eq(MeetingImportTask::getMeetingId, meetingId);
        w.eq(MeetingImportTask::getUserId, userId);
        w.set(MeetingImportTask::getIsNew, 0);
        return meetingImportTaskMapper.update(null, w);
    }
}
