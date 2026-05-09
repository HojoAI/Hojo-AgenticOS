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
package com.hojoai.gateway.omniasst.voice.service.v1.asr;

/**
 * ASR v1 WebSocket 可选平台编码，与 {@link AsrServiceFactory}、客户端 config 中的 platform 字段对应。
 */
public enum AsrPlatform {
    
    HOJO(0, "HOJO"),
    /** 耳机 WebSocket：与 HOJO 相同识别链路，但不走服务端 VAD 前置筛选 */
    HOJO_HEADPHONE(1, "HOJO_HEADPHONE");
    
    private final int code;
    private final String description;

    AsrPlatform(int code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * @return 注册到 {@link AsrServiceFactory} 的整型码
     */
    public int getCode() {
        return code;
    }

    /**
     * @return 人类可读名称
     */
    public String getDescription() {
        return description;
    }

    /**
     * @param code 客户端上报平台码；null 视为 HOJO
     * @return 匹配平台，未知时回退 {@link #HOJO}
     */
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
