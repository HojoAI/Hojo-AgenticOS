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
package com.hojoai.gateway.omniasst.voice.websocket.v1;

import com.hojoai.common.jackson.JacksonUtil;
import com.hojoai.gateway.omniasst.voice.service.v1.asr.AsrCallback;
import com.hojoai.gateway.omniasst.voice.service.v1.asr.AsrService;
import com.hojoai.gateway.omniasst.voice.service.v1.asr.AsrServiceFactory;
import com.hojoai.gateway.omniasst.voice.service.v1.asr.dto.AsrResult;
import com.hojoai.gateway.omniasst.voice.websocket.IWebSocketHandler;
import com.hojoai.gateway.omniasst.voice.websocket.WebSocketHeartbeatManager;
import com.hojoai.gateway.omniasst.voice.websocket.bean.BackCustomPayloadMessage;
import com.hojoai.gateway.omniasst.voice.websocket.bean.IMessage;
import com.hojoai.gateway.omniasst.voice.websocket.bean.IMessageCustom;
import com.hojoai.gateway.omniasst.voice.websocket.bean.dto.ErrorResponse;
import com.hojoai.gateway.omniasst.voice.websocket.bean.dto.HeartbeatRequest;
import com.hojoai.gateway.omniasst.voice.websocket.bean.dto.HeartbeatResponse;
import com.hojoai.gateway.omniasst.voice.websocket.v1.dto.AsrAudioDataRequest;
import com.hojoai.gateway.omniasst.voice.websocket.v1.dto.AsrConfigRequest;
import com.hojoai.gateway.omniasst.voice.websocket.v1.dto.AsrConfigResponse;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.AbstractWebSocketHandler;

import java.util.concurrent.ConcurrentHashMap;

/**
 * ASR v1 WebSocket（路径 {@code /asr/v1}）：按事件配置平台与语言，音频走 {@link AsrServiceFactory} 选型后的 {@link AsrService}。
 */
@Component
public class AsrWebSocketHandler extends AbstractWebSocketHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(AsrWebSocketHandler.class);
    
    @Resource
    private WebSocketHeartbeatManager heartbeatManager;
    
    @Resource
    private AsrServiceFactory asrServiceFactory;
    
    /** 每连接：ASR 平台、语言、音频类型及是否已下发配置响应 */
    private static class AsrSessionState {
        Integer platform;
        String language;
        String type;
        boolean configured;
    }
    
    private final ConcurrentHashMap<String, AsrSessionState> sessionStates = new ConcurrentHashMap<>();
    
    /**
     * 注册心跳并预置会话状态。
     *
     * @param session 新连接
     */
    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        heartbeatManager.registerSession(session, "AsrWebSocketHandler");
        logger.info("ASR WebSocket connected, session id: {}", session.getId());
        
        // 初始化会话状态
        sessionStates.put(session.getId(), new AsrSessionState());
    }
    
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        heartbeatManager.updateLastActiveTime(session);
        
        try {
            IMessage<?> iMessage = JacksonUtil.json2Bean(message.getPayload(), IMessage.class);
            AsrMessageEvent eventType = AsrMessageEvent.getByEventType(iMessage.getEventType());
            
            if (eventType == null) {
                logger.warn("Unknown event type: {}, session: {}", iMessage.getEventType(), session.getId());
                sendErrorWithEvent(session, AsrMessageEvent.UNSUPPORTED_OPERATION, "不支持的操作", "message_handling");
                return;
            }
            
            switch (eventType) {
                case REQ_HEARTBEAT -> handleHeartbeat(session, message.getPayload());
                case REQ_ASR_CONFIG -> handleAsrConfig(session, message.getPayload());
                case REQ_ASR_AUDIO_DATA -> handleAsrAudioData(session, message.getPayload());
                default -> {
                    logger.warn("Unsupported event type: {}, session: {}", eventType, session.getId());
                    sendErrorWithEvent(session, AsrMessageEvent.UNSUPPORTED_OPERATION, "不支持的操作", "message_handling");
                }
            }
        } catch (Exception e) {
            logger.error("Error handling text message, session: {}", session.getId(), e);
            sendError(session, AsrMessageEvent.GENERAL_ERROR.getEventType(), "处理消息失败: " + e.getMessage(), "message_handling");
        }
    }
    
    private void handleHeartbeat(WebSocketSession session, String message) {
        try {
            HeartbeatRequest request = IMessageCustom.parseBusinessRequest(message, HeartbeatRequest.class);
            HeartbeatResponse response = new HeartbeatResponse();
            response.setTimestamp(request != null ? request.getTimestamp() : System.currentTimeMillis());
            response.setServerTime(System.currentTimeMillis());
            sendMessageCustom(session, AsrMessageEvent.BACK_HEARTBEAT, response);
        } catch (Exception e) {
            logger.error("Error handling heartbeat, session: {}", session.getId(), e);
        }
    }
    
    private void handleAsrConfig(WebSocketSession session, String payload) {
        String sessionId = session.getId();
        
        try {
            AsrConfigRequest request = IMessageCustom.parseBusinessRequest(payload, AsrConfigRequest.class);
            AsrSessionState state = sessionStates.get(sessionId);
            
            if (state == null) {
                logger.error("Session state not found for session: {}", sessionId);
                sendError(session, AsrMessageEvent.GENERAL_ERROR.getEventType(), "会话状态未找到", "config");
                return;
            }
            
            if (request != null) {
                state.platform = request.getPlatform();
                state.type = request.getType();
                
                if (StringUtils.isBlank(request.getLanguage())) {
                    if (state.platform == 0) {
                        state.language = "chinese";
                    } else {
                        state.language = "zh-CN";
                    }
                } else {
                    state.language = request.getLanguage();
                }
            } 
            
            state.configured = true;
            
            logger.info("ASR config received, session: {}, platform: {}, language: {}, type: {}", sessionId, state.platform, state.language, state.type);

            AsrConfigResponse response = new AsrConfigResponse();
            response.setStatus("success");
            response.setMessage("配置成功");
            response.setPlatform(state.platform);
            response.setLanguage(state.language);
            response.setType(state.type);
            response.setHeadphoneMode(Boolean.FALSE);
            
            sendMessageCustom(session, AsrMessageEvent.BACK_ASR_CONFIG_ACK, response);
            
        } catch (Exception e) {
            logger.error("Error handling ASR config, session: {}", sessionId, e);
            sendError(session, AsrMessageEvent.GENERAL_ERROR.getEventType(), "配置失败: " + e.getMessage(), "config");
        }
    }
    
    private void handleAsrAudioData(WebSocketSession session, String payload) {
        String sessionId = session.getId();
        
        try {
            AsrSessionState state = sessionStates.get(sessionId);
            
            if (state == null) {
                logger.error("Session state not found for session: {}", sessionId);
                sendError(session, AsrMessageEvent.GENERAL_ERROR.getEventType(), "会话状态未找到", "asr");
                return;
            }
            
            // 检查是否已配置
            if (!state.configured) {
                logger.warn("ASR not configured yet, session: {}", sessionId);
                sendError(session, AsrMessageEvent.ILLEGAL_ARGUMENT.getEventType(), "请先发送配置信息", "asr");
                return;
            }

            AsrAudioDataRequest request = IMessageCustom.parseBusinessRequest(payload, AsrAudioDataRequest.class);
            logger.info("ASR audio data received, session: {}, data length: {}", sessionId, request.getAudioData().length());
            AsrService asrService = asrServiceFactory.getService(state.platform);
            if (asrService == null) {
                logger.error("No ASR service available for platform: {}", state.platform);
                sendError(session, AsrMessageEvent.GENERAL_ERROR.getEventType(), "ASR服务不可用", "asr");
                return;
            }
            
            asrService.recognize(sessionId, request, new AsrCallback() {
                @Override
                public void onResult(AsrResult result) {
                    logger.info("ASR result for session {}: {}", sessionId, result);
                    sendMessageCustom(session, AsrMessageEvent.RESP_ASR_RESULT, result);
                }

                @Override
                public void onVadInvalid() {
                    logger.info("VAD Invalid, sessionId: {}", sessionId);
                }

                @Override
                public void onError(String errorMessage) {
                    sendError(session, AsrMessageEvent.GENERAL_ERROR.getEventType(), "ASR识别失败: " + errorMessage, "asr");
                }
            });
            logger.debug("ASR processing for session: {}, platform: {}, language: {}", sessionId, state.platform, state.language);
        } catch (Exception e) {
            logger.error("Error handling ASR audio data, session: {}", sessionId, e);
            sendError(session, AsrMessageEvent.GENERAL_ERROR.getEventType(), "处理音频数据失败: " + e.getMessage(), "asr");
        }
    }
    
    private <T> void sendMessageCustom(WebSocketSession session, AsrMessageEvent event, T eventContent) {
        IMessageCustom<BackCustomPayloadMessage<Object>> messageCustom = 
            IMessageCustom.newInstanceCustom(event.getEventType(), eventContent);
        IWebSocketHandler.sendText(session, JacksonUtil.bean2Json(messageCustom));
    }
    
    private void sendErrorWithEvent(WebSocketSession session, AsrMessageEvent event, String errorMessage, String stage) {
        ErrorResponse error = new ErrorResponse();
        error.setErrorCode(event.getEventType());
        error.setErrorMessage(errorMessage);
        error.setStage(stage);
        sendMessageCustom(session, AsrMessageEvent.BACK_ERROR, error);
    }
    
    private void sendError(WebSocketSession session, String errorCode, String errorMessage, String stage) {
        ErrorResponse error = new ErrorResponse();
        error.setErrorCode(errorCode);
        error.setErrorMessage(errorMessage);
        error.setStage(stage);
        sendMessageCustom(session, AsrMessageEvent.BACK_ERROR, error);
    }
    
    /**
     * @param session   当前会话
     * @param exception 传输错误
     */
    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        logger.error("WebSocket transport error, session id: {}", session.getId(), exception);
    }
    
    /**
     * 停止底层识别并移除会话状态。
     *
     * @param session     会话
     * @param closeStatus 关闭状态
     */
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
        String sessionId = session.getId();
        heartbeatManager.unregisterSession(session);
        
        AsrSessionState state = sessionStates.remove(sessionId);
        if (state != null && state.platform != null) {
            AsrService asrService = asrServiceFactory.getService(state.platform);
            if (asrService != null) {
                asrService.stopRecognize(sessionId);
            }
        }
        
        logger.info("ASR WebSocket disconnected, session id: {}, close status: {}", sessionId, closeStatus);
    }
}
