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
package com.hojoai.omniasst.focus.configure;

import com.hojoai.omniasst.focus.common.constants.ScheduleConstants;
import org.redisson.Redisson;
import org.redisson.api.RDelayedQueue;
import org.redisson.api.RQueue;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.StringCodec;
import org.redisson.config.Config;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Redisson配置类
 * 提供分布式锁、分布式对象等功能的Redis客户端
 *
 * @author My
 * @version 1.0 2026/01/16
 * @since hojoai-webmvc-omniasst-focus 1.0.0
 */
@Configuration
public class RedissonConfig {

    @Bean
    public RedissonClient redissonClient(RedisProperties redisProperties) {
        Config config = new Config();
        config.setCodec(StringCodec.INSTANCE)
                .useSingleServer()
                .setAddress("redis://" + redisProperties.getHost() + ":" + redisProperties.getPort())
                .setUsername(redisProperties.getUsername())
                .setPassword(redisProperties.getPassword())
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

    /**
     * 日程任务目标队列
     * 延时队列会将延迟时间到期的消息放入此队列
     *
     * @param redissonClient Redisson客户端
     * @return 目标队列
     */
    @Bean
    public RQueue<String> scheduleTaskQueue(RedissonClient redissonClient) {
        return redissonClient.getQueue(ScheduleConstants.REDIS_SCHEDULE_TASK_QUEUE, StringCodec.INSTANCE);
    }

    /**
     * 日程任务延时队列
     * 用于延迟将消息放入目标队列
     *
     * @param redissonClient Redisson客户端
     * @param scheduleTaskQueue 目标队列
     * @return 延时队列
     */
    @Bean
    public RDelayedQueue<String> scheduleTaskDelayedQueue(RedissonClient redissonClient, RQueue<String> scheduleTaskQueue) {
        return redissonClient.getDelayedQueue(scheduleTaskQueue);
    }

}
