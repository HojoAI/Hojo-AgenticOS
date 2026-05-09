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

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import xyz.downgoon.snowflake.Snowflake;

/**
 * Snowflake ID Generator Configuration
 * Provides a thread-safe distributed unique ID generator based on Snowflake algorithm
 * 
 * Snowflake ID structure (64 bits):
 * - 1 bit: sign bit (always 0)
 * - 41 bits: timestamp (milliseconds since epoch)
 * - 5 bits: datacenter ID (0-31)
 * - 5 bits: worker ID (0-31)
 * - 12 bits: sequence number (0-4095)
 *
 * @author My
 * @version 1.0 2025/01/04
 * @since hojoai-webmvc-omniasst.focus 1.0.0
 */
@Configuration
public class SnowflakeConfig {

    /**
     * Datacenter ID for distributed environment
     * Range: 0-31 (5 bits)
     * Each datacenter should have a unique ID
     */
    @Value("${snowflake.datacenter-id:1}")
    private long datacenterId;

    /**
     * Worker/Machine ID within the datacenter
     * Range: 0-31 (5 bits)
     * Each worker in the same datacenter should have a unique ID
     */
    @Value("${snowflake.worker-id:1}")
    private long workerId;

    /**
     * Create Snowflake bean for generating unique IDs
     * This bean is singleton by default and thread-safe
     *
     * @return Snowflake instance configured with datacenter and worker IDs
     */
    @Bean
    public Snowflake snowflake() {
        return new Snowflake(datacenterId, workerId);
    }
}

