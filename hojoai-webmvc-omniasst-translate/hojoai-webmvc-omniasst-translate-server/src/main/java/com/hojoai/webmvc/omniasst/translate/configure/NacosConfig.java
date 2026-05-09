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
package com.hojoai.webmvc.omniasst.translate.configure;

import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.naming.NamingService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Nacos 命名服务：用于按服务名发现语音服务等下游实例。
 */
@Configuration
public class NacosConfig {

    @Value("${spring.cloud.nacos.discovery.server-addr}")
    private String serverAddr;

    @Value("${spring.cloud.nacos.discovery.access-key:}")
    private String accessKey;

    @Value("${spring.cloud.nacos.discovery.secret-key:}")
    private String secretKey;

    /**
     * 创建 NamingService，连接配置中的 Nacos 地址（可选 AK/SK）。
     *
     * @return Nacos 命名服务客户端
     * @throws Exception 创建失败时抛出
     */
    @Bean
    public NamingService namingService() throws Exception {
        java.util.Properties properties = new java.util.Properties();
        properties.setProperty("serverAddr", serverAddr);
        if (org.apache.commons.lang3.StringUtils.isNotBlank(accessKey)) {
            properties.setProperty("accessKey", accessKey);
        }
        if (org.apache.commons.lang3.StringUtils.isNotBlank(secretKey)) {
            properties.setProperty("secretKey", secretKey);
        }
        return NacosFactory.createNamingService(properties);
    }
}
