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
package com.hojoai.omniasst.sysagent.service.speech.bean;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 流式 ASR 请求体，与 {@code asr_stream} 等流式接口字段对齐。
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ASRRequest {

    /** 任务标识或任务类型。 */
    @JsonProperty("task")
    private String task;

    /** 会话/设备维度的语音 ID，用于关联同一路流。 */
    @JsonProperty("vid")
    private String vid;

    /** 当前音频分片序号（从 0 递增）。 */
    @JsonProperty("package_index")
    private int packageIndex;

    /** 本分片载荷大小（字节或与协议约定一致）。 */
    @JsonProperty("package_size")
    private int packageSize;

    /** Base64 等编码后的音频分片数据。 */
    @JsonProperty("package_data")
    private String packageData;

    /** 是否为最后一个分片：非 0 表示流结束。 */
    @JsonProperty("package_end")
    private int packageEnd;

    /** 指定识别语言。 */
    @JsonProperty("lang")
    private String lang;

    /** 是否做语种检测：与上游 ASR 协议约定一致（0/1）。 */
    @JsonProperty("do_lang_detect")
    private int doLangDetect;

    /** 音频或任务类型（如 pcm、opus 等，依服务端约定）。 */
    @JsonProperty("type")
    private String type;

    /** VAD 是否已检测到语句结束：与上游协议约定一致。 */
    @JsonProperty("vad_end")
    private int vadEnd;

    public String getTask() {
        return task;
    }

    public void setTask(String task) {
        this.task = task;
    }

    public String getVid() {
        return vid;
    }

    public void setVid(String vid) {
        this.vid = vid;
    }

    public int getPackageIndex() {
        return packageIndex;
    }

    public void setPackageIndex(int packageIndex) {
        this.packageIndex = packageIndex;
    }

    public int getPackageSize() {
        return packageSize;
    }

    public void setPackageSize(int packageSize) {
        this.packageSize = packageSize;
    }

    public String getPackageData() {
        return packageData;
    }

    public void setPackageData(String packageData) {
        this.packageData = packageData;
    }

    public int getPackageEnd() {
        return packageEnd;
    }

    public void setPackageEnd(int packageEnd) {
        this.packageEnd = packageEnd;
    }

    public String getLang() {
        return lang;
    }

    public void setLang(String lang) {
        this.lang = lang;
    }

    public int getDoLangDetect() {
        return doLangDetect;
    }

    public void setDoLangDetect(int doLangDetect) {
        this.doLangDetect = doLangDetect;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getVadEnd() {
        return vadEnd;
    }

    public void setVadEnd(int vadEnd) {
        this.vadEnd = vadEnd;
    }
}
