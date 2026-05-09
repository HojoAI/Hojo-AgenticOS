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
package com.hojoai.webmvc.omniasst.translate.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 调用 voice 服务 VAD 的请求体。
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class VADRequest {
    @JsonProperty("vid")
    private String vid;
    @JsonProperty("package_size")
    private int packageSize;
    @JsonProperty("package_data")
    private String packageData;
    @JsonProperty("vad_end")
    private int vadEnd;
    @JsonProperty("vad_drop")
    private int vadDrop;

    public String getVid() { return vid; }
    public void setVid(String vid) { this.vid = vid; }
    public int getPackageSize() { return packageSize; }
    public void setPackageSize(int packageSize) { this.packageSize = packageSize; }
    public String getPackageData() { return packageData; }
    public void setPackageData(String packageData) { this.packageData = packageData; }
    public int getVadEnd() { return vadEnd; }
    public void setVadEnd(int vadEnd) { this.vadEnd = vadEnd; }
    public int getVadDrop() { return vadDrop; }
    public void setVadDrop(int vadDrop) { this.vadDrop = vadDrop; }
}

