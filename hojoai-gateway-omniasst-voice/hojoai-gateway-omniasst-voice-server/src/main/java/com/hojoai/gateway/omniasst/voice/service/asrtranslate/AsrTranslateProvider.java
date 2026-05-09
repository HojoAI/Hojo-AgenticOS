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
package com.hojoai.gateway.omniasst.voice.service.asrtranslate;

import org.springframework.web.socket.WebSocketSession;

import java.util.Map;

/**
 * WebSocket「ASR + 翻译」链路的可插拔实现（Azure 或 Hojo 等）。
 */
public interface AsrTranslateProvider {

    /**
     * 初始化会话：语言对、底层 SDK 会话、辅助线程等。
     *
     * @param sessionId 网关侧连接 id
     * @param lang1     双向翻译一端语言码
     * @param lang2     双向翻译另一端语言码
     */
    void configure(String sessionId, String lang1, String lang2);

    /**
     * 推送一帧 PCM 音频；实现类负责 VAD、语种判定与翻译推送。
     *
     * @param session WebSocket（部分实现用于日志或上下文）
     * @param audioData 原始 PCM 字节
     */
    void pushAudio(WebSocketSession session, byte[] audioData);

    /**
     * 设置向上游 WebSocket 回调翻译/错误结果的处理器。
     *
     * @param callback 非空时由实现类在适当时机调用
     */
    void setResultCallback(ResultCallback callback);

    /** 释放会话、线程与 SDK 资源。 */
    void close();

    /** 翻译结果或错误回调（由 {@link #setResultCallback} 注册）。 */
    interface ResultCallback {
        /**
         * @param translations      语种 → 文本（含识别与译文）
         * @param detectedLanguage 当前检测/主语种
         * @param isEnd            是否本句/本轮结束
         */
        void onResult(Map<String, String> translations, String detectedLanguage, boolean isEnd);

        /**
         * @param errorCode    业务错误码
         * @param errorMessage 可读说明
         */
        void onError(String errorCode, String errorMessage);
    }
}
