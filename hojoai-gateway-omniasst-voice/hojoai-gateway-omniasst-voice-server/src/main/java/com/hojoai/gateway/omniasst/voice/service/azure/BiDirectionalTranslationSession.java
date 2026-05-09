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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.util.concurrent.atomic.AtomicLong;

/**
 * 双向翻译会话
 * 同时创建两个Azure Translation Session，支持双向语言自动识别
 * 例如：中文<->英文双向翻译
 */
public class BiDirectionalTranslationSession implements AutoCloseable {
    
    private static final Logger logger = LoggerFactory.getLogger(BiDirectionalTranslationSession.class);
    
    private final String vid;
    private final AzureTranslationSession session1; // 语言1 -> 语言2
    private final AzureTranslationSession session2; // 语言2 -> 语言1
    private final String lang1Business; // 业务语言代码1（例如：chinese）
    private final String lang2Business; // 业务语言代码2（例如：english）
    private final AtomicLong lastAccessTime;
    
    // 当前活跃的session（最近有识别结果的）
    private volatile AzureTranslationSession activeSession = null;
    private volatile String detectedLanguage = null;
    
    /**
     * 构造双向翻译会话
     * 
     * @param vid 会话ID
     * @param subscriptionKey Azure订阅密钥
     * @param region Azure区域
     * @param lang1 语言1业务代码（例如：chinese）
     * @param lang2 语言2业务代码（例如：english）
     */
    public BiDirectionalTranslationSession(String vid, String subscriptionKey, String region, 
                                           String lang1, String lang2) {
        this.vid = vid;
        this.lang1Business = lang1;
        this.lang2Business = lang2;
        this.lastAccessTime = new AtomicLong(System.currentTimeMillis());
        
        logger.info("========== [创建双向翻译会话] vid: {} ==========", vid);
        logger.info("方向1: {} -> {}", lang1, lang2);
        logger.info("方向2: {} -> {}", lang2, lang1);
        
        try {
            // 创建session1: lang1 -> lang2
            this.session1 = new AzureTranslationSession(
                vid + "_s1", 
                subscriptionKey, 
                region, 
                lang1, // 源语言
                lang2  // 目标语言
            );
            
            // 创建session2: lang2 -> lang1
            this.session2 = new AzureTranslationSession(
                vid + "_s2", 
                subscriptionKey, 
                region, 
                lang2, // 源语言
                lang1  // 目标语言
            );
            
            logger.info("双向翻译会话创建成功, vid: {}", vid);
            logger.info("==========================================================");
            
        } catch (Exception e) {
            logger.error("创建双向翻译会话失败, vid: {}", vid, e);
            throw new RuntimeException("Failed to create bi-directional translation session", e);
        }
    }
    
    /**
     * 同时推送音频到两个session
     * 
     * @param audioData PCM音频数据
     */
    public void pushAudio(byte[] audioData) {
        lastAccessTime.set(System.currentTimeMillis());
        session1.pushAudio(audioData);
        session2.pushAudio(audioData);
        logger.debug("推送音频到双向会话, vid: {}, 数据大小: {} bytes", vid, audioData.length);
    }
    
    /**
     * 获取临时识别文本
     * 优先返回有内容的session结果
     */
    public String getTmpText() {
        updateActiveSession();
        if (activeSession == session1 && StringUtils.hasText(session1.getTmpText())) {
            return session1.getTmpText();
        } else if (activeSession == session2 && StringUtils.hasText(session2.getTmpText())) {
            return session2.getTmpText();
        }
        // 兜底：返回任一session有内容的
        return StringUtils.hasText(session1.getTmpText()) ? session1.getTmpText() : session2.getTmpText();
    }
    
    /**
     * 获取临时翻译文本
     */
    public String getTmpTranslation() {
        updateActiveSession();
        if (activeSession == session1 && StringUtils.hasText(session1.getTmpTranslation())) {
            return session1.getTmpTranslation();
        } else if (activeSession == session2 && StringUtils.hasText(session2.getTmpTranslation())) {
            return session2.getTmpTranslation();
        }
        return StringUtils.hasText(session1.getTmpTranslation()) ? session1.getTmpTranslation() : session2.getTmpTranslation();
    }
    
    /**
     * 获取最终识别文本
     */
    public String getFinalText() {
        if (activeSession == session1 && StringUtils.hasText(session1.getFinalText())) {
            return session1.getFinalText();
        } else if (activeSession == session2 && StringUtils.hasText(session2.getFinalText())) {
            return session2.getFinalText();
        }
        return StringUtils.hasText(session1.getFinalText()) ? session1.getFinalText() : session2.getFinalText();
    }
    
    /**
     * 获取最终翻译文本
     */
    public String getFinalTranslation() {
        if (activeSession == session1 && StringUtils.hasText(session1.getFinalTranslation())) {
            return session1.getFinalTranslation();
        } else if (activeSession == session2 && StringUtils.hasText(session2.getFinalTranslation())) {
            return session2.getFinalTranslation();
        }
        return StringUtils.hasText(session1.getFinalTranslation()) ? session1.getFinalTranslation() : session2.getFinalTranslation();
    }
    
    /**
     * 获取检测到的源语言（业务代码）
     */
    public String getDetectedLanguage() {
        updateActiveSession();
        return detectedLanguage;
    }
    
    /**
     * 检查是否句子结束
     */
    public boolean isSentenceDone() {
        return session1.isSentenceDone() || session2.isSentenceDone();
    }
    
    /**
     * 重置句子状态
     */
    public void resetSentenceState() {
        session1.resetSentenceState();
        session2.resetSentenceState();
        // 注意：不重置activeSession，保持语言方向连续性
    }
    
    /**
     * 更新活跃session
     * 根据哪个session有识别结果来判断当前语言方向
     */
    private void updateActiveSession() {
        // 优先检查最终结果（句子结束时）
        if (StringUtils.hasText(session1.getFinalText())) {
            if (activeSession != session1) {
                activeSession = session1;
                detectedLanguage = lang1Business;
                logger.info("检测到语言方向: {} -> {}, vid: {}", lang1Business, lang2Business, vid);
            }
            return;
        }
        if (StringUtils.hasText(session2.getFinalText())) {
            if (activeSession != session2) {
                activeSession = session2;
                detectedLanguage = lang2Business;
                logger.info("检测到语言方向: {} -> {}, vid: {}", lang2Business, lang1Business, vid);
            }
            return;
        }
        
        // 检查临时结果
        if (StringUtils.hasText(session1.getTmpText())) {
            if (activeSession != session1) {
                activeSession = session1;
                detectedLanguage = lang1Business;
                logger.debug("临时检测语言方向: {} -> {}, vid: {}", lang1Business, lang2Business, vid);
            }
        } else if (StringUtils.hasText(session2.getTmpText())) {
            if (activeSession != session2) {
                activeSession = session2;
                detectedLanguage = lang2Business;
                logger.debug("临时检测语言方向: {} -> {}, vid: {}", lang2Business, lang1Business, vid);
            }
        }
    }
    
    public String getVid() {
        return vid;
    }
    
    public long getLastAccessTime() {
        return lastAccessTime.get();
    }
    
    /**
     * 关闭两个session
     */
    @Override
    public void close() {
        logger.info("关闭双向翻译会话, vid: {}", vid);
        try {
            if (session1 != null) {
                session1.close();
            }
        } catch (Exception e) {
            logger.error("关闭session1失败, vid: {}", vid, e);
        }
        try {
            if (session2 != null) {
                session2.close();
            }
        } catch (Exception e) {
            logger.error("关闭session2失败, vid: {}", vid, e);
        }
    }
}
