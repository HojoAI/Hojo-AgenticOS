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

import com.fasterxml.jackson.annotation.JsonAlias;

/**
 * 文件已由其他系统上传至 OSS 后，向会议服务登记并开始转写流水线。
 */
public class MeetingImportRegisterRequest {

    @JsonAlias({"user_id"})
    private String userId;
    /**
     * 他方返回的完整访问 URL，或 OSS 对象键（与 batch-init 返回的 objectKey 同语义）。
     */
    @JsonAlias({"oss_url_or_object_key"})
    private String ossUrlOrObjectKey;
    /**
     * 可选。用户侧原始文件名，用于默认标题；不传则从 {@code ossUrlOrObjectKey} 解析出的对象键最后一段推导
     * （如 {@code .../audio.wav} → {@code audio.wav}）。扩展名优先从此字段解析，否则从对象键解析。
     */
    @JsonAlias({"original_filename"})
    private String originalFilename;
    /** 可选；若传则校验不超过 500MB */
    @JsonAlias({"file_size_bytes"})
    private Long fileSizeBytes;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getOssUrlOrObjectKey() {
        return ossUrlOrObjectKey;
    }

    public void setOssUrlOrObjectKey(String ossUrlOrObjectKey) {
        this.ossUrlOrObjectKey = ossUrlOrObjectKey;
    }

    public String getOriginalFilename() {
        return originalFilename;
    }

    public void setOriginalFilename(String originalFilename) {
        this.originalFilename = originalFilename;
    }

    public Long getFileSizeBytes() {
        return fileSizeBytes;
    }

    public void setFileSizeBytes(Long fileSizeBytes) {
        this.fileSizeBytes = fileSizeBytes;
    }
}
