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
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.nio.ByteBuffer;
import java.util.Objects;

public class IWebSocketHandler {
    private static final Logger logger = LoggerFactory.getLogger(IWebSocketHandler.class);

    public static void sendBinary(WebSocketSession session, ByteBuffer byteBuffer) {
        if (Objects.isNull(session) || !session.isOpen()) {
            logger.warn("Cannot send binary message, session is null or closed");
            return;
        }
        try {
            session.sendMessage(new BinaryMessage(byteBuffer));
        } catch (Exception e) {
            logger.error("Failed to send binary message, session id: {}", session.getId(), e);
        }
    }

    public static void sendText(WebSocketSession session, String message) {
        if (Objects.isNull(session) || !session.isOpen()) {
            logger.warn("Cannot send text message, session is null or closed");
            return;
        }
        try {
            session.sendMessage(new TextMessage(message));
        } catch (Exception e) {
            logger.error("Failed to send text message, session id: {}, message: {}", session.getId(), message, e);
        }
    }
}

