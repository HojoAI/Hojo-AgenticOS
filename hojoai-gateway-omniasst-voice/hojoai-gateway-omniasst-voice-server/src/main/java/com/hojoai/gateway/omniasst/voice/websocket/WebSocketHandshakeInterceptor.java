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
package com.hojoai.gateway.omniasst.voice.websocket;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

/**
 * WebSocket 握手阶段从 Header 或 Query 提取 deviceId、userId，写入 session attributes。
 * 供各业务 Handler 识别终端与用户。
 */
@Component
public class WebSocketHandshakeInterceptor implements HandshakeInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketHandshakeInterceptor.class);

    /**
     * 解析设备与用户标识；Header 优先，缺省时从 QueryString 补全。始终允许握手通过。
     *
     * @param request    HTTP 升级请求
     * @param response   响应（未改写）
     * @param wsHandler  即将绑定的 Handler
     * @param attributes 会话属性，写入 deviceId、userId、session 路径等
     * @return 固定为 {@code true}
     */
    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) {
        String deviceId = request.getHeaders().getFirst(WebSocketConstants.HEADER_DEVICE_ID);
        String userId = request.getHeaders().getFirst(WebSocketConstants.HEADER_USER_ID);
        if (StringUtils.isBlank(deviceId) || StringUtils.isBlank(userId)) {
            String query = request.getURI().getQuery();
            if (StringUtils.isNotBlank(query)) {
                String[] params = query.split("&");
                for (String param : params) {
                    String[] kv = param.split("=", 2);
                    if (kv.length == 2) {
                        if ("deviceId".equals(kv[0]) && StringUtils.isBlank(deviceId)) {
                            deviceId = kv[1];
                        } else if ("userId".equals(kv[0]) && StringUtils.isBlank(userId)) {
                            userId = kv[1];
                        }
                    }
                }
            }
        }
        if (StringUtils.isNotBlank(deviceId)) {
            attributes.put(WebSocketConstants.ATTRIBUTE_DEVICE_ID, deviceId);
        }
        if (StringUtils.isNotBlank(userId)) {
            attributes.put(WebSocketConstants.ATTRIBUTE_USER_ID, userId);
        }
        attributes.put(WebSocketConstants.ATTRIBUTE_SESSION_ID, request.getURI().getPath());
        logger.info("WebSocket handshake, deviceId: {}, userId: {}", deviceId, userId);
        return true;
    }

    /**
     * 握手完成后记录异常（若有），不做额外处理。
     *
     * @param request    HTTP 请求
     * @param response   HTTP 响应
     * @param wsHandler  Handler
     * @param exception  握手阶段异常，可能为 {@code null}
     */
    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {
        if (exception != null) {
            logger.error("WebSocket handshake error", exception);
        }
    }
}

