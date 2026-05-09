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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

/**
 * The web socket handler class.
 *
 * @author kevanwong
 * @version 1.0, 2025/10/21
 * @since hojoai-websocket-omniasst-sysagent 1.0.0
 */
public class IWebSocketHandler {

    private static final Logger logger = LoggerFactory.getLogger(IWebSocketHandler.class);

    public static void sendText(WebSocketSession session, String message) {
        logger.info("web socket on send text, session id: {}, message: {}", session.getId(), message);
        try {
            session.sendMessage(new TextMessage(message));
        } catch (Exception e) {
            logger.error("web socket on send text error, session id: {}, message: {}", session.getId(), message, e);
        }
    }
}