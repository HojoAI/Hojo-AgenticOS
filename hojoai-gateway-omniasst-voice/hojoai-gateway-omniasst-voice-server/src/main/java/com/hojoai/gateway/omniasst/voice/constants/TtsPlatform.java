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
package com.hojoai.gateway.omniasst.voice.constants;

/**
 * WebSocket TTS 队列消费侧平台标识，与 {@code TtsWebSocketRequest#platform} 及 {@link com.hojoai.gateway.omniasst.voice.service.tts.TtsQueueConsumer#getPlatform()} 对齐。
 */
public enum TtsPlatform {

    /** 默认 / Hojo 下游 TTS */
    HOJO(0, "hojo"),
    /** 火山豆包双向 TTS */
    DOUBAO(1, "doubao"),
    /** MiniMax 流式 TTS */
    MINIMAX(2, "minimax");

    private final Integer code;
    private final String name;

    TtsPlatform(Integer code, String name) {
        this.code = code;
        this.name = name;
    }

    /**
     * @return 平台整型码
     */
    public Integer getCode() {
        return code;
    }

    /**
     * @return 平台英文标识
     */
    public String getName() {
        return name;
    }

    /**
     * 根据码解析枚举；未知或 {@code null} 时回退 {@link #HOJO}。
     *
     * @param code 平台码，可为 null
     * @return 对应枚举，默认 HOJO
     */
    public static TtsPlatform fromCode(Integer code) {
        if (code == null) {
            return HOJO;
        }
        for (TtsPlatform platform : values()) {
            if (platform.code.equals(code)) {
                return platform;
            }
        }
        return HOJO;
    }
}
