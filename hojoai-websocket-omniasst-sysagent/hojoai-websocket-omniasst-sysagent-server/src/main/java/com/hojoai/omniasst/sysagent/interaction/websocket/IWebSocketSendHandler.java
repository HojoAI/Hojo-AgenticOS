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

import com.fasterxml.jackson.core.type.TypeReference;
import com.hojoai.common.jackson.JacksonUtil;
import com.hojoai.omniasst.sysagent.dto.request.movie.WebSocketUserMessage;
import com.hojoai.omniasst.sysagent.interaction.websocket.bean.IMessage;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The IWebSocketSendHandler class for webSocket send server.
 *
 * @author My
 * @version 1.0, 2025/11/26
 * @since hojoai-websocket-omniasst-sysagent 1.0.0
 */
@Component
public class IWebSocketSendHandler {

    private static final Map<String, WebSocketSession> sessionMap = new ConcurrentHashMap<>();

    /**
     * 以 userId 为键缓存 WebSocket，供业务主动下行消息。
     *
     * @param userId  业务用户 ID
     * @param session 已建立的会话
     */
    public void addWebSocketSession(String userId, WebSocketSession session) {
        if (StringUtils.isBlank(userId)) {
            return;
        }

        sessionMap.put(userId, session);
    }

    /**
     * 从影票等业务消息体中解析 userId 并注册会话。
     *
     * @param message JSON 格式，事件体为 {@link WebSocketUserMessage}
     * @param session 当前连接
     */
    public void addWebSocketSessionByMessage(String message, WebSocketSession session) {
        IMessage<WebSocketUserMessage> iMessage = JacksonUtil.json2Bean(message, new TypeReference<>() {});

        String userId = iMessage.getEventContent().getUserId();

        this.addWebSocketSession(userId, session);
    }

    /**
     * 移除指定用户的会话缓存。
     *
     * @param userId 用户 ID
     */
    public void deleteWebSocketSession(String userId) {
        if (StringUtils.isBlank(userId)) {
            return;
        }

        sessionMap.remove(userId);
    }

    /** 清空全部已注册会话（例如连接断开时防止脏引用）。 */
    public void clearWebSocketSession() {
        if (MapUtils.isEmpty(sessionMap)) {
            return;
        }

        sessionMap.clear();
    }

    /**
     * 向已绑定用户发送文本帧；会话不存在或已关闭则静默忽略。
     *
     * @param userId  用户 ID
     * @param message 文本负载
     * @throws RuntimeException 底层 {@link java.io.IOException} 时包装抛出
     */
    public void sendMessage(String userId, String message) {
        WebSocketSession session = sessionMap.get(userId);

        if (Objects.isNull(session) || !session.isOpen()) {
            return;
        }

        TextMessage textMessage = new TextMessage(message);

        try {
            session.sendMessage(textMessage);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

}
