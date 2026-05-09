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
package com.hojoai.omniasst.management.configure;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.StringCodec;
import org.redisson.config.Config;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

/**
 * Redisson 客户端配置，与 Spring Data Redis 属性对齐。
 */
@Configuration
public class RedissonConfiguration {

    @Bean
    public RedissonClient redissonClient(RedisProperties redisProperties) {
        Config config = new Config();
        var serverConfig = config.setCodec(StringCodec.INSTANCE)
                .useSingleServer()
                .setAddress("redis://" + redisProperties.getHost() + ":" + redisProperties.getPort())
                .setPassword(redisProperties.getPassword());
        if (StringUtils.hasText(redisProperties.getUsername())) {
            serverConfig.setUsername(redisProperties.getUsername());
        }
        serverConfig
                .setDatabase(redisProperties.getDatabase())
                .setIdleConnectionTimeout(10000)
                .setConnectTimeout(10000)
                .setTimeout(3000)
                .setRetryAttempts(3)
                .setRetryInterval(1000)
                .setSubscriptionConnectionPoolSize(50)
                .setSubscriptionConnectionMinimumIdleSize(1)
                .setSubscriptionsPerConnection(5)
                .setConnectionPoolSize(64)
                .setConnectionMinimumIdleSize(4)
                .setDnsMonitoringInterval(5000);
        return Redisson.create(config);
    }
}
