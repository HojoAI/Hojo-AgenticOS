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
package com.hojoai.webmvc.omniasst.meeting.dao.meeting.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.util.Date;

@TableName(value = "meeting_import_tasks")
public class MeetingImportTask implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("meeting_id")
    private String meetingId;

    @TableField("user_id")
    private String userId;

    @TableField("original_filename")
    private String originalFilename;

    @TableField("file_ext")
    private String fileExt;

    @TableField("file_size_bytes")
    private Long fileSizeBytes;

    @TableField("content_type")
    private String contentType;

    @TableField("oss_object_key")
    private String ossObjectKey;

    @TableField("source")
    private String source;

    @TableField("recording_category")
    private String recordingCategory;

    @TableField("pipeline_status")
    private String pipelineStatus;

    @TableField("knowledge_status")
    private String knowledgeStatus;

    @TableField("oss_multipart_upload_id")
    private String ossMultipartUploadId;

    @TableField("oss_uploaded_parts_json")
    private String ossUploadedPartsJson;

    @TableField("error_code")
    private String errorCode;

    @TableField("error_message")
    private String errorMessage;

    @TableField("estimated_remaining_minutes")
    private Integer estimatedRemainingMinutes;

    @TableField("is_new")
    private Integer isNew;

    @TableField("created_at")
    private Date createdAt;

    @TableField("updated_at")
    private Date updatedAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMeetingId() {
        return meetingId;
    }

    public void setMeetingId(String meetingId) {
        this.meetingId = meetingId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getOriginalFilename() {
        return originalFilename;
    }

    public void setOriginalFilename(String originalFilename) {
        this.originalFilename = originalFilename;
    }

    public String getFileExt() {
        return fileExt;
    }

    public void setFileExt(String fileExt) {
        this.fileExt = fileExt;
    }

    public Long getFileSizeBytes() {
        return fileSizeBytes;
    }

    public void setFileSizeBytes(Long fileSizeBytes) {
        this.fileSizeBytes = fileSizeBytes;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getOssObjectKey() {
        return ossObjectKey;
    }

    public void setOssObjectKey(String ossObjectKey) {
        this.ossObjectKey = ossObjectKey;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getRecordingCategory() {
        return recordingCategory;
    }

    public void setRecordingCategory(String recordingCategory) {
        this.recordingCategory = recordingCategory;
    }

    public String getPipelineStatus() {
        return pipelineStatus;
    }

    public void setPipelineStatus(String pipelineStatus) {
        this.pipelineStatus = pipelineStatus;
    }

    public String getKnowledgeStatus() {
        return knowledgeStatus;
    }

    public void setKnowledgeStatus(String knowledgeStatus) {
        this.knowledgeStatus = knowledgeStatus;
    }

    public String getOssMultipartUploadId() {
        return ossMultipartUploadId;
    }

    public void setOssMultipartUploadId(String ossMultipartUploadId) {
        this.ossMultipartUploadId = ossMultipartUploadId;
    }

    public String getOssUploadedPartsJson() {
        return ossUploadedPartsJson;
    }

    public void setOssUploadedPartsJson(String ossUploadedPartsJson) {
        this.ossUploadedPartsJson = ossUploadedPartsJson;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public Integer getEstimatedRemainingMinutes() {
        return estimatedRemainingMinutes;
    }

    public void setEstimatedRemainingMinutes(Integer estimatedRemainingMinutes) {
        this.estimatedRemainingMinutes = estimatedRemainingMinutes;
    }

    public Integer getIsNew() {
        return isNew;
    }

    public void setIsNew(Integer isNew) {
        this.isNew = isNew;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }
}
