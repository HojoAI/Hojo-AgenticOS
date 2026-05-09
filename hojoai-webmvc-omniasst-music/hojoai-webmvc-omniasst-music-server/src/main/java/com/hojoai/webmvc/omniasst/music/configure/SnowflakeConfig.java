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
package com.hojoai.webmvc.omniasst.music.configure;

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
     * 雪花算法 ID 生成器（当前数据中心与机器 ID 为固定常量，分布式部署时需改为可配置）。
     *
     * @return Snowflake 实例
     */
    @Bean
    public Snowflake snowflake() {
        return new Snowflake(DATACENTER_ID, WORKER_ID);
    }
}

