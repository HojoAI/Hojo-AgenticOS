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

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

/**
 * The handshake interceptor class.
 *
 * @author kevanwong
 * @version 1.0, 2025/10/21
 * @since hojoai-websocket-omniasst-sysagent 1.0.0
 */
@Component
public class IHandshakeInterceptor implements HandshakeInterceptor {

    /**
     * 将设备相关请求头写入 WebSocket session attributes，供后续 Handler 使用。
     *
     * @return 始终 {@code true} 允许握手继续
     */
    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
        String userAgent = request.getHeaders().getFirst(IWebSocketConstants.HEADER_DEVICE_ID);
        String deviceId = request.getHeaders().getFirst(IWebSocketConstants.HEADER_USER_AGENT);

        attributes.put(IWebSocketConstants.ATTRIBUTE_DEVICE_ID, deviceId);
        attributes.put(IWebSocketConstants.ATTRIBUTE_USER_AGENT, userAgent);
        return true;
    }

    /**
     * 握手完成后回调；当前无额外逻辑。
     */
    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Exception exception) {
    }
}
