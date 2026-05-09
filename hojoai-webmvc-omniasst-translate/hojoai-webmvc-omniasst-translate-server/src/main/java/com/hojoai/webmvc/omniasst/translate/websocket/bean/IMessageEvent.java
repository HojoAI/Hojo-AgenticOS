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
package com.hojoai.webmvc.omniasst.translate.websocket.bean;

import org.apache.commons.lang3.StringUtils;

/**
 * 翻译 WebSocket 与语音服务之间的事件类型（请求 / 响应 / 错误码）。
 */
public enum IMessageEvent {
    /** 请求：配置源/目标语言并建立下游语音连接 */
    REQ_ASR_TRANSLATE_CONFIG("REQ_ASR_TRANSLATE_CONFIG"),
    /** 请求：心跳 */
    REQ_HEARTBEAT("REQ_HEARTBEAT"),
    /** 请求：翻译音频（网关侧事件名） */
    REQ_TRANSLATE_AUDIO_DATA("REQ_TRANSLATE_AUDIO_DATA"),
    /** 请求：转发至语音服务的音频数据（与网关命名可能并存） */
    REQ_ASR_TRANSLATE_AUDIO_DATA("REQ_ASR_TRANSLATE_AUDIO_DATA"),
    /** 请求：停止当前翻译会话 */
    REQ_TRANSLATE_STOP("REQ_TRANSLATE_STOP"),
    /** 请求：开启音色克隆采集 */
    REQ_VOICE_CLONE_ENABLE("REQ_VOICE_CLONE_ENABLE"),

    /** 响应：心跳应答 */
    BACK_HEARTBEAT("BACK_HEARTBEAT"),
    /** 响应：下发本连接对应的 chatId */
    BACK_CHAT_ID("BACK_CHAT_ID"),
    /** 响应：ASR 文本与译文 */
    BACK_ASR_TEXT("BACK_ASR_TEXT"),
    /** 响应：ASR 完成（预留） */
    BACK_ASR_COMPLETE("BACK_ASR_COMPLETE"),
    /** 响应：停止翻译确认 */
    BACK_TRANSLATE_STOP_ACK("BACK_TRANSLATE_STOP_ACK"),
    /** 响应：音色克隆开关确认 */
    BACK_VOICE_CLONE_ENABLE_ACK("BACK_VOICE_CLONE_ENABLE_ACK"),
    /** 响应：音色克隆结果 */
    BACK_VOICE_CLONE_RESULT("BACK_VOICE_CLONE_RESULT"),
    /** 响应：语音服务配置 ACK（与下游协议一致） */
    BACK_ASR_TRANSLATE_CONFIG_ACK("BACK_ASR_TRANSLATE_CONFIG_ACK"),
    /** 响应：语音服务翻译结果（与下游协议一致） */
    BACK_ASR_TRANSLATE_RESULT("BACK_ASR_TRANSLATE_RESULT"),
    /** 响应：业务错误 */
    BACK_ERROR("BACK_ERROR"),

    /** 占位：通用错误事件类型 */
    GENERAL_ERROR("GENERAL_ERROR"),
    /** 占位：参数非法 */
    ILLEGAL_ARGUMENT("ILLEGAL_ARGUMENT"),
    /** 占位：不支持的操作 */
    UNSUPPORTED_OPERATION("UNSUPPORTED_OPERATION");

    private String eventType;
    private String reqEventKey;

    IMessageEvent(String eventType) { this.eventType = eventType; }
    IMessageEvent(String eventType, String reqEventKey) {
        this.eventType = eventType;
        this.reqEventKey = reqEventKey;
    }

    /**
     * 按事件字符串解析枚举。
     *
     * @param eventType 协议中的 event 字段
     * @return 匹配项，未找到时为 null
     */
    public static IMessageEvent getByEventType(String eventType) {
        if (StringUtils.isBlank(eventType)) return null;
        for (IMessageEvent event : IMessageEvent.values()) {
            if (event.getEventType().equals(eventType)) return event;
        }
        return null;
    }

    public String getEventType() { return eventType; }
    public String getReqEventKey() { return reqEventKey; }
}
