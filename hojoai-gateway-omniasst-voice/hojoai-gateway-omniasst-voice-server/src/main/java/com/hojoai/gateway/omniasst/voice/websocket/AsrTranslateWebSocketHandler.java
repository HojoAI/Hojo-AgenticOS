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

import com.hojoai.common.jackson.JacksonUtil;
import com.hojoai.gateway.omniasst.voice.api.AsrAPI;
import com.hojoai.gateway.omniasst.voice.api.dto.VADRequest;
import com.hojoai.gateway.omniasst.voice.api.dto.VADResponse;
import com.hojoai.gateway.omniasst.voice.config.AzureSpeechConfig;
import com.hojoai.gateway.omniasst.voice.service.asrtranslate.AsrTranslateProvider;
import com.hojoai.gateway.omniasst.voice.service.asrtranslate.impl.AzureAsrTranslateProvider;
import com.hojoai.gateway.omniasst.voice.service.asrtranslate.impl.HojoAsrTranslateProvider;
import com.hojoai.gateway.omniasst.voice.websocket.bean.IMessage;
import com.hojoai.gateway.omniasst.voice.websocket.bean.IMessageCustom;
import com.hojoai.gateway.omniasst.voice.websocket.bean.IMessageEvent;
import com.hojoai.gateway.omniasst.voice.websocket.bean.dto.AsrTranslateAudioDataRequest;
import com.hojoai.gateway.omniasst.voice.websocket.bean.dto.AsrTranslateConfigRequest;
import com.hojoai.gateway.omniasst.voice.websocket.bean.dto.AsrTranslateConfigResponse;
import com.hojoai.gateway.omniasst.voice.websocket.bean.dto.AsrTranslateResultResponse;
import com.hojoai.gateway.omniasst.voice.websocket.bean.dto.ErrorResponse;
import com.hojoai.gateway.omniasst.voice.websocket.bean.dto.HeartbeatRequest;
import com.hojoai.gateway.omniasst.voice.websocket.bean.dto.HeartbeatResponse;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.AbstractWebSocketHandler;
import xyz.downgoon.snowflake.Snowflake;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ASR + 翻译 WebSocket（路径 {@code /asr/translate}）：缓冲音频、可选 VAD、对接 {@link AsrTranslateProvider}（Azure / Hojo）。
 */
@Component
public class AsrTranslateWebSocketHandler extends AbstractWebSocketHandler {
    private static final Logger logger = LoggerFactory.getLogger(AsrTranslateWebSocketHandler.class);
    private static final int CHUNK_SIZE = 320 * 16 * 2;
    private static final int VAD_QUEUE_SIZE = 3;
    private static final int VAD_FRAME_DURATION_MS = 16;
    private static final int VAD_VALID_SPEECH_THRESHOLD = 320;
    private static final int VAD_VALID_PACKAGE_THRESHOLD = (int) Math.ceil(((double) VAD_VALID_SPEECH_THRESHOLD / VAD_FRAME_DURATION_MS));
    private static final int VAD_END = 60;

    @Resource
    private WebSocketHeartbeatManager heartbeatManager;
    @Resource
    private Snowflake snowflake;
    @Resource
    private AzureSpeechConfig azureConfig;
    @Resource
    private AsrAPI asrAPI;

    private final Map<String, SessionState> sessionStates = new ConcurrentHashMap<>();
    private final Map<String, ByteArrayOutputStream> sessionBuffers = new ConcurrentHashMap<>();
    private final Map<String, String> vidMap = new ConcurrentHashMap<>();
    private final Map<String, Deque<byte[]>> audioBufferMap = new ConcurrentHashMap<>();
    private final Map<String, Deque<Integer>> vadScoreMap = new ConcurrentHashMap<>();
    private final Map<String, Boolean> vadFlagMap = new ConcurrentHashMap<>();

    /** 每连接：会话 id、VAD vid、选定的翻译提供商及是否已完成配置 */
    private static class SessionState {
        String sessionId;
        String vadVid;
        AsrTranslateProvider provider;
        boolean configured;
    }

    /**
     * 注册心跳并初始化 {@link SessionState}。
     *
     * @param session 新连接
     */
    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        heartbeatManager.registerSession(session, "AsrTranslateWebSocketHandler");
        String sessionId = session.getId();
        SessionState state = new SessionState();
        state.sessionId = sessionId;
        sessionStates.put(sessionId, state);
        logger.info("[连接建立] sessionId: {}", sessionId);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        heartbeatManager.updateLastActiveTime(session);
        try {
            IMessage<?> iMessage = JacksonUtil.json2Bean(message.getPayload(), IMessage.class);
            IMessageEvent eventType = IMessageEvent.getByEventType(iMessage.getEventType());
            switch (eventType) {
                case REQ_HEARTBEAT -> handleHeartbeat(session, message.getPayload());
                case REQ_ASR_TRANSLATE_CONFIG -> handleConfig(session, message.getPayload());
                case REQ_ASR_TRANSLATE_AUDIO_DATA -> handleAudioData(session, message.getPayload());
                default -> IWebSocketHandler.sendText(session, JacksonUtil.bean2Json(
                        IMessage.newInstance(IMessageEvent.UNSUPPORTED_OPERATION.getEventType())));
            }
        } catch (Exception e) {
            logger.error("[处理文本消息异常] sessionId: {}", session.getId(), e);
            sendError(session, "GENERAL_ERROR", "Error processing message: " + e.getMessage(), "process");
        }
    }

    @Override
    protected void handleBinaryMessage(WebSocketSession session, BinaryMessage message) {
        heartbeatManager.updateLastActiveTime(session);
        String sessionId = session.getId();
        SessionState state = sessionStates.get(sessionId);

        if (state == null || !state.configured) {
            logger.warn("[未配置] sessionId: {}, 忽略音频数据", sessionId);
            return;
        }

        try {
            ByteBuffer payload = message.getPayload();
            byte[] audioData = new byte[payload.remaining()];
            payload.get(audioData);
            state.provider.pushAudio(session, audioData);
        } catch (Exception e) {
            logger.error("[处理音频数据异常] sessionId: {}", sessionId, e);
            sendError(session, "AUDIO_ERROR", "Error processing audio: " + e.getMessage(), "audio");
        }
    }

    private void handleHeartbeat(WebSocketSession session, String message) {
        try {
            HeartbeatRequest request = IMessageCustom.parseBusinessRequest(message, HeartbeatRequest.class);
            HeartbeatResponse response = new HeartbeatResponse();
            response.setTimestamp(request != null ? request.getTimestamp() : System.currentTimeMillis());
            response.setServerTime(System.currentTimeMillis());
            IMessageCustom.sendMessageCustom(session, IMessageEvent.BACK_HEARTBEAT, response);
        } catch (Exception e) {
            logger.error("[心跳处理异常] sessionId: {}", session.getId(), e);
        }
    }

    private void handleAudioData(WebSocketSession session, String message) {
        String sessionId = session.getId();
        SessionState state = sessionStates.get(sessionId);
        if (state == null || !state.configured) {
            return;
        }
        try {
            AsrTranslateAudioDataRequest request = IMessageCustom.parseBusinessRequest(message, AsrTranslateAudioDataRequest.class);
            if (request == null || StringUtils.isBlank(request.getAudioData())) {
                sendError(session, "AUDIO_ERROR", "Invalid audio data", "audio");
                return;
            }
            byte[] audioData = Base64.getDecoder().decode(request.getAudioData());
            logger.info("[翻译][收到音频], sessionId: {}", sessionId);
            processAudio(session, state, audioData);
        } catch (Exception e) {
            logger.info("[翻译][音频异常], sessionId: {}", sessionId);
            sendError(session, "AUDIO_ERROR", "Error processing audio: " + e.getMessage(), "audio");
        }
    }

    private void processAudio(WebSocketSession session, SessionState state, byte[] audioData) {
        try {
            // Azure provider使用Azure自带的VAD，跳过本地VAD检测，直接推送音频
            if (state.provider instanceof AzureAsrTranslateProvider) {
                // Azure场景：直接推送音频，使用Azure自带的VAD检测
                state.provider.pushAudio(session, audioData);
                return;
            }
            
            // Hojo provider保持原有的VAD检测逻辑
            ByteArrayOutputStream buffer = sessionBuffers.computeIfAbsent(session.getId(), k -> new ByteArrayOutputStream());
            buffer.write(audioData);
            if (buffer.size() >= CHUNK_SIZE) {
                String vid = vidMap.computeIfAbsent(session.getId(), k -> String.valueOf(snowflake.nextId()));
                Deque<byte[]> audioBuffer = audioBufferMap.computeIfAbsent(vid, k -> new ArrayDeque<>());
                Deque<Integer> vadScoreQueue = vadScoreMap.computeIfAbsent(vid, k -> new ArrayDeque<>());
                byte[] completeData = buffer.toByteArray();
                buffer.reset();
                boolean needVad = vadFlagMap.getOrDefault(vid, true);
                if (!needVad) {
                    state.provider.pushAudio(session, completeData);
                    return;
                }
                if (StringUtils.isBlank(state.vadVid)) {
                    state.vadVid = String.valueOf(snowflake.nextId());
                }
                VADRequest vadRequest = makeVadRequest(java.util.Base64.getEncoder().encodeToString(completeData), state.vadVid);
                VADResponse vadResponse = asrAPI.vad(vadRequest);
                if (vadResponse != null && vadResponse.getRtn() != 0) {
                    logger.warn("[VAD检测失败] vid: {}, vadVid: {}, rtn: {}", vid, state.vadVid, vadResponse.getRtn());
                    callVadDrop(state.vadVid);
                    state.vadVid = String.valueOf(snowflake.nextId());
                    audioBuffer.clear();
                    vadScoreQueue.clear();
                    return;
                }
                int vadScore = evaluate(vadResponse);
                audioBuffer.addLast(completeData);
                vadScoreQueue.addLast(vadScore);
                if (audioBuffer.size() > VAD_QUEUE_SIZE) {
                    audioBuffer.pollFirst();
                    vadScoreQueue.pollFirst();
                }
                int totalVadScore = vadScoreQueue.stream().mapToInt(Integer::intValue).sum();
                if (totalVadScore >= VAD_VALID_PACKAGE_THRESHOLD) {
                    vadFlagMap.put(vid, false);
                    callVadDrop(state.vadVid);
                    state.vadVid = null;
                    while (!audioBuffer.isEmpty()) {
                        byte[] audioBytesChunk = audioBuffer.pollFirst();
                        vadScoreQueue.pollFirst();
                        state.provider.pushAudio(session, audioBytesChunk);
                    }
                }
            }
        } catch (Exception e) {
            logger.error("[VAD处理异常] sessionId: {}", session.getId(), e);
        }
    }

    private VADRequest makeVadRequest(String audioData, String vid) {
        VADRequest request = new VADRequest();
        request.setPackageData(audioData);
        request.setPackageSize(audioData.length());
        request.setVadEnd(VAD_END);
        request.setVid(vid);
        request.setVadDrop(0);
        return request;
    }

    private void callVadDrop(String vid) {
        VADRequest request = new VADRequest();
        request.setVid(vid);
        request.setVadDrop(1);
        try {
            asrAPI.vad(request);
        } catch (Exception e) {
            logger.error("[VAD Drop异常] vid: {}", vid, e);
        }
    }

    private int evaluate(VADResponse vadResponse) {
        if (vadResponse == null) {
            return 0;
        }
        return calculateValidSpeechDuration(vadResponse.getVadData());
    }

    private int calculateValidSpeechDuration(int[] vadData) {
        if (vadData == null || vadData.length == 0) {
            return 0;
        }
        return Arrays.stream(vadData).sum();
    }

    private void handleConfig(WebSocketSession session, String message) {
        String sessionId = session.getId();
        SessionState state = sessionStates.get(sessionId);
        if (state == null) {
            sendError(session, "SESSION_ERROR", "Session not found", "config");
            return;
        }

        try {
            AsrTranslateConfigRequest request = IMessageCustom.parseBusinessRequest(message, AsrTranslateConfigRequest.class);
            if (request == null || StringUtils.isBlank(request.getProvider())) {
                sendError(session, "CONFIG_ERROR", "Invalid config request", "config");
                return;
            }

            List<String> targetLanguages = request.getTargetLanguages();
            if (targetLanguages == null || targetLanguages.size() != 2) {
                sendError(session, "CONFIG_ERROR", "targetLanguages must contain exactly 2 languages", "config");
                return;
            }

            String lang1 = targetLanguages.get(0);
            String lang2 = targetLanguages.get(1);

            AsrTranslateProvider provider;
            if ("azure".equalsIgnoreCase(request.getProvider())) {
                provider = new AzureAsrTranslateProvider(azureConfig, asrAPI, snowflake);
            } else if ("hojo".equalsIgnoreCase(request.getProvider())) {
                provider = new HojoAsrTranslateProvider(asrAPI);
            } else {
                sendError(session, "CONFIG_ERROR", "Unsupported provider: " + request.getProvider(), "config");
                return;
            }

            provider.configure(sessionId, lang1, lang2);
            provider.setResultCallback(new AsrTranslateProvider.ResultCallback() {
                @Override
                public void onResult(Map<String, String> translations, String detectedLanguage, boolean isEnd) {
                    if (session.isOpen()) {
                        sendResult(session, translations, detectedLanguage, isEnd);
                        // Azure provider不需要重置VAD状态，因为不使用本地VAD
                        if (isEnd && !(provider instanceof AzureAsrTranslateProvider)) {
                            String vid = vidMap.get(sessionId);
                            if (vid != null) {
                                vadFlagMap.put(vid, true);
                            }
                            state.vadVid = null;
                        }
                    }
                }

                @Override
                public void onError(String errorCode, String errorMessage) {
                    if (session.isOpen()) {
                        sendError(session, errorCode, errorMessage, "provider");
                    }
                }
            });

            state.provider = provider;
            state.configured = true;

            AsrTranslateConfigResponse response = new AsrTranslateConfigResponse();
            response.setStatus("success");
            response.setSessionId(sessionId);
            response.setMessage("Configuration accepted");
            IMessageCustom.sendMessageCustom(session, IMessageEvent.BACK_ASR_TRANSLATE_CONFIG_ACK, response);

            logger.info("[配置成功] sessionId: {}, provider: {}, languages: [{}, {}]",
                    sessionId, request.getProvider(), lang1, lang2);
        } catch (Exception e) {
            logger.error("[配置异常] sessionId: {}", sessionId, e);
            sendError(session, "CONFIG_ERROR", "Configuration failed: " + e.getMessage(), "config");
        }
    }

    private void sendResult(WebSocketSession session, Map<String, String> translations, String detectedLanguage, boolean isEnd) {
        try {
            AsrTranslateResultResponse response = new AsrTranslateResultResponse();
            response.setSessionId(session.getId());
            response.setTranslations(translations);
            response.setDetectedLanguage(detectedLanguage);
            response.setIsEnd(isEnd ? 1 : 0);
            response.setTimestamp(System.currentTimeMillis());
            logger.info("[语音服务返回] sessionId: {}, translations: {}, detectedLanguage: {}, isEnd: {}",
                    session.getId(), translations, detectedLanguage, isEnd);
            IMessageCustom.sendMessageCustom(session, IMessageEvent.BACK_ASR_TRANSLATE_RESULT, response);
        } catch (Exception e) {
            logger.error("[发送结果异常] sessionId: {}", session.getId(), e);
        }
    }

    private void sendError(WebSocketSession session, String errorCode, String errorMessage, String stage) {
        try {
            ErrorResponse error = new ErrorResponse();
            error.setErrorCode(errorCode);
            error.setErrorMessage(errorMessage);
            error.setStage(stage);
            IMessageCustom.sendMessageCustom(session, IMessageEvent.BACK_ERROR, error);
        } catch (Exception e) {
            logger.error("[发送错误异常] sessionId: {}", session.getId(), e);
        }
    }

    /**
     * @param session   当前会话
     * @param exception 传输错误
     */
    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        logger.error("[传输错误] sessionId: {}", session.getId(), exception);
    }

    /**
     * 关闭提供商会话、清理缓冲与 VAD 相关映射。
     *
     * @param session     会话
     * @param closeStatus 关闭状态
     */
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) {
        String sessionId = session.getId();
        heartbeatManager.unregisterSession(session);
        SessionState state = sessionStates.remove(sessionId);
        if (state != null && state.provider != null) {
            state.provider.close();
        }
        cleanup(sessionId);
        logger.info("[连接关闭] sessionId: {}, status: {}", sessionId, closeStatus);
    }

    private void cleanup(String sessionId) {
        sessionBuffers.remove(sessionId);
        String vid = vidMap.remove(sessionId);
        if (vid != null) {
            audioBufferMap.remove(vid);
            vadScoreMap.remove(vid);
            vadFlagMap.remove(vid);
        }
    }
}
