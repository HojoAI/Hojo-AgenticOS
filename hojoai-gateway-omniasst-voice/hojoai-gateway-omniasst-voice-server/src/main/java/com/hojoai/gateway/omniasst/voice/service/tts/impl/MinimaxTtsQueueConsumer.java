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
package com.hojoai.gateway.omniasst.voice.service.tts.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hojoai.gateway.omniasst.voice.constants.TtsPlatform;
import com.hojoai.gateway.omniasst.voice.service.tts.TtsQueueConsumer;
import com.hojoai.gateway.omniasst.voice.service.tts.TtsRequestQueueManager.TtsRequestWrapper;
import com.hojoai.gateway.omniasst.voice.websocket.IWebSocketHandler;
import com.hojoai.gateway.omniasst.voice.websocket.bean.IMessageCustom;
import com.hojoai.gateway.omniasst.voice.websocket.bean.IMessageEvent;
import com.hojoai.gateway.omniasst.voice.websocket.bean.dto.CommonTtsFlagResponse;
import com.hojoai.gateway.omniasst.voice.websocket.bean.dto.FinishFlag;
import com.hojoai.gateway.omniasst.voice.websocket.bean.dto.TtsWebSocketRequest;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.net.URI;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * MiniMax TTS 队列消费者：按 session 复用 WebSocket 长连接，拼接文本断句后 {@code task_continue} 合成并回推客户端。
 */
@Component
public class MinimaxTtsQueueConsumer implements TtsQueueConsumer {
    private static final Logger logger = LoggerFactory.getLogger(MinimaxTtsQueueConsumer.class);
    private static final String DEFAULT_MODEL = "speech-2.8-hd";
    private static final String DEFAULT_FORMAT = "pcm";
    private static final long CONNECTION_TIMEOUT_SECONDS = 10;
    private static final long TASK_START_TIMEOUT_SECONDS = 10;
    private static final long MESSAGE_TIMEOUT_SECONDS = 30;
    private static final String LANGUAGE_ENGLISH = "english";
    private static final String LANGUAGE_CHINESE = "chinese";
    private static final String ENGLISH_SECONDARY_VOICE_ID = "English_Insightful_Speaker";
    private static final int ENGLISH_SECONDARY_VOICE_WEIGHT = 10;
    private static final String CHINESE_SECONDARY_VOICE_ID = "Chinese (Mandarin)_Lyrical_Voice";
    private static final int CHINESE_SECONDARY_VOICE_WEIGHT = 10;
    
    @Value("${minimax.ai.api-key:}")
    private String apiKey;
    
    @Resource
    private ObjectMapper objectMapper;
    
    // 为每个 session 维护一个长连接
    private final ConcurrentHashMap<String, MinimaxTtsWebSocketClient> sessionConnections = new ConcurrentHashMap<>();
    // 为每个 session 维护一个接收任务标志，确保每个 session 只有一个接收任务在运行
    private final ConcurrentHashMap<String, AtomicBoolean> sessionReceivingFlags = new ConcurrentHashMap<>();
    // 为每个 session 维护请求的 isEnd 标志队列（用于发送 FINISH 标志）
    private final ConcurrentHashMap<String, BlockingQueue<Boolean>> sessionIsEndQueues = new ConcurrentHashMap<>();
    // 为每个 session 维护 TTS 状态
    private final ConcurrentHashMap<String, FinishFlag> sessionStates = new ConcurrentHashMap<>();
    // 为每个 session 维护文本缓冲区，用于拼接文本避免断句问题
    private final ConcurrentHashMap<String, StringBuilder> sessionTextBuffers = new ConcurrentHashMap<>();
    
    // 标点符号模式（中英文标点）
    private static final Pattern PUNCTUATION_PATTERN = Pattern.compile("[，。；！？,.;!?~：:、]");
    // 句子结束标点模式
    private static final Pattern SENTENCE_END_PATTERN = Pattern.compile("[。！？.!?]$");
    // 最小发送长度阈值
    private static final int MIN_SEND_LENGTH = 10;
    
    /**
     * 处理文本拼接逻辑，避免断句问题
     * @param sessionId session ID
     * @param text 新接收到的文本
     * @param isEnd 是否为最后一条消息
     * @return 返回要发送的文本，如果返回 null 或空字符串，表示需要继续等待
     */
    private String processTextConcatenation(String sessionId, String text, boolean isEnd) {
        StringBuilder buffer = sessionTextBuffers.computeIfAbsent(sessionId, k -> new StringBuilder());
        
        // 将新文本添加到缓冲区
        if (StringUtils.isNotBlank(text)) {
            buffer.append(text);
        }
        
        String fullText = buffer.toString();
        
        // 如果是最后一条消息，直接返回全部内容
        if (isEnd) {
            buffer.setLength(0);
            return fullText;
        }
        
        // 如果缓冲区为空，返回 null（不发送）
        if (StringUtils.isBlank(fullText)) {
            return null;
        }
        
        // 检查是否应该发送
        if (shouldSendText(fullText)) {
            // 如果文本以句子结束标点结尾，发送全部
            if (endsWithSentenceEndPunctuation(fullText)) {
                buffer.setLength(0);
                return fullText;
            } else {
                // 包含标点但不是以句子结束标点结尾，进行分割
                String[] splitResult = splitTextByPunctuation(fullText);
                if (splitResult != null && splitResult.length == 2) {
                    buffer.setLength(0);
                    buffer.append(splitResult[1]);
                    logger.debug("Session {}: Split text - send: {}, keep: {}", sessionId, splitResult[0], splitResult[1]);
                    return splitResult[0];
                } else {
                    // 分割失败，发送全部
                    buffer.setLength(0);
                    return fullText;
                }
            }
        }
        
        // 不需要发送，继续等待
        return null;
    }
    
    /**
     * 判断是否应该发送文本
     * @param text 文本内容
     * @return true 表示应该发送，false 表示继续等待
     */
    private boolean shouldSendText(String text) {
        if (StringUtils.isBlank(text)) {
            return false;
        }
        // 1. 如果长度超过阈值，直接发送
        if (text.length() >= MIN_SEND_LENGTH) {
            return true;
        }
        // 2. 如果包含标点符号，发送
        return PUNCTUATION_PATTERN.matcher(text).find();
    }
    
    /**
     * 检查文本是否以句子结束标点符号结尾
     */
    private boolean endsWithSentenceEndPunctuation(String text) {
        if (StringUtils.isBlank(text)) {
            return false;
        }
        return SENTENCE_END_PATTERN.matcher(text).find();
    }
    
    /**
     * 分割文本：如果包含标点但不是以句子结束标点结尾，返回最后一个标点符号之前的部分（包含标点）和剩余部分
     * @param text 原始文本
     * @return 数组，[0]是要发送的部分，[1]是要保留的部分。如果不需要分割，返回null
     */
    private String[] splitTextByPunctuation(String text) {
        if (StringUtils.isBlank(text)) {
            return null;
        }
        Matcher matcher = PUNCTUATION_PATTERN.matcher(text);
        
        int lastPunctuationIndex = -1;
        while (matcher.find()) {
            lastPunctuationIndex = matcher.end() - 1; // 获取最后一个标点符号的位置
        }
        
        // 如果找到了标点符号，且不是以标点符号结尾，进行分割
        if (lastPunctuationIndex >= 0 && lastPunctuationIndex < text.length() - 1) {
            String toSend = text.substring(0, lastPunctuationIndex + 1);
            String toKeep = text.substring(lastPunctuationIndex + 1);
            return new String[]{toSend, toKeep};
        }
        
        return null;
    }
    
    /**
     * 清理指定 session 的连接
     */
    public void removeConnection(String sessionId) {
        MinimaxTtsWebSocketClient client = sessionConnections.remove(sessionId);
        if (client != null) {
            try {
                if (client.isConnected()) {
                    client.sendTaskFinish();
                }
                client.close();
                logger.info("Closed and removed Minimax WebSocket connection for session: {}", sessionId);
            } catch (Exception e) {
                logger.error("Error closing Minimax WebSocket connection for session: {}", sessionId, e);
            }
        }
    }
    
    /**
     * 获取或创建 session 的连接，并在连接建立时发送 task_start 配置
     */
    private MinimaxTtsWebSocketClient getOrCreateConnection(String sessionId, String voiceId, String language) throws Exception {
        MinimaxTtsWebSocketClient client = sessionConnections.get(sessionId);
        
        // 如果连接不存在、已断开或 voiceId 变化，创建新连接
        boolean needNewConnection = client == null || !client.isConnected();
        if (!needNewConnection && client != null) {
            // 检查 voiceId 是否变化
            String currentVoiceId = client.getCurrentVoiceId();
            if (currentVoiceId == null || !currentVoiceId.equals(voiceId)) {
                logger.info("VoiceId changed from {} to {}, recreating connection for session: {}", 
                        currentVoiceId, voiceId, sessionId);
                needNewConnection = true;
            }
        }
        
        if (needNewConnection) {
            if (client != null) {
                // 移除旧的断开连接或配置不匹配的连接
                sessionConnections.remove(sessionId);
                try {
                    if (client.isConnected()) {
                        client.sendTaskFinish();
                    }
                    client.close();
                } catch (Exception e) {
                    logger.warn("Error closing old connection for session: {}", sessionId, e);
                }
            }
            
            // 创建新连接
            client = new MinimaxTtsWebSocketClient(apiKey);
            client.connect();
            
            // 等待连接成功
            if (!client.waitForConnection(CONNECTION_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                logger.error("Failed to establish Minimax WebSocket connection for session: {}", sessionId);
                throw new RuntimeException("Failed to establish Minimax WebSocket connection");
            }
            
            // 连接建立后，立即发送 task_start 作为配置
            client.sendTaskStart(voiceId, DEFAULT_MODEL, DEFAULT_FORMAT, language);
            
            // 等待 task_started
            if (!client.waitForTaskStart(TASK_START_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                logger.error("Failed to start Minimax TTS task during connection setup for session: {}", sessionId);
                try {
                    client.close();
                } catch (Exception e) {
                    logger.warn("Error closing failed connection", e);
                }
                throw new RuntimeException("Failed to start Minimax TTS task");
            }
            
            // 设置当前 voiceId
            client.setCurrentVoiceId(voiceId);
            
            sessionConnections.put(sessionId, client);
            logger.info("Created new Minimax WebSocket connection with voiceId {} for session: {}", voiceId, sessionId);
        }
        
        return client;
    }

    /**
     * {@inheritDoc}
     * <p>建立或复用 MiniMax WS，按请求推送文本片段与结束标志，并异步接收二进制音频帧写回 {@link WebSocketSession}。</p>
     */
    @Override
    public void consume(String sessionId, TtsRequestWrapper wrapper) {
        TtsWebSocketRequest request = wrapper.getRequest();
        WebSocketSession session = wrapper.getSession();
        
        if (!session.isOpen()) {
            logger.warn("WebSocket session is closed, skipping TTS request for session: {}", sessionId);
            return;
        }
        
        if (StringUtils.isBlank(apiKey)) {
            logger.error("Minimax API key is not configured");
            return;
        }
        
        String text = request.getText();
        boolean isEnd = request.getIsEnd() != null && request.getIsEnd() == 1;
        String voiceId = StringUtils.isNotBlank(request.getVoiceId()) ? request.getVoiceId() : null;
        String language = StringUtils.isNotBlank(request.getLanguage()) ? request.getLanguage() : LANGUAGE_ENGLISH;
        
        if (voiceId == null) {
            logger.warn("VoiceId is not provided for Minimax TTS, session: {}", sessionId);
            return;
        }
        
        // 使用文本拼接逻辑处理文本，避免断句问题
        String textToSend = processTextConcatenation(sessionId, text != null ? text : "", isEnd);
        
        // 如果文本为空且不是最后一条消息，继续等待
        if (StringUtils.isBlank(textToSend) && !isEnd) {
            logger.debug("Session {}: Text buffered, waiting for more content. Current buffer: {}", 
                    sessionId, sessionTextBuffers.get(sessionId));
            return;
        }
        
        // 如果文本为空但是最后一条消息，也需要发送（可能是空消息）
        if (StringUtils.isBlank(textToSend) && isEnd) {
            textToSend = "";
        }
        
        MinimaxTtsWebSocketClient client = null;
        try {
            // 获取或创建长连接（连接建立时会自动发送 task_start 配置）
            client = getOrCreateConnection(sessionId, voiceId, language);
            
            // 初始化 session 状态（如果不存在）
            sessionStates.computeIfAbsent(sessionId, k -> FinishFlag.START);
            
            // 发送处理后的文本（task_start 已在连接建立时发送）
            // 只有当文本不为空时才发送，如果 isEnd=true 但文本为空，则不发送
            if (StringUtils.isNotBlank(textToSend)) {
                logger.debug("Session {}: Sending text: {}", sessionId, textToSend);
                
                // 只有当实际发送了文本时，才将 isEnd 标志加入队列
                // 因为只有发送了文本，才会收到 minimax 的响应，才能处理 isEnd 标志
                BlockingQueue<Boolean> isEndQueue = sessionIsEndQueues.computeIfAbsent(sessionId, k -> new LinkedBlockingQueue<>());
                try {
                    isEndQueue.put(isEnd);
                    logger.debug("Added isEnd={} to queue for session: {}", isEnd, sessionId);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    logger.error("Failed to add isEnd to queue for session: {}", sessionId, e);
                }
                client.sendTaskContinue(textToSend);
            } else if (isEnd) {
                // 如果 isEnd=true 但文本为空，不发送任何内容
                // 因为可能还在等待之前发送的文本的响应，响应回来后会自动处理 isEnd 标志
                logger.debug("Session {}: isEnd=true but text is empty, waiting for previous responses", sessionId);
            }
            
            // 异步接收音频数据，避免阻塞队列消费线程
            // 每个 session 只启动一个持续运行的接收任务
            AtomicBoolean receivingFlag = sessionReceivingFlags.computeIfAbsent(sessionId, k -> new AtomicBoolean(false));
            
            // 如果还没有接收任务在运行，启动一个持续运行的接收任务
            if (receivingFlag.compareAndSet(false, true)) {
                MinimaxTtsWebSocketClient finalClient = client;
                CompletableFuture.runAsync(() -> {
                    receiveAudioDataAsync(finalClient, session, sessionId, receivingFlag);
                });
            }
            
        } catch (Exception e) {
            logger.error("Error processing Minimax TTS request for session: {}", sessionId, e);
            // 如果连接出错，移除连接以便下次重连
            if (client != null) {
                sessionConnections.remove(sessionId);
            }
            AtomicBoolean receivingFlag = sessionReceivingFlags.get(sessionId);
            if (receivingFlag != null) {
                receivingFlag.set(false);
            }
            if (isEnd) {
                sendTtsFlag(session, FinishFlag.FINISH);
            }
        }
        // 注意：不再在 finally 中关闭连接，保持长连接打开
    }
    
    /**
     * 异步接收音频数据（持续运行，处理所有请求的响应）
     */
    private void receiveAudioDataAsync(MinimaxTtsWebSocketClient client, WebSocketSession session, 
                                      String sessionId, AtomicBoolean receivingFlag) {
        int requestCounter = 0;
        int chunkCounter = 0;
        
        try {
            logger.info("Started continuous audio receiving task for session: {}", sessionId);
            
            while (true) {
                if (!session.isOpen()) {
                    logger.warn("WebSocket session closed, stopping Minimax TTS stream, session: {}", sessionId);
                    break;
                }
                
                if (!client.isConnected()) {
                    logger.warn("Minimax WebSocket connection closed, stopping receiving task for session: {}", sessionId);
                    break;
                }
                
                JsonNode response = client.receiveMessage(MESSAGE_TIMEOUT_SECONDS, TimeUnit.SECONDS);
                if (response == null) {
                    // 超时继续等待，不退出
                    logger.debug("Timeout waiting for Minimax TTS response, continuing...");
                    continue;
                }
                
                // 检查是否有音频数据
                if (response.has("data") && response.get("data").has("audio")) {
                    JsonNode audioNode = response.get("data").get("audio");
                    if (audioNode != null && !audioNode.isNull()) {
                        String audioHex = audioNode.asText();
                        if (StringUtils.isNotBlank(audioHex)) {
                            try {
                                // 将 hex 字符串转换为字节数组
                                byte[] audioBytes = hexStringToByteArray(audioHex);
                                if (audioBytes.length > 0) {
                                    // 检查状态，如果是 START（接收到第一帧），发送 START 标志并设置为 PROCESSING
                                    FinishFlag state = sessionStates.get(sessionId);
                                    if (state == FinishFlag.START) {
                                        sendTtsFlag(session, FinishFlag.START);
                                        sessionStates.put(sessionId, FinishFlag.PROCESSING);
                                        logger.debug("Received first audio chunk, sent START flag and set state to PROCESSING for session: {}", sessionId);
                                    }
                                    
                                    chunkCounter++;
                                    logger.debug("Received audio chunk #{} for session: {}, size: {}", 
                                            chunkCounter, sessionId, audioBytes.length);
                                    
                                    // 发送音频数据到客户端
                                    ByteBuffer byteBuffer = ByteBuffer.wrap(audioBytes);
                                    IWebSocketHandler.sendBinary(session, byteBuffer);
                                }
                            } catch (Exception e) {
                                logger.error("Error processing audio chunk for session: {}", sessionId, e);
                            }
                        }
                    }
                }
                
                // 检查是否完成（一个请求的响应完成）
                if (response.has("is_final") && response.get("is_final").asBoolean()) {
                    logger.info("minimax tts is final response: {}", response);
                    requestCounter++;
                    logger.info("Minimax TTS request #{} completed for session: {}, total chunks: {}", 
                            requestCounter, sessionId, chunkCounter);
                    
                    // 从队列中取出当前请求的 isEnd 标志
                    BlockingQueue<Boolean> isEndQueue = sessionIsEndQueues.get(sessionId);
                    Boolean isEnd = null;
                    if (isEndQueue != null) {
                        try {
                            // 设置超时，避免无限等待
                            isEnd = isEndQueue.poll(1, TimeUnit.SECONDS);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            logger.error("Failed to poll isEnd from queue for session: {}", sessionId, e);
                        }
                    }
                    
                    if (Boolean.TRUE.equals(isEnd)) {
                        // 发送 FINISH 标志
                        sendTtsFlag(session, FinishFlag.FINISH);
                        // 重置状态为 START，准备接收下一个请求
                        sessionStates.put(sessionId, FinishFlag.START);
                        logger.info("Sent FINISH flag and reset state to START for session: {}", sessionId);
                    } else if (isEnd != null) {
                        // isEnd 为 false，不发送 FINISH，但状态保持 PROCESSING
                        logger.debug("Request completed but isEnd=false, not sending FINISH for session: {}", sessionId);
                    } else {
                        logger.warn("No isEnd value found in queue for session: {}, response: {}", sessionId, response);
                    }
                    // 注意：接收任务继续运行，等待下一个请求的响应
                }
            }
            
        } catch (Exception e) {
            logger.error("Error receiving audio data for session: {}", sessionId, e);
        } finally {
            // 重置接收标志
            receivingFlag.set(false);
            sessionIsEndQueues.remove(sessionId);
            logger.info("Stopped continuous audio receiving task for session: {}", sessionId);
        }
    }
    
    private void sendTtsFlag(WebSocketSession session, FinishFlag flag) {
        try {
            logger.info("send tts flag: {}", flag.getValue());
            CommonTtsFlagResponse response = new CommonTtsFlagResponse();
            response.setFinishFlag(flag.getValue());
            IMessageCustom.sendMessageCustom(session, IMessageEvent.BACK_COMMON_TTS_FLAG, response);
        } catch (Exception e) {
            logger.error("Failed to send TTS flag, session: {}, flag: {}", session.getId(), flag.getValue(), e);
        }
    }
    
    private byte[] hexStringToByteArray(String hex) {
        int len = hex.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                    + Character.digit(hex.charAt(i + 1), 16));
        }
        return data;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer getPlatform() {
        return TtsPlatform.MINIMAX.getCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void cleanup(String sessionId) {
        removeConnection(sessionId);
        sessionReceivingFlags.remove(sessionId);
        sessionIsEndQueues.remove(sessionId);
        sessionStates.remove(sessionId);
        sessionTextBuffers.remove(sessionId);
    }
    
    /**
     * Minimax WebSocket 客户端内部类
     */
    private class MinimaxTtsWebSocketClient extends WebSocketClient {
        private static final String WS_URL = "wss://api.minimaxi.com/ws/v1/t2a_v2";
        private static final String DEFAULT_MODEL = "speech-2.8-turbo";
        private static final String DEFAULT_FORMAT = "pcm";
        
        private final BlockingQueue<JsonNode> messageQueue = new LinkedBlockingQueue<>();
        private final CompletableFuture<Boolean> connectionFuture = new CompletableFuture<>();
        private CompletableFuture<Boolean> taskStartFuture = new CompletableFuture<>();
        private boolean connected = false;
        private String currentVoiceId; // 当前配置的 voiceId
        
        public MinimaxTtsWebSocketClient(String apiKey) throws Exception {
            super(URI.create(WS_URL), createHeaders(apiKey));
            
            // 创建不验证证书的 SSLContext
            SSLContext sslContext = SSLContext.getInstance("TLS");
            TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    public java.security.cert.X509Certificate[] getAcceptedIssuers() { return null; }
                    public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) { }
                    public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) { }
                }
            };
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
            this.setSocketFactory(sslContext.getSocketFactory());
        }
        
        private static Map<String, String> createHeaders(String apiKey) {
            Map<String, String> headers = new HashMap<>();
            headers.put("Authorization", "Bearer " + apiKey);
            return headers;
        }
        
        @Override
        public void onOpen(ServerHandshake handshakedata) {
            logger.info("Minimax WebSocket connection opened");
        }
        
        @Override
        public void onMessage(String message) {
            try {
                JsonNode jsonNode = objectMapper.readTree(message);
                String event = jsonNode.has("event") ? jsonNode.get("event").asText() : null;
                
                logger.debug("Received message: {}", message);
                
                if ("connected_success".equals(event)) {
                    connected = true;
                    connectionFuture.complete(true);
                    logger.info("Minimax WebSocket connected successfully");
                } else if ("task_started".equals(event)) {
                    taskStartFuture.complete(true);
                    logger.info("Minimax TTS task started");
                } else {
                    messageQueue.offer(jsonNode);
                }
            } catch (Exception e) {
                logger.error("Error parsing WebSocket message", e);
            }
        }
        
        @Override
        public void onMessage(ByteBuffer bytes) {
            logger.warn("Received unexpected binary message");
        }
        
        @Override
        public void onClose(int code, String reason, boolean remote) {
            logger.info("Minimax WebSocket connection closed: code={}, reason={}, remote={}", code, reason, remote);
            connected = false;
            if (!connectionFuture.isDone()) {
                connectionFuture.complete(false);
            }
            if (!taskStartFuture.isDone()) {
                taskStartFuture.complete(false);
            }
            // 重置 taskStartFuture 以便下次使用
            taskStartFuture = new CompletableFuture<>();
        }
        
        @Override
        public void onError(Exception ex) {
            logger.error("Minimax WebSocket error", ex);
            if (!connectionFuture.isDone()) {
                connectionFuture.completeExceptionally(ex);
            }
            if (!taskStartFuture.isDone()) {
                taskStartFuture.completeExceptionally(ex);
            }
            // 重置 taskStartFuture 以便下次使用
            taskStartFuture = new CompletableFuture<>();
        }
        
        public boolean waitForConnection(long timeout, TimeUnit unit) throws Exception {
            return connectionFuture.get(timeout, unit);
        }
        
        public boolean waitForTaskStart(long timeout, TimeUnit unit) throws Exception {
            return taskStartFuture.get(timeout, unit);
        }
        
        public void sendTaskStart(String voiceId, String model, String format, String language) throws Exception {
            // 重置 taskStartFuture，以便等待新的 task_started 事件
            taskStartFuture = new CompletableFuture<>();
            Map<String, Object> taskStartMsg = new HashMap<>();
            taskStartMsg.put("event", "task_start");
            taskStartMsg.put("model", model != null ? model : DEFAULT_MODEL);
            
            Map<String, Object> voiceSetting = new HashMap<>();
            voiceSetting.put("voice_id", voiceId);
            voiceSetting.put("speed", 1);
            voiceSetting.put("vol", 1);
            voiceSetting.put("pitch", 0);
            voiceSetting.put("english_normalization", false);
            taskStartMsg.put("voice_setting", voiceSetting);
            
            Map<String, Object> audioSetting = new HashMap<>();
            audioSetting.put("sample_rate", 24000);
            audioSetting.put("format", format != null ? format : DEFAULT_FORMAT);
            audioSetting.put("channel", 1);
            taskStartMsg.put("audio_setting", audioSetting);
            
            taskStartMsg.put("continuous_sound", true);
            
            // 添加 timbre_weights 参数，根据 language 动态设置
            // 根据 language 确定 secondary voice_id 和 weight
            String secondaryVoiceId = null;
            int secondaryWeight = 0;
            
            if (StringUtils.isBlank(language)) {
                secondaryWeight = 0;
            } else if (LANGUAGE_ENGLISH.equalsIgnoreCase(language)) {
                secondaryVoiceId = ENGLISH_SECONDARY_VOICE_ID;
                secondaryWeight = ENGLISH_SECONDARY_VOICE_WEIGHT;
            } else if (LANGUAGE_CHINESE.equalsIgnoreCase(language)) {
                secondaryVoiceId = CHINESE_SECONDARY_VOICE_ID;
                secondaryWeight = CHINESE_SECONDARY_VOICE_WEIGHT;
            } else {
                throw new IllegalArgumentException("Unsupported language: " + language + ". Supported languages: " + LANGUAGE_ENGLISH + ", " + LANGUAGE_CHINESE);
            }
            
            if (secondaryWeight > 0) {
                List<Map<String, Object>> timbreWeights = new ArrayList<>();
                
                // 主 voiceId 的权重 = 100 - secondary weight
                Map<String, Object> weight1 = new HashMap<>();
                weight1.put("voice_id", voiceId);
                weight1.put("weight", 100 - secondaryWeight);
                timbreWeights.add(weight1);
                
                Map<String, Object> weight2 = new HashMap<>();
                weight2.put("voice_id", secondaryVoiceId);
                weight2.put("weight", secondaryWeight);
                timbreWeights.add(weight2);
                taskStartMsg.put("timbre_weights", timbreWeights);
            }
            
            String json = objectMapper.writeValueAsString(taskStartMsg);
            send(json);
            logger.info("Sent task_start message: {}", json);
        }
        
        public void sendTaskContinue(String text) throws Exception {
            Map<String, Object> continueMsg = new HashMap<>();
            continueMsg.put("event", "task_continue");
            continueMsg.put("text", text);
            
            String json = objectMapper.writeValueAsString(continueMsg);
            send(json);
            logger.info("Sent task_continue message with text json: {}", json);
        }
        
        public void sendTaskFinish() throws Exception {
            Map<String, Object> finishMsg = new HashMap<>();
            finishMsg.put("event", "task_finish");
            
            String json = objectMapper.writeValueAsString(finishMsg);
            send(json);
            logger.info("Sent task_finish message");
        }
        
        public JsonNode receiveMessage(long timeout, TimeUnit unit) throws InterruptedException {
            JsonNode message = messageQueue.poll(timeout, unit);
            return message;
        }
        
        public boolean isConnected() {
            return connected && isOpen();
        }
        
        public String getCurrentVoiceId() {
            return currentVoiceId;
        }
        
        public void setCurrentVoiceId(String voiceId) {
            this.currentVoiceId = voiceId;
        }
    }
}
