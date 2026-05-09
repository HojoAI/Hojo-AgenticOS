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
package com.hojoai.gateway.omniasst.voice.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ASRRequest {

    @JsonProperty("task")
    private String task;

    @JsonProperty("vid")
    private String vid;

    @JsonProperty("package_index")
    private int packageIndex;

    @JsonProperty("package_size")
    private int packageSize;

    @JsonProperty("package_data")
    private String packageData;

    @JsonProperty("do_itn")
    private int doItn;

    @JsonProperty("package_end")
    private int packageEnd;

    @JsonProperty("lang")
    private String lang;

    @JsonProperty("do_lang_detect")
    private int doLangDetect;

    @JsonProperty("type")
    private String type;

    @JsonProperty("vad_end")
    private int vadEnd;

    @JsonProperty("do_punc")
    private int doPunc = 1;

    public int getDoItn() {
        return doItn;
    }

    public void setDoItn(int doItn) {
        this.doItn = doItn;
    }

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

    public int getDoPunc() {
        return doPunc;
    }

    public void setDoPunc(int doPunc) {
        this.doPunc = doPunc;
    }
    
    @Override
    public String toString() {
        return "ASRRequest{" +
                "task='" + task + '\'' +
                ", vid='" + vid + '\'' +
                ", packageIndex=" + packageIndex +
                ", packageSize=" + packageSize +
                ", packageData='" + (packageData != null ? packageData.substring(0, Math.min(50, packageData.length())) + "..." : "null") + '\'' +
                ", doItn=" + doItn +
                ", packageEnd=" + packageEnd +
                ", lang='" + lang + '\'' +
                ", doLangDetect=" + doLangDetect +
                ", type='" + type + '\'' +
                ", vadEnd=" + vadEnd +
                ", doPunc=" + doPunc +
                '}';
    }
}

