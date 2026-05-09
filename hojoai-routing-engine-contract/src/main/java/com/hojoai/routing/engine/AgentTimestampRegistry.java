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
package com.hojoai.routing.engine;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Agent SSE 消息时间戳注册表。
 * <p>
 * 在路由引擎 dispatch 开始时写入时间戳，Agent 实现据此对齐 SSE 消息时间戳。
 * 闭源路由引擎和开源 Agent 实现共用此工具类。
 * </p>
 */
public final class AgentTimestampRegistry {

    private static final Map<String, Long> timestampMap = new ConcurrentHashMap<>();

    private AgentTimestampRegistry() {
    }

    /**
     * 设置时间戳。
     *
     * @param key        conversationId + messageId 组合键
     * @param timestamp  时间戳（毫秒）
     */
    public static void setTimestamp(String key, Long timestamp) {
        timestampMap.put(key, timestamp);
    }

    /**
     * 移除时间戳。
     *
     * @param key 与 {@link #setTimestamp(String, Long)} 一致的键
     */
    public static void removeTimestamp(String key) {
        timestampMap.remove(key);
    }

    /**
     * 获取时间戳，不存在时返回 -1L。
     *
     * @param key conversationId + messageId 组合键
     * @return 时间戳或 -1L
     */
    public static long getTimestamp(String key) {
        return timestampMap.getOrDefault(key, -1L);
    }
}
