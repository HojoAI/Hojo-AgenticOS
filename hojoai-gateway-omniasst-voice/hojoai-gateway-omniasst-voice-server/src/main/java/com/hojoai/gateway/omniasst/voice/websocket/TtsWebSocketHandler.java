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
import com.hojoai.gateway.omniasst.voice.api.TtsAPI;
import com.hojoai.gateway.omniasst.voice.api.dto.TTSRequest;
import com.hojoai.gateway.omniasst.voice.constants.GlobalConstants;
import com.hojoai.gateway.omniasst.voice.doubao.speech.protocol.Message;
import com.hojoai.gateway.omniasst.voice.doubao.speech.protocol.SpeechWebSocketClient;
import com.hojoai.gateway.omniasst.voice.constants.TtsPlatform;
import com.hojoai.gateway.omniasst.voice.service.tts.DoubaoTtsService;
import com.hojoai.gateway.omniasst.voice.service.tts.TtsQueueService;
import com.hojoai.gateway.omniasst.voice.websocket.bean.IMessage;
import com.hojoai.gateway.omniasst.voice.websocket.bean.IMessageCustom;
import com.hojoai.gateway.omniasst.voice.websocket.bean.IMessageEvent;
import com.hojoai.gateway.omniasst.voice.websocket.bean.dto.CommonTtsFlagResponse;
import com.hojoai.gateway.omniasst.voice.websocket.bean.dto.ErrorResponse;
import com.hojoai.gateway.omniasst.voice.websocket.bean.dto.FinishFlag;
import com.hojoai.gateway.omniasst.voice.websocket.bean.dto.HeartbeatRequest;
import com.hojoai.gateway.omniasst.voice.websocket.bean.dto.HeartbeatResponse;
import com.hojoai.gateway.omniasst.voice.websocket.bean.dto.TtsWebSocketRequest;
import com.hojoai.gateway.omniasst.voice.util.OssUtil;
import feign.Response;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.AbstractWebSocketHandler;
import xyz.downgoon.snowflake.Snowflake;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 通用 TTS WebSocket（路径 {@code /common/tts}）：支持默认 Feign TTS、豆包长连接、队列消费与 OSS 等分支。
 * 与 {@link WebSocketHeartbeatManager}、{@link TtsQueueService}、{@link DoubaoTtsService} 协作。
 */
@Component
public class TtsWebSocketHandler extends AbstractWebSocketHandler {
    private static final Logger logger = LoggerFactory.getLogger(TtsWebSocketHandler.class);

    @Resource
    private TtsAPI ttsAPI;
    @Resource
    private WebSocketHeartbeatManager heartbeatManager;
    @Resource
    private OssUtil ossUtil;
    @Resource
    private Snowflake snowflake;
    @Resource
    private DoubaoTtsService doubaoTtsService;
    
    @Resource
    private TtsQueueService ttsQueueService;

    private static class TtsSessionState {
        StringBuilder cachedText = new StringBuilder();
        boolean ttsInProgress = false;
        boolean isLastRequest = false;
        Integer platform;  // 平台：0=默认, 1=豆包
        String voiceId;    // 音色ID
        String language;   // 语言，用于豆包 explicit_language，空时默认 zh-cn
        StringBuilder concatBuffer = new StringBuilder(); // 用于拼接消息内容的缓存
        boolean concatEnabled = false; // 是否开启拼接逻辑
        /** 豆包：与当前 TTS WebSocket 保持一个连接，在首条豆包请求时创建，连接关闭时释放 */
        SpeechWebSocketClient doubaoClient;
        String doubaoVoiceId; // 创建 doubaoClient 时使用的 voiceId，若变更则需重建 client
        /** 豆包：当前连接上是否已对 doubaoClient 执行过 START_SESSION（单连接单 session 复用） */
        boolean doubaoSessionStarted;
        /** 豆包：与协议共用的 session id，与上游 WebSocket session.getId() 一致 */
        String doubaoTtsSessionId;
        String doubaoSessionVoiceId;
        String doubaoSessionLanguage;
        boolean doubaoFinishRequested = false;
        CompletableFuture<Void> doubaoReceiveFuture;
        ByteArrayOutputStream doubaoRoundAudioStream = new ByteArrayOutputStream();
        /** 豆包且未开启拼接时：括号或 markdown 只有一半时暂存，等完整后再发送 */
        StringBuilder doubaoHalfBuffer = new StringBuilder();
        /** 豆包且开启拼接时：一个 session 生命周期内前 5 条文本直发，不进入 concatBuffer */
        int doubaoConcatDirectSendCount = 0;
    }

    private final ConcurrentHashMap<String, TtsSessionState> ttsSessionStates = new ConcurrentHashMap<>();

    /** WebSocket 建连后预热豆包连接，减少首次合成 RTT。 */
    private static final String DOUBAO_PRECONNECT_VOICE_ID = "zh_female_vv_uranus_bigtts";
    /** 豆包 session 常驻收包线程单次 poll 超时（秒）；超时后若未请求 FINISH，则继续等待下一批文本。 */
    private static final long DOUBAO_RECEIVE_POLL_TIMEOUT_SECONDS = 30L;
    /** 请求 FINISH_SESSION 后等待收包线程退出的最长时间（秒）。 */
    private static final long DOUBAO_RECEIVER_AWAIT_SECONDS = 40L;
    /** 豆包且开启拼接时，一个 session 生命周期内前 N 条文本直发 */
    private static final int DOUBAO_CONCAT_DIRECT_SEND_LIMIT = 50;
    /** 无标点时拼接缓冲上限，过小会频繁切段、多 session 接缝明显 */
    private static final int CONCAT_MAX_BUFFER_WITHOUT_PUNCT = 15;
    /** 用于 flush / 分割的句读字符（含顿号、冒号、省略号等） */
    private static final String PUNCT_CHARS = "，。；！？,.;!?~～、：…—·\u21B5\n\r";

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        heartbeatManager.registerSession(session, "TtsWebSocketHandler");
        TtsSessionState state = ttsSessionStates.computeIfAbsent(session.getId(), k -> new TtsSessionState());
        CompletableFuture.runAsync(() -> preconnectDoubaoClient(session, state));
        logger.info("TTS WebSocket connected, session id: {}", session.getId());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        heartbeatManager.updateLastActiveTime(session);
        try {
            IMessage<?> iMessage = JacksonUtil.json2Bean(message.getPayload(), IMessage.class);
            IMessageEvent eventType = IMessageEvent.getByEventType(iMessage.getEventType());
            switch (eventType) {
                case REQ_HEARTBEAT -> handleHeartbeat(session, message.getPayload());
                case REQ_TTS_TEXT -> handleTtsRequest(session, message.getPayload());
                case null, default -> IWebSocketHandler.sendText(session, JacksonUtil.bean2Json(IMessage.newInstance(IMessageEvent.UNSUPPORTED_OPERATION.getEventType())));
            }
        } catch (Exception e) {
            logger.error("Error handling text message", e);
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
            logger.error("Error handling heartbeat", e);
        }
    }

    private void handleTtsRequest(WebSocketSession session, String payload) {
        try {
            TtsWebSocketRequest request = IMessageCustom.parseBusinessRequest(payload, TtsWebSocketRequest.class);
            if (Objects.isNull(request)) {
                sendError(session, IMessageEvent.ILLEGAL_ARGUMENT.getEventType(), "参数不能为空", "tts");
                return;
            }
            
            // 只有 minimax 平台走队列消费逻辑
            Integer platform = request.getPlatform();
            if (platform != null && platform.equals(TtsPlatform.MINIMAX.getCode())) {
                ttsQueueService.enqueueRequest(request, session);
                return;
            }

            String sessionId = session.getId();
            TtsSessionState state = ttsSessionStates.computeIfAbsent(sessionId, k -> new TtsSessionState());
            
            // 更新 platform 和 voiceId（如果提供了）
            if (request.getPlatform() != null) {
                state.platform = request.getPlatform();
            }
            if (StringUtils.isNotBlank(request.getVoiceId())) {
                state.voiceId = request.getVoiceId();
            }
            if (StringUtils.isNotBlank(request.getLanguage())) {
                state.language = request.getLanguage();
            }
            if (request.getConcatEnabled() != null) {
                state.concatEnabled = request.getConcatEnabled();
            }
            
            boolean isEnd = request.getIsEnd() != null && request.getIsEnd() == 1;

            // 拼接逻辑：仅在 platform=1 (豆包) 且 concatEnabled=true 时生效
            String textToProcess = "";
            boolean shouldProcessNow = true;
            /*if (state.platform != null && state.platform == 1 && state.concatEnabled) {
                String currentText = StringUtils.defaultString(request.getText());
                boolean shouldDirectSend = StringUtils.isNotBlank(currentText)
                        && state.doubaoConcatDirectSendCount < DOUBAO_CONCAT_DIRECT_SEND_LIMIT;
                if (shouldDirectSend) {
                    state.doubaoConcatDirectSendCount++;
                    textToProcess = currentText;
                    logger.info("豆包拼接直发sessionid: {}，第{}条文本: {}", session.getId(), state.doubaoConcatDirectSendCount, textToProcess);
                } else {
                    state.concatBuffer.append(currentText);
                    String concatText = state.concatBuffer.toString();
                    logger.info("豆包拼接缓存sessionid: {}，当前字段text: {} ，拼接后text: {}", session.getId(), request.getText(), concatText);

                    if (!isEnd && !shouldFlushConcatBuffer(concatText)) {
                        // 无句读且未达长度上限，继续等待
                        shouldProcessNow = false;
                    } else {
                        if (!isEnd && !endsWithPunctuation(concatText)) {
                            // 包含标点但不是以标点结尾，进行分割
                            String[] splitResult = splitTextByPunctuation(concatText);
                            if (splitResult != null && splitResult.length == 2) {
                                textToProcess = splitResult[0];
                                state.concatBuffer.setLength(0);
                                state.concatBuffer.append(splitResult[1]);
                                logger.info("sessionid: {}，文本分割 - 发送部分: {}，保留部分: {}", session.getId(), textToProcess, splitResult[1]);
                            } else {
                                // 分割失败，发送全部
                                textToProcess = concatText;
                                state.concatBuffer.setLength(0);
                            }
                        } else {
                            // 以标点符号结尾或者是最后一条消息，直接发送全部
                            textToProcess = concatText;
                            state.concatBuffer.setLength(0);
                        }
                    }
                }
            } else if (state.platform != null && state.platform == 1 && !state.concatEnabled) {
                // 豆包且未开启拼接：若括号或 markdown 只有一半则先缓存，等完整后再发送
                state.doubaoHalfBuffer.append(sanitizedText);
                String halfText = state.doubaoHalfBuffer.toString();
                if (!isEnd && (hasUnclosedBrackets(halfText) || hasUnclosedMarkdown(halfText))) {
                    shouldProcessNow = false;
                } else {
                    textToProcess = halfText;
                    state.doubaoHalfBuffer.setLength(0);
                    logger.info("sessionid: {}，当前字段text: {} ，拼接后text: {}", session.getId(), sanitizedText, textToProcess);
                }
            } else {
                textToProcess = request.getText();
            }*/

            textToProcess = request.getText();

            if (!shouldProcessNow) {
                return;
            }

            if (state.ttsInProgress) {
                state.cachedText.append(textToProcess);
                if (isEnd) state.isLastRequest = true;
                return;
            }
            textToProcess = state.cachedText.toString() + textToProcess;
            state.cachedText.setLength(0);
            state.ttsInProgress = true;
            state.isLastRequest = isEnd;
            
            // 判断使用哪个 TTS 服务：platform=1 且 voiceId 不为空时使用豆包，否则使用默认
            boolean useDoubao = state.platform != null && state.platform == 1 && StringUtils.isNotBlank(state.voiceId);
            if (useDoubao) {
                processDoubaoTtsStreamAsync(session, textToProcess, isEnd, state.voiceId, state.language);
            } else {
                processTtsStreamAsync(session, textToProcess, isEnd);
            }
        } catch (Exception e) {
            logger.error("Error handling TTS request", e);
            sendError(session, IMessageEvent.GENERAL_ERROR.getEventType(), "处理TTS请求失败: " + e.getMessage(), "tts");
        }
    }

    /**
     * 判断是否应该发送拼接缓存中的内容
     */
    private boolean shouldFlushConcatBuffer(String text) {
        if (StringUtils.isBlank(text)) {
            return false;
        }
        if (text.length() > CONCAT_MAX_BUFFER_WITHOUT_PUNCT) {
            return true;
        }
        for (int i = 0; i < text.length(); i++) {
            if (PUNCT_CHARS.indexOf(text.charAt(i)) >= 0) {
                return true;
            }
        }
        return false;
    }

    /**
     * 检查文本是否以标点符号结尾
     */
    private boolean endsWithPunctuation(String text) {
        if (StringUtils.isBlank(text)) {
            return false;
        }
        int i = text.length() - 1;
        while (i >= 0 && Character.isWhitespace(text.charAt(i))) {
            i--;
        }
        if (i < 0) {
            return false;
        }
        return PUNCT_CHARS.indexOf(text.charAt(i)) >= 0;
    }

    /**
     * 分割文本：如果包含句读但不是以句读结尾，返回最后一个句读之前（含句读）与剩余部分
     */
    private String[] splitTextByPunctuation(String text) {
        if (StringUtils.isBlank(text)) {
            return null;
        }
        int lastPunctuationIndex = -1;
        for (int i = 0; i < text.length(); i++) {
            if (PUNCT_CHARS.indexOf(text.charAt(i)) >= 0) {
                lastPunctuationIndex = i;
            }
        }
        if (lastPunctuationIndex >= 0 && lastPunctuationIndex < text.length() - 1) {
            return new String[]{
                    text.substring(0, lastPunctuationIndex + 1),
                    text.substring(lastPunctuationIndex + 1)
            };
        }
        return null;
    }

    /** 括号对：开->闭。用于检测“只有一半”的括号 */
    private static final String BRACKET_PAIRS = "（）()[]{}【】<>〈〉《》";

    /**
     * 判断文本中括号是否未成对（只有一半）
     */
    private boolean hasUnclosedBrackets(String text) {
        if (StringUtils.isBlank(text)) {
            return false;
        }
        int[] stack = new int[text.length()];
        int top = -1;
        for (int i = 0; i < text.length(); i++) {
            int idx = BRACKET_PAIRS.indexOf(text.charAt(i));
            if (idx < 0) continue;
            if (idx % 2 == 0) {
                stack[++top] = idx;
            } else {
                int openIdx = idx - 1;
                if (top < 0 || stack[top] != openIdx) {
                    return true; // 孤立的闭括号或未匹配
                }
                top--;
            }
        }
        return top >= 0; // 未闭合的开括号
    }

    /**
     * 判断文本中 markdown 符号是否只有一半（未成对）
     */
    private boolean hasUnclosedMarkdown(String text) {
        if (StringUtils.isBlank(text)) {
            return false;
        }
        String s = text;
        int doubleStar = countOccurrences(s, "**");
        if (doubleStar % 2 != 0) return true;
        /*int backtick = countOccurrences(s, "`");
        if (backtick % 2 != 0) return true;
        int underscore = countOccurrences(s, "_");
        if (underscore % 2 != 0) return true;
        int tilde = countOccurrences(s, "~~");
        if (tilde % 2 != 0) return true;*/
        // 单 *（排除 ** 中的 *）：先去掉 ** 再数 *
        String withoutDoubleStar = s.replace("**", "");
        int singleStar = countOccurrences(withoutDoubleStar, "*");
        if (singleStar % 2 != 0) return true;
        return false;
    }

    private int countOccurrences(String s, String sub) {
        int count = 0;
        int idx = 0;
        while ((idx = s.indexOf(sub, idx)) >= 0) {
            count++;
            idx += sub.length();
        }
        return count;
    }

    private void processTtsStreamAsync(WebSocketSession session, String text, boolean isLastRequest) {
        CompletableFuture.runAsync(() -> {
            String sessionId = session.getId();
            TtsSessionState state = ttsSessionStates.get(sessionId);
            if (state == null) {
                logger.warn("TtsSessionState not found for session: {}", sessionId);
                return;
            }
            sendTtsFlag(session, FinishFlag.START);
            try {
                TTSRequest ttsRequest = new TTSRequest();
                ttsRequest.setText(text);
                ttsRequest.setLanguage(GlobalConstants.LANGUAGE_CHINESE_STRING);
                ttsRequest.setVoiceId(StringUtils.isNotBlank(state.voiceId) ? state.voiceId : GlobalConstants.DEFAULT_CHINESE_TTS_VOICE_ID);
                Response ttsResponse = ttsAPI.tts(ttsRequest);
                if (Objects.isNull(ttsResponse) || ttsResponse.status() != 200) {
                    logger.error("TTS service call failed, session: {}, status: {}", sessionId, ttsResponse != null ? ttsResponse.status() : "null");
                    state.ttsInProgress = false;
                    if (isLastRequest) {
                        sendTtsFlag(session, FinishFlag.FINISH);
                    }
                    processCachedTextIfExists(session, state);
                    return;
                }
                ByteArrayOutputStream audioStream = new ByteArrayOutputStream();
                try (InputStream inputStream = ttsResponse.body().asInputStream()) {
                    byte[] buffer = new byte[10240];
                    byte[] previousChunk = null;
                    int previousBytesRead = 0;
                    int bytesRead;
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        if (!session.isOpen()) {
                            logger.warn("WebSocket session closed, stopping TTS stream, session: {}", sessionId);
                            break;
                        }
                        if (previousChunk != null) {
                            sendTtsAudioBinary(session, previousChunk, previousBytesRead);
                            audioStream.write(previousChunk, 0, previousBytesRead);
                        }
                        previousChunk = new byte[bytesRead];
                        System.arraycopy(buffer, 0, previousChunk, 0, bytesRead);
                        previousBytesRead = bytesRead;
                    }
                    if (previousChunk != null) {
                        sendTtsAudioBinary(session, previousChunk, previousBytesRead);
                        audioStream.write(previousChunk, 0, previousBytesRead);
                    }
                }
                byte[] audioData = audioStream.toByteArray();
                uploadTtsAudioToOSS(sessionId, audioData);
                state.ttsInProgress = false;
                if (isLastRequest) {
                    sendTtsFlag(session, FinishFlag.FINISH);
                }
                processCachedTextIfExists(session, state);
            } catch (Exception e) {
                logger.error("Error processing TTS stream, session: {}", sessionId, e);
                state.ttsInProgress = false;
                state.isLastRequest = false;
                if (isLastRequest) {
                    sendTtsFlag(session, FinishFlag.FINISH);
                }
                processCachedTextIfExists(session, state);
            }
        });
    }

    private void sendTtsAudioBinary(WebSocketSession session, byte[] audioChunk, int length) {
        try {
            byte[] chunkToSend = new byte[length];
            System.arraycopy(audioChunk, 0, chunkToSend, 0, length);
            ByteBuffer byteBuffer = ByteBuffer.wrap(chunkToSend);
            IWebSocketHandler.sendBinary(session, byteBuffer);
        } catch (Exception e) {
            logger.error("Error sending TTS audio binary, session: {}", session.getId(), e);
        }
    }

    private void sendTtsFlag(WebSocketSession session, FinishFlag flag) {
        if (session == null || !session.isOpen()) {
            logger.info("Skip sendTtsFlag, session is null or closed, flag: {}", flag);
            return;
        }
        try {
            CommonTtsFlagResponse response = new CommonTtsFlagResponse();
            response.setFinishFlag(flag.getValue());
            IMessageCustom.sendMessageCustom(session, IMessageEvent.BACK_COMMON_TTS_FLAG, response);
            logger.info("sendTtsFlag sessionid：{}, flag: {}", session.getId(), flag.getValue());
        } catch (Exception e) {
            logger.error("Failed to send TTS flag, session: {}, flag: {}", session.getId(), flag.getValue(), e);
        }
    }

    private void processCachedTextIfExists(WebSocketSession session, TtsSessionState state) {
        if (state.cachedText.length() > 0) {
            String textToProcess = state.cachedText.toString();
            state.cachedText.setLength(0);
            state.ttsInProgress = true;
            boolean isLast = state.isLastRequest;
            if (isLast) {
                state.isLastRequest = false;
            }
            // 判断使用哪个 TTS 服务
            boolean useDoubao = state.platform != null && state.platform == 1 && StringUtils.isNotBlank(state.voiceId);
            if (useDoubao) {
                processDoubaoTtsStreamAsync(session, textToProcess, isLast, state.voiceId, state.language);
            } else {
                processTtsStreamAsync(session, textToProcess, isLast);
            }
        }
    }

    /**
     * 将请求中的 language（chinese/english）转换为豆包 explicit_language（zh-cn/en），不传或未知时默认 zh-cn。
     */
    private static String toExplicitLanguage(String language) {
        if (StringUtils.isBlank(language)) {
            return "zh-cn";
        }
        return switch (language.trim().toLowerCase()) {
            case "chinese" -> "zh-cn";
            case "english" -> "en";
            default -> "zh-cn";
        };
    }

    /**
     * 处理豆包 TTS 流式请求。使用与当前 TTS WebSocket 保持同步的单个豆包连接（首条豆包请求时创建，连接关闭时释放）。
     */
    private void processDoubaoTtsStreamAsync(WebSocketSession session, String text, boolean isLastRequest, String voiceId, String language) {
        long startTime = System.currentTimeMillis();
        final boolean isLast = isLastRequest;
        CompletableFuture.runAsync(() -> {
            String sessionId = session.getId();
            TtsSessionState state = ttsSessionStates.get(sessionId);
            if (state == null) {
                logger.warn("TtsSessionState not found for session: {}", sessionId);
                return;
            }
            try {
                // 与当前 TTS WebSocket 保持一个豆包连接：建连后预热，voiceId 变更时重建，连接关闭时释放
                SpeechWebSocketClient client = ensureDoubaoClient(state, voiceId);
                String explicitLanguage = toExplicitLanguage(language);
                boolean startedNow = ensureDoubaoSessionStarted(state, client, voiceId, explicitLanguage, sessionId);
                client = state.doubaoClient;
                if (startedNow) {
                    sendTtsFlag(session, FinishFlag.START);
                    startDoubaoReceiverIfNeeded(session, state, client, startTime);
                }
                String processedText = sanitizeDoubaoTtsInputText(text);
                if (StringUtils.isNotBlank(processedText)) {

                    logger.info("[doubao]豆包TTS发送参数 session: {}, text:{}, language：{} ", sessionId, processedText, explicitLanguage);
                    doubaoTtsService.sendTaskRequest(client, processedText, voiceId, "pcm", explicitLanguage, sessionId);
                }
                if (isLast && state.doubaoSessionStarted && state.doubaoTtsSessionId != null) {
                    synchronized (state) {
                        state.doubaoFinishRequested = true;
                    }
                    doubaoTtsService.requestSessionFinish(client, state.doubaoTtsSessionId);
                    awaitDoubaoReceiverFinished(state, sessionId);
                    byte[] audioData;
                    synchronized (state) {
                        audioData = state.doubaoRoundAudioStream.toByteArray();
                        state.doubaoRoundAudioStream.reset();
                    }
                    if (audioData.length > 0) {
                        uploadTtsAudioToOSS(sessionId, audioData);
                    }
                }
            } catch (Exception e) {
                logger.error("[doubao]Error processing Doubao TTS stream, session: {}", sessionId, e);
                try {
                    synchronized (state) {
                        if (state.doubaoClient != null && state.doubaoSessionStarted && state.doubaoTtsSessionId != null) {
                            state.doubaoFinishRequested = true;
                            doubaoTtsService.requestSessionFinish(state.doubaoClient, state.doubaoTtsSessionId);
                        }
                    }
                    awaitDoubaoReceiverFinished(state, sessionId);
                } catch (Exception ex) {
                    logger.warn("[doubao]异常后结束豆包 session 失败: {}", ex.getMessage());
                    synchronized (state) {
                        clearDoubaoSessionState(state);
                        state.doubaoRoundAudioStream.reset();
                    }
                }
                state.isLastRequest = false;
                sendError(session, IMessageEvent.GENERAL_ERROR.getEventType(), "豆包TTS处理失败: " + e.getMessage(), "tts");
            } finally {
                state.ttsInProgress = false;
                if (isLast) {
                    sendTtsFlag(session, FinishFlag.FINISH);
                }
                processCachedTextIfExists(session, state);
            }
        });
    }

    private void preconnectDoubaoClient(WebSocketSession session, TtsSessionState state) {
        try {
            if (!session.isOpen()) {
                return;
            }
            SpeechWebSocketClient client = ensureDoubaoClient(state, DOUBAO_PRECONNECT_VOICE_ID);
            if (!session.isOpen() || ttsSessionStates.get(session.getId()) != state) {
                synchronized (state) {
                    if (state.doubaoClient == client) {
                        if (state.doubaoSessionStarted && state.doubaoTtsSessionId != null) {
                            state.doubaoFinishRequested = true;
                            doubaoTtsService.requestSessionFinish(client, state.doubaoTtsSessionId);
                        }
                        doubaoTtsService.closeClient(client);
                        state.doubaoClient = null;
                        state.doubaoVoiceId = null;
                        clearDoubaoSessionState(state);
                        state.doubaoRoundAudioStream.reset();
                    }
                }
                return;
            }
            logger.info("Preconnected doubao client for session: {}, voiceId: {}", session.getId(), DOUBAO_PRECONNECT_VOICE_ID);
        } catch (Exception e) {
            logger.warn("Failed to preconnect doubao client, session: {}", session.getId(), e);
        }
    }

    private SpeechWebSocketClient ensureDoubaoClient(TtsSessionState state, String voiceId) throws Exception {
        synchronized (state) {
            SpeechWebSocketClient client = state.doubaoClient;
            if (client != null && Objects.equals(state.doubaoVoiceId, voiceId)) {
                return client;
            }
            if (client != null) {
                if (state.doubaoSessionStarted && state.doubaoTtsSessionId != null) {
                    state.doubaoFinishRequested = true;
                    doubaoTtsService.requestSessionFinish(client, state.doubaoTtsSessionId);
                }
                doubaoTtsService.closeClient(client);
                state.doubaoClient = null;
                state.doubaoVoiceId = null;
                clearDoubaoSessionState(state);
                state.doubaoRoundAudioStream.reset();
            }
            SpeechWebSocketClient newClient = doubaoTtsService.createAndConnectClient(voiceId);
            state.doubaoClient = newClient;
            state.doubaoVoiceId = voiceId;
            return newClient;
        }
    }

    private void startDoubaoReceiverIfNeeded(WebSocketSession session, TtsSessionState state, SpeechWebSocketClient client, long startTime) {
        synchronized (state) {
            if (state.doubaoReceiveFuture != null && !state.doubaoReceiveFuture.isDone()) {
                return;
            }
            state.doubaoReceiveFuture = CompletableFuture.runAsync(() -> receiveDoubaoSessionAudio(session, state, client, startTime));
        }
    }

    private void receiveDoubaoSessionAudio(WebSocketSession session, TtsSessionState state, SpeechWebSocketClient client, long startTime) {
        String sessionId = session.getId();
        try {
            while (true) {
                Message msg = client.receiveMessage(DOUBAO_RECEIVE_POLL_TIMEOUT_SECONDS, TimeUnit.SECONDS);
                if (msg == null) {
                    boolean shouldStop;
                    synchronized (state) {
                        shouldStop = state.doubaoFinishRequested || !state.doubaoSessionStarted;
                    }
                    if (shouldStop) {
                        logger.warn("[doubao]等待 SESSION_FINISHED 超时({}s)，结束收包线程 session: {}", DOUBAO_RECEIVE_POLL_TIMEOUT_SECONDS, sessionId);
                        break;
                    }
                    continue;
                }
                switch (msg.getType()) {
                    case FULL_SERVER_RESPONSE:
                        break;
                    case AUDIO_ONLY_SERVER:
                        if (msg.getPayload() != null && msg.getPayload().length > 0) {
                            if (session.isOpen()) {
                                sendTtsAudioBinary(session, msg.getPayload(), msg.getPayload().length);
                                logger.info("[doubao]豆包TTS发送音频数据 session: {}, 耗时：{} 毫秒", sessionId, System.currentTimeMillis() - startTime);
                            }
                            synchronized (state) {
                                state.doubaoRoundAudioStream.write(msg.getPayload(), 0, msg.getPayload().length);
                            }
                        }
                        break;
                    default:
                        logger.error("[doubao]Unexpected message type in session receiver: {}", msg.getType());
                }
                if (msg.getEvent() == com.hojoai.gateway.omniasst.voice.doubao.speech.protocol.EventType.SESSION_FINISHED) {
                    break;
                }
            }
        } catch (Exception e) {
            logger.error("[doubao]Error receiving Doubao session audio, session: {}", sessionId, e);
        } finally {
            synchronized (state) {
                clearDoubaoSessionState(state);
            }
        }
    }

    private void awaitDoubaoReceiverFinished(TtsSessionState state, String sessionId) {
        CompletableFuture<Void> future;
        synchronized (state) {
            future = state.doubaoReceiveFuture;
        }
        if (future == null) {
            return;
        }
        try {
            future.get(DOUBAO_RECEIVER_AWAIT_SECONDS, TimeUnit.SECONDS);
        } catch (Exception e) {
            logger.warn("[doubao]等待收包线程结束超时 session: {}, err: {}", sessionId, e.getMessage());
        }
    }

    private boolean ensureDoubaoSessionStarted(TtsSessionState state, SpeechWebSocketClient client, String voiceId, String explicitLanguage, String sessionId) throws Exception {
        synchronized (state) {
            if (state.doubaoSessionStarted) {
                boolean sameVoice = Objects.equals(state.doubaoSessionVoiceId, voiceId);
                boolean sameLanguage = Objects.equals(state.doubaoSessionLanguage, explicitLanguage);
                if (sameVoice && sameLanguage) {
                    return false;
                }
                logger.warn("[doubao]一轮播报中检测到 voice/language 变化，先关闭旧 session 后重开。session: {}", sessionId);
                if (state.doubaoTtsSessionId != null) {
                    state.doubaoFinishRequested = true;
                    doubaoTtsService.requestSessionFinish(client, state.doubaoTtsSessionId);
                }
                doubaoTtsService.closeClient(client);
                SpeechWebSocketClient newClient = doubaoTtsService.createAndConnectClient(voiceId);
                state.doubaoClient = newClient;
                state.doubaoVoiceId = voiceId;
                clearDoubaoSessionState(state);
                state.doubaoRoundAudioStream.reset();
                client = newClient;
            }
            doubaoTtsService.openSession(client, voiceId, "pcm", explicitLanguage, sessionId);
            state.doubaoSessionStarted = true;
            state.doubaoTtsSessionId = sessionId;
            state.doubaoSessionVoiceId = voiceId;
            state.doubaoSessionLanguage = explicitLanguage;
            state.doubaoFinishRequested = false;
            state.doubaoRoundAudioStream.reset();
            return true;
        }
    }

    private void clearDoubaoSessionState(TtsSessionState state) {
        state.doubaoSessionStarted = false;
        state.doubaoTtsSessionId = null;
        state.doubaoSessionVoiceId = null;
        state.doubaoSessionLanguage = null;
        state.doubaoFinishRequested = false;
        state.doubaoReceiveFuture = null;
    }

    /**
     * 豆包 TTS 入文预处理：去掉星号、尖括号标签（如 &lt;tag_confused&gt;），并压缩空白。
     */
    private String sanitizeDoubaoTtsInputText(String text) {
        if (StringUtils.isBlank(text)) {
            return text;
        }
        return text.replace("*", "").replaceAll("<[^>]*>", "").replaceAll("\\s+", " ").trim();
    }

    private void uploadTtsAudioToOSS(String sessionId, byte[] audioData) {
        try {
            if (Objects.isNull(audioData) || audioData.length == 0) return;
            Path tempDir = Paths.get(System.getProperty("java.io.tmpdir"));
            String pcmFileName = "tts_common_" + sessionId + "_" + snowflake.nextId() + ".pcm";
            File pcmFile = tempDir.resolve(pcmFileName).toFile();
            Files.write(pcmFile.toPath(), audioData);
            String objectKey = "tts/common/" + pcmFileName;
            String ossUrl = ossUtil.uploadFile(objectKey, pcmFile);
            logger.info("TTS original audio stream uploaded to OSS, sessionId: {}, pcmFileSize: {} bytes, ossUrl: {}", sessionId, audioData.length, ossUrl);
            if (pcmFile.exists()) Files.delete(pcmFile.toPath());
        } catch (Exception e) {
            logger.error("Error uploading TTS original audio stream to OSS, sessionId: {}", sessionId, e);
        }
    }

    private void sendError(WebSocketSession session, String errorCode, String errorMessage, String stage) {
        ErrorResponse error = new ErrorResponse();
        error.setErrorCode(errorCode);
        error.setErrorMessage(errorMessage);
        error.setStage(stage);
        IMessageCustom.sendMessageCustom(session, IMessageEvent.BACK_ERROR, error);
    }

    /**
     * 传输层错误仅记录日志，由容器或后续关闭回调清理资源。
     *
     * @param session   当前会话
     * @param exception 底层异常
     */
    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        logger.error("WebSocket transport error, session id: {}", session.getId(), exception);
    }

    /**
     * 注销心跳、关闭豆包侧连接、停止队列消费并移除会话状态。
     *
     * @param session     已关闭会话
     * @param closeStatus 关闭原因
     */
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
        String sessionId = session.getId();
        heartbeatManager.unregisterSession(session);
        TtsSessionState state = ttsSessionStates.get(sessionId);
        if (state != null) {
            if (state.ttsInProgress && state.isLastRequest) {
                sendTtsFlag(session, FinishFlag.FINISH);
            }
            // 关闭豆包 socket，与当前 TTS WebSocket 保持同步
            if (state.doubaoClient != null) {
                if (state.doubaoSessionStarted && state.doubaoTtsSessionId != null) {
                    state.doubaoFinishRequested = true;
                    doubaoTtsService.requestSessionFinish(state.doubaoClient, state.doubaoTtsSessionId);
                }
                awaitDoubaoReceiverFinished(state, sessionId);
                doubaoTtsService.closeClient(state.doubaoClient);
                state.doubaoClient = null;
                state.doubaoVoiceId = null;
                clearDoubaoSessionState(state);
                state.doubaoRoundAudioStream.reset();
            }
        }
        ttsSessionStates.remove(sessionId);
        
        ttsQueueService.stopConsuming(sessionId);
        
        logger.info("TTS WebSocket disconnected, session id: {}", sessionId);
    }
}
