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
 * 调用 voice 服务「音频翻译」接口的请求体（与 ASR 分包协议类似并含语言对）。
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AsrTransRequest {
    @JsonProperty("vid")
    private String vid;
    @JsonProperty("package_index")
    private int packageIndex;
    @JsonProperty("package_size")
    private int packageSize;
    @JsonProperty("package_end")
    private int packageEnd;
    @JsonProperty("type")
    private String type;
    @JsonProperty("source_lang")
    private String sourceLang;
    @JsonProperty("lang")
    private String lang;
    @JsonProperty("package_data")
    private String packageData;
    @JsonProperty("vad_end")
    private int vadEnd;
    @JsonProperty("do_punc")
    private int doPunc;
    @JsonProperty("do_lang_detect")
    private int doLangDetect;
    @JsonProperty("do_itn")
    private int doItn;
    @JsonProperty("do_sub")
    private int doSub;

    public String getVid() { return vid; }
    public void setVid(String vid) { this.vid = vid; }
    public int getPackageIndex() { return packageIndex; }
    public void setPackageIndex(int packageIndex) { this.packageIndex = packageIndex; }
    public int getPackageSize() { return packageSize; }
    public void setPackageSize(int packageSize) { this.packageSize = packageSize; }
    public int getPackageEnd() { return packageEnd; }
    public void setPackageEnd(int packageEnd) { this.packageEnd = packageEnd; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getSourceLang() { return sourceLang; }
    public void setSourceLang(String sourceLang) { this.sourceLang = sourceLang; }
    public String getLang() { return lang; }
    public void setLang(String lang) { this.lang = lang; }
    public String getPackageData() { return packageData; }
    public void setPackageData(String packageData) { this.packageData = packageData; }
    public int getVadEnd() { return vadEnd; }
    public void setVadEnd(int vadEnd) { this.vadEnd = vadEnd; }
    public int getDoPunc() { return doPunc; }
    public void setDoPunc(int doPunc) { this.doPunc = doPunc; }
    public int getDoLangDetect() { return doLangDetect; }
    public void setDoLangDetect(int doLangDetect) { this.doLangDetect = doLangDetect; }
    public int getDoItn() { return doItn; }
    public void setDoItn(int doItn) { this.doItn = doItn; }
    public int getDoSub() { return doSub; }
    public void setDoSub(int doSub) { this.doSub = doSub; }
}

