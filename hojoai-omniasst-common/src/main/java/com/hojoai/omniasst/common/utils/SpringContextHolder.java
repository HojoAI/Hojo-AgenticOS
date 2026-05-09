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
package com.hojoai.omniasst.common.utils;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * Spring 应用上下文持有者，在容器启动时写入静态引用，供非注入场景（如静态工具类）按类型或名称解析 Bean。
 * <p>由 Spring 在启动阶段回调 {@link #setApplicationContext} 完成初始化。</p>
 */
@Component
public class SpringContextHolder implements ApplicationContextAware {

    private static ApplicationContext applicationContext;

    /**
     * Spring 容器回调，缓存全局 {@link ApplicationContext}。
     *
     * @param context 当前应用上下文
     * @throws BeansException Spring 装载上下文失败时
     */
    @Override
    public void setApplicationContext(ApplicationContext context) throws BeansException {
        SpringContextHolder.applicationContext = context;
    }

    /**
     * 获取已缓存的 Spring 应用上下文。
     *
     * @return 应用上下文；容器尚未回调完成时可能为 null
     */
    public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    /**
     * 根据类型从容器中获取唯一 Bean。
     *
     * @param clazz Bean 类型
     * @param <T>   Bean 类型
     * @return Bean 实例
     * @throws IllegalStateException 上下文未初始化时
     * @throws NoSuchBeanDefinitionException 容器中不存在该类型 Bean 时
     */
    public static <T> T getBean(Class<T> clazz) {
        if (applicationContext == null) {
            throw new IllegalStateException("Spring ApplicationContext 未初始化，请确保 Spring 容器已启动");
        }
        return applicationContext.getBean(clazz);
    }

    /**
     * 根据 Bean 名称从容器中获取实例。
     *
     * @param name Bean 名称
     * @return Bean 实例
     * @throws IllegalStateException 上下文未初始化时
     */
    public static Object getBean(String name) {
        if (applicationContext == null) {
            throw new IllegalStateException("Spring ApplicationContext 未初始化，请确保 Spring 容器已启动");
        }
        return applicationContext.getBean(name);
    }

    /**
     * 根据名称与类型从容器中获取 Bean（需名称与类型同时匹配）。
     *
     * @param name  Bean 名称
     * @param clazz Bean 类型
     * @param <T>   Bean 类型
     * @return Bean 实例
     * @throws IllegalStateException 上下文未初始化时
     */
    public static <T> T getBean(String name, Class<T> clazz) {
        if (applicationContext == null) {
            throw new IllegalStateException("Spring ApplicationContext 未初始化，请确保 Spring 容器已启动");
        }
        return applicationContext.getBean(name, clazz);
    }
}
