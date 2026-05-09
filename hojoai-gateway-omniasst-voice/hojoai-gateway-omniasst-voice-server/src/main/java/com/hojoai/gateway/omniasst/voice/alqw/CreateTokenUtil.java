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
package com.hojoai.gateway.omniasst.voice.alqw;

import com.alibaba.nls.client.AccessToken;
import com.hojoai.gateway.omniasst.voice.common.RedisService;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * 阿里云 NLS Token 获取工具。
 * 优先从 Redis 读取已缓存的 token，若无或已过期则向阿里云申请并写入 Redis，
 * Redis 过期时间按 token 的 ExpireTime（秒级时间戳）设置，并在该时间前预留缓冲提前失效。
 */
@Component
public class CreateTokenUtil {

    private static final Logger logger = LoggerFactory.getLogger(CreateTokenUtil.class);

    /**
     * Redis 中缓存阿里云 NLS token 的 key
     */
    private static final String REDIS_KEY_ALIYUN_NLS_TOKEN = "voice:aliyun:nls:token";

    /**
     * 在 token 真实过期时间前多少秒即让 Redis 键过期，避免返回临期 token
     */
    private static final long EXPIRE_BUFFER_SECONDS = 3 * 60 * 60;

    @Value("${aliyun.nls.accessKeyId}")
    private String accessKeyId;

    @Value("${aliyun.nls.accessKeySecret}")
    private String accessKeySecret;

    @Resource
    private RedisService redisService;

    public String getToken(String type) {
        Object cached = redisService.get(REDIS_KEY_ALIYUN_NLS_TOKEN);
        if (cached != null && cached instanceof String) {
            String token = (String) cached;
            if (!token.isEmpty()) {
                logger.info("Use cached Aliyun NLS token from Redis，type:{}, token: {}",type, token);
                return token;
            }
        }

        AccessToken accessToken = new AccessToken(accessKeyId, accessKeySecret);
        try {
            accessToken.apply();
            String token = accessToken.getToken();
            long expireTime = accessToken.getExpireTime();
            logger.info("Aliyun NLS type:{}, Token: {}, expire time: {}", type, token, expireTime);
            if (token != null && !token.isEmpty() && expireTime > 0) {
                long nowSec = System.currentTimeMillis() / 1000;
                long ttlSec = expireTime - nowSec - EXPIRE_BUFFER_SECONDS;
                if (ttlSec > 0) {
                    redisService.set(REDIS_KEY_ALIYUN_NLS_TOKEN, token, ttlSec, TimeUnit.SECONDS);
                    logger.info("Aliyun NLS token saved to Redis, type:{},  token {}, expire at {}, ttl {}s", type, token, expireTime, ttlSec);
                } else {
                    logger.warn("Aliyun NLS token expires soon or already expired, not cached, expireTime: {}", expireTime);
                }
            }

            logger.info("Token obtained, expire time: {}", expireTime);
            return token;
        } catch (Exception e) {
            logger.error("getToken error: ", e);
            return null;
        }
    }
}
