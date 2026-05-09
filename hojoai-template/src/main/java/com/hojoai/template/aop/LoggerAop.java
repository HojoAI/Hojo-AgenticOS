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
package com.hojoai.template.aop;

import com.hojoai.template.basic.LoggerHandler;
import com.hojoai.template.configure.LoggerProperties;
import com.hojoai.template.context.RequestContext;
import jakarta.annotation.Resource;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

/**
 * 日志切面类，用于记录Controller方法的入参、出参和耗时
 * 
 * <p>功能特性：</p>
 * <ul>
 *   <li>使用Spring StopWatch进行精确的性能监控</li>
 *   <li>支持慢接口告警，可配置阈值</li>
 *   <li>异常安全，确保任何情况下都能正确记录和清理</li>
 *   <li>集成MDC，支持链路追踪</li>
 *   <li>使用@Pointcut提高可维护性</li>
 * </ul>
 * 
 * @author HojoAI
 */
@Aspect
@Component
public class LoggerAop {
    
    private static final Logger logger = LoggerFactory.getLogger(LoggerAop.class);
    
    @Resource
    private LoggerProperties loggerProperties;
    
    @Resource
    private LoggerHandler loggerHandler;
    
    /**
     * 定义切点：匹配所有Controller的公共方法
     */
    @Pointcut("execution(* com.hojoai.template.controller.*.*Controller.*(..))")
    public void controllerPointcut() {
    }

    /**
     * 环绕通知：统一处理Controller方法的执行，记录入参、出参和耗时
     * 
     * <p>执行流程：</p>
     * <ol>
     *   <li>初始化请求上下文和StopWatch</li>
     *   <li>记录入参日志</li>
     *   <li>执行目标方法</li>
     *   <li>记录出参和耗时</li>
     *   <li>检测慢接口并告警</li>
     *   <li>清理资源（确保不会内存泄漏）</li>
     * </ol>
     * 
     * @param joinPoint 连接点
     * @return 方法执行结果
     * @throws Throwable 方法执行可能抛出的异常
     */
    @Around("controllerPointcut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        // 获取方法签名
        String methodSignature = buildMethodSignature(joinPoint);
        
        // 初始化请求上下文
        RequestContext requestContext = RequestContext.init(methodSignature);
        
        // 将方法签名放入MDC，方便日志追踪
        if (loggerProperties.isPrintMethodSignature()) {
            MDC.put("method", methodSignature);
        }
        
        // 创建并启动StopWatch进行性能监控
        StopWatch stopWatch = new StopWatch(methodSignature);
        stopWatch.start("controller-execution");
        
        try {
            // 记录入参
            loggerHandler.enter(joinPoint.getArgs());
            
            // 执行目标方法
            Object result = joinPoint.proceed();
            
            // 停止计时并记录出参和耗时
            stopWatch.stop();
            requestContext.calculateCostTime();
            long costTime = requestContext.getCostTime();
            
            loggerHandler.leave(result, costTime);
            
            // 慢接口告警
            checkSlowApi(methodSignature, costTime);
            
            return result;
        } catch (Throwable throwable) {
            // 异常情况下也要停止计时
            // 不在这里记录日志，由ExceptionHandler统一处理
            if (stopWatch.isRunning()) {
                stopWatch.stop();
            }
            requestContext.calculateCostTime();
            throw throwable;
        } finally {
            // 清理MDC和ThreadLocal，防止内存泄漏
            MDC.remove("method");
            // RequestContext会在ExceptionHandler或正常流程中清理
        }
    }
    
    /**
     * 构建方法签名字符串
     * 格式：ClassName.methodName
     * 
     * @param joinPoint 连接点
     * @return 方法签名
     */
    private String buildMethodSignature(ProceedingJoinPoint joinPoint) {
        Signature signature = joinPoint.getSignature();
        String className = signature.getDeclaringType().getSimpleName();
        String methodName = signature.getName();
        return className + "." + methodName;
    }
    
    /**
     * 检查是否为慢接口，如果是则记录告警日志
     * 
     * @param methodSignature 方法签名
     * @param costTime 耗时（毫秒）
     */
    private void checkSlowApi(String methodSignature, long costTime) {
        if (loggerProperties.isSlowApiAlertEnabled() 
                && costTime > loggerProperties.getSlowApiThreshold()) {
            logger.warn("Slow API detected! method: {}, costTime: {}ms, threshold: {}ms", 
                    methodSignature, costTime, loggerProperties.getSlowApiThreshold());
        }
    }
}
