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
package com.hojoai.gateway.omniasst.voice.websocket.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AsrConfigResponse {
    
    /**
     * 配置状态：success, failed等
     */
    @JsonProperty("status")
    private String status;
    
    /**
     * 配置信息
     */
    @JsonProperty("message")
    private String message;
    
    /**
     * 平台
     */
    @JsonProperty("platform")
    private Integer platform;
    
    /**
     * 语种
     */
    @JsonProperty("language")
    private String language;
    
    /**
     * 类型
     */
    @JsonProperty("type")
    private String type;

    @JsonProperty("headphoneMode")
    private Boolean headphoneMode;
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public Integer getPlatform() {
        return platform;
    }
    
    public void setPlatform(Integer platform) {
        this.platform = platform;
    }
    
    public String getLanguage() {
        return language;
    }
    
    public void setLanguage(String language) {
        this.language = language;
    }
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }

    public Boolean getHeadphoneMode() {
        return headphoneMode;
    }

    public void setHeadphoneMode(Boolean headphoneMode) {
        this.headphoneMode = headphoneMode;
    }
    
    @Override
    public String toString() {
        return "AsrConfigResponse{" +
                "status='" + status + '\'' +
                ", message='" + message + '\'' +
                ", platform=" + platform +
                ", language='" + language + '\'' +
                ", type='" + type + '\'' +
                ", headphoneMode=" + headphoneMode +
                '}';
    }
}
