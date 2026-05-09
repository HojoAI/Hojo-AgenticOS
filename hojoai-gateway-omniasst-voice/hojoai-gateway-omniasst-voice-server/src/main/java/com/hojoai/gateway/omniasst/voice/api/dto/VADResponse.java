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
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.io.IOException;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class VADResponse {

    @JsonProperty("msg")
    private String msg;
    @JsonProperty("rtn")
    private int rtn;
    @JsonProperty("vad_data")
    @JsonDeserialize(using = VadDataDeserializer.class)
    private int[] vadData;
    @JsonProperty("vid")
    private String vid;

    public VADResponse() {
    }

    public VADResponse(String msg, int rtn, int[] vadData, String vid) {
        this.msg = msg;
        this.rtn = rtn;
        this.vadData = vadData;
        this.vid = vid;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public int getRtn() {
        return rtn;
    }

    public void setRtn(int rtn) {
        this.rtn = rtn;
    }

    public int[] getVadData() {
        return vadData;
    }

    public void setVadData(int[] vadData) {
        this.vadData = vadData;
    }

    public String getVid() {
        return vid;
    }

    public void setVid(String vid) {
        this.vid = vid;
    }
    
    @Override
    public String toString() {
        return "VADResponse{" +
                "msg='" + msg + '\'' +
                ", rtn=" + rtn +
                ", vadData=" + java.util.Arrays.toString(vadData) +
                ", vid='" + vid + '\'' +
                '}';
    }

    public static class VadDataDeserializer extends JsonDeserializer<int[]> {

        @Override
        public int[] deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            String value = p.getValueAsString();

            if (value == null || value.trim().isEmpty()) {
                return null;
            }

            if (value.startsWith("[") && value.endsWith("]")) {
                value = value.substring(1, value.length() - 1);
            }
            String[] parts = value.split(",");
            int[] result = new int[parts.length];
            for (int i = 0; i < parts.length; i++) {
                try {
                    result[i] = Integer.parseInt(parts[i].trim());
                } catch (NumberFormatException e) {
                    throw new IOException("Invalid number in vad_data: " + parts[i], e);
                }
            }
            return result;
        }
    }
}
