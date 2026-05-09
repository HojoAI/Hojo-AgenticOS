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

/** 耳机 WebSocket ASR：同 HOJO 批量合并与下游调用，无服务端 VAD，音频直入 ASR 队列。 */
@Service
public class HojoHeadphoneAsrServiceImpl implements AsrService {

    private static final Logger logger = LoggerFactory.getLogger(HojoHeadphoneAsrServiceImpl.class);
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

    @PostConstruct
    public void init() {
        asrServiceFactory.registerService(AsrPlatform.HOJO_HEADPHONE.getCode(), this);
        logger.info("HojoHeadphoneAsrServiceImpl initialized for platform {}", AsrPlatform.HOJO_HEADPHONE.getDescription());
    }

    @PreDestroy
    public void destroy() {
        logger.info("Shutting down HojoHeadphoneAsrServiceImpl...");
        sessionContextMap.values().forEach(context -> {
            context.running = false;
            if (context.executor != null) {
                context.executor.shutdownNow();
            }
        });
        sessionContextMap.clear();
        logger.info("HojoHeadphoneAsrServiceImpl shutdown completed");
    }

    @Override
    public void recognize(String sessionId, AsrAudioDataRequest request, AsrCallback callback) {
        AsrSessionContext context = sessionContextMap.computeIfAbsent(sessionId, k -> {
            AsrSessionContext newContext = new AsrSessionContext(sessionId, callback);
            startProcessingThreads(newContext);
            return newContext;
        });
        context.asrQueue.offer(request);
    }

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
        context.executor = Executors.newFixedThreadPool(1, r -> {
            Thread thread = new Thread(r);
            thread.setName("hojo-headphone-asr-" + context.sessionId);
            thread.setDaemon(true);
            return thread;
        });
        context.executor.submit(() -> processAsrQueue(context));
    }

    private void processAsrQueue(AsrSessionContext context) {
        try {
            while (context.running && !Thread.currentThread().isInterrupted()) {
                logger.info("[耳机ASR][收集音频包], sessionId: {}, asrQueue size: {}", context.sessionId, context.asrQueue.size());
                List<AsrAudioDataRequest> batch = collectAsrBatch(context);
                if (!batch.isEmpty()) {
                    processAsrBatch(context, batch);
                }
            }
        } catch (InterruptedException e) {
            logger.error("[耳机ASR][音频处理中断], sessionId: {}", context.sessionId);
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            logger.error("Headphone ASR processing thread error for session: {}", context.sessionId, e);
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
        logger.info("[耳机ASR][批量收集音频包], sessionId: {}, 打包音频包数量: {} 个", context.sessionId, batch.size());
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
            logger.info("[耳机ASR][ASR-API请求], sessionId: {}, vid: {}, index: {}, isEnd: {}",
                context.sessionId, asrRequest.getVid(), asrRequest.getPackageIndex(), asrRequest.getPackageIndex());
            ASRResponse asrResponse;
            if (context.sessionId.contains("file-")) {
                asrResponse = asrAPI.asr(asrRequest);
            } else {
                asrResponse = aliyunRealtimeAsrService.asr(asrRequest);
            }
            logger.info("[耳机ASR][ASR-API返回], sessionId: {}, vid: {}, response: {}",
                context.sessionId, asrResponse.getVid(), JacksonUtil.bean2Json(asrResponse));
            if (asrResponse.getRtn() != 0) {
                logger.warn("Headphone ASR failed for session: {}, asrResponse: {}", context.sessionId, asrResponse);
                resetAsrContext(context);
                return;
            }
            AsrResult result = buildAsrResult(asrResponse, mergedRequest.getCustom());
            logger.info("[耳机ASR][ASR-RESULT返回], sessionId: {}, result: {}", context.sessionId, JacksonUtil.bean2Json(result));
            context.callback.onResult(result);
            if (asrResponse.getIsEnd() == 1) {
                resetAsrContext(context);
                context.asrQueue.clear();
                logger.info("[耳机ASR][ASR-断句], sessionId: {}, vid: {}", context.sessionId, asrResponse.getVid());
            }
        } catch (Exception e) {
            logger.error("Error processing headphone ASR batch for session: {}", context.sessionId, e);
            resetAsrContext(context);
        }
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
            logger.info("[耳机ASR][ASR-合并音频包], 合并包数量: {}, 合并后音频大小: {}, 结束符: {}",
                requests.size(), mergedAudioBytes.length, isEnd);
            return mergedRequest;
        } catch (Exception e) {
            logger.error("Error merging audio requests", e);
            return requests.get(0);
        }
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
        final LinkedBlockingQueue<AsrAudioDataRequest> asrQueue = new LinkedBlockingQueue<>();
        volatile boolean running = true;
        ExecutorService executor;
        String asrVid;
        int packageIndex = 0;
        String language = "chinese";
        String type = "pcm";

        AsrSessionContext(String sessionId, AsrCallback callback) {
            this.sessionId = sessionId;
            this.callback = callback;
        }
    }
}
