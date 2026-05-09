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
package com.hojoai.webmvc.omniasst.meeting.service.v1.meeting;

import com.hojoai.common.jackson.JacksonUtil;
import com.hojoai.webmvc.omniasst.meeting.websocket.bean.IMessage;
import com.hojoai.webmvc.omniasst.meeting.websocket.bean.IMessageCustom;
import com.hojoai.webmvc.omniasst.meeting.websocket.v1.AsrWebSocketEvents;
import com.hojoai.webmvc.omniasst.meeting.websocket.v1.dto.AsrConfigRequest;
import com.hojoai.webmvc.omniasst.meeting.websocket.v1.dto.AsrConfigResponse;
import com.hojoai.webmvc.omniasst.meeting.websocket.v1.dto.AsrWebSocketMessage;
import com.hojoai.webmvc.omniasst.meeting.websocket.v1.dto.AsrWebSocketRequest;
import com.hojoai.webmvc.omniasst.meeting.websocket.v1.dto.AsrWebSocketResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class AsrWebSocketClient {

    private static final Logger logger = LoggerFactory.getLogger(AsrWebSocketClient.class);
    private static final long HEARTBEAT_INTERVAL_SECONDS = 10;

    private final String meetingId;
    private final String asrWebSocketUrl;
    private WebSocketSession session;
    private Consumer<AsrWebSocketResponse> responseCallback;
    private volatile boolean configSent = false;
    
    // 心跳相关
    private ScheduledExecutorService heartbeatScheduler;
    private ScheduledFuture<?> heartbeatTask;

    public AsrWebSocketClient(String meetingId, String asrWebSocketUrl) {
        this.meetingId = meetingId;
        this.asrWebSocketUrl = asrWebSocketUrl;
    }

    /**
     * 连接到ASR服务
     */
    public CompletableFuture<Boolean> connect() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                if (session != null && session.isOpen()) {
                    logger.warn("[ASR连接-已存在] meetingId: {}", meetingId);
                    return true;
                }

                logger.info("[ASR连接-开始] meetingId: {}, url: {}", meetingId, asrWebSocketUrl);

                StandardWebSocketClient client = new StandardWebSocketClient();
                session = client.execute(
                    new AsrWebSocketHandler(),
                    null,
                    new URI(asrWebSocketUrl)
                ).get();

                logger.info("[ASR连接-成功] meetingId: {}, sessionId: {}", meetingId, session.getId());
                return true;
            } catch (Exception e) {
                logger.error("[ASR连接-失败] meetingId: {}", meetingId, e);
                return false;
            }
        });
    }

    /**
     * 发送心跳到ASR服务
     */
    private void sendHeartbeat() {
        try {
            if (session == null || !session.isOpen()) {
                logger.warn("[ASR心跳-跳过] meetingId: {}, 连接已关闭", meetingId);
                return;
            }
            
            Map<String, Object> heartbeatData = new HashMap<>();
            heartbeatData.put("timestamp", System.currentTimeMillis());
            
            boolean sent = sendMessage(AsrWebSocketEvents.REQ_HEARTBEAT, heartbeatData);
            if (sent) {
                logger.debug("[ASR心跳-发送] meetingId: {}, timestamp: {}", meetingId, heartbeatData.get("timestamp"));
            }
        } catch (Exception e) {
            logger.error("[ASR心跳-异常] meetingId: {}", meetingId, e);
        }
    }

    /**
     * 启动心跳任务
     */
    private void startHeartbeat() {
        if (heartbeatScheduler == null || heartbeatScheduler.isShutdown()) {
            heartbeatScheduler = Executors.newSingleThreadScheduledExecutor(r -> {
                Thread thread = new Thread(r);
                thread.setName("ASR-Heartbeat-" + meetingId);
                thread.setDaemon(true);
                return thread;
            });
        }
        
        heartbeatTask = heartbeatScheduler.scheduleAtFixedRate(
            this::sendHeartbeat,
            HEARTBEAT_INTERVAL_SECONDS,  // 初始延迟
            HEARTBEAT_INTERVAL_SECONDS,  // 间隔时间
            TimeUnit.SECONDS
        );
        
        logger.info("[ASR心跳-启动] meetingId: {}, 间隔: {}秒", meetingId, HEARTBEAT_INTERVAL_SECONDS);
    }

    /**
     * 停止心跳任务
     */
    private void stopHeartbeat() {
        if (heartbeatTask != null && !heartbeatTask.isCancelled()) {
            heartbeatTask.cancel(false);
            heartbeatTask = null;
            logger.info("[ASR心跳-停止] meetingId: {}", meetingId);
        }
        
        if (heartbeatScheduler != null && !heartbeatScheduler.isShutdown()) {
            heartbeatScheduler.shutdown();
            try {
                if (!heartbeatScheduler.awaitTermination(1, TimeUnit.SECONDS)) {
                    heartbeatScheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                heartbeatScheduler.shutdownNow();
                Thread.currentThread().interrupt();
            }
            heartbeatScheduler = null;
        }
    }

    /**
     * 发送消息到ASR服务
     */
    private boolean sendMessage(String event, Object data) {
        try {
            if (session == null || !session.isOpen()) {
                logger.error("[ASR消息发送-失败] meetingId: {}, event: {}, 连接未建立或已关闭", meetingId, event);
                return false;
            }

            AsrWebSocketMessage<?> message = AsrWebSocketMessage.create(event, data);
            String jsonData = JacksonUtil.bean2Json(message);
            session.sendMessage(new TextMessage(jsonData));
            
            logger.debug("[ASR消息发送-成功] meetingId: {}, event: {}, dataSize: {} bytes", 
                    meetingId, event, jsonData.length());
            return true;
        } catch (Exception e) {
            logger.error("[ASR消息发送-异常] meetingId: {}, event: {}", meetingId, event, e);
            return false;
        }
    }

    /**
     * 发送配置到ASR服务（连接后第一步）
     */
    public boolean sendConfig(AsrConfigRequest config) {
        try {
            boolean sent = sendMessage(AsrWebSocketEvents.REQ_ASR_CONFIG, config);
            if (sent) {
                configSent = true;
                logger.info("[ASR配置-成功] meetingId: {}, config: platform={}, language={}, type={}", 
                        meetingId, config.getPlatform(), config.getLanguage(), config.getType());
            }
            return sent;
        } catch (Exception e) {
            logger.error("[ASR配置-异常] meetingId: {}", meetingId, e);
            return false;
        }
    }

    /**
     * 发送音频数据到ASR服务
     */
    public boolean sendAudioData(AsrWebSocketRequest request) {
        try {
            if (!configSent) {
                logger.error("[ASR发送-失败] meetingId: {}, 配置未发送，请先调用sendConfig()", meetingId);
                return false;
            }

            return sendMessage(AsrWebSocketEvents.REQ_ASR_AUDIO, request);
        } catch (Exception e) {
            logger.error("[ASR发送-异常] meetingId: {}", meetingId, e);
            return false;
        }
    }

    /**
     * 设置响应回调处理器
     */
    public void setResponseCallback(Consumer<AsrWebSocketResponse> callback) {
        this.responseCallback = callback;
        logger.info("[ASR回调-设置] meetingId: {}", meetingId);
    }

    /**
     * 断开与ASR服务的连接
     */
    public void disconnect() {
        try {
            // 先停止心跳
            stopHeartbeat();
            
            if (session != null && session.isOpen()) {
                session.close(CloseStatus.NORMAL);
                logger.info("[ASR断开-成功] meetingId: {}", meetingId);
            } else {
                logger.warn("[ASR断开-跳过] meetingId: {}, 连接已关闭或不存在", meetingId);
            }
        } catch (Exception e) {
            logger.error("[ASR断开-异常] meetingId: {}", meetingId, e);
        } finally {
            session = null;
            responseCallback = null;
            configSent = false;
        }
    }

    /**
     * 检查连接状态
     */
    public boolean isConnected() {
        return session != null && session.isOpen();
    }

    /**
     * ASR WebSocket 处理器
     */
    private class AsrWebSocketHandler extends TextWebSocketHandler {

        @Override
        public void afterConnectionEstablished(WebSocketSession session) throws Exception {
            logger.info("[ASR连接建立] meetingId: {}, sessionId: {}", meetingId, session.getId());
            // 连接建立后启动心跳
            startHeartbeat();
        }

        @Override
        protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
            try {
                String payload = message.getPayload();
                logger.info("[ASR接收消息] meetingId: {}, payload: {}", meetingId, payload);

                // 先解析为 IMessage 获取 event
                IMessage<?> iMessage = JacksonUtil.json2Bean(payload, IMessage.class);
                String event = iMessage.getEventType();
                
                // 处理系统事件（心跳、配置等）
                if (handleSystemEvent(event, payload)) {
                    return;
                }
                
                // 处理ASR识别响应 - 使用 IMessageCustom.parseBusinessRequest 从 payload.pdata 中提取
                AsrWebSocketResponse response = IMessageCustom.parseBusinessRequest(payload, AsrWebSocketResponse.class);
                if (response == null) {
                    logger.warn("[ASR响应-内容为空] meetingId: {}", meetingId);
                    return;
                }
                
                // 调用回调处理响应
                if (responseCallback != null) {
                    responseCallback.accept(response);
                } else {
                    logger.warn("[ASR响应-无回调] meetingId: {}", meetingId);
                }
            } catch (Exception e) {
                logger.error("[ASR处理消息-异常] meetingId: {}", meetingId, e);
            }
        }

        /**
         * 处理系统事件（心跳、配置等）
         * @return true表示已处理，false表示不是系统事件
         */
        private boolean handleSystemEvent(String event, String payload) {
            if (event == null) {
                return false;
            }
            
            switch (event) {
                case AsrWebSocketEvents.BACK_HEARTBEAT:
                    handleHeartbeatResponse(payload);
                    return true;
                    
                case AsrWebSocketEvents.BACK_ASR_CONFIG_ACK:
                    handleConfigResponse(payload);
                    return true;
                    
                default:
                    return false;
            }
        }

        /**
         * 处理心跳响应
         */
        private void handleHeartbeatResponse(String payload) {
            try {
                logger.debug("[ASR心跳-响应] meetingId: {}", meetingId);
            } catch (Exception e) {
                logger.error("[ASR心跳响应-处理异常] meetingId: {}", meetingId, e);
            }
        }

        /**
         * 处理配置响应
         */
        private void handleConfigResponse(String payload) {
            try {
                AsrWebSocketMessage<?> message = JacksonUtil.json2Bean(payload, AsrWebSocketMessage.class);
                Object pdataObj = message.getPayload().getPdata();
                
                if (pdataObj != null) {
                    String pdataJson = JacksonUtil.bean2Json(pdataObj);
                    AsrConfigResponse configResponse = JacksonUtil.json2Bean(pdataJson, AsrConfigResponse.class);
                    
                    if ("success".equalsIgnoreCase(configResponse.getStatus())) {
                        logger.info("[ASR配置-确认成功] meetingId: {}, message: {}, platform: {}, language: {}, type: {}", 
                                meetingId, configResponse.getMessage(), configResponse.getPlatform(), 
                                configResponse.getLanguage(), configResponse.getType());
                    } else {
                        logger.warn("[ASR配置-确认失败] meetingId: {}, status: {}, message: {}", 
                                meetingId, configResponse.getStatus(), configResponse.getMessage());
                    }
                } else {
                    logger.warn("[ASR配置-响应无数据] meetingId: {}", meetingId);
                }
            } catch (Exception e) {
                logger.error("[ASR配置响应-处理异常] meetingId: {}", meetingId, e);
            }
        }

        @Override
        public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
            logger.error("[ASR传输错误] meetingId: {}", meetingId, exception);
        }

        @Override
        public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
            logger.info("[ASR连接关闭] meetingId: {}, status: {}", meetingId, status);
            // 连接关闭时停止心跳
            stopHeartbeat();
            AsrWebSocketClient.this.session = null;
        }
    }
}
