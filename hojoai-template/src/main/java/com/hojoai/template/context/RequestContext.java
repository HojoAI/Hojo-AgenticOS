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
package com.hojoai.template.context;

import lombok.Getter;

/**
 * 请求上下文信息持有者
 * 用于在请求处理的不同阶段传递上下文信息
 * 使用ThreadLocal确保线程安全
 * 
 */
@Getter
public class RequestContext {

    // Getters
    /**
     * 请求开始时间戳（毫秒）
     */
    private long startTime;
    
    /**
     * 请求耗时（毫秒）
     */
    private long costTime;
    
    /**
     * 方法签名
     */
    private String methodSignature;
    
    /**
     * ThreadLocal持有者，确保线程隔离
     */
    private static final ThreadLocal<RequestContext> CONTEXT_HOLDER = new ThreadLocal<>();
    
    private RequestContext() {
    }
    
    /**
     * 初始化请求上下文
     * 
     * @param methodSignature 方法签名
     * @return 请求上下文对象
     */
    public static RequestContext init(String methodSignature) {
        RequestContext context = new RequestContext();
        context.startTime = System.currentTimeMillis();
        context.methodSignature = methodSignature;
        CONTEXT_HOLDER.set(context);
        return context;
    }
    
    /**
     * 获取当前请求上下文
     * 
     * @return 请求上下文，如果不存在返回null
     */
    public static RequestContext get() {
        return CONTEXT_HOLDER.get();
    }
    
    /**
     * 计算并设置请求耗时
     */
    public void calculateCostTime() {
        this.costTime = System.currentTimeMillis() - this.startTime;
    }
    
    /**
     * 清理请求上下文
     * 必须在请求结束时调用，防止内存泄漏
     */
    public static void clear() {
        CONTEXT_HOLDER.remove();
    }
}
