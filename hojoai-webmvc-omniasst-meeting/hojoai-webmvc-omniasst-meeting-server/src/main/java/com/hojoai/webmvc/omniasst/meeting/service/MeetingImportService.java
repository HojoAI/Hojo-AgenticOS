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
package com.hojoai.webmvc.omniasst.meeting.service;

import com.hojoai.webmvc.omniasst.meeting.service.bean.*;

import java.util.List;

public interface MeetingImportService {

    /**
     * 外部已完成 OSS 上传时：登记一条会议导入并启动异步流水线（无需 batch-init / upload-complete）。
     */
    MeetingImportRegisterResponse registerFromExternalOss(MeetingImportRegisterRequest request);

    /**
     * 批量初始化导入任务并返回 OSS 直传预签名等信息（单请求最多 5 个文件）。
     *
     * @param request 用户与文件元数据
     * @return 每项任务的预签名 URL 等
     */
    MeetingImportBatchInitResponse batchInit(MeetingImportBatchInitRequest request);

    /**
     * 客户端直传 OSS 完成后的回调，触发转写与后续流水线。
     *
     * @param request 任务与对象键等信息
     */
    void uploadComplete(MeetingImportUploadCompleteRequest request);

    /**
     * 查询用户导入任务列表（处理中或未读等筛选由请求决定）。
     *
     * @param request 查询条件
     * @return 任务条目列表
     */
    List<MeetingImportTaskItemResponse> listTasks(MeetingImportTaskListRequest request);

    /**
     * 统计当前用户仍在处理中的导入任务数量（供首页 Banner 等）。
     *
     * @param userId 用户 ID
     * @return 处理中数量封装
     */
    MeetingImportProcessingCountResponse countProcessing(String userId);

    /**
     * 将导入列表的「新」标记置为已读。
     *
     * @param request 用户与任务范围
     */
    void markRead(MeetingImportReadRequest request);
}
