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
package com.hojoai.omniasst.sysagent.interaction.websocket;

/**
 * The web socket constants class.
 *
 * @author kevanwong
 * @version 1.0, 2025/08/19
 * @since hojoai-websocket-omniasst-sysagent 1.0.0
 */
public class IWebSocketConstants {

    /** 握手请求头：设备 ID（与 {@link com.hojoai.omniasst.sysagent.interaction.websocket.IHandshakeInterceptor} 读取约定一致）。 */
    public static final String HEADER_DEVICE_ID = "X-Device-Id";
    /** 握手请求头：User-Agent。 */
    public static final String HEADER_USER_AGENT = "User-Agent";

    /** 存入 WebSocket session attributes 的设备 ID 键。 */
    public static final String ATTRIBUTE_DEVICE_ID = "deviceId";
    /** 存入 session attributes 的 User-Agent 键。 */
    public static final String ATTRIBUTE_USER_AGENT = "userAgent";
}
