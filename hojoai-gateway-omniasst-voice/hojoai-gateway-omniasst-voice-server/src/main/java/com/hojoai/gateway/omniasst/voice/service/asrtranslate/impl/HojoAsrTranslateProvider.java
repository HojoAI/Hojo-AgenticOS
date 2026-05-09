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
package com.hojoai.gateway.omniasst.voice.service.asrtranslate.impl;

import com.hojoai.gateway.omniasst.voice.api.AsrAPI;
import com.hojoai.gateway.omniasst.voice.api.dto.ASRRequest;
import com.hojoai.gateway.omniasst.voice.api.dto.ASRResponse;
import com.hojoai.gateway.omniasst.voice.api.dto.AsrTransRequest;
import com.hojoai.gateway.omniasst.voice.api.dto.AsrTransResponse;
import com.hojoai.gateway.omniasst.voice.service.asrtranslate.AsrTranslateProvider;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.WebSocketSession;

import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 基于 {@link AsrAPI} 的「先语种检测 ASR、再音频翻译」链路实现，供 WebSocket 翻译 Handler 在非 Azure 模式下使用。
 */
public class HojoAsrTranslateProvider implements AsrTranslateProvider {
    private static final Logger logger = LoggerFactory.getLogger(HojoAsrTranslateProvider.class);

    private final AsrAPI asrAPI;

    /**
     * @param asrAPI Feign ASR + 翻译统一入口
     */
    public HojoAsrTranslateProvider(AsrAPI asrAPI) {
        this.asrAPI = asrAPI;
    }

    private String sessionId;
    private String lang1;
    private String lang2;
    private String asrVid;
    private String transVid;
    private ResultCallback callback;
    private ByteArrayOutputStream audioBuffer;
    private volatile String detectedLang;
    private volatile String recognizedText;
    private volatile boolean langDetected;
    private final AtomicInteger asrPackageIndex = new AtomicInteger(0);
    private final AtomicInteger transPackageIndex = new AtomicInteger(0);
    private final AtomicLong lastAccessTime = new AtomicLong(System.currentTimeMillis());

    /**
     * {@inheritDoc}
     */
    @Override
    public void configure(String sessionId, String lang1, String lang2) {
        this.sessionId = sessionId;
        this.lang1 = lang1;
        this.lang2 = lang2;
        this.audioBuffer = new ByteArrayOutputStream();
        this.asrVid = sessionId + "_asr";
        this.transVid = sessionId + "_trans";
        logger.info("[Hojo会话配置] sessionId: {}, 目标语种: [{}, {}]", sessionId, lang1, lang2);
    }

    /**
     * {@inheritDoc}
     * <p>语种未定时走 ASR 检测；确定后缓存音频转发翻译接口。</p>
     */
    @Override
    public void pushAudio(WebSocketSession session, byte[] audioData) {
        lastAccessTime.set(System.currentTimeMillis());

        if (langDetected) {
            sendToTranslation(audioData);
        } else {
            audioBuffer.write(audioData, 0, audioData.length);
            sendToAsr(audioData);
        }
    }

    private void sendToAsr(byte[] audioData) {
        try {
            ASRRequest request = new ASRRequest();
            request.setVid(asrVid);
            request.setPackageIndex(asrPackageIndex.getAndIncrement());
            request.setPackageData(Base64.getEncoder().encodeToString(audioData));
            request.setPackageSize(audioData.length);
            request.setDoLangDetect(1);
            request.setType("pcm");
            request.setLang("all");

            ASRResponse response = asrAPI.asr(request);
            if (response == null || response.getRtn() != 0) {
                logger.warn("[Hojo ASR失败] sessionId: {}, rtn: {}, msg: {}", 
                        sessionId, response != null ? response.getRtn() : "null", 
                        response != null ? response.getMsg() : "null");
                return;
            }

            String lang = response.getLangDetect();
            if (StringUtils.isNotBlank(lang) && !"<UNK>".equals(lang)) {
                if (!langDetected) {
                    detectedLang = lang;
                    langDetected = true;
                    logger.info("[Hojo检测到语种] sessionId: {}, 语种: {}", sessionId, detectedLang);

                    if (!lang1.equals(detectedLang) && !lang2.equals(detectedLang)) {
                        logger.warn("[语种不在目标列表中] sessionId: {}, 检测到: {}, 目标: [{}, {}]", 
                                sessionId, detectedLang, lang1, lang2);
                        return;
                    }

                    sendCachedAudioToTranslation();
                }
            }

            String text = response.getText() != null ? response.getText().getText() : null;
            if (StringUtils.isNotBlank(text)) {
                recognizedText = text;
                if (langDetected) {
                    sendTemporaryResult();
                }
            }
        } catch (Exception e) {
            logger.error("[Hojo ASR异常] sessionId: {}", sessionId, e);
            if (callback != null) {
                callback.onError("ASR_ERROR", e.getMessage());
            }
        }
    }

    private void sendCachedAudioToTranslation() {
        if (audioBuffer.size() > 0) {
            byte[] cachedAudio = audioBuffer.toByteArray();
            audioBuffer.reset();
            sendToTranslation(cachedAudio);
        }
    }

    private void sendTemporaryResult() {
        if (callback == null || !langDetected || StringUtils.isBlank(detectedLang)) {
            return;
        }

        String targetLang = lang1.equals(detectedLang) ? lang2 : lang1;
        Map<String, String> translations = new HashMap<>();
        translations.put(detectedLang, recognizedText);
        translations.put(targetLang, "");

        callback.onResult(translations, detectedLang, false);
    }

    private void sendToTranslation(byte[] audioData) {
        if (!langDetected || StringUtils.isBlank(detectedLang)) {
            return;
        }

        String targetLang = lang1.equals(detectedLang) ? lang2 : lang1;

        try {
            AsrTransRequest request = new AsrTransRequest();
            request.setVid(transVid);
            request.setPackageIndex(transPackageIndex.getAndIncrement());
            request.setPackageData(Base64.getEncoder().encodeToString(audioData));
            request.setPackageSize(audioData.length);
            request.setSourceLang(detectedLang);
            request.setLang(targetLang);
            request.setType("pcm");

            AsrTransResponse response = asrAPI.asrTrans(request);
            if (response == null || response.getRtn() != 0) {
                logger.warn("[Hojo翻译失败] sessionId: {}, rtn: {}, msg: {}", 
                        sessionId, response != null ? response.getRtn() : "null", 
                        response != null ? response.getMsg() : "null");
                return;
            }

            String translatedText = response.getText() != null ? response.getText().getText() : null;
            if (StringUtils.isNotBlank(translatedText)) {
                boolean isEnd = response.getIsEnd() == 1;
                Map<String, String> translations = new HashMap<>();
                translations.put(detectedLang, recognizedText);
                translations.put(targetLang, translatedText);

                logger.info("[Hojo翻译结果] sessionId: {}, 识别: [{}], 翻译: [{}], isEnd: {}", 
                        sessionId, recognizedText, translatedText, isEnd);

                if (callback != null) {
                    callback.onResult(translations, detectedLang, isEnd);
                }
            }
        } catch (Exception e) {
            logger.error("[Hojo翻译异常] sessionId: {}", sessionId, e);
            if (callback != null) {
                callback.onError("TRANSLATION_ERROR", e.getMessage());
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setResultCallback(ResultCallback callback) {
        this.callback = callback;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() {
        logger.info("[Hojo会话关闭] sessionId: {}", sessionId);
    }

    /**
     * @return 最近一次 {@link #pushAudio} 的时间戳（毫秒），用于空闲清理
     */
    public long getLastAccessTime() {
        return lastAccessTime.get();
    }
}
