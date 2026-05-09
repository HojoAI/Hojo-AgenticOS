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

import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * 导入任务异步入口：上传完成后触发 {@link MeetingImportDirectProcessService} 直处理流水线。
 */
@Service
public class MeetingImportAsyncService {

    private static final Logger logger = LoggerFactory.getLogger(MeetingImportAsyncService.class);

    @Resource
    private MeetingImportDirectProcessService meetingImportDirectProcessService;

    @Async
    public void runPipelineAfterUpload(String meetingId) {
        logger.info("[导入异步] 启动直处理 meetingId: {}", meetingId);
        try {
            meetingImportDirectProcessService.process(meetingId);
        } catch (Exception e) {
            logger.error("[导入异步] 流水线异常 meetingId: {}", meetingId, e);
        }
    }
}
