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
 * WebSocket 会话心跳登记与超时清理。
 * 业务 Handler 在消息到达时调用 {@link #updateLastActiveTime}，定时任务关闭长期无活动连接。
 */
@Component
public class WebSocketHeartbeatManager {
    private static final Logger logger = LoggerFactory.getLogger(WebSocketHeartbeatManager.class);

    @Value("${websocket.heartbeat.interval:10}")
    private int heartbeatInterval;

    @Value("${websocket.heartbeat.timeout-rounds:3}")
    private int timeoutRounds;

    private final Map<String, SessionInfo> sessions = new ConcurrentHashMap<>();

    /** 单条会话的心跳元数据 */
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

    /**
     * 注册会话并开始心跳跟踪。
     *
     * @param session     WebSocket 会话
     * @param handlerName 业务 Handler 名称，用于日志
     */
    public void registerSession(WebSocketSession session, String handlerName) {
        String sessionId = session.getId();
        sessions.put(sessionId, new SessionInfo(session, handlerName));
        logger.debug("Heartbeat session registered, session id: {}, handler: {}", sessionId, handlerName);
    }

    /**
     * 收到业务消息或 pong 时刷新最后活跃时间，避免被判定超时。
     *
     * @param session 当前会话
     */
    public void updateLastActiveTime(WebSocketSession session) {
        String sessionId = session.getId();
        SessionInfo info = sessions.get(sessionId);
        if (info != null) {
            info.lastActiveTime = System.currentTimeMillis();
        }
    }

    /**
     * 连接关闭时移除跟踪。
     *
     * @param session 会话
     */
    public void unregisterSession(WebSocketSession session) {
        String sessionId = session.getId();
        sessions.remove(sessionId);
        logger.debug("Heartbeat session unregistered, session id: {}", sessionId);
    }

    /**
     * 定时扫描：超过 {@code heartbeatInterval * timeoutRounds} 无活动的会话将被关闭。
     */
    @Scheduled(fixedDelayString = "${websocket.heartbeat.check-interval:1000}000")
    public void checkTimeoutSessions() {
        long currentTime = System.currentTimeMillis();
        // 无消息轮次 × 每轮间隔秒 × 1000 = 判定超时毫秒
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

    /**
     * @return 当前登记的心跳会话数量（含可能已断连但未及时 unregister 的条目，直至超时清理）
     */
    public int getActiveSessionCount() {
        return sessions.size();
    }
}

