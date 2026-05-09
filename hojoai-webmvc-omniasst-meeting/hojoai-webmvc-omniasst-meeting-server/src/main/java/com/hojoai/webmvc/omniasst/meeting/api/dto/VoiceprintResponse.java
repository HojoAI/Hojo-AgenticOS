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
package com.hojoai.webmvc.omniasst.meeting.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.StringUtils;

public class VoiceprintResponse {
    private String msg;
    private Integer rtn;
    private String vid;
    @JsonProperty("voiceprint")
    private String voiceprint;

    public String getMsg() { return msg; }
    public void setMsg(String msg) { this.msg = msg; }
    public Integer getRtn() { return rtn; }
    public void setRtn(Integer rtn) { this.rtn = rtn; }
    public String getVid() { return vid; }
    public void setVid(String vid) { this.vid = vid; }

    public void setVoicePrint(String voiceprint) {
        this.voiceprint = voiceprint;
    }

    public double[] getVoicePrint() {
        if (StringUtils.isEmpty(voiceprint)) {
            return null;
        }
        String content = voiceprint.trim();
        if (content.startsWith("[")) {
            content = content.substring(1);
        }
        if (content.endsWith("]")) {
            content = content.substring(0, content.length() - 1);
        }
        String[] values = content.split(",");
        double[] vector = new double[values.length];
        for (int i = 0; i < values.length; i++) {
            vector[i] = Double.parseDouble(values[i].trim());
        }
        return vector;
    }
}

