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
package com.hojoai.webmvc.omniasst.meeting.service.impl;

import com.hojoai.common.asserts.Asserts;
import com.hojoai.webmvc.omniasst.meeting.api.dto.PreSignUrlResponse;
import com.hojoai.webmvc.omniasst.meeting.dao.meeting.MeetingImportTaskDAO;
import com.hojoai.webmvc.omniasst.meeting.dao.meeting.po.Meeting;
import com.hojoai.webmvc.omniasst.meeting.dao.meeting.po.MeetingImportTask;
import com.hojoai.webmvc.omniasst.meeting.service.MeetingImportService;
import com.hojoai.webmvc.omniasst.meeting.service.MeetingService;
import com.hojoai.webmvc.omniasst.meeting.service.bean.*;
import com.hojoai.webmvc.omniasst.meeting.service.importtask.MeetingImportAsyncService;
import com.hojoai.webmvc.omniasst.meeting.service.importtask.MeetingImportConstants;
import com.hojoai.webmvc.omniasst.meeting.service.importtask.MeetingImportDirectProcessService;
import com.hojoai.webmvc.omniasst.meeting.util.StorageUtil;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import xyz.downgoon.snowflake.Snowflake;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * {@link com.hojoai.webmvc.omniasst.meeting.service.MeetingImportService} 实现：OSS 预签名、任务状态、触发异步直处理或流水线。
 */
@Service
public class MeetingImportServiceImpl implements MeetingImportService {

    private static final Logger logger = LoggerFactory.getLogger(MeetingImportServiceImpl.class);

    @Override
    public MeetingImportRegisterResponse registerFromExternalOss(MeetingImportRegisterRequest request) {
        Asserts.notNull(request, "request");
        Asserts.notBlank(request.getUserId(), "userId");
        Asserts.notBlank(request.getOssUrlOrObjectKey(), "ossUrlOrObjectKey");

        if (request.getFileSizeBytes() != null) {
            if (request.getFileSizeBytes() < 0) {
                throw new IllegalArgumentException("文件大小无效");
            }
            if (request.getFileSizeBytes() > MeetingImportConstants.MAX_FILE_SIZE_BYTES) {
                throw new IllegalArgumentException("单文件需小于 500MB");
            }
        }

        String objectKey = storageUtil.normalizeToObjectKey(request.getOssUrlOrObjectKey());
        if (StringUtils.isBlank(objectKey)) {
            throw new IllegalArgumentException("无法解析 OSS 地址或对象键");
        }

        String effectiveFilename = StringUtils.trimToNull(request.getOriginalFilename());
        if (effectiveFilename == null) {
            effectiveFilename = lastSegmentOfObjectKey(objectKey);
        }
        if (effectiveFilename == null) {
            effectiveFilename = "未命名录音";
        }

        String ext = resolveExtension(effectiveFilename);
        if (ext == null) {
            ext = resolveExtensionFromObjectKey(objectKey);
        }
        if (ext == null || !isAllowedExtension(ext)) {
            throw new IllegalArgumentException("仅支持 mp3、wav、m4a");
        }

        String userId = request.getUserId();
        String meetingId = "M" + snowflake.nextId();
        String contentType = contentTypeForExtension(ext);

        Meeting meeting = buildPlaceholderMeeting(userId, meetingId, effectiveFilename);
        meeting.setOssUrl(objectKey);

        MeetingImportFileItemRequest fileMeta = new MeetingImportFileItemRequest();
        fileMeta.setFileName(effectiveFilename);
        // 未传大小时用 0，避免 pre 等环境 MySQL 对 file_size_bytes NOT NULL 且无默认时插入失败（本地库宽松时可能仍成功）
        fileMeta.setFileSizeBytes(request.getFileSizeBytes() != null ? request.getFileSizeBytes() : 0L);

        MeetingImportTask task = buildPlaceholderTask(userId, meetingId, fileMeta, ext, objectKey, contentType);
        task.setPipelineStatus(MeetingImportConstants.PIPELINE_TRANSCRIBING);

        try {
            meetingImportTransactionHelper.insertMeetingAndTask(meeting, task);
        } catch (org.springframework.dao.DataAccessException e) {
            logger.error("[导入登记-外部OSS] 入库失败 userId={}, objectKey={}, meetingId={}", userId, objectKey, meetingId, e);
            throw e;
        }
        // 同步执行直处理（下载 → 转写 → 落库），保证本接口返回时 totalDuration / segments 已就绪；异步仅用于 upload-complete 等场景
        meetingImportDirectProcessService.process(meetingId);

        MeetingImportRegisterResponse resp = new MeetingImportRegisterResponse();
        resp.setMeetingId(meetingId);
        resp.setObjectKey(objectKey);
        MeetingDetailResponse detail = meetingService.getMeetingDetail(meetingId);
        if (detail != null) {
            resp.setTitle(detail.getTitle());
            resp.setOssUrl(detail.getOssUrl());
            resp.setTotalDuration(detail.getTotalDuration());
            resp.setSegments(detail.getSegments());
        }
        logger.info("[导入登记-外部OSS] meetingId: {}, objectKey: {}, totalDuration: {}", meetingId, objectKey,
                detail != null ? detail.getTotalDuration() : null);
        return resp;
    }

    @Resource
    private MeetingImportTaskDAO meetingImportTaskDAO;
    @Resource
    private StorageUtil storageUtil;
    @Resource
    private Snowflake snowflake;
    @Resource
    private MeetingImportAsyncService meetingImportAsyncService;
    @Resource
    private MeetingImportDirectProcessService meetingImportDirectProcessService;
    @Resource
    private MeetingService meetingService;
    @Resource
    private MeetingImportTransactionHelper meetingImportTransactionHelper;

    @Override
    public MeetingImportBatchInitResponse batchInit(MeetingImportBatchInitRequest request) {
        Asserts.notNull(request, "request");
        Asserts.notBlank(request.getUserId(), "userId");
        MeetingImportBatchInitResponse response = new MeetingImportBatchInitResponse();
        List<MeetingImportBatchItemResponse> items = new ArrayList<>();
        response.setItems(items);

        if (CollectionUtils.isEmpty(request.getFiles())) {
            return response;
        }

        String userId = request.getUserId();
        for (int i = 0; i < request.getFiles().size(); i++) {
            MeetingImportFileItemRequest file = request.getFiles().get(i);
            MeetingImportBatchItemResponse item = new MeetingImportBatchItemResponse();

            if (i >= MeetingImportConstants.MAX_FILES_PER_BATCH) {
                item.setErrorCode(MeetingImportConstants.ERROR_BATCH_LIMIT_EXCEEDED);
                item.setErrorMessage("单次最多选择 " + MeetingImportConstants.MAX_FILES_PER_BATCH + " 个文件");
                items.add(item);
                continue;
            }

            if (file == null || StringUtils.isBlank(file.getFileName())) {
                item.setErrorCode(MeetingImportConstants.ERROR_UNSUPPORTED_FORMAT);
                item.setErrorMessage("文件名为空");
                items.add(item);
                continue;
            }

            if (file.getFileSizeBytes() == null || file.getFileSizeBytes() < 0) {
                item.setErrorCode(MeetingImportConstants.ERROR_UNSUPPORTED_FORMAT);
                item.setErrorMessage("文件大小无效");
                items.add(item);
                continue;
            }

            if (file.getFileSizeBytes() > MeetingImportConstants.MAX_FILE_SIZE_BYTES) {
                item.setErrorCode(MeetingImportConstants.ERROR_FILE_TOO_LARGE);
                item.setErrorMessage("单文件需小于 500MB");
                items.add(item);
                continue;
            }

            String ext = resolveExtension(file.getFileName());
            if (ext == null || !isAllowedExtension(ext)) {
                item.setErrorCode(MeetingImportConstants.ERROR_UNSUPPORTED_FORMAT);
                item.setErrorMessage("仅支持 mp3、wav、m4a");
                items.add(item);
                continue;
            }

            String meetingId = "M" + snowflake.nextId();
            String objectKey = "meeting/" + meetingId + "/import." + ext;
            String contentType = contentTypeForExtension(ext);

            PreSignUrlResponse presign;
            try {
                presign = storageUtil.getPresignedUpload(objectKey, contentType);
            } catch (Exception e) {
                logger.error("[导入初始化] 预签名失败 meetingId: {}, objectKey: {}", meetingId, objectKey, e);
                item.setErrorCode(MeetingImportConstants.ERROR_STORAGE_PRESIGN_FAILED);
                item.setErrorMessage("存储预签名失败");
                items.add(item);
                continue;
            }

            Meeting meeting = buildPlaceholderMeeting(userId, meetingId, file.getFileName());
            MeetingImportTask task = buildPlaceholderTask(userId, meetingId, file, ext, objectKey, contentType);

            try {
                meetingImportTransactionHelper.insertMeetingAndTask(meeting, task);
            } catch (Exception e) {
                logger.error("[导入初始化] 入库失败 meetingId: {}", meetingId, e);
                item.setErrorCode(MeetingImportConstants.ERROR_STORAGE_PRESIGN_FAILED);
                item.setErrorMessage("创建会议记录失败");
                items.add(item);
                continue;
            }

            item.setMeetingId(meetingId);
            item.setObjectKey(objectKey);
            item.setPutUrl(presign.getPutUrl());
            item.setGetUrl(presign.getGetUrl());
            items.add(item);
        }

        return response;
    }

    @Override
    public void uploadComplete(MeetingImportUploadCompleteRequest request) {
        Asserts.notNull(request, "request");
        Asserts.notBlank(request.getUserId(), "userId");
        Asserts.notBlank(request.getMeetingId(), "meetingId");
        Asserts.notBlank(request.getOssObjectKey(), "ossObjectKey");

        MeetingImportTask task = meetingImportTaskDAO.selectByMeetingId(request.getMeetingId());
        if (task == null) {
            throw new IllegalArgumentException("导入任务不存在");
        }
        if (!Objects.equals(task.getUserId(), request.getUserId())) {
            throw new IllegalArgumentException("无权操作该任务");
        }
        if (!MeetingImportConstants.PIPELINE_PENDING_UPLOAD.equals(task.getPipelineStatus())) {
            throw new IllegalArgumentException("任务状态不允许完成上传");
        }
        if (!Objects.equals(task.getOssObjectKey(), request.getOssObjectKey())) {
            throw new IllegalArgumentException("OSS 对象键与创建时不一致");
        }

        task.setOssMultipartUploadId(request.getOssMultipartUploadId());
        task.setOssUploadedPartsJson(request.getOssUploadedPartsJson());
        task.setPipelineStatus(MeetingImportConstants.PIPELINE_TRANSCRIBING);
        meetingImportTransactionHelper.applyUploadComplete(task, request.getOssObjectKey());

        meetingImportAsyncService.runPipelineAfterUpload(request.getMeetingId());
    }

    @Override
    public List<MeetingImportTaskItemResponse> listTasks(MeetingImportTaskListRequest request) {
        Asserts.notNull(request, "request");
        Asserts.notBlank(request.getUserId(), "userId");
        int limit = request.getLimit() != null ? request.getLimit() : 50;
        List<MeetingImportTask> list = meetingImportTaskDAO.selectQueueByUserId(request.getUserId(), limit);
        List<MeetingImportTaskItemResponse> out = new ArrayList<>();
        for (MeetingImportTask t : list) {
            MeetingImportTaskItemResponse row = new MeetingImportTaskItemResponse();
            row.setMeetingId(t.getMeetingId());
            row.setTitle(defaultTitle(t.getOriginalFilename()));
            row.setPipelineStatus(t.getPipelineStatus());
            row.setRecordingCategory(t.getRecordingCategory());
            row.setKnowledgeStatus(t.getKnowledgeStatus());
            row.setEstimatedRemainingMinutes(t.getEstimatedRemainingMinutes());
            row.setErrorCode(t.getErrorCode());
            row.setErrorMessage(t.getErrorMessage());
            out.add(row);
        }
        return out;
    }

    @Override
    public MeetingImportProcessingCountResponse countProcessing(String userId) {
        Asserts.notBlank(userId, "userId");
        return new MeetingImportProcessingCountResponse(meetingImportTaskDAO.countActiveProcessing(userId));
    }

    @Override
    public void markRead(MeetingImportReadRequest request) {
        Asserts.notNull(request, "request");
        Asserts.notBlank(request.getUserId(), "userId");
        Asserts.notBlank(request.getMeetingId(), "meetingId");
        int n = meetingImportTaskDAO.markRead(request.getMeetingId(), request.getUserId());
        if (n <= 0) {
            throw new IllegalArgumentException("记录不存在或无权操作");
        }
    }

    private static Meeting buildPlaceholderMeeting(String userId, String meetingId, String fileName) {
        Meeting meeting = new Meeting();
        meeting.setMeetingId(meetingId);
        meeting.setUserId(userId);
        meeting.setTitle(truncate(defaultTitle(fileName), 200));
        meeting.setMeetingType(MeetingImportConstants.MEETING_TYPE_FILE_IMPORT);
        long now = System.currentTimeMillis();
        meeting.setStartTime(now);
        meeting.setEndTime(now);
        meeting.setTotalDuration(0L);
        meeting.setSummaryStatus(0);
        meeting.setSegmentsJson("[]");
        Date stamp = new Date(now);
        meeting.setCreatedAt(stamp);
        meeting.setUpdatedAt(stamp);
        return meeting;
    }

    private static MeetingImportTask buildPlaceholderTask(String userId, String meetingId,
                                                          MeetingImportFileItemRequest file, String ext,
                                                          String objectKey, String contentType) {
        MeetingImportTask task = new MeetingImportTask();
        task.setMeetingId(meetingId);
        task.setUserId(userId);
        task.setOriginalFilename(file.getFileName());
        task.setFileExt(ext);
        task.setFileSizeBytes(file.getFileSizeBytes());
        task.setContentType(contentType);
        task.setOssObjectKey(objectKey);
        task.setSource(MeetingImportConstants.SOURCE_FILE_IMPORT);
        task.setRecordingCategory(MeetingImportConstants.CATEGORY_PENDING);
        task.setPipelineStatus(MeetingImportConstants.PIPELINE_PENDING_UPLOAD);
        task.setKnowledgeStatus(MeetingImportConstants.KNOWLEDGE_PENDING);
        task.setIsNew(1);
        Date stamp = new Date();
        task.setCreatedAt(stamp);
        task.setUpdatedAt(stamp);
        return task;
    }

    /**
     * 对象键路径最后一段（通常即 OSS 上的文件名），无则返回 null。
     */
    private static String lastSegmentOfObjectKey(String objectKey) {
        if (StringUtils.isBlank(objectKey)) {
            return null;
        }
        String trimmed = objectKey.trim();
        int slash = trimmed.lastIndexOf('/');
        String name = slash >= 0 ? trimmed.substring(slash + 1) : trimmed;
        return StringUtils.isNotBlank(name) ? name : null;
    }

    private static String resolveExtension(String fileName) {
        if (StringUtils.isBlank(fileName)) {
            return null;
        }
        int dot = fileName.lastIndexOf('.');
        if (dot < 0 || dot == fileName.length() - 1) {
            return null;
        }
        return fileName.substring(dot + 1).toLowerCase(Locale.ROOT);
    }

    private static String resolveExtensionFromObjectKey(String objectKey) {
        if (StringUtils.isBlank(objectKey)) {
            return null;
        }
        int slash = objectKey.lastIndexOf('/');
        String name = slash >= 0 ? objectKey.substring(slash + 1) : objectKey;
        return resolveExtension(name);
    }

    private static boolean isAllowedExtension(String ext) {
        return "mp3".equals(ext) || "wav".equals(ext) || "m4a".equals(ext);
    }

    private static String contentTypeForExtension(String ext) {
        return switch (ext) {
            case "mp3" -> "audio/mpeg";
            case "wav" -> "audio/wav";
            case "m4a" -> "audio/mp4";
            default -> "application/octet-stream";
        };
    }

    private static String defaultTitle(String fileName) {
        if (StringUtils.isBlank(fileName)) {
            return "未命名录音";
        }
        int dot = fileName.lastIndexOf('.');
        if (dot > 0) {
            return fileName.substring(0, dot);
        }
        return fileName;
    }

    private static String truncate(String s, int max) {
        if (s == null) {
            return null;
        }
        return s.length() <= max ? s : s.substring(0, max);
    }
}
