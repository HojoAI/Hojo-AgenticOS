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

import com.hojoai.common.jackson.JacksonUtil;
import com.hojoai.webmvc.omniasst.translate.websocket.bean.IMessage;
import com.hojoai.webmvc.omniasst.translate.websocket.bean.IMessageCustom;
import com.hojoai.webmvc.omniasst.translate.websocket.bean.IMessageEvent;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.*;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;

import java.net.URI;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * 与语音服务（ASR+翻译）建立的出站 WebSocket 客户端：发送配置与音频帧，解析配置 ACK 与翻译结果。
 */
public class VoiceServiceWebSocketClient {
    private static final Logger logger = LoggerFactory.getLogger(VoiceServiceWebSocketClient.class);

    private final String voiceServiceUrl;
    private final String sessionId;
    private final ResultCallback callback;
    private WebSocketSession webSocketSession;
    private volatile boolean connected = false;

    /**
     * 语音服务异步回调：配置确认、翻译结果或错误。
     */
    public interface ResultCallback {
        /** 收到 {@code BACK_ASR_TRANSLATE_CONFIG_ACK} */
        void onConfigAck();
        /** 收到 {@code BACK_ASR_TRANSLATE_RESULT} */
        void onResult(Map<String, String> translations, String detectedLanguage, int isEnd);
        /** 收到 {@code BACK_ERROR} 或传输异常 */
        void onError(String errorCode, String errorMessage);
    }

    /**
     * @param voiceServiceUrl 语音服务 ws 地址
     * @param sessionId       翻译网关 session，用于日志关联
     * @param callback        结果回调
     */
    public VoiceServiceWebSocketClient(String voiceServiceUrl, String sessionId, ResultCallback callback) {
        this.voiceServiceUrl = voiceServiceUrl;
        this.sessionId = sessionId;
        this.callback = callback;
    }

    /**
     * 建立 WebSocket 并等待连接就绪（含超时）。
     *
     * @throws Exception 连接超时或失败
     */
    public void connect() throws Exception {
        StandardWebSocketClient client = new StandardWebSocketClient();
        CompletableFuture<Void> connectFuture = new CompletableFuture<>();

        WebSocketHandler handler = new WebSocketHandler() {
            @Override
            public void afterConnectionEstablished(WebSocketSession session) {
                webSocketSession = session;
                connected = true;
                logger.info("[语音服务WS连接成功] sessionId: {}, voiceServiceUrl: {}", sessionId, voiceServiceUrl);
                connectFuture.complete(null);
            }

            @Override
            public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) {
                if (message instanceof TextMessage) {
                    handleTextMessage((TextMessage) message);
                }
            }

            @Override
            public void handleTransportError(WebSocketSession session, Throwable exception) {
                logger.error("[语音服务WS传输错误] sessionId: {}", sessionId, exception);
                if (callback != null) {
                    callback.onError("VOICE_SERVICE_ERROR", exception.getMessage());
                }
            }

            @Override
            public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) {
                connected = false;
                logger.info("[语音服务WS连接关闭] sessionId: {}, status: {}", sessionId, closeStatus);
            }

            @Override
            public boolean supportsPartialMessages() {
                return false;
            }
        };

        client.execute(handler, null, new URI(voiceServiceUrl)).get(10, TimeUnit.SECONDS);
        connectFuture.get(10, TimeUnit.SECONDS);
    }

    /**
     * 发送 ASR 翻译配置（语言对）。
     *
     * @param lang1        语言一（与协议 target_languages 列表顺序一致）
     * @param lang2        语言二
     * @param asrProvider  下游 ASR/翻译引擎提供商标识，来自配置 {@code translate.voice.downstream.asr-provider}
     * @throws IllegalStateException 未连接时抛出
     */
    public void sendConfig(String lang1, String lang2, String asrProvider) throws Exception {
        if (!connected || webSocketSession == null) {
            throw new IllegalStateException("WebSocket not connected");
        }
        Map<String, Object> config = Map.of(
            "provider", asrProvider,
            "target_languages", java.util.List.of(lang1, lang2)
        );
        IMessageCustom.sendMessageCustom(webSocketSession, IMessageEvent.REQ_ASR_TRANSLATE_CONFIG, config);
        logger.info("[发送配置到语音服务] sessionId: {}, lang1: {}, lang2: {}", sessionId, lang1, lang2);
    }

    /**
     * 发送一帧 Base64 PCM 音频。
     *
     * @param audioDataBase64 Base64 音频数据
     */
    public void sendAudioData(String audioDataBase64) throws Exception {
        if (!connected || webSocketSession == null) {
            return;
        }
        Map<String, String> audioData = Map.of("audio_data", audioDataBase64);
        logger.info("[翻译][发送音频到语音服务], sessionId: {}", sessionId);
        IMessageCustom.sendMessageCustom(webSocketSession, IMessageEvent.REQ_ASR_TRANSLATE_AUDIO_DATA, audioData);
    }

    /** 关闭与语音服务的连接。 */
    public void close() {
        try {
            if (webSocketSession != null && webSocketSession.isOpen()) {
                webSocketSession.close();
            }
            connected = false;
            logger.info("[关闭语音服务WS连接] sessionId: {}", sessionId);
        } catch (Exception e) {
            logger.error("[关闭语音服务WS连接失败] sessionId: {}", sessionId, e);
        }
    }

    private void handleTextMessage(TextMessage message) {
        try {
            String payload = message.getPayload();
            logger.info("[收到语音服务报文] sessionId: {}, payload: {}", sessionId, payload);

            IMessage<?> iMessage = JacksonUtil.json2Bean(payload, IMessage.class);
            String eventType = iMessage.getEventType();

            if ("BACK_ASR_TRANSLATE_CONFIG_ACK".equals(eventType)) {
                logger.info("[语音服务配置确认] sessionId: {}", sessionId);
                if (callback != null) {
                    callback.onConfigAck();
                }
            } else if ("BACK_ASR_TRANSLATE_RESULT".equals(eventType)) {
                Map<String, Object> eventContent = (Map<String, Object>) iMessage.getEventContent();
                Map<String, Object> pdata = (Map<String, Object>) eventContent.get("pdata");
                Map<String, String> translations = (Map<String, String>) pdata.get("translations");
                String detectedLanguage = (String) pdata.get("detected_language");
                Integer isEnd = (Integer) pdata.get("is_end");

                if (callback != null) {
                    callback.onResult(translations, detectedLanguage, isEnd != null ? isEnd : 0);
                }
            } else if ("BACK_ERROR".equals(eventType)) {
                Map<String, Object> eventContent = (Map<String, Object>) iMessage.getEventContent();
                Map<String, Object> pdata = (Map<String, Object>) eventContent.get("pdata");
                String errorCode = (String) pdata.get("errorCode");
                String errorMessage = (String) pdata.get("errorMessage");
                if (callback != null) {
                    callback.onError(errorCode, errorMessage);
                }
            }
        } catch (Exception e) {
            logger.error("[处理语音服务报文异常] sessionId: {}", sessionId, e);
        }
    }

    /**
     * @return 是否仍处于已连接且会话打开状态
     */
    public boolean isConnected() {
        return connected && webSocketSession != null && webSocketSession.isOpen();
    }
}
