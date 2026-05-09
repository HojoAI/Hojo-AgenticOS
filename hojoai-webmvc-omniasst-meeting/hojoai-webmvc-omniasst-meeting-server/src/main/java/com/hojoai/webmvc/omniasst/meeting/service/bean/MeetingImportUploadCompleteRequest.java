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
package com.hojoai.webmvc.omniasst.meeting.service.bean;

/**
 * 客户端直传 OSS 成功后回调；断点续传可附带分片信息。
 */
public class MeetingImportUploadCompleteRequest {

    private String userId;
    private String meetingId;
    /** 与 batch-init 返回的 objectKey 一致 */
    private String ossObjectKey;
    private String ossMultipartUploadId;
    private String ossUploadedPartsJson;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getMeetingId() {
        return meetingId;
    }

    public void setMeetingId(String meetingId) {
        this.meetingId = meetingId;
    }

    public String getOssObjectKey() {
        return ossObjectKey;
    }

    public void setOssObjectKey(String ossObjectKey) {
        this.ossObjectKey = ossObjectKey;
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
}
