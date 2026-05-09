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

import com.hojoai.gateway.omniasst.voice.config.AzureSpeechConfig;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Azure翻译会话管理器
 * 负责管理所有活跃的Azure双向翻译会话
 * 提供会话创建、获取、清理等功能
 */
@Component
public class AzureTranslationSessionManager {

    private static final Logger logger = LoggerFactory.getLogger(AzureTranslationSessionManager.class);

    private final AzureSpeechConfig azureConfig;
    private final Map<String, BiDirectionalTranslationSession> sessions = new ConcurrentHashMap<>();

    public AzureTranslationSessionManager(AzureSpeechConfig azureConfig) {
        this.azureConfig = azureConfig;
        logger.info("Azure双向翻译会话管理器已初始化, 会话超时: {}ms", azureConfig.getSessionTimeoutMs());
    }

    /**
     * 获取或创建Azure双向翻译会话
     * 如果会话不存在则创建新会话
     * 支持双向翻译：lang1 <-> lang2
     * 
     * @param vid 会话ID
     * @param lang1 语言1(业务代码，例如：chinese)
     * @param lang2 语言2(业务代码，例如：english)
     * @return Azure双向翻译会话
     */
    public BiDirectionalTranslationSession getOrCreateSession(String vid, String lang1, String lang2) {
        BiDirectionalTranslationSession existingSession = sessions.get(vid);
        if (existingSession != null) {
            logger.debug("[使用已存在会话] vid: {}, 当前活跃会话数: {}", vid, sessions.size());
            return existingSession;
        }
        
        return sessions.computeIfAbsent(vid, k -> {
            logger.info("========== [创建新Azure双向翻译会话] ==========");
            logger.info("会话ID(vid): {}", vid);
            logger.info("支持双向翻译: {} <-> {}", lang1, lang2);
            logger.info("Azure区域: {}", azureConfig.getRegion());
            logger.info("会话超时配置: {}ms", azureConfig.getSessionTimeoutMs());
            logger.info("当前活跃会话数: {}", sessions.size() + 1);
            logger.info("=======================================");
            
            return new BiDirectionalTranslationSession(
                vid,
                azureConfig.getSubscriptionKey(),
                azureConfig.getRegion(),
                lang1,
                lang2
            );
        });
    }

    /**
     * 获取已存在的会话
     * 
     * @param vid 会话ID
     * @return Azure双向翻译会话，如果不存在则返回null
     */
    public BiDirectionalTranslationSession getSession(String vid) {
        return sessions.get(vid);
    }

    /**
     * 移除并关闭指定会话
     * 
     * @param vid 会话ID
     */
    public void removeSession(String vid) {
        BiDirectionalTranslationSession session = sessions.remove(vid);
        if (session != null) {
            session.close();
            logger.info("========== [移除Azure会话] ==========");
            logger.info("会话ID(vid): {}", vid);
            logger.info("剩余活跃会话数: {}", sessions.size());
            logger.info("====================================");
        }
    }

    /**
     * 定时清理超时的会话
     * 每分钟执行一次
     */
    @Scheduled(fixedRate = 60000)
    public void cleanupExpiredSessions() {
        long now = System.currentTimeMillis();
        long timeoutMs = azureConfig.getSessionTimeoutMs();
        int cleanedCount = 0;

        for (Map.Entry<String, BiDirectionalTranslationSession> entry : sessions.entrySet()) {
            String vid = entry.getKey();
            BiDirectionalTranslationSession session = entry.getValue();
            long idleTime = now - session.getLastAccessTime();

            if (idleTime > timeoutMs) {
                sessions.remove(vid);
                session.close();
                cleanedCount++;
                logger.info("清理超时会话, vid: {}, 空闲时间: {}ms", vid, idleTime);
            }
        }

        if (cleanedCount > 0) {
            logger.info("定时清理完成, 清理会话数: {}, 剩余会话数: {}", cleanedCount, sessions.size());
        }
    }

    /**
     * 获取当前活跃会话数
     * 
     * @return 活跃会话数量
     */
    public int getActiveSessionCount() {
        return sessions.size();
    }

    /**
     * 应用关闭时清理所有会话
     */
    @PreDestroy
    public void shutdown() {
        logger.info("开始关闭所有Azure双向翻译会话, 当前会话数: {}", sessions.size());
        sessions.forEach((vid, session) -> {
            try {
                session.close();
            } catch (Exception e) {
                logger.error("关闭会话失败, vid: {}", vid, e);
            }
        });
        sessions.clear();
        logger.info("所有Azure双向翻译会话已关闭");
    }
}
