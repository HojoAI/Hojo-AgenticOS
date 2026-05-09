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
package com.hojoai.webmvc.omniasst.translate.configure;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * Redis 工具：用于会话结束等场景的分布式互斥（setIfAbsent + TTL）。
 */
@Component
public class RedisUtils {

    /** 会话结束锁 key 前缀，与 chatId 拼接 */
    private static final String END_LOCK_PREFIX = "translate:chat:id:";

    private final StringRedisTemplate stringRedisTemplate;

    public RedisUtils(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    /**
     * 尝试获取分布式锁，成功则设置过期时间。
     *
     * @param key 业务键（内部会加前缀）
     * @param ttl 锁持有时间
     * @return 是否获取成功
     */
    public boolean tryLock(String key, Duration ttl) {
        String redisKey = END_LOCK_PREFIX + key;
        Boolean success = stringRedisTemplate.opsForValue().setIfAbsent(redisKey, "1", ttl);
        return Boolean.TRUE.equals(success);
    }

}
