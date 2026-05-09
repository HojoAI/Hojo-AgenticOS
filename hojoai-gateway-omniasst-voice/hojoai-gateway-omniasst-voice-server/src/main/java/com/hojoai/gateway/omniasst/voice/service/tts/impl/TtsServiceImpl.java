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

import com.hojoai.gateway.omniasst.voice.api.TtsAPI;
import com.hojoai.gateway.omniasst.voice.api.dto.TTSRequest;
import com.hojoai.gateway.omniasst.voice.constants.GlobalConstants;
import com.hojoai.gateway.omniasst.voice.service.tts.TtsService;
import com.hojoai.gateway.omniasst.voice.service.tts.dto.TtsRequest;
import com.hojoai.gateway.omniasst.voice.service.tts.dto.TtsResponse;
import com.hojoai.gateway.omniasst.voice.util.AudioConverter;
import com.hojoai.gateway.omniasst.voice.util.OssUtil;
import feign.Response;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.UUID;

/**
 * {@link TtsService} 默认实现：通过 Feign 调用下游 {@link TtsAPI} 获取 PCM 流，再转 WAV、上传 OSS 或以 SSE/Reactive/裸流返回。
 * 依赖 {@link AudioConverter}、{@link OssUtil} 与配置项 {@code gateway.tts.audio.save-path}。
 */
@Service
public class TtsServiceImpl implements TtsService {

    private static final Logger logger = LoggerFactory.getLogger(TtsServiceImpl.class);

    @Resource
    private TtsAPI ttsAPI;
    @Resource
    private AudioConverter audioConverter;
    @Resource
    private OssUtil ossUtil;

    @Value("${gateway.tts.audio.save-path:./audio}")
    private String audioSavePath;

    /**
     * 调用 TTS API 写 PCM 临时文件，再转为 WAV 并删除 PCM。
     */
    private File synthesizeToWavFile(TtsRequest request) throws Exception {
        Response ttsResponse = callTtsApi(request);
        if (Objects.isNull(ttsResponse)) {
            throw new RuntimeException("TTS synthesis failed");
        }
        Path saveDir = Paths.get(audioSavePath);
        if (!Files.exists(saveDir)) Files.createDirectories(saveDir);
        String baseFileName = "tts_" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 8);
        String pcmFileName = baseFileName + ".pcm";
        File pcmFile = saveDir.resolve(pcmFileName).toFile();
        try (InputStream inputStream = ttsResponse.body().asInputStream(); FileOutputStream fos = new FileOutputStream(pcmFile)) {
            byte[] checkBuffer = new byte[1024];
            int checkBytesRead = inputStream.read(checkBuffer);
            if (checkBytesRead == -1) {
                throw new RuntimeException("TTS service returned empty response");
            }
            fos.write(checkBuffer, 0, checkBytesRead);
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                fos.write(buffer, 0, bytesRead);
            }
        }
        String wavFileName = baseFileName + ".wav";
        String wavFilePath = saveDir.resolve(wavFileName).toString();
        File wavFile = audioConverter.convertPcmToWav(pcmFile, wavFilePath);
        if (pcmFile.exists()) {
            try {
                Files.delete(pcmFile.toPath());
            } catch (IOException e) {
                logger.warn("Failed to delete PCM file: {}", pcmFile.getAbsolutePath(), e);
            }
        }
        return wavFile;
    }

    /**
     * 合成语音并返回本地 WAV 文件引用（调用方负责后续清理策略）。
     *
     * @param request 文本、语言、音色等，{@code text} 必填
     * @return 含 {@link TtsResponse#getFile()} 的响应
     * @throws RuntimeException 合成或转码失败
     */
    @Override
    public TtsResponse convertToSpeechFile(TtsRequest request) {
        Objects.requireNonNull(request.getText(), "text must not be null");
        File wavFile = null;
        try {
            wavFile = synthesizeToWavFile(request);
            TtsResponse response = new TtsResponse();
            response.setFile(wavFile);
            return response;
        } catch (Exception e) {
            logger.error("Error synthesizing TTS to file", e);
            if (wavFile != null && wavFile.exists()) {
                try {
                    Files.delete(wavFile.toPath());
                } catch (IOException ex) {
                    logger.warn("Failed to delete WAV file: {}", wavFile.getAbsolutePath(), ex);
                }
            }
            throw new RuntimeException("Error synthesizing speech to file", e);
        }
    }

    /**
     * 合成 WAV 并上传 OSS，返回访问 URL，本地临时文件会尝试删除。
     *
     * @param request 合成参数，{@code text} 必填
     * @return 含 {@link TtsResponse#getOssUrl()} 的响应
     * @throws RuntimeException 合成、转码或上传失败
     */
    @Override
    public TtsResponse convertToSpeechOss(TtsRequest request) {
        Objects.requireNonNull(request.getText(), "text must not be null");
        File wavFile = null;
        try {
            wavFile = synthesizeToWavFile(request);
            String wavFileName = wavFile.getName();
            String objectKey = "tts/" + wavFileName;
            String ossUrl = ossUtil.uploadFile(objectKey, wavFile);
            TtsResponse response = new TtsResponse();
            response.setOssUrl(ossUrl);
            if (wavFile.exists()) {
                try {
                    Files.delete(wavFile.toPath());
                } catch (IOException e) {
                    logger.warn("Failed to delete WAV file: {}", wavFile.getAbsolutePath(), e);
                }
            }
            return response;
        } catch (Exception e) {
            logger.error("Error synthesizing TTS to OSS", e);
            if (wavFile != null && wavFile.exists()) {
                try {
                    Files.delete(wavFile.toPath());
                } catch (IOException ex) {
                    logger.warn("Failed to delete WAV file: {}", wavFile.getAbsolutePath(), ex);
                }
            }
            throw new RuntimeException("Error synthesizing speech to OSS", e);
        }
    }

    /**
     * 以 SSE 分块推送 PCM 音频（与 {@link #convertToSpeechStreamReactive} 数据源相同，Servlet 栈使用）。
     *
     * @param request 合成参数
     * @return 长连接 Emitter，超时 300s；失败时 {@link SseEmitter#completeWithError}
     */
    @Override
    public SseEmitter convertToSpeechStream(TtsRequest request) {
        Objects.requireNonNull(request.getText(), "text must not be null");
        SseEmitter emitter = new SseEmitter(300000L);
        try {
            Response ttsResponse = callTtsApi(request);
            if (ttsResponse == null) {
                emitter.completeWithError(new RuntimeException("TTS synthesis failed"));
                return emitter;
            }

            byte[] buffer = new byte[1024];
            try (InputStream inputStream = ttsResponse.body().asInputStream()) {
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    byte[] chunk = new byte[bytesRead];
                    System.arraycopy(buffer, 0, chunk, 0, bytesRead);
                    emitter.send(SseEmitter.event().data(chunk));
                }
                emitter.complete();
            } catch (Exception e) {
                logger.error("Error reading TTS stream for SSE", e);
                emitter.completeWithError(e);
            }
        } catch (Exception e) {
            logger.error("TTS stream error", e);
            emitter.completeWithError(e);
        }

        return emitter;
    }

    /**
     * 响应式流式 TTS，按块发出 PCM 字节数组。
     *
     * @param request 合成参数
     * @return 冷/热流由调度决定；错误时 Flux 失败
     */
    @Override
    public Flux<byte[]> convertToSpeechStreamReactive(TtsRequest request) {
        Objects.requireNonNull(request.getText(), "text must not be null");
        return Mono.fromCallable(() -> callTtsApi(request))
                .subscribeOn(Schedulers.boundedElastic())
                .flatMapMany(ttsResponse -> {
                    if (ttsResponse == null) {
                        return Flux.<byte[]>error(new RuntimeException("TTS synthesis failed"));
                    }
                    return Flux.<byte[]>create(sink -> {
                        try (InputStream inputStream = ttsResponse.body().asInputStream()) {
                            byte[] buffer = new byte[1024];
                            int bytesRead;
                            while ((bytesRead = inputStream.read(buffer)) != -1) {
                                byte[] chunk = new byte[bytesRead];
                                System.arraycopy(buffer, 0, chunk, 0, bytesRead);
                                sink.next(chunk);
                            }
                            sink.complete();
                        } catch (Exception e) {
                            logger.error("Error reading TTS stream", e);
                            sink.error(e);
                        }
                    });
                })
                .onErrorResume(e -> {
                    logger.error("TTS stream error", e);
                    return Flux.<byte[]>error(new RuntimeException("TTS stream error", e));
                });
    }

    /**
     * HTTP {@code application/octet-stream} 直出 PCM，适合非 SSE 的二进制流客户端。
     *
     * @param request 合成参数
     * @return 200 + 流式 body；下游非 200 时返回 500 无 body
     */
    @Override
    public ResponseEntity<StreamingResponseBody> convertToPcmStream(TtsRequest request) {
        try {
            Response ttsResponse = callTtsApi(request);
            if (ttsResponse == null || ttsResponse.status() != 200) {
                logger.error("TTS API call failed, status: {}", ttsResponse != null ? ttsResponse.status() : "null");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }
            StreamingResponseBody stream = outputStream -> {
                try (InputStream inputStream = ttsResponse.body().asInputStream()) {
                    byte[] buffer = new byte[8192];
                    int bytesRead;
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, bytesRead);
                        outputStream.flush();
                    }
                } catch (Exception e) {
                    logger.error("Error streaming TTS PCM data", e);
                    throw e;
                } finally {
                    if (ttsResponse.body() != null) try { ttsResponse.body().close(); } catch (Exception e) { logger.warn("Error closing TTS response body", e); }
                }
            };
            return ResponseEntity.ok().contentType(MediaType.parseMediaType("application/octet-stream")).body(stream);
        } catch (Exception e) {
            logger.error("Error processing TTS PCM stream request", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    private Response callTtsApi(TtsRequest request) {
        String lang = StringUtils.isNotEmpty(request.getLanguage()) ? request.getLanguage() : GlobalConstants.DEFAULT_TTS_LANG;
        String voiceId;
        if (StringUtils.isNotEmpty(request.getVoiceId())) {
            voiceId = request.getVoiceId();
        } else {
            if (GlobalConstants.LANGUAGE_ENGLISH_STRING.equalsIgnoreCase(lang)) {
                voiceId = GlobalConstants.DEFAULT_ENGLISH_TTS_VOICE_ID;
            } else if (GlobalConstants.LANGUAGE_CHINESE_STRING.equalsIgnoreCase(lang)) {
                voiceId = GlobalConstants.DEFAULT_CHINESE_TTS_VOICE_ID;
            } else if (GlobalConstants.LANGUAGE_FRENCH_STRING.equalsIgnoreCase(lang)) {
                voiceId = GlobalConstants.DEFAULT_FRENCH_TTS_VOICE_ID;
            } else {
                voiceId = GlobalConstants.DEFAULT_TTS_VOICE_ID;
            }
        }
        TTSRequest ttsRequest = new TTSRequest();
        ttsRequest.setText(request.getText());
        ttsRequest.setLanguage(lang);
        ttsRequest.setVoiceId(voiceId);
        Response ttsResponse = ttsAPI.tts(ttsRequest);
        if (ttsResponse.status() != 200) {
            logger.error("TTS service returned error: {}", ttsResponse.status());
            return null;
        }
        return ttsResponse;
    }
}

