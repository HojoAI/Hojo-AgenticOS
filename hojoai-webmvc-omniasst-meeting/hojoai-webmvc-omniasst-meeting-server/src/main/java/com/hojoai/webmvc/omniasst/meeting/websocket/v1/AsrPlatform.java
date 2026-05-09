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
package com.hojoai.webmvc.omniasst.meeting.websocket.v1;

public enum AsrPlatform {
    
    HOJO(0, "HOJO");
    
    private final int code;
    private final String description;
    
    AsrPlatform(int code, String description) {
        this.code = code;
        this.description = description;
    }
    
    public int getCode() {
        return code;
    }
    
    public String getDescription() {
        return description;
    }
    
    public static AsrPlatform fromCode(Integer code) {
        if (code == null) {
            return HOJO;
        }
        for (AsrPlatform platform : values()) {
            if (platform.code == code) {
                return platform;
            }
        }
        return HOJO;
    }
}
