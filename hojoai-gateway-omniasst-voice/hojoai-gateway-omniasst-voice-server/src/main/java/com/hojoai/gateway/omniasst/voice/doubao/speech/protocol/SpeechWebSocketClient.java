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
package com.hojoai.gateway.omniasst.voice.doubao.speech.protocol;

import lombok.extern.slf4j.Slf4j;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * 豆包语音开放协议 WebSocket 客户端：将二进制帧解析为 {@link Message} 入队，并提供发送连接/会话/任务等标准事件。
 */
@Slf4j
public class SpeechWebSocketClient extends WebSocketClient {
    private final BlockingQueue<Message> messageQueue = new LinkedBlockingQueue<>();

    /**
     * @param serverUri 如双向 TTS wss 地址
     * @param headers   鉴权等请求头（X-Api-*）
     */
    public SpeechWebSocketClient(URI serverUri, Map<String, String> headers) {
        super(serverUri, headers);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onOpen(ServerHandshake handshakedata) {
        log.info("WebSocket connection established, Logid: {}", handshakedata.getFieldValue("x-tt-logid"));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onMessage(String message) {
        log.warn("Received unexpected text message: {}", message);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onMessage(ByteBuffer bytes) {
        try {
            Message message = Message.unmarshal(bytes.array());
            messageQueue.put(message);
        } catch (Exception e) {
            log.error("Failed to parse message", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onClose(int code, String reason, boolean remote) {
        log.info("WebSocket connection closed: code={}, reason={}, remote={}", code, reason, remote);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onError(Exception ex) {
        log.error("WebSocket error", ex);
    }

    /**
     * 发送连接级 {@link EventType#START_CONNECTION}。
     */
    public void sendStartConnection() throws Exception {
        Message message = new Message(MsgType.FULL_CLIENT_REQUEST, MsgTypeFlagBits.WITH_EVENT);
        message.setEvent(EventType.START_CONNECTION);
        message.setPayload("{}".getBytes());
        sendMessage(message);
    }

    /**
     * 发送 {@link EventType#FINISH_CONNECTION}，准备关闭 WebSocket。
     */
    public void sendFinishConnection() throws Exception {
        Message message = new Message(MsgType.FULL_CLIENT_REQUEST, MsgTypeFlagBits.WITH_EVENT);
        message.setEvent(EventType.FINISH_CONNECTION);
        sendMessage(message);
    }

    /**
     * 发送 {@link EventType#START_SESSION} 及业务 JSON payload。
     *
     * @param payload   序列化后的会话参数
     * @param sessionId 会话 UUID
     */
    public void sendStartSession(byte[] payload, String sessionId) throws Exception {
        Message message = new Message(MsgType.FULL_CLIENT_REQUEST, MsgTypeFlagBits.WITH_EVENT);
        message.setEvent(EventType.START_SESSION);
        message.setSessionId(sessionId);
        message.setPayload(payload);
        sendMessage(message);
    }

    /**
     * 发送 {@link EventType#FINISH_SESSION}，结束当前合成会话。
     *
     * @param sessionId 会话 ID
     */
    public void sendFinishSession(String sessionId) throws Exception {
        Message message = new Message(MsgType.FULL_CLIENT_REQUEST, MsgTypeFlagBits.WITH_EVENT);
        message.setEvent(EventType.FINISH_SESSION);
        message.setSessionId(sessionId);
        message.setPayload("{}".getBytes());
        sendMessage(message);
    }

    /**
     * 发送 {@link EventType#TASK_REQUEST}（如 TTS 单字/片段文本）。
     *
     * @param payload   任务 JSON
     * @param sessionId 会话 ID
     */
    public void sendTaskRequest(byte[] payload, String sessionId) throws Exception {
        Message message = new Message(MsgType.FULL_CLIENT_REQUEST, MsgTypeFlagBits.WITH_EVENT);
        message.setEvent(EventType.TASK_REQUEST);
        message.setSessionId(sessionId);
        message.setPayload(payload);
        sendMessage(message);
    }

    /**
     * 发送无事件位的全量客户端请求（自定义 payload）。
     *
     * @param payload 原始负载
     */
    public void sendFullClientMessage(byte[] payload) throws Exception {
        Message message = new Message(MsgType.FULL_CLIENT_REQUEST, MsgTypeFlagBits.NO_SEQ);
        message.setPayload(payload);
        sendMessage(message);
    }

    /**
     * 将 {@link Message} 编码后经 WebSocket 发送。
     *
     * @param message 已填充头部的协议消息
     */
    public void sendMessage(Message message) throws Exception {
        log.info("Send: {}", message);
        send(message.marshal());
    }

    /**
     * 阻塞直到收到下一条服务端 {@link Message}。
     *
     * @return 解析后的消息
     * @throws InterruptedException 等待被中断
     */
    public Message receiveMessage() throws InterruptedException {
        Message message = messageQueue.take();
        log.info("Receive: {}", message);
        return message;
    }

    /**
     * 带超时的接收，避免服务端未发送 SESSION_FINISHED 时永久阻塞。
     *
     * @param timeout 超时时间
     * @param unit    时间单位
     * @return 消息，超时返回 null
     */
    public Message receiveMessage(long timeout, TimeUnit unit) throws InterruptedException {
        Message message = messageQueue.poll(timeout, unit);
        if (message != null) {
            log.info("Receive: {}", message);
        }
        return message;
    }

    /**
     * 循环收包直至匹配指定类型与事件，否则抛出运行时异常（用于严格握手序）。
     *
     * @param type  期望 {@link MsgType}
     * @param event 期望 {@link EventType}
     * @return 匹配到的消息
     * @throws InterruptedException  等待被中断
     * @throws RuntimeException      收到非预期消息
     */
    public Message waitForMessage(MsgType type, EventType event) throws InterruptedException {
        while (true) {
            Message message = receiveMessage();
            if (message.getType() == type && message.getEvent() == event) {
                return message;
            } else {
                throw new RuntimeException("Unexpected message: " + message);
            }
        }
    }
}