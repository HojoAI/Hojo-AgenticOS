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
package com.hojoai.webmvc.omniasst.translate.websocket;

/**
 * WebSocket 握手 Header 与会话 {@link org.springframework.web.socket.WebSocketSession#getAttributes()} 键名常量。
 */
public class WebSocketConstants {
    /** 请求头：设备 ID */
    public static final String HEADER_DEVICE_ID = "X-Device-Id";
    /** 请求头：用户 ID */
    public static final String HEADER_USER_ID = "X-User-Id";
    /** 会话属性：设备 ID */
    public static final String ATTRIBUTE_DEVICE_ID = "deviceId";
    /** 会话属性：会话路径等标识 */
    public static final String ATTRIBUTE_SESSION_ID = "sessionId";
    /** 会话属性：用户 ID */
    public static final String ATTRIBUTE_USER_ID = "userId";
    /** 会话属性：业务会话 ID（用于推送关联） */
    public static final String ATTRIBUTE_CONVERSATION_ID = "conversationId";
}

