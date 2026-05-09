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
package com.hojoai.gateway.omniasst.voice.service.asr.impl;

import com.hojoai.gateway.omniasst.voice.api.AsrAPI;
import com.hojoai.gateway.omniasst.voice.api.dto.ASRRequest;
import com.hojoai.gateway.omniasst.voice.api.dto.ASRResponse;
import com.hojoai.gateway.omniasst.voice.service.asr.AsrService;
import com.hojoai.gateway.omniasst.voice.service.asr.dto.AsrFileOssRequest;
import com.hojoai.gateway.omniasst.voice.service.v1.asr.AsrCallback;
import com.hojoai.gateway.omniasst.voice.service.v1.asr.AsrPlatform;
import com.hojoai.gateway.omniasst.voice.service.v1.asr.AsrServiceFactory;
import com.hojoai.gateway.omniasst.voice.service.v1.asr.dto.AsrResult;
import com.hojoai.gateway.omniasst.voice.util.AudioConverter;
import com.hojoai.gateway.omniasst.voice.websocket.v1.dto.AsrAudioDataRequest;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import com.hojoai.gateway.omniasst.voice.websocket.bean.IMessageCustom;
import com.hojoai.gateway.omniasst.voice.websocket.bean.IMessageEvent;
import com.hojoai.gateway.omniasst.voice.websocket.bean.dto.AsrTextResponse;
import com.hojoai.gateway.omniasst.voice.websocket.bean.dto.ErrorResponse;
import org.springframework.web.socket.WebSocketSession;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import xyz.downgoon.snowflake.Snowflake;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * {@link AsrService} 实现：HTTP 场景下从 OSS/上传文件拉音频、按固定帧长分包调用 {@link AsrAPI}；
 * WebSocket 场景下维护会话状态并推送识别文本；并与 v1 {@link AsrServiceFactory} 协同做流式识别。
 */
@Service
public class AsrServiceImpl implements AsrService {

    private static final Logger logger = LoggerFactory.getLogger(AsrServiceImpl.class);
    private static final String DEFAULT_TASK = "all";
    private static final String DEFAULT_TYPE = "pcm";
    private static final String DEFAULT_LANG = "all";
    private static final int NOT_PACKAGE_END = 0;
    private static final int VAD_END = 60;
    /** 16kHz、16bit、单声道下约 20ms 一帧的字节数（320 采样 × 2 字节） */
    private static final int STANDARD_CHUNK_SIZE = 320 * 16 * 2;
    private final ConcurrentHashMap<String, SessionState> wsSessions = new ConcurrentHashMap<>();

    @Resource
    private Snowflake snowflake;
    @Resource
    private AsrAPI asrAPI;
    @Resource
    private RestTemplate restTemplate;
    @Resource
    private AudioConverter audioConverter;
    @Resource
    private AsrServiceFactory asrServiceFactory;

    private static class SessionState {
        private final AtomicInteger packageIndex = new AtomicInteger(0);
        private String vid;
        private String asrLanguage = "all";
        private String fullText = "";
    }

    private ASRRequest makeAsrRequest(String audioData, String vid, AtomicInteger packageIndex, String lang, String task, boolean isEnd) {
        ASRRequest request = new ASRRequest();
        request.setTask(task);
        request.setVid(vid);
        request.setDoItn(1);
        request.setPackageIndex(packageIndex.getAndIncrement());
        request.setPackageSize(audioData.length());
        request.setPackageEnd(isEnd ? 1 : NOT_PACKAGE_END);
        request.setType(DEFAULT_TYPE);
        request.setLang(StringUtils.isNotBlank(lang) ? lang : DEFAULT_LANG);
        request.setPackageData(audioData);
        request.setVadEnd(VAD_END);
        request.setDoLangDetect(0);
        return request;
    }

    private ASRResponse callAsrAPI(ASRRequest request, String vid) {
        try {
            ASRResponse response = asrAPI.asr(request);
            if (Objects.isNull(response) || response.getRtn() != 0) {
                logger.error("ASR error, vid: {}, packageIndex: {}, rtn: {}", vid, request.getPackageIndex(), response != null ? response.getRtn() : "null");
                return null;
            }
            return response;
        } catch (Exception e) {
            logger.error("ASR API call failed, vid: {}, packageIndex: {}", vid, request.getPackageIndex(), e);
            return null;
        }
    }

    /**
     * 为 WebSocket 会话创建或更新 ASR 语言偏好。
     *
     * @param session     当前连接
     * @param asrLanguage 识别语言，可空表示沿用默认
     */
    public void configureSession(WebSocketSession session, String asrLanguage) {
        String sessionId = session.getId();
        SessionState state = wsSessions.computeIfAbsent(sessionId, k -> {
            SessionState s = new SessionState();
            s.vid = String.valueOf(System.currentTimeMillis());
            return s;
        });
        if (StringUtils.isNotBlank(asrLanguage)) {
            state.asrLanguage = asrLanguage;
        }
    }

    /**
     * 处理 WebSocket 二进制音频：Base64 封装后按包调用实时 ASR，并通过自定义消息推送文本。
     *
     * @param session WebSocket 会话
     * @param payload 一帧 PCM 原始字节
     */
    @Override
    public void handleBinaryMessage(WebSocketSession session, ByteBuffer payload) {
        String sessionId = session.getId();
        SessionState state = wsSessions.computeIfAbsent(sessionId, k -> {
            SessionState s = new SessionState();
            s.vid = String.valueOf(System.currentTimeMillis());
            return s;
        });
        try {
            byte[] audioData = new byte[payload.remaining()];
            payload.get(audioData);
            String lang = StringUtils.isNotBlank(state.asrLanguage) ? state.asrLanguage : DEFAULT_LANG;
            ASRRequest asrRequest = makeAsrRequest(Base64.getEncoder().encodeToString(audioData), state.vid, state.packageIndex, lang, DEFAULT_TASK, false);
            ASRResponse asrResponse = callAsrAPI(asrRequest, state.vid);
            if (Objects.isNull(asrResponse)) {
                sendError(session, "ASR_ERROR", "ASR service returned error", "asr");
                return;
            }
            ASRResponse.ASRText textInfo = asrResponse.getText();
            if (Objects.nonNull(textInfo) && StringUtils.isNotBlank(textInfo.getText())) {
                state.fullText = textInfo.getText();
                AsrTextResponse response = new AsrTextResponse();
                response.setText(state.fullText);
                response.setIsEnd(asrResponse.getIsEnd());
                IMessageCustom.sendMessageCustom(session, IMessageEvent.BACK_ASR_TEXT, response);
            }
            if (asrResponse.getIsEnd() == 1) wsSessions.remove(sessionId);
        } catch (Exception e) {
            logger.error("Error processing WebSocket binary message", e);
            sendError(session, "ASR_ERROR", "Error processing audio: " + e.getMessage(), "asr");
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
     * 连接关闭时清理会话状态与 vid 映射。
     *
     * @param session 已关闭或正在关闭的会话
     */
    @Override
    public void handleConnectionClosed(WebSocketSession session) {
        String sessionId = session.getId();
        SessionState state = wsSessions.remove(sessionId);
        if (Objects.nonNull(state)) logger.info("ASR WebSocket session closed, sessionId: {}, vid: {}", sessionId, state.vid);
    }

    private File downloadAndConvertToPcm(String url) throws Exception {
        Path tempDir = Paths.get(System.getProperty("java.io.tmpdir"));
        File audioFile = tempDir.resolve("asr_" + System.currentTimeMillis() + ".wav").toFile();
        File pcmFile = tempDir.resolve("asr_" + System.currentTimeMillis() + ".pcm").toFile();
        try {
            logger.info("Downloading audio file from URL: {}", url);
            URI uri = new URI(url);
            byte[] audioData = restTemplate.getForObject(uri, byte[].class);
            if (audioData == null || audioData.length == 0) {
                throw new RuntimeException("Downloaded audio file is empty");
            }
            try (FileOutputStream fos = new FileOutputStream(audioFile)) {
                fos.write(audioData);
            }
            logger.info("Downloaded audio file, size: {} bytes", audioData.length);
            
            audioConverter.convertWavToPcm(audioFile, pcmFile.getAbsolutePath());
            return pcmFile;
        } finally {
            if (audioFile.exists()) Files.deleteIfExists(audioFile.toPath());
        }
    }

    private String processPcmStream(InputStream pcmStream, String vid, String lang, String task) throws IOException {
        String sessionId = "file-" + vid;
        com.hojoai.gateway.omniasst.voice.service.v1.asr.AsrService asrService =
            asrServiceFactory.getService(AsrPlatform.HOJO.getCode());
        
        if (asrService == null) {
            logger.error("No ASR service available for platform: {}", AsrPlatform.HOJO.getCode());
            throw new RuntimeException("ASR服务不可用");
        }

        logger.info("Starting ASR processing for sessionId: {}, vid: {}", sessionId, vid);
        
        List<String> textResults = new ArrayList<>();
        CountDownLatch completionLatch = new CountDownLatch(1);
        
        byte[] buffer = new byte[STANDARD_CHUNK_SIZE];
        byte[] pendingChunk = null;
        int bytesRead;
        
        try {
            while ((bytesRead = pcmStream.read(buffer)) > 0) {
                boolean isLastChunk = false;
                byte[] chunkToSend = null;
                
                if (bytesRead == STANDARD_CHUNK_SIZE) {
                    if (pendingChunk != null) {
                        chunkToSend = pendingChunk;
                    }
                    pendingChunk = new byte[STANDARD_CHUNK_SIZE];
                    System.arraycopy(buffer, 0, pendingChunk, 0, STANDARD_CHUNK_SIZE);
                } else {
                    if (pendingChunk != null) {
                        byte[] mergedChunk = new byte[pendingChunk.length + bytesRead];
                        System.arraycopy(pendingChunk, 0, mergedChunk, 0, pendingChunk.length);
                        System.arraycopy(buffer, 0, mergedChunk, pendingChunk.length, bytesRead);
                        chunkToSend = mergedChunk;
                    } else {
                        chunkToSend = new byte[bytesRead];
                        System.arraycopy(buffer, 0, chunkToSend, 0, bytesRead);
                    }
                    isLastChunk = true;
                    pendingChunk = null;
                }
                
                if (chunkToSend != null) {
                    logger.info("Sending audio chunk, sessionId: {}, bytes: {}, isLastChunk: {}", sessionId, chunkToSend.length, isLastChunk);
                    sendAudioChunk(sessionId, chunkToSend, isLastChunk, isLastChunk, asrService, textResults, completionLatch);
                }

                if (isLastChunk) {
                    logger.info("Last chunk sent for sessionId: {}", sessionId);
                    break;
                }
            }

            if (bytesRead <= 0) {
                logger.info("sending stop signal for sessionId: {}", sessionId);
                sendStopSignal(sessionId, asrService, textResults, completionLatch);
            }
            
            boolean completed = completionLatch.await(60, TimeUnit.SECONDS);
            if (!completed) {
                logger.error("ASR processing timeout,sessionId:{}",sessionId);
                throw new RuntimeException("ASR processing timeout");
            }
            logger.info("ASR processing completed for sessionId: {}, vid: {},  result length: {}", sessionId, vid, textResults.size());
            return String.join("", textResults);
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("ASR processing interrupted", e);
        } finally {
            logger.info("Cleaning up ASR sessionId: {}, vid: {}", sessionId, vid);
            asrService.stopRecognize(sessionId);
        }
    }
    
    private void sendAudioChunk(String sessionId, byte[] audioData, boolean isEnd, boolean isStop,
                                com.hojoai.gateway.omniasst.voice.service.v1.asr.AsrService asrService,
                                List<String> textResults, CountDownLatch completionLatch) {
        AsrAudioDataRequest request = new AsrAudioDataRequest();
        request.setAudioData(Base64.getEncoder().encodeToString(audioData));
        request.setIsEnd(isEnd);
        if (isStop) {
            request.setCustom("Stop");
        }
        
        asrService.recognize(sessionId, request, new AsrCallback() {
            @Override
            public void onResult(AsrResult result) {
                logger.info("ASR result for session {}: isEnd={}, text={}, custom={}", sessionId, result.getIsEnd(), result.getText(), result.getCustom());
                if (result.getIsEnd() != null && result.getIsEnd() == 1 && StringUtils.isNotBlank(result.getText())) {
                    textResults.add(result.getText());
                }
                if (StringUtils.isNotBlank(result.getCustom()) && result.getCustom().contains("Stop")) {
                    logger.info("Received stop signal for session: {}", sessionId);
                    completionLatch.countDown();
                }
            }

            @Override
            public void onVadInvalid() {
                logger.info("VAD Invalid, sessionId: {}", sessionId);
                textResults.add(StringUtils.EMPTY);
                completionLatch.countDown();
            }

            @Override
            public void onError(String errorMessage) {
                logger.error("ASR error for session {}: {}", sessionId, errorMessage);
                completionLatch.countDown();
            }
        });
    }
    
    private void sendStopSignal(String sessionId, com.hojoai.gateway.omniasst.voice.service.v1.asr.AsrService asrService,
                                List<String> textResults, CountDownLatch completionLatch) {
        byte[] emptyAudio = new byte[0];
        sendAudioChunk(sessionId, emptyAudio, true, true, asrService,
            textResults, completionLatch);
    }
    

    /**
     * 根据 OSS 音频地址下载、转 PCM 后整段识别，返回最终文本。
     *
     * @param request 含 {@code ossUrl} 与可选语言
     * @return 识别结果字符串
     * @throws RuntimeException 下载、转码或识别失败
     */
    @Override
    public String convertToText(AsrFileOssRequest request) {
        Objects.requireNonNull(request.getOssUrl(), "ossUrl must not be null");
        File pcmFile = null;
        try {
            pcmFile = downloadAndConvertToPcm(request.getOssUrl());
            String vid = "http-" + snowflake.nextId();
            String lang = StringUtils.isNotBlank(request.getLanguage()) ? request.getLanguage() : DEFAULT_LANG;
            try (FileInputStream fis = new FileInputStream(pcmFile)) {
                return processPcmStream(fis, vid, lang, DEFAULT_TASK);
            }
        } catch (Exception e) {
            logger.error("Error processing ASR from URL, ossUrl: {}", request.getOssUrl(), e);
            throw new RuntimeException("Error processing audio file from URL", e);
        } finally {
            if (pcmFile != null && pcmFile.exists()) {
                try {
                    Files.deleteIfExists(pcmFile.toPath());
                } catch (IOException e) {
                    logger.warn("Failed to delete PCM file: {}", pcmFile.getAbsolutePath(), e);
                }
            }
        }
    }

    /**
     * 同 {@link #convertToText}，但以 SSE 流式推送中间/最终结果。
     *
     * @param request OSS 请求
     * @return SSE Emitter，超时 300s
     */
    @Override
    public SseEmitter convertToTextStream(AsrFileOssRequest request) {
        Objects.requireNonNull(request.getOssUrl(), "ossUrl must not be null");
        SseEmitter emitter = new SseEmitter(300000L);
        File pcmFile = null;
        try {
            pcmFile = downloadAndConvertToPcm(request.getOssUrl());
            String vid = "sse-" + System.currentTimeMillis();
            String lang = StringUtils.isNotBlank(request.getLanguage()) ? request.getLanguage() : DEFAULT_LANG;
            try (FileInputStream fis = new FileInputStream(pcmFile)) {
                processPcmStreamForSse(fis, vid, lang, DEFAULT_TASK, emitter);
            }
        } catch (Exception e) {
            logger.error("Error processing ASR stream from URL, ossUrl: {}", request.getOssUrl(), e);
            emitter.completeWithError(e);
        } finally {
            if (pcmFile != null && pcmFile.exists()) {
                try {
                    Files.deleteIfExists(pcmFile.toPath());
                } catch (IOException e) {
                    logger.warn("Failed to delete PCM file: {}", pcmFile.getAbsolutePath(), e);
                }
            }
        }
        return emitter;
    }

    /**
     * 响应式流式识别，按片段下发文本并在结束后清理临时 PCM。
     *
     * @param request OSS 请求
     * @return 文本片段流
     */
    @Override
    public Flux<String> convertToTextStreamReactive(AsrFileOssRequest request) {
        Objects.requireNonNull(request.getOssUrl(), "ossUrl must not be null");
        return Mono.fromCallable(() -> downloadAndConvertToPcm(request.getOssUrl()))
                .subscribeOn(Schedulers.boundedElastic())
                .flatMapMany(pcmFile -> {
                    String vid = "sse-" + System.currentTimeMillis();
                    String lang = StringUtils.isBlank(request.getLanguage()) ? DEFAULT_LANG : request.getLanguage();
                    return processPcmStreamReactive(pcmFile, vid, lang, DEFAULT_TASK)
                            .doFinally(signalType -> {
                                if (pcmFile != null && pcmFile.exists()) {
                                    try {
                                        Files.deleteIfExists(pcmFile.toPath());
                                    } catch (IOException e) {
                                        logger.warn("Failed to delete PCM file: {}", pcmFile.getAbsolutePath(), e);
                                    }
                                }
                            });
                })
                .onErrorResume(e -> {
                    logger.error("Error processing ASR stream from URL, ossUrl: {}", request.getOssUrl(), e);
                    return Flux.error(new RuntimeException("Error processing audio file", e));
                });
    }

    private Flux<String> processPcmStreamReactive(File pcmFile, String vid, String lang, String task) {
        return Flux.create(sink -> {
            try (FileInputStream fis = new FileInputStream(pcmFile)) {
                AtomicInteger packageIndex = new AtomicInteger(0);
                byte[] buffer = new byte[STANDARD_CHUNK_SIZE];
                byte[] pendingChunk = null;
                int bytesRead;
                while ((bytesRead = fis.read(buffer)) > 0) {
                    if (bytesRead == STANDARD_CHUNK_SIZE) {
                        if (pendingChunk != null) {
                            String text = processChunkForReactive(pendingChunk, vid, packageIndex, lang, task, false);
                            if (text != null) {
                                sink.next(text);
                            }
                        }
                        pendingChunk = new byte[STANDARD_CHUNK_SIZE];
                        System.arraycopy(buffer, 0, pendingChunk, 0, STANDARD_CHUNK_SIZE);
                    } else {
                        byte[] finalChunk = mergeChunks(pendingChunk, buffer, bytesRead);
                        String text = processChunkForReactive(finalChunk, vid, packageIndex, lang, task, true);
                        if (text != null) {
                            sink.next(text);
                        }
                        sink.complete();
                        return;
                    }
                }
                if (pendingChunk != null) {
                    String text = processChunkForReactive(pendingChunk, vid, packageIndex, lang, task, true);
                    if (text != null) {
                        sink.next(text);
                    }
                }
                sink.complete();
            } catch (Exception e) {
                sink.error(e);
            }
        });
    }

    /**
     * 校验上传 WAV（≤5MB），转 PCM 后走与 OSS 相同的分片识别流程。
     *
     * @param file 多部分上传的 WAV
     * @return 完整识别文本
     * @throws IllegalArgumentException 空文件、超大小或非 WAV
     * @throws Exception                转码或识别异常
     */
    @Override
    public String convertToTextFromFile(MultipartFile file) throws Exception {
        if (Objects.isNull(file) || file.isEmpty()) {
            throw new IllegalArgumentException("文件不能为空");
        }
        if (file.getSize() > 5 * 1024 * 1024L) {
            throw new IllegalArgumentException("文件大小超过限制，最大5MB");
        }
        long convertStartTime = System.currentTimeMillis();
        File tempWavFile = null;
        File pcmFile = null;
        String vid = "file-" + snowflake.nextId();
        try {
            if (!isWavFile(file)) {
                throw new IllegalArgumentException("文件格式不正确，仅支持WAV格式");
            }
            Path tempDir = Paths.get(System.getProperty("java.io.tmpdir"));
            tempWavFile = tempDir.resolve("asr_upload_" + System.currentTimeMillis() + ".wav").toFile();
            file.transferTo(tempWavFile);
            pcmFile = tempDir.resolve("asr_file_" + System.currentTimeMillis() + ".pcm").toFile();
            audioConverter.convertWavToPcm(tempWavFile, pcmFile.getAbsolutePath());
            try (FileInputStream fis = new FileInputStream(pcmFile)) {
                return processPcmStream(fis, vid, DEFAULT_LANG, DEFAULT_TASK);
            }
        } finally {
            long convertCostTime = System.currentTimeMillis() - convertStartTime;
            logger.info("[asrFile接口] vid:{}, tempWavFile: {}, pcmFile: {}, costTime: {}ms", vid, tempWavFile.getAbsolutePath(), pcmFile.getAbsolutePath(), convertCostTime);
            if (Objects.nonNull(tempWavFile) && tempWavFile.exists()) {
                try {
                    Files.deleteIfExists(tempWavFile.toPath());
                } catch (IOException e) {
                    logger.warn("Failed to delete temp WAV file: {}", tempWavFile.getAbsolutePath(), e);
                }
            }
            if (Objects.nonNull(pcmFile) && pcmFile.exists()) {
                try {
                    Files.deleteIfExists(pcmFile.toPath());
                } catch (IOException e) {
                    logger.warn("Failed to delete PCM file: {}", pcmFile.getAbsolutePath(), e);
                }
            }
        }
    }

    private boolean isWavFile(MultipartFile file) {
        try (var inputStream = file.getInputStream()) {
            byte[] header = new byte[4];
            if (inputStream.read(header) != 4) return false;
            byte[] wavHeader = new byte[]{0x52, 0x49, 0x46, 0x46};
            for (int i = 0; i < 4; i++) {
                if (header[i] != wavHeader[i]) return false;
            }
            return true;
        } catch (IOException e) {
            logger.error("Error reading file header", e);
            return false;
        }
    }

    private String processChunkForReactive(byte[] chunk, String vid, AtomicInteger packageIndex, String lang, String task, boolean isEnd) {
        try {
            ASRRequest asrRequest = makeAsrRequest(Base64.getEncoder().encodeToString(chunk), vid, packageIndex, lang, task, isEnd);
            ASRResponse asrResponse = callAsrAPI(asrRequest, vid);
            if (asrResponse != null) {
                ASRResponse.ASRText textInfo = asrResponse.getText();
                String text = textInfo != null ? textInfo.getText() : "";
                AsrTextResponse response = new AsrTextResponse();
                response.setText(text);
                response.setIsEnd(asrResponse.getIsEnd());
                return com.hojoai.common.jackson.JacksonUtil.bean2Json(response);
            }
        } catch (Exception e) {
            logger.error("Error processing chunk", e);
        }
        return null;
    }

    private void processPcmStreamForSse(InputStream pcmStream, String vid, String lang, String task, SseEmitter emitter) throws Exception {
        AtomicInteger packageIndex = new AtomicInteger(0);
        byte[] buffer = new byte[STANDARD_CHUNK_SIZE];
        byte[] pendingChunk = null;
        int bytesRead;

        while ((bytesRead = pcmStream.read(buffer)) > 0) {
            if (bytesRead == STANDARD_CHUNK_SIZE) {
                // 如果有待发送的chunk，先发送它
                if (pendingChunk != null) {
                    if (sendChunkToAsrAndEmit(emitter, pendingChunk, vid, packageIndex, lang, task, false)) {
                        return; // 流已结束
                    }
                }
                // 保存当前标准大小的chunk，等待下次循环
                pendingChunk = new byte[STANDARD_CHUNK_SIZE];
                System.arraycopy(buffer, 0, pendingChunk, 0, STANDARD_CHUNK_SIZE);
            } else {
                // 读取到最后一个小于标准大小的chunk，与pendingChunk合并后发送
                byte[] finalChunk = mergeChunks(pendingChunk, buffer, bytesRead);
                sendChunkToAsrAndEmit(emitter, finalChunk, vid, packageIndex, lang, task, true);
                emitter.complete();
                return;
            }
        }

        // 流读取完毕，如果还有pendingChunk，发送最后一个chunk
        if (pendingChunk != null) {
            sendChunkToAsrAndEmit(emitter, pendingChunk, vid, packageIndex, lang, task, true);
        }
        emitter.complete();
    }

    private byte[] mergeChunks(byte[] pendingChunk, byte[] buffer, int bytesRead) {
        if (pendingChunk != null) {
            byte[] mergedChunk = new byte[pendingChunk.length + bytesRead];
            System.arraycopy(pendingChunk, 0, mergedChunk, 0, pendingChunk.length);
            System.arraycopy(buffer, 0, mergedChunk, pendingChunk.length, bytesRead);
            return mergedChunk;
        } else {
            byte[] chunk = new byte[bytesRead];
            System.arraycopy(buffer, 0, chunk, 0, bytesRead);
            return chunk;
        }
    }

    /**
     * 发送chunk到ASR API并通过SSE发送响应
     * @return true表示流应该结束（isEnd=1），false表示继续处理
     */
    private boolean sendChunkToAsrAndEmit(SseEmitter emitter, byte[] chunk, String vid, AtomicInteger packageIndex,
                                          String lang, String task, boolean isEnd) throws Exception {
        ASRRequest asrRequest = makeAsrRequest(Base64.getEncoder().encodeToString(chunk), vid, packageIndex, lang, task, isEnd);
        ASRResponse asrResponse = callAsrAPI(asrRequest, vid);
        if (asrResponse != null) {
            sendAsrTextResponseToSse(emitter, asrResponse);
            return asrResponse.getIsEnd() == 1; // 返回是否应该结束流
        }
        return false;
    }

    private void sendAsrTextResponseToSse(SseEmitter emitter, ASRResponse asrResponse) throws Exception {
        ASRResponse.ASRText textInfo = asrResponse.getText();
        String text = textInfo != null ? textInfo.getText() : "";
        AsrTextResponse response = new AsrTextResponse();
        response.setText(text);
        response.setIsEnd(asrResponse.getIsEnd());
        emitter.send(SseEmitter.event().data(com.hojoai.common.jackson.JacksonUtil.bean2Json(response)));
    }
}
