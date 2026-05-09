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
package com.hojoai.omniasst.weather.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import xyz.downgoon.snowflake.Snowflake;

/**
 * Snowflake Configuration
 * Snowflake ID生成器配置
 * 
 * @author HojoAI
 * @since 2026-01-13
 */
@Configuration
public class SnowflakeConfig {
    
    /**
     * 数据中心ID，可以从配置文件或环境变量读取
     */
    private static final long DATACENTER_ID = 1L;
    
    /**
     * 机器ID，可以从配置文件或环境变量读取
     */
    private static final long WORKER_ID = 1L;

    /**
     * 提供全局 Snowflake 发号器实例，使用本类配置的 datacenterId 与 workerId。
     *
     * @return Snowflake 实例
     */
    @Bean
    public Snowflake snowflake() {
        return new Snowflake(DATACENTER_ID, WORKER_ID);
    }
}

