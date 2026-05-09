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
package com.hojoai.webmvc.omniasst.meeting.dao.meeting;

import com.hojoai.webmvc.omniasst.meeting.dao.meeting.po.MeetingImportTask;

import java.util.List;

public interface MeetingImportTaskDAO {

    int insert(MeetingImportTask task);

    MeetingImportTask selectByMeetingId(String meetingId);

    int updateByMeetingId(MeetingImportTask task);

    /**
     * 处理中队列：未完成主流程的任务（含失败可展示重试）
     */
    List<MeetingImportTask> selectQueueByUserId(String userId, int limit);

    /**
     * 首页「X 条录音处理中」：主流程未结束，或知识仍在处理
     */
    long countActiveProcessing(String userId);

    void updatePipelineFailed(String meetingId, String errorCode, String errorMessage);

    /**
     * 更新主流程状态（不改变 knowledge_status）；会清空 error_code/error_message。
     */
    void updatePipelineStatus(String meetingId, String pipelineStatus);

    int markRead(String meetingId, String userId);
}
