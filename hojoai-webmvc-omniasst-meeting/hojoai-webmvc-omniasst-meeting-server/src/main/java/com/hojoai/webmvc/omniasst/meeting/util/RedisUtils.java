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
package com.hojoai.webmvc.omniasst.meeting.util;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * Redis 轻量封装：分布式锁、带过期时间的字符串缓存等。
 */
@Component
public class RedisUtils {

    private static final String END_LOCK_PREFIX = "meeting:lock:id:";

    private final StringRedisTemplate stringRedisTemplate;

    /**
     * @param stringRedisTemplate Spring 注入的字符串 Redis 模板
     */
    public RedisUtils(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    /**
     * 尝试获取分布式锁（SET NX + TTL），key 会加上固定前缀。
     *
     * @param key 业务侧锁键
     * @param ttl 锁持有时间，到期自动释放
     * @return 是否获取成功
     */
    public boolean tryLock(String key, Duration ttl) {
        String redisKey = END_LOCK_PREFIX + key;
        Boolean success = stringRedisTemplate.opsForValue().setIfAbsent(redisKey, "1", ttl);
        return Boolean.TRUE.equals(success);
    }

    /**
     * 写入字符串并设置过期时间。
     *
     * @param key Redis 键（无前缀变换）
     * @param value 值
     * @param ttl 过期时间
     */
    public void setEx(String key, String value, Duration ttl) {
        stringRedisTemplate.opsForValue().set(key, value, ttl);
    }

    /**
     * @param key Redis 键
     * @return 值，不存在时为 null
     */
    public String get(String key) {
        return stringRedisTemplate.opsForValue().get(key);
    }

    /**
     * 删除指定键。
     *
     * @param key Redis 键
     */
    public void delete(String key) {
        stringRedisTemplate.delete(key);
    }

}
