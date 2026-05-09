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
package com.hojoai.omniasst.weather.util;

import org.slf4j.MDC;

import java.util.UUID;

/**
 * TraceId工具类
 * 用于生成和管理请求追踪ID
 * 
 * @author HojoAI
 * @since 2026-01-28
 */
public class TraceIdUtil {
    
    /**
     * MDC中traceId的key
     */
    private static final String TRACE_ID_KEY = "traceId";
    
    /**
     * 生成新的traceId（使用UUID）
     * 
     * @return traceId字符串
     */
    public static String generateTraceId() {
        return UUID.randomUUID().toString().replace("-", "");
    }
    
    /**
     * 设置traceId到MDC
     * 
     * @param traceId 追踪ID
     */
    public static void setTraceId(String traceId) {
        if (traceId != null && !traceId.isEmpty()) {
            MDC.put(TRACE_ID_KEY, traceId);
        }
    }
    
    /**
     * 从MDC获取traceId
     * 
     * @return traceId，如果不存在则返回null
     */
    public static String getTraceId() {
        return MDC.get(TRACE_ID_KEY);
    }
    
    /**
     * 清除MDC中的traceId
     */
    public static void clearTraceId() {
        MDC.remove(TRACE_ID_KEY);
    }
    
    /**
     * 清除MDC中的所有内容
     */
    public static void clear() {
        MDC.clear();
    }
}
