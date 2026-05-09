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

/**
 * 录音导入：状态、错误码与校验常量（与 meeting_import_tasks 表约定一致）。
 */
public final class MeetingImportConstants {

    private MeetingImportConstants() {
    }

    public static final int MAX_FILES_PER_BATCH = 5;
    public static final long MAX_FILE_SIZE_BYTES = 500L * 1024 * 1024;

    public static final String SOURCE_FILE_IMPORT = "FILE_IMPORT";

    /** 待分类（上传完成后由流水线更新） */
    public static final String CATEGORY_PENDING = "PENDING_CLASSIFY";
    public static final String CATEGORY_MEETING = "MEETING";
    public static final String CATEGORY_MUSIC = "MUSIC";
    public static final String CATEGORY_AMBIENT = "AMBIENT";

    public static final String PIPELINE_PENDING_UPLOAD = "PENDING_UPLOAD";
    public static final String PIPELINE_TRANSCRIBING = "TRANSCRIBING";
    public static final String PIPELINE_ANALYZING = "ANALYZING";
    public static final String PIPELINE_COMPLETED = "COMPLETED";
    public static final String PIPELINE_FAILED = "FAILED";

    public static final String KNOWLEDGE_PENDING = "PENDING";
    public static final String KNOWLEDGE_PROCESSING = "PROCESSING";
    public static final String KNOWLEDGE_DONE = "DONE";
    public static final String KNOWLEDGE_SKIPPED = "SKIPPED";
    public static final String KNOWLEDGE_FAILED = "FAILED";

    public static final String ERROR_UNSUPPORTED_FORMAT = "UNSUPPORTED_FORMAT";
    public static final String ERROR_FILE_TOO_LARGE = "FILE_TOO_LARGE";
    public static final String ERROR_BATCH_LIMIT_EXCEEDED = "BATCH_LIMIT_EXCEEDED";
    public static final String ERROR_STORAGE_PRESIGN_FAILED = "STORAGE_PRESIGN_FAILED";
    public static final String ERROR_NOT_FOUND = "NOT_FOUND";
    public static final String ERROR_FORBIDDEN = "FORBIDDEN";
    /** 离线转写/分类尚未接入时可返回 */
    public static final String ERROR_PIPELINE_NOT_CONFIGURED = "PIPELINE_NOT_CONFIGURED";

    public static final String MEETING_TYPE_FILE_IMPORT = "FILE_IMPORT";
}
