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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket 会话心跳与超时检测：定时清理长期无响应的连接。
 */
@Component
public class WebSocketHeartbeatManager {
    private static final Logger logger = LoggerFactory.getLogger(WebSocketHeartbeatManager.class);

    @Value("${websocket.heartbeat.interval:10}")
    private int heartbeatInterval;
    @Value("${websocket.heartbeat.timeout-rounds:3}")
    private int timeoutRounds;

    private final Map<String, SessionInfo> sessions = new ConcurrentHashMap<>();

    private static class SessionInfo {
        WebSocketSession session;
        long lastActiveTime;
        String handlerName;

        SessionInfo(WebSocketSession session, String handlerName) {
            this.session = session;
            this.handlerName = handlerName;
            this.lastActiveTime = System.currentTimeMillis();
        }
    }

    public void registerSession(WebSocketSession session, String handlerName) {
        String sessionId = session.getId();
        sessions.put(sessionId, new SessionInfo(session, handlerName));
        logger.debug("Heartbeat session registered, session id: {}, handler: {}", sessionId, handlerName);
    }

    public void updateLastActiveTime(WebSocketSession session) {
        String sessionId = session.getId();
        SessionInfo info = sessions.get(sessionId);
        if (info != null) {
            info.lastActiveTime = System.currentTimeMillis();
        }
    }

    public void unregisterSession(WebSocketSession session) {
        String sessionId = session.getId();
        sessions.remove(sessionId);
        logger.debug("Heartbeat session unregistered, session id: {}", sessionId);
    }

    @Scheduled(fixedDelayString = "${websocket.heartbeat.check-interval:1000}000")
    public void checkTimeoutSessions() {
        long currentTime = System.currentTimeMillis();
        long timeoutMillis = heartbeatInterval * timeoutRounds * 1000L;
        sessions.entrySet().removeIf(entry -> {
            SessionInfo info = entry.getValue();
            if (currentTime - info.lastActiveTime > timeoutMillis) {
                try {
                    if (info.session.isOpen()) {
                        logger.warn("Closing timeout session, session id: {}, handler: {}, timeout: {}ms", 
                                entry.getKey(), info.handlerName, currentTime - info.lastActiveTime);
                        info.session.close(CloseStatus.SESSION_NOT_RELIABLE);
                    }
                } catch (Exception e) {
                    logger.error("Error closing timeout session, session id: {}", entry.getKey(), e);
                }
                return true;
            }
            return false;
        });
    }

    public int getActiveSessionCount() { return sessions.size(); }
}

