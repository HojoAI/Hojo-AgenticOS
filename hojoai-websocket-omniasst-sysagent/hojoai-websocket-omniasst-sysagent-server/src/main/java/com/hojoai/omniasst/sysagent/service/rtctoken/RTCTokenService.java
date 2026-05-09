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
package com.hojoai.omniasst.sysagent.service.rtctoken;

import org.springframework.web.socket.WebSocketSession;

/**
 * The service interface for rtc token.
 *
 * @author kevanwong
 * @version 1.0, 2025/10/21
 * @since hojoai-websocket-omniasst-sysagent 1.0.0
 */
public interface RTCTokenService {

    /**
     * 解析 WebSocket 消息中的设备信息，签发客户端/服务端 RTC Token 并通过会话回传。
     *
     * @param session 当前 WebSocket 会话
     * @param message 原始 JSON，内含 {@link com.hojoai.omniasst.sysagent.service.rtctoken.bean.BuildRTCTokenMessage}
     */
    void buildRTCToken(WebSocketSession session, String message);
}
