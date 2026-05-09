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

import com.hojoai.common.jackson.JacksonUtil;
import com.hojoai.omniasst.sysagent.service.rtctoken.RTCTokenService;
import com.hojoai.omniasst.sysagent.interaction.websocket.bean.IMessage;
import com.hojoai.omniasst.sysagent.interaction.websocket.bean.IMessageEvent;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

/**
 * The text web socket handler class.
 *
 * @author kevanwong
 * @version 1.0, 2025/10/21
 * @since hojoai-websocket-omniasst-sysagent 1.0.0
 */
@Component
public class ITextWebSocketHandler extends TextWebSocketHandler {

    private static final Logger logger = LoggerFactory.getLogger(ITextWebSocketHandler.class);

    @Resource
    private RTCTokenService rtcTokenService;

    @Resource
    private IWebSocketSendHandler iWebSocketSendHandler;

    /**
     * 连接建立后记录 session、设备 ID 与 User-Agent。
     */
    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        String deviceId = (String) session.getAttributes().get(IWebSocketConstants.ATTRIBUTE_DEVICE_ID);
        String userAgent = (String) session.getAttributes().get(IWebSocketConstants.ATTRIBUTE_USER_AGENT);

        logger.info("web socket on open, session id: {}, deviceId: {}, userAgent: {}", session.getId(), deviceId, userAgent);
    }

    /**
     * 解析 JSON 消息，校验 {@link IMessageEvent}，分发 RTC 建连或影票会话绑定等事件；异常时回写统一错误事件帧。
     */
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        try {
            IMessage<?> iMessage = JacksonUtil.json2Bean(message.getPayload(), IMessage.class);
            logger.info("web socket on message, session id: {}, message: {}", session.getId(), JacksonUtil.bean2Json(iMessage));

            String eventType = iMessage.getEventType();
            if (!validateEvent(session, eventType)) {
                throw new UnsupportedOperationException(String.format("unsupported operation event type: [%s]", eventType));
            }

            processEvent(session, eventType, message.getPayload());
        } catch (IllegalArgumentException e) {
            logger.error("web socket on message, session id: {}, illegal argument exception", session.getId(), e);
            IWebSocketHandler.sendText(session, JacksonUtil.bean2Json(IMessage.newInstance(IMessageEvent.ILLEGAL_ARGUMENT)));
        } catch (UnsupportedOperationException e) {
            logger.error("web socket on message, session id: {}, unsupported operation exception", session.getId(), e);
            IWebSocketHandler.sendText(session, JacksonUtil.bean2Json(IMessage.newInstance(IMessageEvent.UNSUPPORTED_OPERATION)));
        } catch (Exception e) {
            logger.error("web socket on message, session id: {}, exception", session.getId(), e);
            IWebSocketHandler.sendText(session, JacksonUtil.bean2Json(IMessage.newInstance(IMessageEvent.GENERAL_ERROR)));
        }
    }

    /**
     * 连接关闭时清理全局 session 映射（如影票推送用的 userId 会话）。
     */
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        logger.info("web socket on close, session id: {}, code: {}, reason: {}", session.getId(), status.getCode(), status.getReason());

        iWebSocketSendHandler.clearWebSocketSession();
    }

    /**
     * 传输层错误仅记录日志，由容器关闭连接。
     */
    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        logger.error("web socket on error, session id: {}, error message: {}", session.getId(), exception.getMessage(), exception);
    }

    private boolean validateEvent(WebSocketSession session, String eventType) {
        if (eventType == null || eventType.trim().isEmpty()) {
            logger.error("web socket on message, session id: {}, null or empty event type: {}", session.getId(), eventType);
            return false;
        }

        boolean isValidEventType = false;
        for (IMessageEvent iMessageEvent : IMessageEvent.values()) {
            if (iMessageEvent.name().equals(eventType)) {
                isValidEventType = true;
                break;
            }
        }

        if (!isValidEventType) {
            logger.error("web socket on message, session id: {}, unknown or invalid event type: {}", session.getId(), eventType);
            return false;
        }

        return true;
    }

    private void processEvent(WebSocketSession session, String eventType, String message) {
        logger.info("web socket on message, processing, session id: {}, event type: {}", session.getId(), eventType);
        // RTC：签发 token；影票：将当前 WebSocket 与用户 ID 绑定以便服务端下行
        if (IMessageEvent.BUILD_RTC_TOKEN.name().equals(eventType)) {
            rtcTokenService.buildRTCToken(session, message);
        } else if (IMessageEvent.TICKET_RESULT.name().equals(eventType)) {
            iWebSocketSendHandler.addWebSocketSessionByMessage(message, session);
        }
    }
}
