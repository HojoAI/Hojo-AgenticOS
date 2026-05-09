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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hojoai.gateway.omniasst.voice.doubao.speech.protocol.EventType;
import com.hojoai.gateway.omniasst.voice.doubao.speech.protocol.Message;
import com.hojoai.gateway.omniasst.voice.doubao.speech.protocol.MsgType;
import com.hojoai.gateway.omniasst.voice.doubao.speech.protocol.SpeechWebSocketClient;
import com.hojoai.gateway.omniasst.voice.service.tts.DoubaoTtsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static com.hojoai.gateway.omniasst.voice.doubao.speech.protocol.EventType.CONNECTION_STARTED;
import static com.hojoai.gateway.omniasst.voice.doubao.speech.protocol.EventType.SESSION_FINISHED;
import static com.hojoai.gateway.omniasst.voice.doubao.speech.protocol.EventType.SESSION_STARTED;
import static com.hojoai.gateway.omniasst.voice.doubao.speech.protocol.EventType.TTS_ENDED;
import static com.hojoai.gateway.omniasst.voice.doubao.speech.protocol.EventType.TTS_SENTENCE_END;
import static com.hojoai.gateway.omniasst.voice.doubao.speech.protocol.MsgType.FULL_SERVER_RESPONSE;

/**
 * 豆包 TTS 服务
 */
@Service
public class DoubaoTtsServiceImpl implements DoubaoTtsService {
    private static final Logger logger = LoggerFactory.getLogger(DoubaoTtsServiceImpl.class);
    /** 豆包双向流式 TTS WebSocket 接入地址 */
    private static final String ENDPOINT = "wss://openspeech.bytedance.com/api/v3/tts/bidirection";
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final long SESSION_DRAIN_TIMEOUT_SECONDS = 30L;
    private static final long SINGLE_UTTERANCE_RECEIVE_TIMEOUT_SECONDS = 30L;
    private final ConcurrentHashMap<String, Map<String, Object>> reqParamsTemplateCache = new ConcurrentHashMap<>();

    @Value("${doubao.tts.app-id:}")
    private String appId;

    @Value("${doubao.tts.access-token:}")
    private String accessToken;

    @Value("${doubao.tts.resource-id:}")
    private String resourceId;

    /**
     * 根据 voice 类型获取 resource ID
     */
    private String voiceToResourceId(String voice) {
        if (voice != null && voice.startsWith("S_")) {
            return "volc.megatts.default";
        }
        return resourceId != null && !resourceId.isEmpty() ? resourceId : "volc.service_type.10029";
    }

    /**
     * 与 {@link #openSession} / {@link #synthesizeWithClient} 共用的 req_params（含 explicit_language）。
     * 以 voiceId + encoding + language 为 key 做模板缓存，避免每次重复构造 additions JSON。
     */
    private Map<String, Object> buildReqParams(String voiceId, String encoding, String explicitLanguage) throws Exception {
        String actualEncoding = encoding != null && !encoding.isEmpty() ? encoding : "pcm";
        String lang = explicitLanguage != null && !explicitLanguage.isEmpty() ? explicitLanguage : "zh-cn";
        String speaker = voiceId != null && !voiceId.isEmpty() ? voiceId : "zh_female_shuangkuaisisi_moon_bigtts";
        String cacheKey = speaker + "|" + actualEncoding + "|" + lang;
        Map<String, Object> cached = reqParamsTemplateCache.get(cacheKey);
        if (cached != null) {
            return cached;
        }
        Map<String, Object> reqParams = Map.of(
                "speaker", speaker,
                "audio_params", Map.of(
                        "format", actualEncoding,
                        "sample_rate", 24000,
                        "enable_timestamp", true),
                "additions", objectMapper.writeValueAsString(Map.of(
                        "disable_markdown_filter", true,
                        "disable_emoji_filter", true,
                        "max_length_to_filter_parenthesis", 100,
                        "explicit_language", lang)));
        Map<String, Object> previous = reqParamsTemplateCache.putIfAbsent(cacheKey, reqParams);
        return previous != null ? previous : reqParams;
    }

    /**
     * 双向 TTS：在一次会话内用单条 TASK_REQUEST 提交整段文本。逐字发送会产生大量请求，韵律差且易在片段边界出现音色/停顿突变。
     */
    private void sendTaskRequestFullText(SpeechWebSocketClient client, Map<String, Object> request, String sessionId, String text) throws Exception {
        if (text == null || text.isEmpty()) {
            return;
        }
        @SuppressWarnings("unchecked")
        Map<String, Object> currentReqParams = new HashMap<>((Map<String, Object>) request.get("req_params"));
        currentReqParams.put("text", text);
        Map<String, Object> currentRequest = Map.of(
                "user", request.get("user"),
                "namespace", request.get("namespace"),
                "req_params", currentReqParams,
                "event", EventType.TASK_REQUEST.getValue());
        client.sendTaskRequest(objectMapper.writeValueAsBytes(currentRequest), sessionId);
    }

    /**
     * 调用豆包 TTS 生成音频
     *
     * @param text     要合成的文本
     * @param voice    音色ID
     * @param encoding 音频格式，默认 pcm
     * @param callback 音频数据回调函数，每次收到音频数据时调用
     * @return 完整的音频数据
     */
    @Override
    public byte[] synthesize(String text, String voice, String encoding, Consumer<byte[]> callback) throws Exception {
        if (appId == null || appId.isEmpty() || accessToken == null || accessToken.isEmpty()) {
            throw new IllegalArgumentException("豆包 TTS 配置参数 appId 和 accessToken 不能为空");
        }

        String actualResourceId = voiceToResourceId(voice);
        String actualEncoding = encoding != null && !encoding.isEmpty() ? encoding : "pcm";

        // 设置请求头
        Map<String, String> headers = Map.of(
                "X-Api-App-Key", appId,
                "X-Api-Access-Key", accessToken,
                "X-Api-Resource-Id", actualResourceId,
                "X-Api-Connect-Id", UUID.randomUUID().toString());

        // 创建 WebSocket 客户端
        SpeechWebSocketClient client = new SpeechWebSocketClient(new URI(ENDPOINT), headers);
        ByteArrayOutputStream audioStream = new ByteArrayOutputStream();

        try {
            client.connectBlocking();
            // 1. StartConnection → 等待 CONNECTION_STARTED
            // 2. StartSession → 等待 SESSION_STARTED
            // 3. 按字符发送 TASK_REQUEST（豆包协议要求），FinishSession 后收音频直至 SESSION_FINISHED
            Map<String, Object> request = Map.of(
                    "user", Map.of("uid", UUID.randomUUID().toString()),
                    "namespace", "BidirectionalTTS",
                    "req_params", Map.of(
                            "speaker", voice != null && !voice.isEmpty() ? voice : "zh_female_vv_uranus_bigtts",
                            "audio_params", Map.of(
                                    "format", actualEncoding,
                                    "sample_rate", 24000,
                                    "enable_timestamp", true),
                            "additions", objectMapper.writeValueAsString(Map.of(
                                    "disable_markdown_filter", true,
                                    "disable_emoji_filter",true,
                                    "max_length_to_filter_parenthesis",100))));

            // 启动连接
            client.sendStartConnection();
            // 等待连接启动
            client.waitForMessage(MsgType.FULL_SERVER_RESPONSE, EventType.CONNECTION_STARTED);

            String sessionId = UUID.randomUUID().toString();

            // 启动会话
            Map<String, Object> startReq = Map.of(
                    "user", request.get("user"),
                    "namespace", request.get("namespace"),
                    "req_params", request.get("req_params"),
                    "event", EventType.START_SESSION.getValue());
            client.sendStartSession(objectMapper.writeValueAsBytes(startReq), sessionId);
            // 等待会话启动
            client.waitForMessage(MsgType.FULL_SERVER_RESPONSE, EventType.SESSION_STARTED);

            // 单次 TASK_REQUEST 发送整段文本：逐字会产生过多 round-trip，韵律与接缝听感较差
            sendTaskRequestFullText(client, request, sessionId, text);

            // 结束会话
            client.sendFinishSession(sessionId);

            // 接收响应
            boolean audioReceived = false;
            while (true) {
                Message msg = client.receiveMessage();
                switch (msg.getType()) {
                    case FULL_SERVER_RESPONSE:
                        break;
                    case AUDIO_ONLY_SERVER:
                        if (msg.getPayload() != null && msg.getPayload().length > 0) {
                            if (!audioReceived) {
                                audioReceived = true;
                            }
                            // 调用回调函数
                            if (callback != null) {
                                callback.accept(msg.getPayload());
                            }
                            audioStream.write(msg.getPayload());
                        }
                        break;
                    default:
                        logger.error("[doubao]Unexpected message type: {}", msg.getType());
                }
                if (msg.getEvent() == EventType.SESSION_FINISHED) {
                    break;
                }
            }

            if (!audioReceived) {
                logger.error("[doubao]未收到音频数据，返回空数组 text：{}", text);
                return new byte[0];
            }

            // 结束连接
            client.sendFinishConnection();
        } finally {
            client.closeBlocking();
        }

        return audioStream.toByteArray();
    }

    /**
     * 创建已连接并完成连接级握手的 {@link SpeechWebSocketClient}，供上层复用连接做多轮合成。
     *
     * @param voiceId 音色 ID，用于解析 resource id
     * @return 已 connect 且收到 {@link EventType#CONNECTION_STARTED} 的客户端
     * @throws IllegalArgumentException 当 appId 或 accessToken 未配置
     * @throws Exception                WebSocket 或协议握手失败
     */
    @Override
    public SpeechWebSocketClient createAndConnectClient(String voiceId) throws Exception {
        if (appId == null || appId.isEmpty() || accessToken == null || accessToken.isEmpty()) {
            throw new IllegalArgumentException("豆包 TTS 配置参数 appId 和 accessToken 不能为空");
        }
        String actualResourceId = voiceToResourceId(voiceId);
        Map<String, String> headers = Map.of(
                "X-Api-App-Key", appId,
                "X-Api-Access-Key", accessToken,
                "X-Api-Resource-Id", actualResourceId,
                "X-Api-Connect-Id", UUID.randomUUID().toString());
        SpeechWebSocketClient client = new SpeechWebSocketClient(new URI(ENDPOINT), headers);
        client.connectBlocking();
        client.sendStartConnection();
        client.waitForMessage(FULL_SERVER_RESPONSE, CONNECTION_STARTED);
        return client;
    }

    /**
     * 在已有客户端上发起一轮会话级合成（含 StartSession、逐字 TaskRequest、收音频）。
     * 使用带超时的收包以避免上游长期阻塞。
     *
     * @param client           已通过 {@link #createAndConnectClient(String)} 建立的连接
     * @param text             待合成文本
     * @param voiceId          音色 ID
     * @param encoding         音频编码，默认 pcm
     * @param explicitLanguage 显式语言提示，写入 additions
     * @param callback         可选，边收边推音频块
     * @return 合并后的完整音频；无音频时返回空数组
     * @throws Exception 会话或网络异常
     */
    @Override
    public void openSession(SpeechWebSocketClient client, String voiceId, String encoding, String explicitLanguage, String sessionId) throws Exception {
        if (client == null) {
            throw new IllegalArgumentException("client 不能为空");
        }
        String effectiveSessionId = (sessionId != null && !sessionId.isEmpty()) ? sessionId : UUID.randomUUID().toString();
        // user.uid 与协议 sessionId 一致，避免 fallback 时两次随机 UUID 不一致
        String uid = effectiveSessionId;
        Map<String, Object> request = Map.of(
                "user", Map.of("uid", uid),
                "namespace", "BidirectionalTTS",
                "req_params", buildReqParams(voiceId, encoding, explicitLanguage));
        Map<String, Object> startReq = Map.of(
                "user", request.get("user"),
                "namespace", request.get("namespace"),
                "req_params", request.get("req_params"),
                "event", EventType.START_SESSION.getValue());
        client.sendStartSession(objectMapper.writeValueAsBytes(startReq), effectiveSessionId);
        client.waitForMessage(FULL_SERVER_RESPONSE, SESSION_STARTED);
    }

    @Override
    public void finishSessionBeforeClose(SpeechWebSocketClient client, String sessionId) throws Exception {
        finishSessionBeforeClose(client, sessionId, SESSION_DRAIN_TIMEOUT_SECONDS);
    }

    @Override
    public void finishSessionBeforeClose(SpeechWebSocketClient client, String sessionId, long drainTimeoutSeconds) throws Exception {
        if (client == null || sessionId == null || sessionId.isEmpty()) {
            return;
        }
        try {
            client.sendFinishSession(sessionId);
        } catch (Exception e) {
            logger.warn("[doubao]finishSessionBeforeClose sendFinishSession: {}", e.getMessage());
            return;
        }
        while (true) {
            Message msg = client.receiveMessage(drainTimeoutSeconds, TimeUnit.SECONDS);
            if (msg == null) {
                logger.warn("[doubao]drain SESSION_FINISHED 超时({}s)，继续关闭连接", drainTimeoutSeconds);
                break;
            }
            if (msg.getEvent() == SESSION_FINISHED) {
                break;
            }
        }
    }

    @Override
    public void sendTaskRequest(SpeechWebSocketClient client, String text, String voiceId, String encoding, String explicitLanguage, String sessionId) throws Exception {
        String actualEncoding = encoding != null && !encoding.isEmpty() ? encoding : "pcm";
        String effectiveSessionId = (sessionId != null && !sessionId.isEmpty()) ? sessionId : UUID.randomUUID().toString();
        String uid = effectiveSessionId;
        Map<String, Object> request = Map.of(
                "user", Map.of("uid", uid),
                "namespace", "BidirectionalTTS",
                "req_params", buildReqParams(voiceId, actualEncoding, explicitLanguage));
        sendTaskRequestFullText(client, request, effectiveSessionId, text);
    }

    @Override
    public void requestSessionFinish(SpeechWebSocketClient client, String sessionId) throws Exception {
        if (client == null || sessionId == null || sessionId.isEmpty()) {
            return;
        }
        client.sendFinishSession(sessionId);
    }

    @Override
    public byte[] synthesizeWithClient(SpeechWebSocketClient client, String text, String voiceId, String encoding, String explicitLanguage, String sessionId, Consumer<byte[]> callback) throws Exception {
        return synthesizeWithClient(client, text, voiceId, encoding, explicitLanguage, sessionId, callback, SINGLE_UTTERANCE_RECEIVE_TIMEOUT_SECONDS);
    }

    @Override
    public byte[] synthesizeWithClient(SpeechWebSocketClient client, String text, String voiceId, String encoding, String explicitLanguage, String sessionId, Consumer<byte[]> callback, long receivePollTimeoutSeconds) throws Exception {
        String actualEncoding = encoding != null && !encoding.isEmpty() ? encoding : "pcm";
        String effectiveSessionId = (sessionId != null && !sessionId.isEmpty()) ? sessionId : UUID.randomUUID().toString();
        String uid = effectiveSessionId;
        Map<String, Object> request = Map.of(
                "user", Map.of("uid", uid),
                "namespace", "BidirectionalTTS",
                "req_params", buildReqParams(voiceId, actualEncoding, explicitLanguage));

        sendTaskRequestFullText(client, request, effectiveSessionId, text);

        ByteArrayOutputStream audioStream = new ByteArrayOutputStream();
        boolean audioReceived = false;
        while (true) {
            Message msg = client.receiveMessage(receivePollTimeoutSeconds, TimeUnit.SECONDS);
            if (msg == null) {
                logger.warn("[doubao]等待单次合成结束事件超时({}s) text：{}", receivePollTimeoutSeconds, text);
                break;
            }
            switch (msg.getType()) {
                case FULL_SERVER_RESPONSE:
                    break;
                case AUDIO_ONLY_SERVER:
                    if (msg.getPayload() != null && msg.getPayload().length > 0) {
                        audioReceived = true;
                        if (callback != null) {
                            callback.accept(msg.getPayload());
                        }
                        audioStream.write(msg.getPayload());
                    }
                    break;
                default:
                    logger.error("[doubao]Unexpected message type: {}", msg.getType());
            }
            EventType ev = msg.getEvent();
            if (ev == TTS_ENDED || ev == TTS_SENTENCE_END) {
                break;
            }
            if (ev == SESSION_FINISHED) {
                logger.warn("[doubao]单次合成中收到 SESSION_FINISHED，提前结束收包 text：{}", text);
                break;
            }
        }
        if (!audioReceived) {
            logger.error("[doubao]未收到音频数据，返回空数组 text：{}", text);
            return new byte[0];
        }
        return audioStream.toByteArray();
    }

    /**
     * 向豆包侧发送 FinishConnection 并关闭 WebSocket；忽略发送/关闭中的非致命异常。
     *
     * @param client 可为 null，null 时直接返回
     */
    @Override
    public void closeClient(SpeechWebSocketClient client) {
        if (client == null) {
            return;
        }
        try {
            client.sendFinishConnection();
        } catch (Exception e) {
            logger.error("[doubao]Error sending finish connection: {}", e.getMessage());
        } finally {
            try {
                client.closeBlocking();
            } catch (Exception e) {
                logger.error("[doubao]Error closing Doubao client: {}", e.getMessage());
            }
        }
    }
}

