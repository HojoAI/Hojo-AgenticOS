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
package com.hojoai.central.account.aop;

import com.hojoai.central.account.basic.ILoggerHandler;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

/**
 * The logger aop class.
 *
 * @author kevanwong
 * @version 1.0, 2017/10/21
 * @since hojoai-webmvc-central-account 1.0.0
 */
@Aspect
@Component
public class LoggerAop {

    @Before("execution(* com.hojoai.central.account.controller.*.*Controller.*(..))")
    public void before(JoinPoint joinPoint) {
        ILoggerHandler.enter(joinPoint.getArgs());
    }

    @AfterReturning(value = "execution(* com.hojoai.central.account.controller.*.*Controller.*(..))", returning = "object")
    public void doAfterReturning(JoinPoint joinPoint, Object object) {
        ILoggerHandler.leave(object);
    }
}
