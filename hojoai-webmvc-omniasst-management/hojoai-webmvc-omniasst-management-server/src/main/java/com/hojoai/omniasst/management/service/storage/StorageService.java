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
package com.hojoai.omniasst.management.service.storage;

import com.hojoai.omniasst.management.service.storage.bean.AppendUploadResponse;
import com.hojoai.omniasst.management.service.storage.bean.ExtendSignResponse;
import com.hojoai.omniasst.management.service.storage.bean.PreSignUrlResponse;

public interface StorageService {

    PreSignUrlResponse generatePreSignUrl(String path, String contentType, String style);

    ExtendSignResponse extendSign(String path, String style);

    /**
     * 生成追加上传的预签名URL
     * 用于连续上传音频流片段，每次追加到文件末尾
     *
     * @param path        文件路径
     * @param position    追加上传的位置（首次上传传0，后续上传传上次返回的nextPosition）
     * @param contentType 文件类型，如 "audio/mpeg", "audio/wav" 等
     * @return 追加上传URL和下次上传位置
     */
    AppendUploadResponse generateAppendUploadUrl(String path, Long position, String contentType);
}

