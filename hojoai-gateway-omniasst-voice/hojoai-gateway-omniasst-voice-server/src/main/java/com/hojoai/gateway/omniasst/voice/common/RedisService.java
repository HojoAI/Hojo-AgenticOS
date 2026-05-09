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
package com.hojoai.gateway.omniasst.voice.common;

import jakarta.annotation.Resource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * Redis 通用 KV 封装，基于 String 键、Object 值的 {@link RedisTemplate}。
 * 供业务缓存、分布式状态等场景复用。
 */
@Service
public class RedisService {

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 写入键值，无过期时间。
     *
     * @param key   键
     * @param value 值，由模板序列化
     */
    public void set(String key, Object value) {
        redisTemplate.opsForValue().set(key, value);
    }

    /**
     * 写入键值并设置过期时间。
     *
     * @param key     键
     * @param value   值
     * @param timeout 时长
     * @param unit    时间单位
     */
    public void set(String key, Object value, long timeout, TimeUnit unit) {
        redisTemplate.opsForValue().set(key, value, timeout, unit);
    }

    /**
     * @param key 键
     * @return 反序列化后的值，不存在时为 {@code null}
     */
    public Object get(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    /**
     * @param key 键
     * @return 是否删除成功（键存在且被删除）
     */
    public Boolean delete(String key) {
        return redisTemplate.delete(key);
    }

    /**
     * @param key 键
     * @return 键是否存在
     */
    public Boolean hasKey(String key) {
        return redisTemplate.hasKey(key);
    }

    /**
     * 为已存在键设置过期时间。
     *
     * @param key     键
     * @param timeout 时长
     * @param unit    时间单位
     * @return 是否设置成功
     */
    public Boolean expire(String key, long timeout, TimeUnit unit) {
        return redisTemplate.expire(key, timeout, unit);
    }

    /**
     * @param key 键
     * @return 剩余过期秒数；-1 表示永不过期；-2 表示键不存在
     */
    public Long getExpire(String key) {
        return redisTemplate.getExpire(key);
    }
}
