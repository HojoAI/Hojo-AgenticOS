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
package com.hojoai.omniasst.management.service.storage.bean;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class AppendUploadResponse {

    /**
     * 文件路径
     */
    private String path;

    /**
     * 追加上传的预签名URL
     */
    private String appendUrl;

    /**
     * 下次追加上传的位置（position）
     * 首次上传返回0，后续上传返回当前文件长度
     */
    private Long nextPosition;

    /**
     * 当前文件长度（字节）
     */
    private Long contentLength;

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getAppendUrl() {
        return appendUrl;
    }

    public void setAppendUrl(String appendUrl) {
        this.appendUrl = appendUrl;
    }

    public Long getNextPosition() {
        return nextPosition;
    }

    public void setNextPosition(Long nextPosition) {
        this.nextPosition = nextPosition;
    }

    public Long getContentLength() {
        return contentLength;
    }

    public void setContentLength(Long contentLength) {
        this.contentLength = contentLength;
    }
}

