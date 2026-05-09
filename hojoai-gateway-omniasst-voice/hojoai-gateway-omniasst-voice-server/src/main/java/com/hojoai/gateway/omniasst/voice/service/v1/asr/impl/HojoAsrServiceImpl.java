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
package com.hojoai.gateway.omniasst.voice.service.v1.asr.impl;

import com.hojoai.common.jackson.JacksonUtil;
import com.hojoai.gateway.omniasst.voice.api.AsrAPI;
import com.hojoai.gateway.omniasst.voice.api.dto.ASRRequest;
import com.hojoai.gateway.omniasst.voice.api.dto.ASRResponse;
import com.hojoai.gateway.omniasst.voice.api.dto.VADRequest;
import com.hojoai.gateway.omniasst.voice.api.dto.VADResponse;
import com.hojoai.gateway.omniasst.voice.service.aliyunrealtime.AliyunRealtimeAsrService;
import com.hojoai.gateway.omniasst.voice.service.v1.asr.*;
import com.hojoai.gateway.omniasst.voice.service.v1.asr.dto.AsrResult;
import com.hojoai.gateway.omniasst.voice.websocket.v1.dto.AsrAudioDataRequest;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import xyz.downgoon.snowflake.Snowflake;

import java.io.ByteArrayOutputStream;
import java.util.*;
import java.util.concurrent.*;

/**
 * Hojo 平台 ASR v1：VAD 与 ASR 双线程流水线，调用 {@link AsrAPI} / {@link AliyunRealtimeAsrService}（按上下文分支）并回调 {@link AsrCallback}。
 */
@Service
public class HojoAsrServiceImpl implements AsrService {
    
    private static final Logger logger = LoggerFactory.getLogger(HojoAsrServiceImpl.class);
    /** VAD 判有效语音的累计阈值 */
    private static final int VAD_VALID_THRESHOLD = 20;
    /** 单次会话 ASR 分包数量上限 */
    private static final int MAX_ASR_PACKAGES = 32;
    
    @Resource
    private AsrServiceFactory asrServiceFactory;
    @Resource
    private AsrAPI asrAPI;
    @Resource
    private Snowflake snowflake;

    @Resource
    private AliyunRealtimeAsrService aliyunRealtimeAsrService;
    
    private final Map<String, AsrSessionContext> sessionContextMap = new ConcurrentHashMap<>();

    /**
     * 向 {@link AsrServiceFactory} 注册 {@link AsrPlatform#HOJO}。
     */
    @PostConstruct
    public void init() {
        asrServiceFactory.registerService(AsrPlatform.HOJO.getCode(), this);
        logger.info("HojoAsrServiceImpl initialized for platform {}", AsrPlatform.HOJO.getDescription());
    }

    /**
     * 停止所有会话线程池并清空上下文。
     */
    @PreDestroy
    public void destroy() {
        logger.info("Shutting down HojoAsrServiceImpl...");
        sessionContextMap.values().forEach(context -> {
            context.running = false;
            if (context.executor != null) {
                context.executor.shutdownNow();
            }
        });
        sessionContextMap.clear();
        logger.info("HojoAsrServiceImpl shutdown completed");
    }

    /**
     * {@inheritDoc}
     * <p>音频帧进入 VAD 队列，由后台线程决定是否送入 ASR 队列。</p>
     */
    @Override
    public void recognize(String sessionId, AsrAudioDataRequest request, AsrCallback callback) {
        AsrSessionContext context = sessionContextMap.computeIfAbsent(sessionId, k -> {
            AsrSessionContext newContext = new AsrSessionContext(sessionId, callback);
            startProcessingThreads(newContext);
            return newContext;
        });
        context.vadQueue.offer(request);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void stopRecognize(String sessionId) {
        AsrSessionContext context = sessionContextMap.remove(sessionId);
        if (context != null) {
            context.running = false;
            if (context.executor != null) {
                context.executor.shutdownNow();
                try {
                    if (!context.executor.awaitTermination(2, TimeUnit.SECONDS)) {
                        logger.warn("Executor did not terminate in time for session: {}", sessionId);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }
    
    private void startProcessingThreads(AsrSessionContext context) {
        context.executor = Executors.newFixedThreadPool(2, r -> {
            Thread thread = new Thread(r);
            thread.setName("hojo-asr-" + context.sessionId);
            thread.setDaemon(true);
            return thread;
        });
        context.executor.submit(() -> processVadQueue(context));
        context.executor.submit(() -> processAsrQueue(context));
    }
    
    private void processVadQueue(AsrSessionContext context) {
        try {
            while (context.running && !Thread.currentThread().isInterrupted()) {
                AsrAudioDataRequest request = context.vadQueue.take();
                if (context.vadValid) {
                    context.asrQueue.offer(request);
                } else {
                    processVadDetection(context, request);
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            logger.error("VAD processing thread error for session: {}", context.sessionId, e);
            context.callback.onError("VAD处理异常: " + e.getMessage());
        }
    }
    
    private void processVadDetection(AsrSessionContext context, AsrAudioDataRequest request) {
        try {
            if (StringUtils.isBlank(context.vadVid)) {
                context.vadVid = generateVid();
                context.vadHistory.clear();
                context.lastVadRequest = null;
            }
            
            VADRequest vadRequest = buildVadRequest(context.vadVid, request.getAudioData());
            logger.warn("[ASR流程][VAD-API请求] sessionId: {}, vid: {}", context.sessionId, vadRequest.getVid());
            VADResponse vadResponse = asrAPI.vad(vadRequest);
            logger.warn("[ASR流程][VAD-API返回] sessionId: {}, vadResponse: {}", context.sessionId, JacksonUtil.bean2Json(vadResponse));
            if (Objects.isNull(vadResponse) || vadResponse.getRtn() != 0) {
                logger.warn("[ASR流程][VAD-检测失败][重置VAD状态], sessionId: {}, vid: {}, vadResponse: {}", context.sessionId, context.vadVid, JacksonUtil.bean2Json(vadResponse));
                resetVadContext(context);
                return;
            }
            int vadSum = calculateVadSum(vadResponse.getVadData());
            context.vadHistory.add(vadSum);
            if (context.vadHistory.size() > 2) {
                context.vadHistory.remove(0);
            }
            if (isVadValid(context.vadHistory)) {
                if (Objects.nonNull(context.lastVadRequest)) {
                    context.asrQueue.offer(context.lastVadRequest);
                }
                context.asrQueue.offer(request);
                context.vadValid = true;
                context.callback.onVadPassed();
                resetVadContext(context);
                logger.info("[ASR流程][VAD-检测通过], sessionId: {}", context.sessionId);
            } else {
                context.lastVadRequest = request;
                if (request.getIsEnd()) {
                    context.callback.onVadInvalid();
                }
            }
        } catch (Exception e) {
            logger.error("[ASR流程][VAD-检测异常][重置VAD状态], sessionId: {}, vid: {}, error: {}", context.sessionId, context.vadVid, e.getMessage(), e);
            resetVadContext(context);
        }
    }
    
    private int calculateVadSum(int[] vadData) {
        if (Objects.isNull(vadData) || vadData.length == 0) {
            return 0;
        }
        int sum = 0;
        for (int value : vadData) {
            sum += value;
        }
        return sum;
    }
    
    private boolean isVadValid(List<Integer> vadHistory) {
        if (vadHistory.isEmpty()) {
            return false;
        }
        if (vadHistory.size() == 1) {
            return vadHistory.get(0) >= VAD_VALID_THRESHOLD;
        }
        int first = vadHistory.get(0);
        int second = vadHistory.get(1);
        if (first >= VAD_VALID_THRESHOLD) {
            return true;
        }
        return (first + second) >= VAD_VALID_THRESHOLD;
    }
    
    private void processAsrQueue(AsrSessionContext context) {
        try {
            while (context.running && !Thread.currentThread().isInterrupted()) {
                logger.info("[ASR流程][收集音频包], sessionId: {}, asrQueue size: {}", context.sessionId, context.asrQueue.size());
                List<AsrAudioDataRequest> batch = collectAsrBatch(context);
                if (!batch.isEmpty()) {
                    processAsrBatch(context, batch);
                }
            }
        } catch (InterruptedException e) {
            logger.error("[ASR流程][音频处理中断], sessionId: {}", context.sessionId);
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            logger.error("ASR processing thread error for session: {}", context.sessionId, e);
            context.callback.onError("ASR处理异常: " + e.getMessage());
        }
    }
    
    private List<AsrAudioDataRequest> collectAsrBatch(AsrSessionContext context) throws InterruptedException {
        List<AsrAudioDataRequest> batch = new ArrayList<>();
        AsrAudioDataRequest firstRequest = context.asrQueue.take();
        batch.add(firstRequest);
        boolean foundEnd = Objects.nonNull(firstRequest.getIsEnd()) && firstRequest.getIsEnd();
        if (!foundEnd) {
            AsrAudioDataRequest nextRequest;
            while ((nextRequest = context.asrQueue.poll()) != null && batch.size() < MAX_ASR_PACKAGES) {
                batch.add(nextRequest);
                if (Objects.nonNull(nextRequest.getIsEnd()) && nextRequest.getIsEnd()) {
                    break;
                }
            }
        }
        logger.info("[ASR流程][批量收集音频包], sessionId: {}, 打包音频包数量: {} 个", context.sessionId, batch.size());
        return batch;
    }
    
    private void processAsrBatch(AsrSessionContext context, List<AsrAudioDataRequest> batch) {
        try {
            if (StringUtils.isBlank(context.asrVid)) {
                context.asrVid = generateVid();
                context.packageIndex = 0;
            }
            boolean isEnd = batch.stream().anyMatch(req -> Objects.nonNull(req.getIsEnd()) && req.getIsEnd());
            AsrAudioDataRequest mergedRequest = mergeAudioRequests(batch, isEnd);
            if (Objects.isNull(mergedRequest)) {
                return;
            }
            ASRRequest asrRequest = buildAsrRequest(context, mergedRequest.getAudioData(), isEnd);
            logger.info("[ASR流程][ASR-API请求], sessionId: {}, vid: {}, index: {}, isEnd: {}", context.sessionId, asrRequest.getVid(), asrRequest.getPackageIndex(), asrRequest.getPackageIndex());
            ASRResponse asrResponse;
            if(context.sessionId.contains("file-")){
                asrResponse = asrAPI.asr(asrRequest);
            }else {
                asrResponse = aliyunRealtimeAsrService.asr(asrRequest);
            }
            logger.info("[ASR流程][ASR-API返回], sessionId: {}, vid: {}, response: {}", context.sessionId, asrResponse.getVid(), JacksonUtil.bean2Json(asrResponse));
            if (asrResponse.getRtn() != 0) {
                logger.warn("ASR failed for session: {}, asrResponse: {}", context.sessionId, asrResponse);
                resetAsrContext(context);
                context.vadValid = false;
                return;
            }
            AsrResult result = buildAsrResult(asrResponse, mergedRequest.getCustom());
            logger.info("[ASR流程][ASR-RESULT返回], sessionId: {}, result: {}", context.sessionId, JacksonUtil.bean2Json(result));
            context.callback.onResult(result);
            if (asrResponse.getIsEnd() == 1) {
                resetAsrContext(context);
                context.vadValid = false;
                context.asrQueue.clear();
                logger.info("[ASR流程][ASR-断句][VAD流程开始], sessionId: {}, vid: {}", context.sessionId, asrResponse.getVid());
            }
        } catch (Exception e) {
            logger.error("Error processing ASR batch for session: {}", context.sessionId, e);
            resetAsrContext(context);
            context.vadValid = false;
        }
    }
    
    private void resetVadContext(AsrSessionContext context) {
        context.vadVid = null;
        context.vadHistory.clear();
        context.lastVadRequest = null;
    }
    
    private void resetAsrContext(AsrSessionContext context) {
        context.asrVid = null;
        context.packageIndex = 0;
    }
    
    private AsrAudioDataRequest mergeAudioRequests(List<AsrAudioDataRequest> requests, boolean isEnd) {
        if (requests.isEmpty()) {
            return null;
        }
        if (requests.size() == 1) {
            AsrAudioDataRequest single = requests.get(0);
            if (isEnd && (single.getIsEnd() == null || !single.getIsEnd())) {
                single.setIsEnd(true);
            }
            return single;
        }
        try {
            ByteArrayOutputStream mergedAudioStream = new ByteArrayOutputStream();
            String lastCustom = null;
            for (AsrAudioDataRequest request : requests) {
                if (StringUtils.isNotBlank(request.getAudioData())) {
                    byte[] audioBytes = Base64.getDecoder().decode(request.getAudioData());
                    mergedAudioStream.write(audioBytes);
                }
                if (StringUtils.isNotBlank(request.getCustom())) {
                    lastCustom = request.getCustom();
                }
            }
            AsrAudioDataRequest mergedRequest = new AsrAudioDataRequest();
            byte[] mergedAudioBytes = mergedAudioStream.toByteArray();
            mergedRequest.setAudioData(Base64.getEncoder().encodeToString(mergedAudioBytes));
            mergedRequest.setCustom(lastCustom);
            mergedRequest.setIsEnd(isEnd);
            logger.info("[ASR流程][ASR-合并音频包], 合并包数量: {}, 合并后音频大小: {}, 结束符: {}", requests.size(), mergedAudioBytes.length, isEnd);
            return mergedRequest;
        } catch (Exception e) {
            logger.error("Error merging audio requests", e);
            return requests.get(0);
        }
    }
    
    private VADRequest buildVadRequest(String vadVid, String base64Audio) {
        VADRequest vadRequest = new VADRequest();
        vadRequest.setVid(vadVid);
        vadRequest.setPackageSize(base64Audio.length());
        vadRequest.setPackageData(base64Audio);
        vadRequest.setVadEnd(AsrConstant.ASR_VAD_END_DEFAULT);
        vadRequest.setVadDrop(0);
        return vadRequest;
    }
    
    private ASRRequest buildAsrRequest(AsrSessionContext context, String base64Audio, Boolean isEnd) {
        if (isEnd && StringUtils.isBlank(base64Audio)) {
            base64Audio = generateSilencePacket320ms();
        }
        ASRRequest asrRequest = new ASRRequest();
        asrRequest.setTask(AsrConstant.ASR_TASK_DEFAULT);
        asrRequest.setVid(context.asrVid);
        asrRequest.setPackageIndex(context.packageIndex++);
        asrRequest.setPackageSize(base64Audio.length());
        asrRequest.setPackageData(base64Audio);
        asrRequest.setPackageEnd(isEnd ? 1 : 0);
        asrRequest.setLang(context.language);
        asrRequest.setType(context.type);
        asrRequest.setDoItn(1);
        asrRequest.setDoPunc(1);
        asrRequest.setDoLangDetect(0);
        asrRequest.setVadEnd(AsrConstant.ASR_VAD_END_DEFAULT);
        return asrRequest;
    }
    
    private String generateSilencePacket320ms() {
        int sampleRate = 16000;
        int durationMs = 320;
        int bytesPerSample = 2;
        int totalBytes = (sampleRate * durationMs / 1000) * bytesPerSample;
        byte[] silenceData = new byte[totalBytes];
        return Base64.getEncoder().encodeToString(silenceData);
    }
    
    private String generateVid() {
        return String.valueOf(snowflake.nextId());
    }
    
    private AsrResult buildAsrResult(ASRResponse asrResponse, String custom) {
        AsrResult result = new AsrResult();
        ASRResponse.ASRText text = asrResponse.getText();
        if (Objects.nonNull(text)) {
            result.setFixedText(text.getFixedText());
            result.setTmpText(text.getTmpText());
            result.setText(text.getText());
        }
        result.setIsEnd(asrResponse.getIsEnd());
        result.setCustom(custom);
        return result;
    }
    
    private static class AsrSessionContext {
        final String sessionId;
        final AsrCallback callback;
        final LinkedBlockingQueue<AsrAudioDataRequest> vadQueue = new LinkedBlockingQueue<>();
        final LinkedBlockingQueue<AsrAudioDataRequest> asrQueue = new LinkedBlockingQueue<>();
        volatile boolean running = true;
        ExecutorService executor;
        String vadVid;
        String asrVid;
        int packageIndex = 0;
        String language = "chinese";
        String type = "pcm";
        boolean vadValid = false;
        List<Integer> vadHistory = new ArrayList<>();
        AsrAudioDataRequest lastVadRequest;
        
        AsrSessionContext(String sessionId, AsrCallback callback) {
            this.sessionId = sessionId;
            this.callback = callback;
        }
    }
}
