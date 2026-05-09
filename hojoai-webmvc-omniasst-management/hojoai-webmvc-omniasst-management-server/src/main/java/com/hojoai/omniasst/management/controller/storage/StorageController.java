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
package com.hojoai.omniasst.management.controller.storage;

import com.hojoai.common.asserts.Asserts;
import com.hojoai.common.response.Response;
import com.hojoai.omniasst.management.service.storage.StorageService;
import com.hojoai.omniasst.management.service.storage.bean.AppendUploadResponse;
import com.hojoai.omniasst.management.service.storage.bean.ExtendSignResponse;
import com.hojoai.omniasst.management.service.storage.bean.PreSignUrlResponse;
import com.hojoai.omniasst.management.utils.ResultUtils;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

/**
 * OSS 预签名、上传续签等存储相关 HTTP 接口。
 */
@RestController
@RequestMapping("/v0/storage")
public class StorageController {
    private static final Logger logger = LoggerFactory.getLogger(StorageController.class);

    @Resource
    private StorageService storageService;

    @GetMapping("/presign")
    public Response<PreSignUrlResponse> getPreSignUrl(
            @RequestParam(name = "path") String path,
            @RequestParam(name = "contentType", required = false, defaultValue = "") String contentType,
            @RequestParam(name = "style", required = false, defaultValue = "") String style) {
        Asserts.notBlank(path, "path");

        PreSignUrlResponse response = storageService.generatePreSignUrl(path, contentType, style);
        return ResultUtils.Ok(response);
    }

    @GetMapping("/extend-sign")
    public Response<ExtendSignResponse> extendSign(
            @RequestParam(name = "path") String path,
            @RequestParam(name = "style", required = false, defaultValue = "") String style) {
        Asserts.notBlank(path, "path");

        ExtendSignResponse response = storageService.extendSign(path, style);
        return ResultUtils.Ok(response);
    }

    /**
     * 生成追加上传的预签名URL
     * 用于连续上传音频流片段，每次追加到文件末尾
     * 
     * 使用流程：
     * 1. 首次上传：position=0，获取appendUrl和nextPosition
     * 2. 后续上传：使用上次返回的nextPosition作为position参数
     * 3. 重复步骤2直到所有音频片段上传完成
     *
     * @param path        文件路径
     * @param position    追加上传的位置（首次上传传0，后续上传传上次返回的nextPosition）
     * @param contentType 文件类型，如 "audio/mpeg", "audio/wav" 等
     * @return 追加上传URL和下次上传位置
     */
    @GetMapping("/append-upload")
    public Response<AppendUploadResponse> generateAppendUploadUrl(
            @RequestParam(name = "path") String path,
            @RequestParam(name = "position", required = false, defaultValue = "0") Long position,
            @RequestParam(name = "contentType", required = false, defaultValue = "") String contentType) {
        logger.info("追加上传-接收请求 path: {}, position: {}, contentType: {}", path, position, contentType);
        Asserts.notBlank(path, "path");
        Asserts.notNull(position, "position");

        AppendUploadResponse response = storageService.generateAppendUploadUrl(path, position, contentType);
        logger.info("追加上传-返回响应 path: {}, position: {}, appendUrl: {}", path, position, response != null ? response.getAppendUrl() : "null");
        return ResultUtils.Ok(response);
    }
}

