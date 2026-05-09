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
package com.hojoai.webmvc.omniasst.meeting.service.importtask;

import com.hojoai.webmvc.omniasst.meeting.dao.meeting.MeetingImportTaskDAO;
import com.hojoai.webmvc.omniasst.meeting.dao.meeting.po.MeetingImportTask;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * 录音导入后的异步流水线：分类 → 离线转写 → 纪要/解读 → 知识。
 * <p>
 * 当前占位：接入离线 ASR 与分类模型后，在此实现分支逻辑（会议/音乐/环境）。
 * </p>
 */
@Service
public class MeetingImportPipelineService {

    private static final Logger logger = LoggerFactory.getLogger(MeetingImportPipelineService.class);

    @Resource
    private MeetingImportTaskDAO meetingImportTaskDAO;

    public void runPipeline(String meetingId) {
        MeetingImportTask task = meetingImportTaskDAO.selectByMeetingId(meetingId);
        if (task == null) {
            logger.warn("[导入流水线] 任务不存在 meetingId: {}", meetingId);
            return;
        }
        if (!MeetingImportConstants.PIPELINE_TRANSCRIBING.equals(task.getPipelineStatus())) {
            logger.info("[导入流水线] 状态非 TRANSCRIBING，跳过 meetingId: {}, status: {}", meetingId, task.getPipelineStatus());
            return;
        }

        logger.info("[导入流水线] 开始（占位） meetingId: {}, ossKey: {}", meetingId, task.getOssObjectKey());

        // TODO: 1) 录音类型分类（有人声则会议；否则音乐/环境）
        // TODO: 2) 会议：离线转写 → 写 meeting_segments → ANALYZING → ModelService 纪要/解读 → COMPLETED
        // TODO: 3) 音乐：可选歌词转写；知识/纪要跳过
        // TODO: 4) 环境：仅 COMPLETED，无转写与 AI 输出
        // TODO: 5) 知识：主流程完成后异步 knowledge_status=PROCESSING → DONE

        meetingImportTaskDAO.updatePipelineFailed(meetingId,
                MeetingImportConstants.ERROR_PIPELINE_NOT_CONFIGURED,
                "离线转写与分类流水线尚未接入，请接入后替换此占位逻辑");
    }
}
