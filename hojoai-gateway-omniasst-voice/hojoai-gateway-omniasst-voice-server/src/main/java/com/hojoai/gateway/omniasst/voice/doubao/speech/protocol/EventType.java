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
package com.hojoai.gateway.omniasst.voice.doubao.speech.protocol;

import lombok.Getter;

/**
 * 豆包语音二进制协议中的事件类型（连接/会话/TTS/ASR 等共用数值空间，与官方协议文档一致）。
 * 部分枚举名在不同子协议下复用同一 value。
 */
@Getter
public enum EventType {
    // Default event
    NONE(0),

    // Upstream Connection events (1-49)
    START_CONNECTION(1),
    START_TASK(1),
    FINISH_CONNECTION(2),
    FINISH_TASK(2),

    // Downstream Connection events (50-99)
    CONNECTION_STARTED(50),
    TASK_STARTED(50),
    CONNECTION_FAILED(51),
    TASK_FAILED(51),
    CONNECTION_FINISHED(52),
    TASK_FINISHED(52),

    // Upstream Session events (100-149)
    START_SESSION(100),
    CANCEL_SESSION(101),
    FINISH_SESSION(102),

    // Downstream Session events (150-199)
    SESSION_STARTED(150),
    SESSION_CANCELED(151),
    SESSION_FINISHED(152),
    SESSION_FAILED(153),
    USAGE_RESPONSE(154),
    CHARGE_DATA(154),

    // Upstream General events (200-249)
    TASK_REQUEST(200),
    UPDATE_CONFIG(201),

    // Downstream General events (250-299)
    AUDIO_MUTED(250),

    // Upstream TTS events (300-349)
    SAY_HELLO(300),

    // Downstream TTS events (350-399)
    TTS_SENTENCE_START(350),
    TTS_SENTENCE_END(351),
    TTS_RESPONSE(352),
    TTS_ENDED(359),
    PODCAST_ROUND_START(360),
    PODCAST_ROUND_RESPONSE(361),
    PODCAST_ROUND_END(362),

    // Downstream ASR events (450-499)
    ASR_INFO(450),
    ASR_RESPONSE(451),
    ASR_ENDED(459),

    // Upstream Chat events (500-549)
    CHAT_TTS_TEXT(500),

    // Downstream Chat events (550-599)
    CHAT_RESPONSE(550),
    CHAT_ENDED(559),

    // Subtitle events (650-699)
    SOURCE_SUBTITLE_START(650),
    SOURCE_SUBTITLE_RESPONSE(651),
    SOURCE_SUBTITLE_END(652),
    TRANSLATION_SUBTITLE_START(653),
    TRANSLATION_SUBTITLE_RESPONSE(654),
    TRANSLATION_SUBTITLE_END(655);

    private final int value;

    EventType(int value) {
        this.value = value;
    }

    /**
     * @param value 协议中的整型事件码
     * @return 对应枚举
     * @throws IllegalArgumentException 未知取值
     */
    public static EventType fromValue(int value) {
        for (EventType type : EventType.values()) {
            if (type.value == value) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown EventType value: " + value);
    }
} 