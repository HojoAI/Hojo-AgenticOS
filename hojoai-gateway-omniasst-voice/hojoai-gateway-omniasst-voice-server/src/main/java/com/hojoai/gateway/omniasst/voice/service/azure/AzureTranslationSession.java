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
package com.hojoai.gateway.omniasst.voice.service.azure;

import com.hojoai.common.jackson.JacksonUtil;
import com.microsoft.cognitiveservices.speech.*;
import com.microsoft.cognitiveservices.speech.audio.AudioConfig;
import com.microsoft.cognitiveservices.speech.audio.AudioInputStream;
import com.microsoft.cognitiveservices.speech.audio.AudioStreamFormat;
import com.microsoft.cognitiveservices.speech.audio.PushAudioInputStream;
import com.microsoft.cognitiveservices.speech.translation.SpeechTranslationConfig;
import com.microsoft.cognitiveservices.speech.translation.TranslationRecognitionResult;
import com.microsoft.cognitiveservices.speech.translation.TranslationRecognizer;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Azure翻译会话
 * 封装单个翻译会话的Azure SDK交互逻辑
 * 管理音频流推送、事件监听和结果缓存
 */
public class AzureTranslationSession {

    private static final Logger logger = LoggerFactory.getLogger(AzureTranslationSession.class);

    private final String vid;
    private final PushAudioInputStream pushStream;
    private final TranslationRecognizer recognizer;
    private final AtomicLong lastAccessTime;
    private final String transTargetLang;

    private volatile String tmpText = "";
    private volatile String tmpTranslation = "";
    private volatile String finalText = "";
    private volatile String finalTranslation = "";
    private volatile String detectedLanguage = "";
    private volatile boolean sentenceDone = false;
    private volatile CountDownLatch recognizedLatch = new CountDownLatch(1);
    private volatile ResultCallback resultCallback;

    public interface ResultCallback {
        void onRecognizing(String text, String translation);
        void onRecognized(String text, String translation, String detectedLanguage);
        void onError(String errorDetails);
    }

    /**
     * 创建Azure翻译会话
     * 支持源语言指定或自动检测
     * 
     * @param vid 会话ID
     * @param subscriptionKey Azure订阅密钥
     * @param region Azure服务区域
     * @param sourceLanguage 源语言(业务代码，如chinese)，如果为空则自动检测
     * @param targetLanguage 目标语言(业务代码，如english)
     */
    public AzureTranslationSession(String vid, String subscriptionKey, String region, String sourceLanguage, String targetLanguage) {
        this.vid = vid;
        this.lastAccessTime = new AtomicLong(System.currentTimeMillis());

        try {
            // 转换目标语言代码
            String recognitionLanguage = AzureLanguageMapper.toRecognitionLanguage(sourceLanguage); // 音频语种
            logger.info("[翻译][源音频语种], sessionId: {}, 语种: {}", vid, recognitionLanguage);

            String transSourceLang = AzureLanguageMapper.toTranslationLanguage(sourceLanguage);
            this.transTargetLang = AzureLanguageMapper.toTranslationLanguage(targetLanguage);
            logger.info("[翻译][目标语种], sessionId: {}, 语种: {}", vid, transTargetLang);

            AudioStreamFormat format = AudioStreamFormat.getWaveFormatPCM(16000, (short)16, (short)1);
            this.pushStream = AudioInputStream.createPushStream(format);

            // 配置Azure翻译服务
            SpeechTranslationConfig config = SpeechTranslationConfig.fromSubscription(subscriptionKey, region);
            config.setProperty(PropertyId.Speech_SegmentationSilenceTimeoutMs, "1000");
            config.setSpeechRecognitionLanguage(recognitionLanguage);
            config.addTargetLanguage(transSourceLang);
            config.addTargetLanguage(transTargetLang);

            // 创建音频配置
            AudioConfig audioConfig = AudioConfig.fromStreamInput(pushStream);

            // 自动检测开启会导致非常慢
            this.recognizer = new TranslationRecognizer(config, audioConfig);
            logger.info("[翻译][创建Azure会话], sessionId: {}, 目标语言: {} -> {}", vid, recognitionLanguage, transTargetLang);

            recognizer.recognizing.addEventListener((s, e) -> {
                if (e.getResult().getReason() == ResultReason.TranslatingSpeech) {
                    Map<String, String> translations = e.getResult().getTranslations();
                    logger.info("[翻译][识别中], 原始输出: {}", JacksonUtil.bean2Json(translations));
                    this.tmpText = e.getResult().getText();
                    this.tmpTranslation = translations != null ? translations.getOrDefault(transTargetLang, "") : "";
                    if (resultCallback != null && StringUtils.isNotBlank(tmpText)) {
                        resultCallback.onRecognizing(tmpText, tmpTranslation);
                    }
                }
            });

            recognizer.recognized.addEventListener((s, e) -> {
                if (e.getResult().getReason() == ResultReason.TranslatedSpeech) {
                    this.finalText = e.getResult().getText();
                    Map<String, String> translations = e.getResult().getTranslations();
                    logger.info("[翻译][识别完成], 原始输出: {}", JacksonUtil.bean2Json(translations));
                    this.finalTranslation = translations != null ? translations.getOrDefault(transTargetLang, "") : "";
                    this.detectedLanguage = extractDetectedLanguage(e.getResult());
                    this.sentenceDone = true;
                    this.recognizedLatch.countDown();
                    
                    if (resultCallback != null && StringUtils.isNotBlank(finalText)) {
                        resultCallback.onRecognized(finalText, finalTranslation, detectedLanguage);
                    }
                } else if (e.getResult().getReason() == ResultReason.NoMatch) {
                    logger.debug("[翻译][Azure无匹配] sessionId: {}", vid);
                }
            });

            recognizer.canceled.addEventListener((s, e) -> {
                logger.error("Azure识别取消, vid: {}, reason: {}, errorDetails: {}", vid, e.getReason(), e.getErrorDetails());
                this.sentenceDone = true;
                this.recognizedLatch.countDown();
                if (resultCallback != null) {
                    resultCallback.onError(e.getErrorDetails());
                }
            });

            // 启动连续识别
            recognizer.startContinuousRecognitionAsync().get();
            logger.info("Azure翻译会话已启动, vid: {}", vid);

        } catch (Exception e) {
            logger.error("创建Azure翻译会话失败, vid: {}", vid, e);
            throw new RuntimeException("Failed to create Azure translation session", e);
        }
    }

    public void pushAudio(byte[] audioData) {
        try {
            pushStream.write(audioData);
            lastAccessTime.set(System.currentTimeMillis());
        } catch (Exception e) {
            logger.error("[翻译][推送音频到Azure失败], sessionId: {}", vid, e);
        }
    }

    public void close() {
        try {
            if (recognizer != null) {
                recognizer.stopContinuousRecognitionAsync().get();
                recognizer.close();
            }
            if (pushStream != null) {
                pushStream.close();
            }
            logger.info("Azure翻译会话已关闭, vid: {}", vid);
        } catch (Exception e) {
            logger.error("关闭Azure翻译会话失败, vid: {}", vid, e);
        }
    }

    private String extractDetectedLanguage(TranslationRecognitionResult result) {
        try {
            String azureLang = result.getProperties().getProperty(PropertyId.SpeechServiceConnection_AutoDetectSourceLanguageResult);
            if (azureLang != null) {
                return AzureLanguageMapper.fromRecognitionLanguage(azureLang);
            }
        } catch (Exception e) {
            logger.debug("无法提取检测语言, vid: {}", vid);
        }
        return "";
    }

    // Getters
    public String getVid() {
        return vid;
    }

    public String getTmpText() {
        return tmpText;
    }

    public String getTmpTranslation() {
        return tmpTranslation;
    }

    public String getFinalText() {
        return finalText;
    }

    public String getFinalTranslation() {
        return finalTranslation;
    }

    public String getDetectedLanguage() {
        return detectedLanguage;
    }

    public boolean isSentenceDone() {
        return sentenceDone;
    }

    public void resetSentenceState() {
        this.sentenceDone = false;
        this.finalText = "";
        this.finalTranslation = "";
        this.recognizedLatch = new CountDownLatch(1);
        logger.debug("重置句子状态, vid: {}", vid);
    }

    public long getLastAccessTime() {
        return lastAccessTime.get();
    }

    public void setResultCallback(ResultCallback callback) {
        this.resultCallback = callback;
    }
}
