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
package com.hojoai.omniasst.weather.entity;

/**
 * Dify 文件 DTO
 *
 * @author wangjian
 * @since 2026-01-22
 */
public class DifyFile {

    /**
     * 文件类型，例如：image
     */
    private String type;

    /**
     * 传输方式，例如：remote_url
     */
    private String transfer_method;

    /**
     * 远程 URL
     */
    private String url;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTransfer_method() {
        return transfer_method;
    }

    public void setTransfer_method(String transfer_method) {
        this.transfer_method = transfer_method;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}

