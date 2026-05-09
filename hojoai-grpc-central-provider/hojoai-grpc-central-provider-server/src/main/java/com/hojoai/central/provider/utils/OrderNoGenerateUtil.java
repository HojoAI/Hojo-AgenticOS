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
package com.hojoai.central.provider.utils;

import jakarta.annotation.Resource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * The OrderNoGenerateUtil class for orderNo.
 *
 * @author My
 * @version 1.0, 2025/11/21
 * @since hojoai-grpc-central-provider 1.0.0
 */
@Component
public class OrderNoGenerateUtil {

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    private static final String ORDER_SEQ_KEY = "order:sequence";

    /**
     * 生成订单序列号
     */
    public Long generateOrderSequence() {
        return redisTemplate.opsForValue().increment(ORDER_SEQ_KEY, 1);
    }

    /**
     * 生成带日期的订单号
     */
    public String generateOrderNo() {
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        Long sequence = generateOrderSequence();
        return String.format("O%s%08d", date, sequence);
    }

    /**
     * 按日期分段的订单号（每天从1开始）
     */
    public String generateDailyOrderNo(String prefix) {
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String dailyKey = ORDER_SEQ_KEY + ":" + date;
        Long sequence = redisTemplate.opsForValue().increment(dailyKey, 1);

        // 设置过期时间（第二天凌晨过期）
        redisTemplate.expire(dailyKey, Duration.ofHours(24));

        return prefix.concat(String.format("O%s%08d", date, sequence));
    }

}
