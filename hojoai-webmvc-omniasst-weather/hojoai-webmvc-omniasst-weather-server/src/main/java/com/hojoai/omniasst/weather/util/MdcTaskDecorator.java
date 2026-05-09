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
import org.springframework.core.task.TaskDecorator;

import java.util.Map;

/**
 * MDC任务装饰器
 * 用于在异步线程中传递MDC上下文，确保traceId能够传递到异步任务中
 * 
 * @author HojoAI
 * @since 2026-01-28
 */
public class MdcTaskDecorator implements TaskDecorator {

    /**
     * 包装任务：在子线程执行前注入当前线程的 MDC，执行后清空，避免 traceId 丢失与线程污染。
     *
     * @param runnable 原始异步任务
     * @return 包装后的任务，提交给线程池执行
     */
    @Override
    public Runnable decorate(Runnable runnable) {
        // 获取当前线程的MDC上下文
        Map<String, String> contextMap = MDC.getCopyOfContextMap();
        
        return () -> {
            try {
                // 在异步线程中设置MDC上下文
                if (contextMap != null) {
                    MDC.setContextMap(contextMap);
                }
                // 执行原始任务
                runnable.run();
            } finally {
                // 任务执行完成后清除MDC，避免内存泄漏
                MDC.clear();
            }
        };
    }
}
