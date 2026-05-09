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
package com.hojoai.webmvc.omniasst.translate.websocket;

import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.hojoai.common.jackson.JacksonUtil;
import com.hojoai.common.response.Response;
import com.hojoai.webmvc.omniasst.translate.api.ManagementServiceClient;
import com.hojoai.webmvc.omniasst.translate.api.dto.VoiceCloneResponse;
import com.hojoai.webmvc.omniasst.translate.service.TranslationPersistenceService;
import com.hojoai.webmvc.omniasst.translate.websocket.bean.IMessage;
import com.hojoai.webmvc.omniasst.translate.websocket.bean.IMessageCustom;
import com.hojoai.webmvc.omniasst.translate.websocket.bean.IMessageEvent;
import com.hojoai.webmvc.omniasst.translate.websocket.bean.dto.*;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.AbstractWebSocketHandler;
import xyz.downgoon.snowflake.Snowflake;

import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * 翻译场景 WebSocket 入口：解析客户端事件，转发语音服务 ASR/翻译，可选音色克隆，并将结果交给持久化服务。
 * 依赖 Nacos 发现语音服务、{@link ManagementServiceClient} 克隆音色、{@link TranslationPersistenceService} 落库。
 */
@Component
public class TranslateWebSocketHandler extends AbstractWebSocketHandler {
    private static final Logger logger = LoggerFactory.getLogger(TranslateWebSocketHandler.class);
    /** 触发音色克隆所需的最短累计音频时长（毫秒） */
    private static final int VOICE_CLONE_MIN_DURATION_MS = 14 * 1000;
    /** 每帧音频估算时长（毫秒），用于累加克隆缓冲 */
    private static final int AUDIO_CHUNK_DURATION_MS = 320;
    /** 超过该时间未收到真实音频则注入静音包，驱动下游断句 */
    private static final long SILENCE_TIMEOUT_MS = 500;
    /** 单次注入的静音时长（毫秒） */
    private static final int SILENCE_DURATION_MS = 500;
    /** 静音检测定时任务间隔（毫秒） */
    private static final long SILENCE_CHECK_INTERVAL_MS = 200;

    @Resource
    private Snowflake snowflake;
    @Resource
    private WebSocketHeartbeatManager heartbeatManager;
    @Resource
    private TranslationPersistenceService translationPersistenceService;
    @Resource
    private ManagementServiceClient managementServiceClient;
    @Resource
    private NamingService namingService;
    @Resource
    private ScheduledExecutorService scheduledExecutorService;

    /** 下游语音 WebSocket 配置中的 ASR/翻译 provider，与语音服务协议一致 */
    @Value("${translate.voice.downstream.asr-provider:azure}")
    private String downstreamAsrProvider;

    /** 调用 management 音色克隆接口时使用的服务商标识 */
    @Value("${translate.voice.clone.provider:minimax}")
    private String voiceCloneProvider;

    private final Map<String, SessionState> sessionStates = new ConcurrentHashMap<>();

    private static class SessionState {
        String chatId;
        String sourceLanguage;
        String targetLanguage;
        long connectionStartTime;
        VoiceServiceWebSocketClient voiceClient;
        String asrText = "";
        String transText = "";
        String detectedLang;
        boolean voiceCloneEnabled = false;
        ByteArrayOutputStream voiceCloneAudioBuffer = new ByteArrayOutputStream();
        int voiceCloneAudioDurationMs = 0;
        boolean voiceCloneTriggered = false;
        boolean voiceCloneSuccess = false;
        CompletableFuture<Void> voiceCloneFuture;
        long lastAudioReceiveTime = 0;
        ScheduledFuture<?> silenceTimer;
    }

    /**
     * 连接建立：注册心跳、初始化会话状态。
     *
     * @param session WebSocket 会话
     */
    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        String deviceId = (String) session.getAttributes().get(WebSocketConstants.ATTRIBUTE_DEVICE_ID);
        heartbeatManager.registerSession(session, "TranslateWebSocketHandler");
        String sessionId = session.getId();
        SessionState state = sessionStates.computeIfAbsent(sessionId, k -> new SessionState());
        state.connectionStartTime = System.currentTimeMillis();
        logger.info("[翻译服务连接建立] sessionId: {}, deviceId: {}", sessionId, deviceId);
    }

    /**
     * 处理文本帧：按 {@link IMessageEvent} 分发心跳、配置、音频、停止等逻辑。
     *
     * @param session WebSocket 会话
     * @param message 客户端 JSON 消息
     */
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        heartbeatManager.updateLastActiveTime(session);
        try {
            IMessage<?> iMessage = JacksonUtil.json2Bean(message.getPayload(), IMessage.class);
            String eventTypeStr = iMessage.getEventType();
            String sessionId = session.getId();
            SessionState state = sessionStates.get(sessionId);
            IMessageEvent eventType = IMessageEvent.getByEventType(eventTypeStr);
            switch (eventType) {
                case REQ_HEARTBEAT -> handleHeartbeat(session, message.getPayload());
                case REQ_ASR_TRANSLATE_CONFIG -> handleConfig(session, message.getPayload());
                case REQ_VOICE_CLONE_ENABLE -> handleVoiceCloneEnable(session, message.getPayload());
                case REQ_TRANSLATE_AUDIO_DATA -> {
                    if (Objects.isNull(state)) {
                        sendError(session, "CONFIG_ERROR", "Configuration not found", "config");
                        return;
                    }
                    handleTranslateAudioData(session, message.getPayload(), state);
                }
                case REQ_TRANSLATE_STOP -> handleTranslateStop(session, state);
                case null, default -> IWebSocketHandler.sendText(session, JacksonUtil.bean2Json(IMessage.newInstance(IMessageEvent.UNSUPPORTED_OPERATION.getEventType())));
            }
        } catch (Exception e) {
            logger.error("[翻译服务处理消息异常] sessionId: {}", session.getId(), e);
            IWebSocketHandler.sendText(session, JacksonUtil.bean2Json(IMessage.newInstance(IMessageEvent.GENERAL_ERROR.getEventType())));
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
            logger.error("[心跳处理异常]", e);
        }
    }

    private void handleConfig(WebSocketSession session, String message) {
        try {
            logger.info("[配置请求] sessionId: {}, message: {}", session.getId(), message);
            AsrTranslateConfigRequest config = IMessageCustom.parseBusinessRequest(message, AsrTranslateConfigRequest.class);
            if (config == null || StringUtils.isBlank(config.getSourceLanguage()) || StringUtils.isBlank(config.getTargetLanguage())) {
                IWebSocketHandler.sendText(session, JacksonUtil.bean2Json(IMessage.newInstance(IMessageEvent.ILLEGAL_ARGUMENT.getEventType())));
                return;
            }
            String sessionId = session.getId();
            SessionState state = sessionStates.computeIfAbsent(sessionId, k -> new SessionState());
            resetRuntimeState(state);
            state.chatId = "chatId-" + snowflake.nextId();
            state.sourceLanguage = config.getSourceLanguage();
            state.targetLanguage = config.getTargetLanguage();

            // 从 Nacos 选取健康实例，拼接语音服务 ASR+翻译 WebSocket 地址
            Instance instance = namingService.selectOneHealthyInstance("hojoai-webmvc-omniasst-voice");
            String voiceServiceWsUrl = String.format("ws://%s:%d/omniasst-voice/asr/translate", instance.getIp(), instance.getPort());
            logger.info("[从Nacos获取语音服务实例] sessionId: {}, instance: {}:{}", sessionId, instance.getIp(), instance.getPort());

            state.voiceClient = new VoiceServiceWebSocketClient(voiceServiceWsUrl, sessionId, new VoiceServiceWebSocketClient.ResultCallback() {
                @Override
                public void onConfigAck() {
                    ChatIdResponse response = new ChatIdResponse();
                    response.setChatId(state.chatId);
                    logger.info("[下发ChatId] sessionId: {}, chatId: {}", sessionId, state.chatId);
                    IMessageCustom.sendMessageCustom(session, IMessageEvent.BACK_CHAT_ID, response);
                }

                @Override
                public void onResult(Map<String, String> translations, String detectedLanguage, int isEnd) {
                    logger.info("[翻译][收到结果]sessionId: {}, 原始报文: {}, 当前语种: {}, 是否结束: {}", sessionId, JacksonUtil.bean2Json(translations), detectedLanguage, isEnd);
                    handleVoiceServiceResult(session, state, translations, detectedLanguage, isEnd);
                }

                @Override
                public void onError(String errorCode, String errorMessage) {
                    sendError(session, errorCode, errorMessage, "voice_service");
                }
            });

            state.voiceClient.connect();
            state.voiceClient.sendConfig(config.getSourceLanguage(), config.getTargetLanguage(), downstreamAsrProvider);
            state.lastAudioReceiveTime = System.currentTimeMillis();
            startSilenceTimer(session, state);
        } catch (Exception e) {
            logger.error("[配置异常] sessionId: {}", session.getId(), e);
            IWebSocketHandler.sendText(session, JacksonUtil.bean2Json(IMessage.newInstance(IMessageEvent.ILLEGAL_ARGUMENT.getEventType())));
        }
    }

    private void handleVoiceCloneEnable(WebSocketSession session, String message) {
        try {
            String sessionId = session.getId();
            SessionState state = sessionStates.get(sessionId);
            if (state == null) {
                sendError(session, "CONFIG_ERROR", "Configuration not found", "config");
                return;
            }
            // 如果已经开启且正在收集音频或已成功，返回已开启状态
            if (state.voiceCloneEnabled && (!state.voiceCloneTriggered || state.voiceCloneSuccess)) {
                logger.warn("[音色克隆已开启] sessionId: {}, chatId: {}, success: {}", sessionId, state.chatId, state.voiceCloneSuccess);
                VoiceCloneEnableResponse response = new VoiceCloneEnableResponse();
                response.setStatus("already_enabled");
                response.setChatId(state.chatId);
                response.setMessage(state.voiceCloneSuccess ? "Voice clone already completed" : "Voice clone already enabled");
                IMessageCustom.sendMessageCustom(session, IMessageEvent.BACK_VOICE_CLONE_ENABLE_ACK, response);
                return;
            }
            // 如果克隆已触发但还在进行中（future未完成），返回已开启状态
            if (state.voiceCloneEnabled && state.voiceCloneFuture != null && !state.voiceCloneFuture.isDone()) {
                logger.warn("[音色克隆进行中] sessionId: {}, chatId: {}, 克隆任务正在进行，请等待完成", sessionId, state.chatId);
                VoiceCloneEnableResponse response = new VoiceCloneEnableResponse();
                response.setStatus("already_enabled");
                response.setChatId(state.chatId);
                response.setMessage("Voice clone is in progress, please wait");
                IMessageCustom.sendMessageCustom(session, IMessageEvent.BACK_VOICE_CLONE_ENABLE_ACK, response);
                return;
            }
            // 如果之前复刻失败（已触发且已完成但未成功），允许重新开启
            if (state.voiceCloneEnabled && state.voiceCloneTriggered && !state.voiceCloneSuccess) {
                logger.info("[音色克隆重新开启] sessionId: {}, chatId: {}, 上次复刻失败，重新开始收集音频", sessionId, state.chatId);
            } else {
                logger.info("[音色克隆开启] sessionId: {}, chatId: {}", sessionId, state.chatId);
            }
            // 重置状态，重新开始收集音频
            state.voiceCloneEnabled = true;
            state.voiceCloneAudioBuffer = new ByteArrayOutputStream();
            state.voiceCloneAudioDurationMs = 0;
            state.voiceCloneTriggered = false;
            state.voiceCloneSuccess = false;
            state.voiceCloneFuture = null;
            VoiceCloneEnableResponse response = new VoiceCloneEnableResponse();
            response.setStatus("enabled");
            response.setChatId(state.chatId);
            response.setMessage("Voice clone enabled, collecting audio");
            IMessageCustom.sendMessageCustom(session, IMessageEvent.BACK_VOICE_CLONE_ENABLE_ACK, response);
        } catch (Exception e) {
            logger.error("[音色克隆开启失败] sessionId: {}", session.getId(), e);
            sendError(session, "PROCESS_ERROR", "Failed to enable voice clone: " + e.getMessage(), "process");
        }
    }

    private void handleTranslateAudioData(WebSocketSession session, String message, SessionState state) {
        try {
            TranslateAudioDataRequest request = IMessageCustom.parseBusinessRequest(message, TranslateAudioDataRequest.class);
            if (Objects.isNull(request) || StringUtils.isBlank(request.getAudioData())) {
                sendError(session, "ILLEGAL_ARGUMENT", "Invalid audio data", "process");
                return;
            }
            byte[] audioBytes;
            try {
                audioBytes = Base64.getDecoder().decode(request.getAudioData());
            } catch (IllegalArgumentException e) {
                sendError(session, "ILLEGAL_ARGUMENT", "Invalid Base64 audio data", "process");
                return;
            }
            if (audioBytes.length == 0) {
                return;
            }

            state.lastAudioReceiveTime = System.currentTimeMillis();

            collectVoiceCloneAudio(state, audioBytes);
            if (state.voiceCloneEnabled && !state.voiceCloneTriggered && state.voiceCloneAudioDurationMs >= VOICE_CLONE_MIN_DURATION_MS) {
                triggerVoiceClone(session, state);
            }

            if (state.voiceClient != null && state.voiceClient.isConnected()) {
                state.voiceClient.sendAudioData(request.getAudioData());
            }
        } catch (Exception e) {
            logger.error("[音频数据处理异常] sessionId: {}, chatId: {}", session.getId(), state.chatId, e);
            sendError(session, "PROCESS_ERROR", "Error processing audio: " + e.getMessage(), "process");
        }
    }

    private void handleTranslateStop(WebSocketSession session, SessionState state) {
        if (state == null) {
            return;
        }
        try {
            logger.info("[停止翻译] sessionId: {}, chatId: {}", session.getId(), state.chatId);
            cancelSilenceTimer(state);
            long endTime = System.currentTimeMillis();
            publishTranslateConnectionEnd(session, state, endTime);
            sendBackTranslateStopAck(session, state.chatId, "success", "Translation stopped successfully");
            if (state.voiceClient != null) {
                state.voiceClient.close();
            }
        } catch (Exception e) {
            logger.error("[停止翻译异常] chatId: {}", state.chatId, e);
            sendError(session, "PROCESS_ERROR", "Error stopping translation: " + e.getMessage(), "process");
        }
    }

    private void handleVoiceServiceResult(WebSocketSession session, SessionState state, Map<String, String> translations, String detectedLanguage, int isEnd) {
        try {
            if (CollectionUtils.isEmpty(translations)) {
                return;
            }
            if (StringUtils.isNotBlank(detectedLanguage)) {
                state.detectedLang = detectedLanguage;
            }
            String sourceLangText = translations.get(state.sourceLanguage);
            String targetLangText = translations.get(state.targetLanguage);
            if (StringUtils.isNotBlank(detectedLanguage)) {
                if (detectedLanguage.equals(state.sourceLanguage)) {
                    state.asrText = StringUtils.isNotBlank(sourceLangText) ? sourceLangText : "";
                    state.transText = StringUtils.isNotBlank(targetLangText) ? targetLangText : "";
                } else if (detectedLanguage.equals(state.targetLanguage)) {
                    state.asrText = StringUtils.isNotBlank(targetLangText) ? targetLangText : "";
                    state.transText = StringUtils.isNotBlank(sourceLangText) ? sourceLangText : "";
                }
            }
            sendAsrTextResponse(session, state, isEnd);
            if (isEnd == 1) {
                handleAsrEnd(session, state);
            }
        } catch (Exception e) {
            logger.error("[处理语音服务结果异常] chatId: {}", state.chatId, e);
        }
    }

    private void handleAsrEnd(WebSocketSession session, SessionState state) {
        try {
            publishTranslateResult(session, state, 0);
            sendBackTranslateStopAck(session, state.chatId, "complete", "Translation completed");
            resetRuntimeState(state);
            logger.info("[翻译][下发语句结束标识], sessionId: {}, chatId: {}", session.getId(), state.chatId);
        } catch (Exception e) {
            logger.error("[句子结束处理异常] chatId: {}", state.chatId, e);
        }
    }

    private void sendAsrTextResponse(WebSocketSession session, SessionState state, int isEnd) {
        try {
            AsrTextWithTranslateResponse response = new AsrTextWithTranslateResponse();
            response.setText(state.asrText);
            response.setTranText(state.transText);
            response.setCurrLang(state.detectedLang);
            response.setIsEnd(isEnd);
            logger.info("[翻译][下发识别结果到端侧] sessionId: {}, chatId: {}, response: {}", session.getId(), state.chatId, JacksonUtil.bean2Json(response));
            IMessageCustom.sendMessageCustom(session, IMessageEvent.BACK_ASR_TEXT, response);
        } catch (Exception e) {
            logger.error("[下发ASR文本异常] chatId: {}", state.chatId, e);
        }
    }

    private void sendBackTranslateStopAck(WebSocketSession session, String chatId, String status, String message) {
        if (session == null || !session.isOpen()) {
            return;
        }
        try {
            TranslateStopResponse response = new TranslateStopResponse();
            response.setStatus(status);
            response.setChatId(chatId);
            response.setMessage(message);
            IMessageCustom.sendMessageCustom(session, IMessageEvent.BACK_TRANSLATE_STOP_ACK, response);
        } catch (Exception e) {
            logger.error("[发送 BACK_TRANSLATE_STOP_ACK 异常] chatId: {}", chatId, e);
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
            logger.error("[发送错误异常]", e);
        }
    }

    private void publishTranslateResult(WebSocketSession session, SessionState state, Integer isEnd) {
        if (StringUtils.isBlank(state.chatId) || StringUtils.isBlank(state.asrText)) {
            return;
        }
        try {
            String userId = (String) session.getAttributes().get(WebSocketConstants.ATTRIBUTE_USER_ID);
            String conversationId = Objects.nonNull(session.getAttributes().get(WebSocketConstants.ATTRIBUTE_CONVERSATION_ID)) ? (String) session.getAttributes().get(WebSocketConstants.ATTRIBUTE_CONVERSATION_ID) : StringUtils.EMPTY;
            TranslateResultMessage message = new TranslateResultMessage();
            message.setChatId(state.chatId);
            message.setUserId(userId);
            message.setOriginalText(state.asrText);
            message.setTranslatedText(state.transText);
            message.setTimestamp(System.currentTimeMillis());
            message.setSourceLanguage(state.sourceLanguage);
            message.setTargetLanguage(state.targetLanguage);
            message.setDetectedLanguage(state.detectedLang);
            message.setIsEnd(isEnd);
            logger.info("[发布翻译结果] chatId: {}, userId: {}, isEnd: {}, originalText: [{}], translatedText: [{}]", state.chatId, userId, isEnd, state.asrText, state.transText);
            translationPersistenceService.persistTranslateResult(message, conversationId);
        } catch (Exception e) {
            logger.error("[发布翻译结果异常] chatId: {}", state.chatId, e);
        }
    }

    private void publishTranslateConnectionEnd(WebSocketSession session, SessionState state, long endTime) {
        if (StringUtils.isBlank(state.chatId)) {
            return;
        }
        try {
            String userId = (String) session.getAttributes().get(WebSocketConstants.ATTRIBUTE_USER_ID);
            String conversationId = Objects.nonNull(session.getAttributes().get(WebSocketConstants.ATTRIBUTE_CONVERSATION_ID)) ? (String) session.getAttributes().get(WebSocketConstants.ATTRIBUTE_CONVERSATION_ID) : StringUtils.EMPTY;
            TranslateResultMessage message = new TranslateResultMessage();
            message.setChatId(state.chatId);
            message.setUserId(userId);
            message.setStartTime(state.connectionStartTime);
            message.setEndTime(endTime);
            message.setIsEnd(1);
            logger.info("[发布连接结束] chatId: {}, userId: {}, duration: {}ms", state.chatId, userId, endTime - state.connectionStartTime);
            translationPersistenceService.persistTranslateResult(message, conversationId);
        } catch (Exception e) {
            logger.error("[发布连接结束异常] chatId: {}", state.chatId, e);
        }
    }

    private void collectVoiceCloneAudio(SessionState state, byte[] audioData) {
        if (!state.voiceCloneEnabled || state.voiceCloneTriggered) {
            return;
        }
        try {
            state.voiceCloneAudioBuffer.write(audioData);
            state.voiceCloneAudioDurationMs += AUDIO_CHUNK_DURATION_MS;
        } catch (Exception e) {
            logger.error("[音色克隆音频收集异常] chatId: {}", state.chatId, e);
        }
    }

    private void triggerVoiceClone(WebSocketSession session, SessionState state) {
        if (state.voiceCloneTriggered) {
            return;
        }
        state.voiceCloneTriggered = true;
        String userId = (String) session.getAttributes().get(WebSocketConstants.ATTRIBUTE_USER_ID);
        if (StringUtils.isBlank(userId)) {
            state.voiceCloneSuccess = false;
            logger.warn("[音色克隆跳过] userId为空, chatId: {}", state.chatId);
            sendVoiceCloneResult(session, false, null, "ILLEGAL_ARGUMENT", "用户ID不能为空");
            return;
        }
        byte[] audioData = state.voiceCloneAudioBuffer.toByteArray();
        if (audioData.length == 0) {
            state.voiceCloneSuccess = false;
            logger.warn("[音色克隆跳过] 音频数据为空, chatId: {}", state.chatId);
            sendVoiceCloneResult(session, false, null, "ILLEGAL_ARGUMENT", "音频数据为空");
            return;
        }
        state.voiceCloneFuture = CompletableFuture.runAsync(() -> {
            long startTime = System.currentTimeMillis();
            logger.info("[音色克隆接口调用开始] chatId: {}, userId: {}, 音频时长: {}ms, 音频大小: {} bytes", state.chatId, userId, state.voiceCloneAudioDurationMs, audioData.length);
            try {
                MultipartFile multipartFile = new ByteArrayMultipartFile(audioData, "file", "voice_clone.pcm", "audio/pcm");
                Response<VoiceCloneResponse> response = managementServiceClient.cloneVoice(userId, null, voiceCloneProvider, Boolean.TRUE, multipartFile);
                long duration = System.currentTimeMillis() - startTime;
                if (response != null && response.getResContent() != null) {
                    String voiceId = response.getResContent().getVoiceId();
                    state.voiceCloneSuccess = true;
                    logger.info("[音色克隆成功] chatId: {}, userId: {}, voiceId: {}, 耗时: {}ms", state.chatId, userId, voiceId, duration);
                    sendVoiceCloneResult(session, true, voiceId, null, null);
                } else {
                    String errorMsg = response != null && StringUtils.isNotBlank(response.getResMessage()) ? response.getResMessage() : "音色克隆服务异常";
                    state.voiceCloneSuccess = false;
                    logger.error("[音色克隆失败] chatId: {}, userId: {}, 错误: {}, 耗时: {}ms", state.chatId, userId, errorMsg, duration);
                    sendVoiceCloneResult(session, false, null, "VOICE_CLONE_ERROR", errorMsg);
                }
            } catch (Exception e) {
                long duration = System.currentTimeMillis() - startTime;
                state.voiceCloneSuccess = false;
                logger.error("[音色克隆异常] chatId: {}, userId: {}, 耗时: {}ms, 异常: {}", state.chatId, userId, duration, e.getMessage());
                sendVoiceCloneResult(session, false, null, "VOICE_CLONE_ERROR", "音色克隆异常: " + e.getMessage());
            }
        });
    }

    private void sendVoiceCloneResult(WebSocketSession session, boolean success, String voiceId, String errorCode, String errorMessage) {
        if (session == null || !session.isOpen()) {
            return;
        }
        try {
            VoiceCloneResultResponse response = new VoiceCloneResultResponse();
            response.setSuccess(success);
            response.setVoiceId(voiceId);
            response.setErrorCode(errorCode);
            response.setErrorMessage(errorMessage);
            IMessageCustom.sendMessageCustom(session, IMessageEvent.BACK_VOICE_CLONE_RESULT, response);
        } catch (Exception e) {
            logger.error("[下发音色克隆结果异常] 异常: {}", e.getMessage());
        }
    }

    private void resetRuntimeState(SessionState state) {
        state.asrText = "";
        state.transText = "";
        state.detectedLang = null;
    }

    private String generateSilencePacket(int durationMs) {
        int sampleRate = 16000;
        int bytesPerSample = 2;
        int totalBytes = (sampleRate * durationMs / 1000) * bytesPerSample;
        byte[] silenceData = new byte[totalBytes];
        return Base64.getEncoder().encodeToString(silenceData);
    }

    private void startSilenceTimer(WebSocketSession session, SessionState state) {
        if (Objects.nonNull(state.silenceTimer) && !state.silenceTimer.isDone()) {
            return;
        }
        if (Objects.isNull(scheduledExecutorService)) {
            return;
        }
        state.silenceTimer = scheduledExecutorService.scheduleAtFixedRate(() -> {
            try {
                long timeSinceLastAudio = System.currentTimeMillis() - state.lastAudioReceiveTime;
                if (timeSinceLastAudio >= SILENCE_TIMEOUT_MS) {
                    if (state.voiceClient != null && state.voiceClient.isConnected()) {
                        String silenceData = generateSilencePacket(SILENCE_DURATION_MS);
                        state.voiceClient.sendAudioData(silenceData);
                        state.lastAudioReceiveTime = System.currentTimeMillis();
                    }
                }
            } catch (Exception e) {
                logger.error("[静音检测异常] sessionId: {}, chatId: {}", session.getId(), state.chatId, e);
            }
        }, SILENCE_CHECK_INTERVAL_MS, SILENCE_CHECK_INTERVAL_MS, TimeUnit.MILLISECONDS);
    }

    private void cancelSilenceTimer(SessionState state) {
        if (state != null && state.silenceTimer != null && !state.silenceTimer.isDone()) {
            state.silenceTimer.cancel(false);
            state.silenceTimer = null;
        }
    }

    /**
     * 传输层错误仅记录日志，不断开由客户端重连。
     *
     * @param session   WebSocket 会话
     * @param exception 异常
     */
    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        logger.error("[传输错误] sessionId: {}", session.getId(), exception);
    }

    /**
     * 连接关闭：注销心跳、发布连接结束事件、关闭下游语音 WebSocket。
     *
     * @param session     WebSocket 会话
     * @param closeStatus 关闭原因
     */
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) {
        String sessionId = session.getId();
        heartbeatManager.unregisterSession(session);
        SessionState state = sessionStates.remove(sessionId);
        if (state != null) {
            cancelSilenceTimer(state);
            long endTime = System.currentTimeMillis();
            publishTranslateConnectionEnd(session, state, endTime);
            if (state.voiceClient != null) {
                state.voiceClient.close();
            }
        }
        logger.info("[连接关闭] sessionId: {}, status: {}", sessionId, closeStatus);
    }
}
