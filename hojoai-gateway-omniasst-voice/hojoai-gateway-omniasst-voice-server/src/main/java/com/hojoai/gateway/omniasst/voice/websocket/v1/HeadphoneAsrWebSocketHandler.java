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
import com.hojoai.gateway.omniasst.voice.service.v1.asr.AsrPlatform;
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

import java.io.IOException;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@Component
public class HeadphoneAsrWebSocketHandler extends AbstractWebSocketHandler {

    private static final Logger logger = LoggerFactory.getLogger(HeadphoneAsrWebSocketHandler.class);
    private static final long VAD_TIMEOUT_WINDOW_MS = 30000L;
    private static final long EMPTY_ASR_TEXT_TIMEOUT_MS = 10 * 1000L;

    private static final ScheduledExecutorService EMPTY_ASR_TEXT_TIMEOUT_EXECUTOR =
        Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "headphone-asr-empty-text-timeout");
            t.setDaemon(true);
            return t;
        });

    @Resource
    private WebSocketHeartbeatManager heartbeatManager;

    @Resource
    private AsrServiceFactory asrServiceFactory;

    private static class AsrSessionState {
        Integer platform;
        String language;
        String type;
        boolean configured;
        final boolean headphoneMode = true;
        private boolean waitingVad = true;
        private long vadWindowStartAt;
        private int vadPassedAudioPackageCount;

        private ScheduledFuture<?> emptyAsrTextTimeoutFuture;
        /** 当前 5 秒窗口内各次回调对 {@link AsrResult#getText()} trim 后长度的累加。 */
        private int emptyTextWindowCumulativeLen;

        synchronized void resetVadWindow() {
            waitingVad = true;
            vadWindowStartAt = 0L;
            vadPassedAudioPackageCount = 0;
            cancelEmptyAsrTextTimeoutLocked();
            emptyTextWindowCumulativeLen = 0;
        }

        synchronized void cancelEmptyAsrTextTimeout() {
            cancelEmptyAsrTextTimeoutLocked();
        }

        private void cancelEmptyAsrTextTimeoutLocked() {
            if (emptyAsrTextTimeoutFuture != null) {
                emptyAsrTextTimeoutFuture.cancel(false);
                emptyAsrTextTimeoutFuture = null;
            }
        }

        /**
         * 仅 {@link AsrResult#getText()}：每个 5s 窗口内多次回调将 trim 后长度累加。
         * 窗口到时若累加仍为 0 → 超时；若累加大于 0 → 清零并立即进入下一轮 5s（重新计时、重新累加），直至会话结束或 {@link #resetVadWindow()}。
         */
        synchronized void scheduleEmptyTextTimeoutIfNeeded(AsrResult result, Runnable onTimeout) {
            int chunk = HeadphoneAsrWebSocketHandler.nonBlankTextLength(result);
            emptyTextWindowCumulativeLen += chunk;

            if (emptyTextWindowCumulativeLen > 0) {
                cancelEmptyAsrTextTimeoutLocked();
                emptyTextWindowCumulativeLen = 0;
                armEmptyTextWindowTimeout(onTimeout);
                return;
            }

            if (emptyAsrTextTimeoutFuture != null) {
                return;
            }

            armEmptyTextWindowTimeout(onTimeout);
        }

        private void armEmptyTextWindowTimeout(Runnable onTimeout) {
            final AsrSessionState self = this;
            emptyAsrTextTimeoutFuture = EMPTY_ASR_TEXT_TIMEOUT_EXECUTOR.schedule(() -> {
                try {
                    synchronized (self) {
                        if (emptyTextWindowCumulativeLen > 0) {
                            return;
                        }
                    }
                    onTimeout.run();
                } catch (Exception e) {
                    logger.error("Empty ASR text timeout task failed", e);
                }
            }, EMPTY_ASR_TEXT_TIMEOUT_MS, TimeUnit.MILLISECONDS);
        }

        synchronized boolean shouldTimeout(long now) {
            if (!waitingVad) {
                return false;
            }
            if (vadWindowStartAt <= 0L) {
                vadWindowStartAt = now;
                return false;
            }
            return now - vadWindowStartAt >= VAD_TIMEOUT_WINDOW_MS && vadPassedAudioPackageCount == 0;
        }

        synchronized void markVadPassed() {
            vadPassedAudioPackageCount++;
            waitingVad = false;
            vadWindowStartAt = 0L;
        }

        synchronized void markVadPhaseFinishedWithoutResult() {
            resetVadWindow();
        }
    }

    private final ConcurrentHashMap<String, AsrSessionState> sessionStates = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        heartbeatManager.registerSession(session, "HeadphoneAsrWebSocketHandler");
        logger.info("Headphone ASR WebSocket connected, session id: {}", session.getId());
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
            state.resetVadWindow();

            logger.info("Headphone ASR config received, session: {}, platform: {}, language: {}, type: {}",
                sessionId, state.platform, state.language, state.type);

            AsrConfigResponse response = new AsrConfigResponse();
            response.setStatus("success");
            response.setMessage("配置成功");
            response.setPlatform(state.platform);
            response.setLanguage(state.language);
            response.setType(state.type);
            response.setHeadphoneMode(state.headphoneMode);

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

            if (!state.configured) {
                logger.warn("ASR not configured yet, session: {}", sessionId);
                sendError(session, AsrMessageEvent.ILLEGAL_ARGUMENT.getEventType(), "请先发送配置信息", "asr");
                return;
            }

            AsrAudioDataRequest request = IMessageCustom.parseBusinessRequest(payload, AsrAudioDataRequest.class);
            logger.info("Headphone ASR audio data received, session: {}, data:{}, data length: {}", sessionId, request.getAudioData(),request.getAudioData().length());

            /*if (state.shouldTimeout(System.currentTimeMillis())) {
                logger.info("Headphone ASR VAD timeout, session: {}", sessionId);
                sendVadTimeoutAndClose(session, state);
                return;
            }*/

            AsrService asrService = asrServiceFactory.getService(AsrPlatform.HOJO_HEADPHONE.getCode());
            if (asrService == null) {
                logger.error("No ASR service available for platform: {}", AsrPlatform.HOJO_HEADPHONE.getCode());
                sendError(session, AsrMessageEvent.GENERAL_ERROR.getEventType(), "ASR服务不可用", "asr");
                return;
            }

            asrService.recognize(sessionId, request, new AsrCallback() {
                @Override
                public void onResult(AsrResult result) {
                    logger.info("Headphone ASR result for session {}: {}", sessionId, result);
                    state.scheduleEmptyTextTimeoutIfNeeded(result, () -> {
                        if (!session.isOpen()) {
                            return;
                        }
                        sendVadTimeoutAndClose(session, state);
                    });
                    sendMessageCustom(session, AsrMessageEvent.RESP_ASR_RESULT, result);
                    if (result != null && result.getIsEnd() != null && result.getIsEnd() == 1) {
                        state.resetVadWindow();
                    }
                }

                @Override
                public void onVadPassed() {
                    state.markVadPassed();
                }

                @Override
                public void onVadInvalid() {
                    logger.info("Headphone ASR VAD invalid, sessionId: {}", sessionId);
                    state.markVadPhaseFinishedWithoutResult();
                }

                @Override
                public void onError(String errorMessage) {
                    sendError(session, AsrMessageEvent.GENERAL_ERROR.getEventType(), "ASR识别失败: " + errorMessage, "asr");
                }
            });
            logger.debug("Headphone ASR processing for session: {}, platform: {}, language: {}",
                sessionId, state.platform, state.language);
        } catch (Exception e) {
            logger.error("Error handling ASR audio data, session: {}", sessionId, e);
            sendError(session, AsrMessageEvent.GENERAL_ERROR.getEventType(), "处理音频数据失败: " + e.getMessage(), "asr");
        }
    }

    /** 仅 {@link AsrResult#getText()}，非空白时返回 trim 后长度，否则 0。 */
    private static int nonBlankTextLength(AsrResult r) {
        if (r == null || StringUtils.isBlank(r.getText())) {
            return 0;
        }
        return r.getText().trim().length();
    }

    private void sendVadTimeoutAndClose(WebSocketSession session, AsrSessionState state) {
        state.markVadPhaseFinishedWithoutResult();
        try {
            sendMessageCustom(session, AsrMessageEvent.RESP_VAD_TIMEOUT,
                Collections.singletonMap("message", "5秒内未检测到有效语音"));
        } finally {
            logger.info("Headphone ASR 5秒内未检测到有效语音, session: {}", session.getId());
            //closeSession(session);
        }
    }

    private void closeSession(WebSocketSession session) {
        if (!session.isOpen()) {
            return;
        }
        try {
            session.close(CloseStatus.NORMAL);
        } catch (IOException e) {
            logger.warn("Failed to close headphone ASR session: {}", session.getId(), e);
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

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        logger.error("Headphone ASR WebSocket transport error, session id: {}", session.getId(), exception);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
        String sessionId = session.getId();
        heartbeatManager.unregisterSession(session);

        AsrSessionState state = sessionStates.remove(sessionId);
        if (state != null) {
            state.cancelEmptyAsrTextTimeout();
            if (state.platform != null) {
                AsrService asrService = asrServiceFactory.getService(AsrPlatform.HOJO_HEADPHONE.getCode());
                if (asrService != null) {
                    asrService.stopRecognize(sessionId);
                }
            }
        }

        logger.info("Headphone ASR WebSocket disconnected, session id: {}, close status: {}", sessionId, closeStatus);
    }
}
