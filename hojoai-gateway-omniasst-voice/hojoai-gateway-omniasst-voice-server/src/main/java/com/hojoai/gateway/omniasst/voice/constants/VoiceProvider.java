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
 * 声音克隆等业务使用的厂商标识，用于 {@link com.hojoai.gateway.omniasst.voice.service.voice.VoiceCloneProviderFactory} 路由。
 */
public enum VoiceProvider {

    /** 自建 / Hojo TTS 网关 */
    HOJO("hojo", "HOJO"),
    /** MiniMax 语音克隆 API */
    MINIMAX("minimax", "MINIMAX");

    private final String code;
    private final String name;

    VoiceProvider(String code, String name) {
        this.code = code;
        this.name = name;
    }

    /**
     * @return 请求参数中的 provider 字符串（小写）
     */
    public String getCode() {
        return code;
    }

    /**
     * @return 展示用名称
     */
    public String getName() {
        return name;
    }

    /**
     * 根据 code 解析；空或未知时回退 {@link #HOJO}。
     *
     * @param code provider 字符串
     * @return 枚举值
     */
    public static VoiceProvider fromCode(String code) {
        if (code == null) {
            return HOJO;
        }
        for (VoiceProvider provider : values()) {
            if (provider.code.equalsIgnoreCase(code)) {
                return provider;
            }
        }
        return HOJO;
    }
}
