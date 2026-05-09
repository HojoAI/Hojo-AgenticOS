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
package com.hojoai.gateway.omniasst.voice.service.aliyunrealtime.impl;

import com.alibaba.nls.client.protocol.InputFormatEnum;
import com.alibaba.nls.client.protocol.NlsClient;
import com.alibaba.nls.client.protocol.SampleRateEnum;
import com.alibaba.nls.client.protocol.asr.SpeechTranscriber;
import com.alibaba.nls.client.protocol.asr.SpeechTranscriberListener;
import com.alibaba.nls.client.protocol.asr.SpeechTranscriberResponse;
import com.hojoai.gateway.omniasst.voice.api.dto.ASRRequest;
import com.hojoai.gateway.omniasst.voice.api.dto.ASRResponse;
import com.hojoai.gateway.omniasst.voice.service.aliyunrealtime.AliyunRealtimeAsrService;
import com.hojoai.gateway.omniasst.voice.alqw.CreateTokenUtil;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 阿里云实时语音识别服务实现
 * 使用阿里云智能语音交互 Java SDK（SpeechTranscriber）实现流式 ASR，
 * 支持按 vid 维度的多包实时音频接收与识别，入参与回参与 /realtime 一致。
 */
@Service
public class AliyunRealtimeAsrServiceImpl implements AliyunRealtimeAsrService {

    private static final Logger log = LoggerFactory.getLogger(AliyunRealtimeAsrServiceImpl.class);

    private static final int STATUS_SUCCESS = 20000000;
    private static final int AWAIT_COMPLETE_SECONDS = 15;
    /** 标准 PCM WAV 文件头长度（RIFF + fmt + data 最小结构） */
    private static final int WAV_HEADER_SIZE = 44;
    /** 模拟实时发送时每包音频长度：3200 字节 ≈ 100ms @ 16kHz 16bit 单声道 */
    private static final int CHUNK_SIZE = 3200;
    /** 模拟实时发送时每包间隔（毫秒），与 CHUNK_SIZE 对应约 100ms */
    private static final int CHUNK_INTERVAL_MS = 100;

    @Value("${aliyun.nls.appkey:}")
    private String appkey;

    @Value("${aliyun.nls.gatewayUrl:wss://nls-gateway-cn-shanghai.aliyuncs.com/ws/v1}")
    private String nlsGatewayUrl;

    @Value("${aliyun.nls.token}")
    private String xmToken;

    @Resource
    private CreateTokenUtil createTokenUtil;

    private volatile NlsClient nlsClient;
    /** 当前 NlsClient 使用的 token，用于检测 token 变更并重建 client */
    private volatile String lastTokenUsed;
    private final ConcurrentHashMap<String, AliyunTranscriberSession> sessions = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Object> vidLocks = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        ensureNlsClient();
    }

    @PreDestroy
    public void destroy() {
        if (nlsClient != null) {
            try {
                nlsClient.shutdown();
            } catch (Exception e) {
                log.warn("NlsClient shutdown error", e);
            }
        }
    }

    /**
     * 确保 NlsClient 可用，且使用最新 token。
     * 每次调用都会从 createTokenUtil.getToken 取最新 token；
     * 若 token 与上次使用的一致则复用现有 client，否则关闭旧 client 并用新 token 创建新 client。
     */
    private void ensureNlsClient() {
        String token = createTokenUtil.getToken("实时语音识别");
        if (token == null || token.isEmpty()) {
            if (nlsClient == null) {
                throw new IllegalStateException("Failed to obtain Aliyun NLS token");
            }
            return;
        }
        if (nlsClient != null && token.equals(lastTokenUsed)) {
            return;
        }
        synchronized (this) {
            if (nlsClient != null && token.equals(lastTokenUsed)) {
                return;
            }
            if (nlsClient != null) {
                try {
                    nlsClient.shutdown();
                } catch (Exception e) {
                    log.warn("NlsClient shutdown before recreate", e);
                }
                nlsClient = null;
            }
            String url = StringUtils.isBlank(nlsGatewayUrl) ? null : nlsGatewayUrl.trim();
            if (StringUtils.isBlank(url)) {
                nlsClient = new NlsClient(token);
            } else {
                nlsClient = new NlsClient(url, token);
            }
            lastTokenUsed = token;
            log.info("NlsClient created with fresh token, gatewayUrl: {}", url);
        }
    }

    private NlsClient getNlsClient() {
        ensureNlsClient();
        return nlsClient;
    }

    @Override
    public ASRResponse asr(ASRRequest request) {
        if (request == null) {
            return buildErrorResponse(null, -1, "request is null");
        }
        String vid = request.getVid();
        if (StringUtils.isBlank(vid)) {
            return buildErrorResponse(null, -1, "vid is required");
        }
        if (StringUtils.isBlank(appkey)) {
            return buildErrorResponse(vid, -1, "aliyun.nls.appkey is not configured");
        }
        log.info("[AliyunRealtimeAsr] start, vid: {}, packageIndex:{}, packageEnd:{}",vid, request.getPackageIndex(), request.getPackageEnd());
        String packageDataB64 = request.getPackageData();
        int packageEnd = request.getPackageEnd();
        int vadEnd = request.getVadEnd();
        boolean isEnd = (packageEnd == 1);

        byte[] audioData = null;
        if (StringUtils.isNotBlank(packageDataB64)) {
            try {
                audioData = Base64.getDecoder().decode(packageDataB64);
            } catch (IllegalArgumentException e) {
                log.error("[AliyunRealtimeAsr] invalid base64 package_data, vid: {}", vid, e);
                return buildErrorResponse(vid, -1, "invalid base64 package_data");
            }
        }

        Object lock = vidLocks.computeIfAbsent(vid, k -> new Object());
        synchronized (lock) {
            AliyunTranscriberSession session = sessions.get(vid);
            try {
                if (session == null) {
                    if (isEnd && (audioData == null || audioData.length == 0)) {
                        return buildOkResponse(vid, "", isEnd);
                    }
                    session = createSession(vid, request);
                    sessions.put(vid, session);
                }
                if (audioData != null && audioData.length > 0) {
                    session.transcriber.send(audioData, audioData.length);
                    log.info("[AliyunRealtimeAsr] send data length: {}, vid: {}", audioData.length, vid);
                }
                boolean effectiveIsEnd = isEnd || session.listener.isSentenceEndReceived();
                if (effectiveIsEnd) {
                    log.info("[AliyunRealtimeAsr] session stop, vid: {} ",vid);
                    session.transcriber.stop();
                    boolean completed = session.listener.awaitComplete(AWAIT_COMPLETE_SECONDS, TimeUnit.SECONDS);
                    sessions.remove(vid);
                    vidLocks.remove(vid);
                    try {
                        session.transcriber.close();
                    } catch (Exception e) {
                        log.error("transcriber.close error, vid: {}", vid, e);
                    }
                    String fullText = session.listener.getFullText();
                    Integer status = session.listener.getLastStatus();
                    String statusText = session.listener.getLastStatusText();
                    log.info("[AliyunRealtimeAsr] isend response fullText:{}, status:{},statusText:{},vid:{}", fullText, status, statusText,vid);
                    if (!completed) {
                        log.error("[AliyunRealtimeAsr] await complete timeout, vid: {}", vid);
                    }
                    if (status != null && status != STATUS_SUCCESS && StringUtils.isNotBlank(statusText)) {
                        return buildErrorResponse(vid, status, statusText);
                    }
                    return buildOkResponse(vid, fullText != null ? fullText : "", true);
                }
                String currentText = session.listener.getTextWithIntermediate();
                log.info("[AliyunRealtimeAsr] response text(with intermediate):{}, vid:{}", currentText, vid);
                return buildOkResponse(vid, currentText != null ? currentText : "", false);
            } catch (Exception e) {
                log.error("[AliyunRealtimeAsr] asr error, vid: {}", vid, e);
                sessions.remove(vid);
                vidLocks.remove(vid);
                if (session != null) {
                    try {
                        session.transcriber.close();
                    } catch (Exception ex) {
                        log.error("transcriber.close error", ex);
                    }
                }
                return buildErrorResponse(vid, -1, e.getMessage() != null ? e.getMessage() : "asr error");
            }
        }
    }

    private AliyunTranscriberSession createSession(String vid, ASRRequest request) throws Exception {
        TranscriberResultListener listener = new TranscriberResultListener();
        SpeechTranscriber transcriber = new SpeechTranscriber(getNlsClient(), listener);
        transcriber.setAppKey(appkey);
        transcriber.setFormat(InputFormatEnum.PCM);
        transcriber.setSampleRate(SampleRateEnum.SAMPLE_RATE_16K);
        transcriber.setEnableIntermediateResult(true);
        transcriber.setEnablePunctuation(request != null && request.getDoPunc() == 1);
        transcriber.setEnableITN(request != null && request.getDoItn() == 1);
        transcriber.addCustomedParam("max_sentence_silence", 800);
        transcriber.start();
        return new AliyunTranscriberSession(transcriber, listener);
    }

    private ASRResponse buildOkResponse(String vid, String text, boolean isEnd) {
        ASRResponse resp = new ASRResponse();
        resp.setRtn(0);
        resp.setMsg("Success");
        resp.setIsEnd(isEnd ? 1 : 0);
        resp.setVid(vid);
        ASRResponse.ASRText at = new ASRResponse.ASRText();
        at.setText(text);
        at.setFixedText(text);
        resp.setText(at);
        return resp;
    }

    private ASRResponse buildErrorResponse(String vid, int rtn, String msg) {
        ASRResponse resp = new ASRResponse();
        resp.setRtn(rtn);
        resp.setMsg(msg);
        resp.setIsEnd(1);
        resp.setVid(vid);
        resp.setText(new ASRResponse.ASRText());
        return resp;
    }

    private static final class AliyunTranscriberSession {
        final SpeechTranscriber transcriber;
        final TranscriberResultListener listener;

        AliyunTranscriberSession(SpeechTranscriber transcriber, TranscriberResultListener listener) {
            this.transcriber = transcriber;
            this.listener = listener;
        }
    }

    private static final class TranscriberResultListener extends SpeechTranscriberListener {
        private final StringBuilder fullText = new StringBuilder();
        /** 当前句子的中间识别结果，用于在句子结束前即可返回部分内容 */
        private final AtomicReference<String> currentIntermediateText = new AtomicReference<>("");
        private final CountDownLatch completeLatch = new CountDownLatch(1);
        private final AtomicInteger lastStatus = new AtomicInteger(STATUS_SUCCESS);
        private final AtomicReference<String> lastStatusText = new AtomicReference<>("");
        /** onSentenceEnd 是否已触发，用于在请求未传 isEnd 时也能按句子结束处理 */
        private final AtomicBoolean sentenceEndReceived = new AtomicBoolean(false);

        String getFullText() {
            synchronized (fullText) {
                return fullText.toString();
            }
        }

        /** 返回已确认文本 + 当前句子的中间结果，供非结束包即时返回，减少等待 */
        String getTextWithIntermediate() {
            String full = getFullText();
            String inter = currentIntermediateText.get();
            return (inter != null && !inter.isEmpty()) ? full + inter : full;
        }

        boolean awaitComplete(long timeout, TimeUnit unit) {
            try {
                return completeLatch.await(timeout, unit);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
        }

        Integer getLastStatus() {
            return lastStatus.get();
        }

        String getLastStatusText() {
            return lastStatusText.get();
        }

        boolean isSentenceEndReceived() {
            return sentenceEndReceived.get();
        }

        @Override
        public void onTranscriberStart(SpeechTranscriberResponse response) {
            //task_id是调用方和服务端通信的唯一标识，遇到问题时，需要提供此task_id。
            log.info("[AliyunRealtimeAsr] task_id: " + response.getTaskId() + ", name: " + response.getName() + ", status: " + response.getStatus());
            if (response != null) {
                lastStatus.set(response.getStatus());
            }
        }

        @Override
        public void onTranscriptionResultChange(SpeechTranscriberResponse response) {
            // 保存中间结果，便于在句子结束前就返回给调用方，减少等待
            if (response != null && response.getStatus() == STATUS_SUCCESS) {
                String t = response.getTransSentenceText();
                currentIntermediateText.set(t != null ? t : "");
            }
            log.info("[AliyunRealtimeAsr] task_id: " + response.getTaskId() +
                    ", name: " + response.getName() +
                    ", status: " + response.getStatus() +
                    ", index: " + response.getTransSentenceIndex() +
                    ", result: " + response.getTransSentenceText() +
                    ", time: " + response.getTransSentenceTime());
        }

        @Override
        public void onSentenceBegin(SpeechTranscriberResponse response) {
            // no-op
            log.info("[AliyunRealtimeAsr] task_id: " + response.getTaskId() + ", name: " + response.getName() + ", status: " + response.getStatus());
        }

        @Override
        public void onSentenceEnd(SpeechTranscriberResponse response) {
            log.info("[AliyunRealtimeAsr] task_id: " + response.getTaskId() +
                    ", name: " + response.getName() +
                    //状态码“20000000”表示正常识别。
                    ", status: " + response.getStatus() +
                    //句子编号，从1开始递增。
                    ", index: " + response.getTransSentenceIndex() +
                    //当前的识别结果。
                    ", result: " + response.getTransSentenceText() +
                    //置信度
                    ", confidence: " + response.getConfidence() +
                    //开始时间
                    ", begin_time: " + response.getSentenceBeginTime() +
                    //当前已处理的音频时长，单位为毫秒。
                    ", time: " + response.getTransSentenceTime());
            if (response != null && response.getStatus() == STATUS_SUCCESS) {
                String t = response.getTransSentenceText();
                if (t != null && !t.isEmpty()) {
                    synchronized (fullText) {
                        fullText.append(t);
                    }
                }
                currentIntermediateText.set("");
                sentenceEndReceived.set(true);
            }
        }

        @Override
        public void onTranscriptionComplete(SpeechTranscriberResponse response) {
            if (response != null) {
                lastStatus.set(response.getStatus());
                if (response.getStatusText() != null) {
                    lastStatusText.set(response.getStatusText());
                }
            }
            completeLatch.countDown();
        }

        @Override
        public void onFail(SpeechTranscriberResponse response) {
            if (response != null) {
                lastStatus.set(response.getStatus());
                if (response.getStatusText() != null) {
                    lastStatusText.set(response.getStatusText());
                }
            }
            completeLatch.countDown();
        }
    }

    @Override
    public ASRResponse asrFromLocalWav(String wavFilePath) {
        return asrFromLocalWav(wavFilePath, "wav-" + UUID.randomUUID());
    }

    @Override
    public ASRResponse asrFromLocalWav(String wavFilePath, String vid) {
        Path path = Paths.get(wavFilePath);
        if (!Files.exists(path) || !Files.isRegularFile(path)) {
            log.error("[AliyunRealtimeAsr] wav file not found or not file: {}", wavFilePath);
            return buildErrorResponse(vid != null ? vid : "", -1, "wav file not found: " + wavFilePath);
        }
        byte[] raw;
        try {
            raw = Files.readAllBytes(path);
        } catch (Exception e) {
            log.error("[AliyunRealtimeAsr] read wav file error: {}", wavFilePath, e);
            return buildErrorResponse(vid != null ? vid : "", -1, "read wav file error: " + e.getMessage());
        }
        int dataLen = raw.length <= WAV_HEADER_SIZE ? 0 : raw.length - WAV_HEADER_SIZE;
        if (dataLen <= 0) {
            log.error("[AliyunRealtimeAsr] wav file too small or no pcm data: {}", wavFilePath);
            return buildErrorResponse(vid != null ? vid : "", -1, "wav file too small or no pcm data");
        }
        byte[] pcm = new byte[dataLen];
        System.arraycopy(raw, WAV_HEADER_SIZE, pcm, 0, dataLen);

        ASRResponse lastResponse = null;
        int offset = 0;
        int index = 0;
        while (offset < pcm.length) {
            int len = Math.min(CHUNK_SIZE, pcm.length - offset);
            byte[] chunk = new byte[len];
            System.arraycopy(pcm, offset, chunk, 0, len);
            offset += len;
            boolean isLast = (offset >= pcm.length);

            ASRRequest req = new ASRRequest();
            req.setVid(vid);
            req.setPackageData(Base64.getEncoder().encodeToString(chunk));
            req.setPackageEnd(isLast ? 1 : 0);
            req.setVadEnd(isLast ? 1 : 0);
            req.setPackageIndex(index);
            req.setPackageSize(len);
            req.setDoPunc(1);
            req.setDoItn(1);

            lastResponse = asr(req);
            if (lastResponse != null && lastResponse.getRtn() != STATUS_SUCCESS) {
                return lastResponse;
            }
            index++;
            if (!isLast && CHUNK_INTERVAL_MS > 0) {
                try {
                    Thread.sleep(CHUNK_INTERVAL_MS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    log.error("[AliyunRealtimeAsr] asrFromLocalWav interrupted");
                    return lastResponse != null ? lastResponse : buildErrorResponse(vid, -1, "interrupted");
                }
            }
        }
        return lastResponse != null ? lastResponse : buildOkResponse(vid, "", true);
    }
}
