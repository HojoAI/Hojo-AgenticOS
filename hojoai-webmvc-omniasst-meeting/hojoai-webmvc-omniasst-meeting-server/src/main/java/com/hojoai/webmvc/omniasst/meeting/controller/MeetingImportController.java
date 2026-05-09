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
package com.hojoai.webmvc.omniasst.meeting.controller;

import com.hojoai.common.asserts.Asserts;
import com.hojoai.common.response.Response;
import com.hojoai.common.response.ResponseStatusBasic;
import com.hojoai.webmvc.omniasst.meeting.service.MeetingImportService;
import com.hojoai.webmvc.omniasst.meeting.service.bean.*;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 会议录音导入 REST：外部 OSS 登记、批量预签名、上传完成回调、任务列表与已读等。
 * 依赖 {@link com.hojoai.webmvc.omniasst.meeting.service.MeetingImportService}；转写与后续步骤由模块内异步任务执行。
 */
@RestController
@RequestMapping("/v1/meeting/import")
public class MeetingImportController {

    @Resource
    private MeetingImportService meetingImportService;

    /**
     * 推荐：文件已由其他系统上传 OSS 后调用。入参为他方返回的 URL 或对象键 + 原始文件名，返回本系统的 meetingId。
     */
    @PostMapping("/register-from-oss")
    public Response<MeetingImportRegisterResponse> registerFromOss(@RequestBody MeetingImportRegisterRequest request) {
        Asserts.notNull(request, "request");
        MeetingImportRegisterResponse result = meetingImportService.registerFromExternalOss(request);
        return Response.newInstance(ResponseStatusBasic.OK, result);
    }

    /**
     * 批量创建导入任务并返回 OSS 直传预签名 URL（最多 5 个文件，逐项校验）。
     */
    @PostMapping("/batch-init")
    public Response<MeetingImportBatchInitResponse> batchInit(@RequestBody MeetingImportBatchInitRequest request) {
        Asserts.notNull(request, "request");
        MeetingImportBatchInitResponse result = meetingImportService.batchInit(request);
        return Response.newInstance(ResponseStatusBasic.OK, result);
    }

    /**
     * 客户端直传 OSS 成功后回调，进入转写流水线。
     */
    @PostMapping("/upload-complete")
    public Response<String> uploadComplete(@RequestBody MeetingImportUploadCompleteRequest request) {
        Asserts.notNull(request, "request");
        meetingImportService.uploadComplete(request);
        return Response.newInstance(ResponseStatusBasic.OK, "success");
    }

    /**
     * 处理中队列（主流程未完成 COMPLETED 的任务）。
     */
    @PostMapping("/tasks/list")
    public Response<List<MeetingImportTaskItemResponse>> listTasks(@RequestBody MeetingImportTaskListRequest request) {
        Asserts.notNull(request, "request");
        List<MeetingImportTaskItemResponse> list = meetingImportService.listTasks(request);
        return Response.newInstance(ResponseStatusBasic.OK, list);
    }

    /**
     * 首页 Banner：当前仍在处理中的任务数量（含知识处理中）。
     */
    @GetMapping("/processing-count")
    public Response<MeetingImportProcessingCountResponse> processingCount(@RequestParam("userId") String userId) {
        Asserts.notBlank(userId, "userId");
        MeetingImportProcessingCountResponse count = meetingImportService.countProcessing(userId);
        return Response.newInstance(ResponseStatusBasic.OK, count);
    }

    /**
     * 清除列表「新」标记。
     */
    @PostMapping("/read")
    public Response<String> markRead(@RequestBody MeetingImportReadRequest request) {
        Asserts.notNull(request, "request");
        meetingImportService.markRead(request);
        return Response.newInstance(ResponseStatusBasic.OK, "success");
    }
}
