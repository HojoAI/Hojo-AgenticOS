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
package com.hojoai.template.utils;

import org.slf4j.MDC;

import java.util.UUID;

/**
 * TraceId 工具类：生成无横线 UUID 作为 traceId，并在 SLF4J {@link MDC} 中存取，供日志与过滤器配合使用。
 *
 * @author HojoAI
 * @since 2026-01-28
 */
public class TraceIdUtil {
    
    /**
     * MDC 中存放当前请求 traceId 的键名。
     */
    private static final String TRACE_ID_KEY = "traceId";
    
    /**
     * 生成新的 traceId（UUID 去掉连字符）。
     *
     * @return 32 位十六进制字符串，非空
     */
    public static String generateTraceId() {
        return UUID.randomUUID().toString().replace("-", "");
    }
    
    /**
     * 将 traceId 写入当前线程 MDC（空串或 null 时不写入）。
     *
     * @param traceId 追踪 ID
     */
    public static void setTraceId(String traceId) {
        if (traceId != null && !traceId.isEmpty()) {
            MDC.put(TRACE_ID_KEY, traceId);
        }
    }
    
    /**
     * 读取当前线程 MDC 中的 traceId。
     *
     * @return traceId；未设置时返回 {@code null}
     */
    public static String getTraceId() {
        return MDC.get(TRACE_ID_KEY);
    }
    
    /**
     * 移除当前线程 MDC 中与 traceId 对应的键，不影响其它 MDC 键。
     */
    public static void clearTraceId() {
        MDC.remove(TRACE_ID_KEY);
    }
    
    /**
     * 清空当前线程 MDC 全部键值（例如请求结束时由过滤器调用）。
     */
    public static void clear() {
        MDC.clear();
    }
}
