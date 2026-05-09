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
import com.hojoai.gateway.omniasst.voice.api.dto.VADRequest;
import com.hojoai.gateway.omniasst.voice.api.dto.VADResponse;
import com.hojoai.gateway.omniasst.voice.config.AzureSpeechConfig;
import com.hojoai.gateway.omniasst.voice.service.asrtranslate.AsrTranslateProvider;
import com.hojoai.gateway.omniasst.voice.service.azure.AzureTranslationSession;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.WebSocketSession;
import xyz.downgoon.snowflake.Snowflake;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Azure Speech 双向翻译 + Hojo ASR 语种检测混合实现：双 {@link AzureTranslationSession} 与 VAD/ASR 辅助线程协同。
 */
public class AzureAsrTranslateProvider implements AsrTranslateProvider {
    private static final Logger logger = LoggerFactory.getLogger(AzureAsrTranslateProvider.class);
    private static final int VAD_VALID_THRESHOLD = 20;
    private static final int VAD_END = 55;

    private final AzureSpeechConfig azureConfig;
    private final AsrAPI asrAPI;
    private final Snowflake snowflake;

    /**
     * @param azureConfig Azure 密钥与区域
     * @param asrAPI      Hojo 语种检测用 ASR/VAD
     * @param snowflake   生成 vid 等
     */
    public AzureAsrTranslateProvider(AzureSpeechConfig azureConfig, AsrAPI asrAPI, Snowflake snowflake) {
        this.azureConfig = azureConfig;
        this.asrAPI = asrAPI;
        this.snowflake = snowflake;
    }

    // Azure双通路
    private String sessionId;
    private String lang1;
    private String lang2;
    private AzureTranslationSession session1;
    private AzureTranslationSession session2;
    private ResultCallback callback;
    private volatile String detectedLang;
    private volatile AzureTranslationSession activeSession;
    private final AtomicLong lastAccessTime = new AtomicLong(System.currentTimeMillis());
    
    // Hojo语种检测相关
    private ExecutorService langDetectExecutor;
    private final LinkedBlockingQueue<byte[]> vadQueue = new LinkedBlockingQueue<>(100);
    private final LinkedBlockingQueue<byte[]> asrQueue = new LinkedBlockingQueue<>(50);
    private volatile boolean running = false;
    
    // VAD状态
    private volatile boolean vadValid = false;
    private String vadVid = null;
    private final List<Integer> vadHistory = new ArrayList<>();
    private byte[] lastVadAudio = null;
    
    // ASR语种检测状态
    private volatile boolean langDetected = false;
    private volatile String detectedLangFromHojo = null;
    private String asrVid = null;
    private int packageIndex = 0;
    
    // 当前句是否已由 activeSession 断句（用于区分另一路延迟断句 vs 新句第一路）
    private volatile boolean activeSessionEnded = false;
    private volatile boolean azureSentenceEnd = false;

    /**
     * {@inheritDoc}
     * <p>创建两路 {@link AzureTranslationSession} 并启动语种检测线程。</p>
     */
    @Override
    public void configure(String sessionId, String lang1, String lang2) {
        this.sessionId = sessionId;
        this.lang1 = lang1;
        this.lang2 = lang2;
        this.running = true;

        try {
            logger.info("[翻译][创建Azure会话] sessionId: {}, 双向翻译: {} <-> {}", sessionId, lang1, lang2);

            // 创建Azure双通路
            session1 = new AzureTranslationSession(sessionId + "_s1", azureConfig.getSubscriptionKey(), azureConfig.getRegion(), lang1, lang2);
            session1.setResultCallback(new AzureTranslationSession.ResultCallback() {
                @Override
                public void onRecognizing(String text, String translation) {
                    updateActiveSession(session1, text, translation, lang1, null, false);
                }

                @Override
                public void onRecognized(String text, String translation, String detectedLanguage) {
                    String actualLang = StringUtils.isNotBlank(detectedLanguage) ? detectedLanguage : lang1;
                    updateActiveSession(session1, text, translation, lang1, actualLang, true);
                }

                @Override
                public void onError(String errorDetails) {
                    logger.warn("[翻译][Session1错误] sessionId: {}, 错误: {}", sessionId, errorDetails);
                }
            });

            session2 = new AzureTranslationSession(sessionId + "_s2", azureConfig.getSubscriptionKey(), azureConfig.getRegion(), lang2, lang1);
            session2.setResultCallback(new AzureTranslationSession.ResultCallback() {
                @Override
                public void onRecognizing(String text, String translation) {
                    updateActiveSession(session2, text, translation, lang2, null, false);
                }

                @Override
                public void onRecognized(String text, String translation, String detectedLanguage) {
                    String actualLang = StringUtils.isNotBlank(detectedLanguage) ? detectedLanguage : lang2;
                    updateActiveSession(session2, text, translation, lang2, actualLang, true);
                }

                @Override
                public void onError(String errorDetails) {
                    logger.warn("[翻译][Session2错误] sessionId: {}, 错误: {}", sessionId, errorDetails);
                }
            });

            logger.info("[翻译][双向通道已建立] sessionId: {}, session1: {}→{}, session2: {}→{}", sessionId, lang1, lang2, lang2, lang1);

            // 启动Hojo语种检测线程
            startLangDetectionThreads();
            
        } catch (Exception e) {
            logger.error("[翻译][双向通道建立失败] sessionId: {}", sessionId, e);
            throw new RuntimeException("Failed to create Azure sessions", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void pushAudio(WebSocketSession session, byte[] audioData) {
        try {
            // 推送到Azure双通路
            session1.pushAudio(audioData);
            session2.pushAudio(audioData);
            
            // 推送到Hojo语种检测VAD队列
            if (running && !azureSentenceEnd) {
                vadQueue.offer(audioData);
                logger.debug("[翻译][推送音频] sessionId: {}, 推送到Azure双通路+VAD队列, 音频大小: {} bytes", 
                    sessionId, audioData.length);
            }
            
            lastAccessTime.set(System.currentTimeMillis());
        } catch (Exception e) {
            logger.error("[翻译][推送音频失败], sessionId: {}", sessionId, e);
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
        try {
            logger.info("[翻译][开始关闭] sessionId: {}", sessionId);
            
            // 停止Hojo语种检测线程
            running = false;
            if (langDetectExecutor != null) {
                langDetectExecutor.shutdownNow();
                try {
                    if (!langDetectExecutor.awaitTermination(2, TimeUnit.SECONDS)) {
                        logger.warn("[翻译][语种检测线程未及时终止] sessionId: {}", sessionId);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            
            if (session1 != null) session1.close();
            if (session2 != null) session2.close();
            logger.info("[翻译][会话已关闭] sessionId: {}", sessionId);
        } catch (Exception e) {
            logger.error("[翻译][关闭失败] sessionId: {}", sessionId, e);
        }
    }

    private void updateActiveSession(AzureTranslationSession session, String text, String translation,
                                     String sourceLang, String detectedLang, boolean isEnd) {
        if (callback == null || StringUtils.isBlank(text) || Objects.equals(text, translation) || StringUtils.isBlank(translation)) {
            return;
        }
        String actualDetectedLang = detectedLang != null ? detectedLang : sourceLang;

        if (activeSession == null) {
            activeSession = session;
            this.detectedLang = actualDetectedLang;
            String src = (session == session1) ? lang1 : lang2;
            String tgt = (session == session1) ? lang2 : lang1;
            logger.info("[翻译][确定通路-先到先得] sessionId: {}, 通路: {}→{}", sessionId, src, tgt);
            emit(sourceLang, actualDetectedLang, text, translation, isEnd);
            return;
        }

        if (activeSession == session) {
            if (isEnd) {
                activeSessionEnded = true;
                onAzureSentenceEnd();
            } else {
                activeSessionEnded = false;
            }
            emit(sourceLang, actualDetectedLang, text, translation, isEnd);
            return;
        }

        if (activeSessionEnded && isEnd) {
            String oldPath = (activeSession == session1) ? (lang1 + "→" + lang2) : (lang2 + "→" + lang1);
            activeSession = null;
            activeSessionEnded = false;
            azureSentenceEnd = false;
            logger.info("[翻译][另一路延迟断句-开启新轮] sessionId: {}, 忽略通路断句, 上一句: {}", sessionId, oldPath);
            return;
        }
        logger.debug("[翻译][忽略另一路] sessionId: {}, isEnd: {}", sessionId, isEnd);
        return;
    }

    private void emit(String sourceLang, String actualDetectedLang, String text, String translation, boolean isEnd) {
        String targetLang = lang1.equals(sourceLang) ? lang2 : lang1;
        Map<String, String> map = new HashMap<>();
        map.put(sourceLang, text);
        map.put(targetLang, translation);
        callback.onResult(map, actualDetectedLang, isEnd);
    }

    /**
     * @return 最近一次 {@link #pushAudio} 的时间戳（毫秒）
     */
    public long getLastAccessTime() {
        return lastAccessTime.get();
    }
    
    private void startLangDetectionThreads() {
        langDetectExecutor = Executors.newFixedThreadPool(2, r -> {
            Thread thread = new Thread(r);
            thread.setName("lang-detect-" + sessionId);
            thread.setDaemon(true);
            return thread;
        });
        
        langDetectExecutor.submit(this::processVadQueue);
        langDetectExecutor.submit(this::processAsrQueue);
        
        logger.info("[翻译][语种检测线程已启动] sessionId: {}", sessionId);
    }
    
    /**
     * Azure断句回调
     */
    private void onAzureSentenceEnd() {
        logger.info("[翻译][Azure断句] sessionId: {}, 重置状态准备下一句话", sessionId);
        azureSentenceEnd = true;
        
        // 重置状态，准备下一句话
        vadValid = false;
        langDetected = false;
        detectedLangFromHojo = null;
        vadQueue.clear();
        asrQueue.clear();
    }
    
    /**
     * VAD检测线程
     */
    private void processVadQueue() {
        try {
            while (running && !Thread.currentThread().isInterrupted()) {
                byte[] audioData = vadQueue.poll(100, TimeUnit.MILLISECONDS);
                if (audioData == null) {
                    continue;
                }
                
                if (vadValid) {
                    // VAD已通过，直接进ASR队列
                    asrQueue.offer(audioData);
                } else {
                    // 进行VAD检测
                    processVadDetection(audioData);
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.info("[翻译][VAD线程中断] sessionId: {}", sessionId);
        } catch (Exception e) {
            logger.error("[翻译][VAD线程异常] sessionId: {}", sessionId, e);
        }
    }
    
    /**
     * VAD检测处理
     */
    private void processVadDetection(byte[] audioData) {
        try {
            // 创建vadVid
            if (StringUtils.isBlank(vadVid)) {
                vadVid = generateVid();
                vadHistory.clear();
                lastVadAudio = null;
                logger.info("[翻译][创建VAD vid] sessionId: {}, vadVid: {}", sessionId, vadVid);
            }
            
            // 调用VAD接口
            VADRequest vadRequest = buildVadRequest(vadVid, audioData);
            logger.debug("[翻译][VAD请求] sessionId: {}, vadVid: {}", sessionId, vadVid);
            
            VADResponse vadResponse = asrAPI.vad(vadRequest);
            
            logger.info("[翻译][VAD返回] sessionId: {}, vadVid: {}, rtn: {}, vadData: {}", 
                sessionId, vadVid, vadResponse.getRtn(), 
                vadResponse.getVadData() != null ? Arrays.toString(vadResponse.getVadData()) : "null");
            
            if (vadResponse == null || vadResponse.getRtn() != 0) {
                logger.warn("[翻译][VAD失败][重置] sessionId: {}, vadVid: {}", sessionId, vadVid);
                resetVadContext();
                return;
            }
            
            // 计算VAD分数
            int vadSum = calculateVadSum(vadResponse.getVadData());
            vadHistory.add(vadSum);
            if (vadHistory.size() > 2) {
                vadHistory.remove(0);
            }
            
            // 判断VAD是否通过
            if (isVadValid(vadHistory)) {
                logger.info("[翻译][VAD通过] sessionId: {}, vadHistory: {}", sessionId, vadHistory);
                
                // VAD通过，将历史音频加入ASR队列
                if (lastVadAudio != null) {
                    asrQueue.offer(lastVadAudio);
                }
                asrQueue.offer(audioData);
                
                vadValid = true;
                resetVadContext();
            } else {
                lastVadAudio = audioData;
            }
            
        } catch (Exception e) {
            logger.error("[翻译][VAD异常][重置] sessionId: {}, vadVid: {}", sessionId, vadVid, e);
            resetVadContext();
        }
    }
    
    /**
     * ASR语种检测线程
     */
    private void processAsrQueue() {
        try {
            while (running && !Thread.currentThread().isInterrupted()) {
                // 检查是否已检测到语种
                if (langDetected) {
                    Thread.sleep(100);
                    continue;
                }
                
                byte[] audioData = asrQueue.poll(100, TimeUnit.MILLISECONDS);
                if (audioData == null) {
                    continue;
                }
                
                processAsrLangDetection(audioData);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.info("[翻译][ASR线程中断] sessionId: {}", sessionId);
        } catch (Exception e) {
            logger.error("[翻译][ASR线程异常] sessionId: {}", sessionId, e);
        }
    }
    
    /**
     * ASR语种检测处理
     */
    private void processAsrLangDetection(byte[] audioData) {
        try {
            // 检查Azure是否已断句
            if (azureSentenceEnd) {
                logger.info("[翻译][Azure已断句][停止语种检测] sessionId: {}", sessionId);
                return;
            }
            
            // 创建asrVid
            if (StringUtils.isBlank(asrVid)) {
                asrVid = generateVid();
                packageIndex = 0;
                logger.info("[翻译][创建ASR vid] sessionId: {}, asrVid: {}", sessionId, asrVid);
            }
            
            // 调用ASR接口（启用语种检测）
            ASRRequest asrRequest = buildAsrRequest(audioData, false);
            logger.debug("[翻译][ASR请求] sessionId: {}, asrVid: {}, packageIndex: {}", 
                sessionId, asrVid, packageIndex - 1);
            
            ASRResponse asrResponse = asrAPI.asr(asrRequest);
            
            logger.info("[翻译][ASR返回] sessionId: {}, asrVid: {}, rtn: {}, langDetect: {}, text: {}", 
                sessionId, asrVid, asrResponse.getRtn(), asrResponse.getLangDetect(),
                asrResponse.getText() != null ? asrResponse.getText().getText() : "null");
            
            if (asrResponse.getRtn() != 0) {
                logger.warn("[翻译][ASR失败] sessionId: {}, asrVid: {}", sessionId, asrVid);
                return;
            }
            
            // 检查是否检测到语种
            String langDetect = asrResponse.getLangDetect();
            if (StringUtils.isNotBlank(langDetect)) {
                logger.info("[翻译][检测到语种] sessionId: {}, langDetect: {}", sessionId, langDetect);
                
                // 再次检查Azure是否已断句
                if (azureSentenceEnd) {
                    logger.info("[翻译][Azure已断句][丢弃语种信息] sessionId: {}, langDetect: {}", 
                        sessionId, langDetect);
                    resetAsrContext();
                    return;
                }
                
                // 设置语种并切换通路
                detectedLangFromHojo = langDetect;
                langDetected = true;
                switchChannelByLanguage(langDetect);
                
                // 重置ASR状态
                resetAsrContext();
            }
            
        } catch (Exception e) {
            logger.error("[翻译][ASR异常] sessionId: {}, asrVid: {}", sessionId, asrVid, e);
        }
    }
    
    /**
     * 根据Hojo ASR检测到的语种切换Azure通路
     * 这是唯一允许切换activeSession的地方
     */
    private void switchChannelByLanguage(String detectedLang) {
        logger.info("[翻译][Hojo语种检测] sessionId: {}, Hojo检测语种: {}, 配置: lang1={}, lang2={}", 
            sessionId, detectedLang, lang1, lang2);
        
        if (detectedLang.equals(lang1)) {
            // 应该使用session1 (lang1 -> lang2)
            if (activeSession != session1) {
                String oldPath = (activeSession == session2) ? (lang2 + "→" + lang1) : "未确定";
                logger.info("========== [翻译][Hojo触发切换通路] ==========");
                logger.info("[翻译][切换通路-BY-HOJO] sessionId: {}, 从 {} 切换到 {}→{}", sessionId, oldPath, lang1, lang2);
                logger.info("================================================");
                activeSession = session1;
                this.detectedLang = lang1;
            } else {
                logger.info("[翻译][Hojo确认通路] sessionId: {}, 当前通路: {}→{}, 无需切换", sessionId, lang1, lang2);
            }
        } else if (detectedLang.equals(lang2)) {
            // 应该使用session2 (lang2 -> lang1)
            if (activeSession != session2) {
                String oldPath = (activeSession == session1) ? (lang1 + "→" + lang2) : "未确定";
                logger.info("========== [翻译][Hojo触发切换通路] ==========");
                logger.info("[翻译][切换通路-BY-HOJO] sessionId: {}, 从 {} 切换到 {}→{}", sessionId, oldPath, lang2, lang1);
                logger.info("================================================");
                activeSession = session2;
                this.detectedLang = lang2;
            } else {
                logger.info("[翻译][Hojo确认通路] sessionId: {}, 当前通路: {}→{}, 无需切换", sessionId, lang2, lang1);
            }
        } else {
            logger.info("[翻译][语种不匹配][忽略] sessionId: {}, Hojo检测: {}, 配置: {}←→{}, 保持当前通路", 
                sessionId, detectedLang, lang1, lang2);
        }
    }
    
    private void resetVadContext() {
        vadVid = null;
        vadHistory.clear();
        lastVadAudio = null;
    }
    
    private void resetAsrContext() {
        asrVid = null;
        packageIndex = 0;
    }
    
    private int calculateVadSum(int[] vadData) {
        if (vadData == null || vadData.length == 0) {
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
    
    private VADRequest buildVadRequest(String vadVid, byte[] audioData) {
        String base64Audio = Base64.getEncoder().encodeToString(audioData);
        
        VADRequest request = new VADRequest();
        request.setVid(vadVid);
        request.setPackageSize(base64Audio.length());
        request.setPackageData(base64Audio);
        request.setVadEnd(VAD_END);
        request.setVadDrop(0);
        return request;
    }
    
    private ASRRequest buildAsrRequest(byte[] audioData, boolean isEnd) {
        String base64Audio = Base64.getEncoder().encodeToString(audioData);
        
        ASRRequest request = new ASRRequest();
        request.setVid(asrVid);
        request.setPackageIndex(packageIndex++);
        request.setPackageSize(base64Audio.length());
        request.setPackageData(base64Audio);
        request.setPackageEnd(isEnd ? 1 : 0);
        request.setLang("all");
        request.setDoLangDetect(1);
        request.setType("pcm");
        request.setTask("all");
        request.setDoItn(1);
        request.setDoPunc(1);
        request.setVadEnd(VAD_END);
        return request;
    }
    
    private String generateVid() {
        return String.valueOf(snowflake.nextId());
    }
}
