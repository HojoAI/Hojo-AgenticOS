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
package com.hojoai.webmvc.omniasst.meeting.websocket;

import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;
import org.springframework.web.util.UriUtils;
import xyz.downgoon.snowflake.Snowflake;

import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * WebSocket 握手拦截：解析 TraceId、写入 MDC，便于日志与链路关联。
 */
@Component
public class WebSocketHandshakeInterceptor implements HandshakeInterceptor {
    private static final Logger logger = LoggerFactory.getLogger(WebSocketHandshakeInterceptor.class);
    private static final String TRACE_ID_HEADER = "X-Trace-Id";
    private static final String TRACE_ID_KEY = "traceId";
    private static final String SPAN_ID_KEY = "spanId";

    @Resource
    private Snowflake snowflake;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) {
        String deviceId = request.getHeaders().getFirst(WebSocketConstants.HEADER_DEVICE_ID);
        String userId = request.getHeaders().getFirst(WebSocketConstants.HEADER_USER_ID);
        String conversationId = request.getHeaders().getFirst(WebSocketConstants.HEADER_CONVERSATION_ID);
        if (StringUtils.isBlank(conversationId)) {
            conversationId = request.getHeaders().getFirst(WebSocketConstants.ATTRIBUTE_CONVERSATION_ID);
        }
        String resumeMeetingId = request.getHeaders().getFirst(WebSocketConstants.HEADER_MEETING_ID);
        Long clientRecordElapsedMs = null;

        String query = request.getURI().getQuery();
        if (StringUtils.isNotBlank(query)) {
            String[] params = query.split("&");
            for (String param : params) {
                if (StringUtils.isBlank(param)) {
                    continue;
                }
                String[] kv = param.split("=", 2);
                String key = kv[0];
                String rawVal = kv.length > 1 ? kv[1] : "";
                String val = rawVal.isEmpty() ? "" : UriUtils.decode(rawVal, StandardCharsets.UTF_8);
                if ("deviceId".equals(key) && StringUtils.isBlank(deviceId)) {
                    deviceId = val;
                } else if ("userId".equals(key) && StringUtils.isBlank(userId)) {
                    userId = val;
                } else if ("conversationId".equals(key) && StringUtils.isBlank(conversationId)) {
                    conversationId = val;
                } else if ("meetingId".equals(key) && StringUtils.isBlank(resumeMeetingId)) {
                    resumeMeetingId = val;
                } else if ("clientRecordElapsedMs".equals(key) && StringUtils.isNotBlank(val)) {
                    try {
                        clientRecordElapsedMs = Long.parseLong(val);
                    } catch (Exception ignore) {
                    }
                }
            }
        }

        if (StringUtils.isBlank(deviceId)) {
            logger.warn("WebSocket handshake rejected: X-Device-Id / deviceId is required");
            return false;
        }

        attributes.put(WebSocketConstants.ATTRIBUTE_DEVICE_ID, deviceId);
        if (StringUtils.isNotBlank(userId)) {
            attributes.put(WebSocketConstants.ATTRIBUTE_USER_ID, userId);
        }
        if (StringUtils.isNotBlank(conversationId)) {
            attributes.put(WebSocketConstants.ATTRIBUTE_CONVERSATION_ID, conversationId);
        }
        if (StringUtils.isNotBlank(resumeMeetingId)) {
            attributes.put(WebSocketConstants.ATTRIBUTE_MEETING_ID, resumeMeetingId);
        }
        if (clientRecordElapsedMs != null && clientRecordElapsedMs >= 0) {
            attributes.put(WebSocketConstants.ATTRIBUTE_CLIENT_RECORD_ELAPSED_MS, clientRecordElapsedMs);
        }
        attributes.put(WebSocketConstants.ATTRIBUTE_SESSION_ID, request.getURI().getPath());

        String traceId = request.getHeaders().getFirst(TRACE_ID_HEADER);
        if (StringUtils.isBlank(traceId)) {
            traceId = String.valueOf(snowflake.nextId());
        }
        String spanId = String.valueOf(snowflake.nextId());

        MDC.put(TRACE_ID_KEY, traceId);
        MDC.put(SPAN_ID_KEY, spanId);

        attributes.put(TRACE_ID_KEY, traceId);
        attributes.put(SPAN_ID_KEY, spanId);

        logger.info("WebSocket handshake, deviceId: {}, userId: {}, meetingId(resume): {}, clientRecordElapsedMs: {}, traceId: {}",
                deviceId, userId, resumeMeetingId, clientRecordElapsedMs, traceId);
        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {
        if (exception != null) {
            logger.error("WebSocket handshake error", exception);
        }
    }
}
