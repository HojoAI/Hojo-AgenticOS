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
package com.hojoai.template.configure;

import com.hojoai.template.aop.TraceIdFilter;
import jakarta.annotation.Resource;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Filter配置类
 * 显式注册TraceIdFilter
 * 
 * @author HojoAI
 * @since 2026-01-28
 */
@Configuration
public class FilterConfig {
    
    @Resource
    private TraceIdFilter traceIdFilter;
    
    /**
     * 将 {@link TraceIdFilter} 注册为 Servlet Filter，拦截所有 URL，顺序与 Filter 自身 {@code @Order} 一致。
     *
     * @return 已配置 URL 模式与顺序的 Filter 注册 Bean
     */
    @Bean
    public FilterRegistrationBean<TraceIdFilter> traceIdFilterRegistration() {
        FilterRegistrationBean<TraceIdFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(traceIdFilter);
        registration.addUrlPatterns("/*");
        registration.setName("traceIdFilter");
        registration.setOrder(1);
        return registration;
    }
}
