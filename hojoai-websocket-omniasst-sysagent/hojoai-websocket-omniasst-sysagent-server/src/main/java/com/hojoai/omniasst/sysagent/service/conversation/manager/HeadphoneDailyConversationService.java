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
package com.hojoai.omniasst.sysagent.service.conversation.manager;

import com.hojoai.omniasst.sysagent.common.RedisService;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 耳机模式（{@code headphoneMode=true}）下同一用户按「业务日」仅绑定一个会话 ID：
 * 优先读 Redis；未绑定时短时加锁创建会话并写入，避免并发首刷产生多个当日会话。
 */
@Service
public class HeadphoneDailyConversationService {

    private static final Logger logger = LoggerFactory.getLogger(HeadphoneDailyConversationService.class);

    private static final String KEY_PREFIX = "Omniasst:Headphone:Daily:";
    private static final String LOCK_PREFIX = "Omniasst:Headphone:Daily:Lock:";
    private static final int LOCK_WAIT_ROUNDS = 80;
    private static final long LOCK_WAIT_MS = 50;

    @Resource
    private ConversationManagement conversationManagement;

    @Resource
    private RedisService redisService;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 创建或复用会话/消息 ID。非耳机模式时与 {@link ConversationManagement#createConversationAndMessageId} 行为一致。
     *
     * @param headphoneMode         是否耳机模式（单日单会话）
     * @param clientConversationId  客户端传入的会话 ID（非耳机模式原样参与创建逻辑）
     * @param userId                用户 ID
     * @param userNowDate           客户端业务日字符串，可为空（则使用服务器当日）
     * @return 与 {@link ConversationManagement#createConversationAndMessageId} 相同结构的 Map
     */
    public Map<String, String> createConversationAndMessageId(boolean headphoneMode, String clientConversationId,
                                                              String userId, String userNowDate) {
        if (!headphoneMode) {
            return conversationManagement.createConversationAndMessageId(clientConversationId, userId, headphoneMode);
        }
        if (StringUtils.isEmpty(userId)) {
            return conversationManagement.createConversationAndMessageId(clientConversationId, userId, headphoneMode);
        }

        String businessDate = resolveBusinessDate(userNowDate);
        String dailyKey = KEY_PREFIX + userId + ":" + businessDate;
        String lockKey = LOCK_PREFIX + userId + ":" + businessDate;

        Object cached = redisService.get(dailyKey);
        if (cached != null) {
            String cid = normalizeConversationId(cached);
            if (StringUtils.isNotEmpty(cid)) {
                return conversationManagement.createConversationAndMessageId(cid, userId, headphoneMode);
            }
        }

        for (int i = 0; i < LOCK_WAIT_ROUNDS; i++) {
            Boolean locked = redisTemplate.opsForValue().setIfAbsent(lockKey, "1", Duration.ofSeconds(30));
            if (Boolean.TRUE.equals(locked)) {
                try {
                    cached = redisService.get(dailyKey);
                    if (cached != null) {
                        String cid = normalizeConversationId(cached);
                        if (StringUtils.isNotEmpty(cid)) {
                            return conversationManagement.createConversationAndMessageId(cid, userId, headphoneMode);
                        }
                    }
                    Map<String, String> created = conversationManagement.createConversationAndMessageId(null, userId, headphoneMode);
                    if (created != null && created.size() == 1) {
                        String newCid = created.keySet().iterator().next();
                        long ttlSeconds = secondsUntilEndOfBusinessDay(businessDate);
                        redisService.set(dailyKey, newCid, ttlSeconds, TimeUnit.SECONDS);
                        logger.info("Headphone daily conversation bound: userId={}, date={}, conversationId={}",
                                userId, businessDate, newCid);
                    }
                    return created;
                } finally {
                    redisTemplate.delete(lockKey);
                }
            }
            try {
                Thread.sleep(LOCK_WAIT_MS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
            cached = redisService.get(dailyKey);
            if (cached != null) {
                String cid = normalizeConversationId(cached);
                if (StringUtils.isNotEmpty(cid)) {
                    return conversationManagement.createConversationAndMessageId(cid, userId, headphoneMode);
                }
            }
        }

        logger.warn("Headphone daily lock wait exhausted, falling back to new conversation: userId={}, date={}",
                userId, businessDate);
        return conversationManagement.createConversationAndMessageId(null, userId, headphoneMode);
    }

    private static String normalizeConversationId(Object cached) {
        if (cached == null) {
            return null;
        }
        if (cached instanceof String) {
            return (String) cached;
        }
        return String.valueOf(cached);
    }

    private static long secondsUntilEndOfBusinessDay(String businessDate) {
        try {
            LocalDate d = LocalDate.parse(businessDate);
            ZoneId z = ZoneId.systemDefault();
            ZonedDateTime endOfDay = d.plusDays(1).atStartOfDay(z);
            long seconds = Duration.between(Instant.now(), endOfDay.toInstant()).getSeconds();
            if (seconds < 120) {
                return 120;
            }
            if (seconds > TimeUnit.DAYS.toSeconds(2)) {
                return TimeUnit.DAYS.toSeconds(1);
            }
            return seconds;
        } catch (Exception e) {
            return TimeUnit.HOURS.toSeconds(26);
        }
    }

    private static String resolveBusinessDate(String userNowDate) {
        if (StringUtils.isNotBlank(userNowDate)) {
            String trimmed = userNowDate.trim();
            try {
                return LocalDate.parse(trimmed, DateTimeFormatter.ISO_LOCAL_DATE).toString();
            } catch (DateTimeParseException e) {
                try {
                    return LocalDate.parse(trimmed, DateTimeFormatter.ofPattern("yyyy/MM/dd")).toString();
                } catch (DateTimeParseException e2) {
                    logger.warn("Cannot parse userNowDate [{}], using server local date", userNowDate);
                }
            }
        }
        return LocalDate.now(ZoneId.systemDefault()).toString();
    }
}
