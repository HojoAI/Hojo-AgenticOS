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
package com.hojoai.omniasst.weather.service.dify;

import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.core5.util.Timeout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import jakarta.annotation.PreDestroy;

import java.util.concurrent.TimeUnit;

/**
 * HTTP客户端管理器（单例）
 * 负责创建和管理共享的HTTP客户端实例，使用连接池优化性能
 *
 * @author wangjian
 * @since 2026-01-28
 */
@Component
public class HttpClientManager {

    private static final Logger logger = LoggerFactory.getLogger(HttpClientManager.class);

    /**
     * HTTP连接超时时间（秒）
     */
    private static final int CONNECTION_TIMEOUT_SECONDS = 30;

    /**
     * HTTP读取超时时间（秒），流式响应需要较长时间
     */
    private static final int READ_TIMEOUT_SECONDS = 300;

    /**
     * 连接池最大连接数
     */
    private static final int MAX_TOTAL_CONNECTIONS = 200;

    /**
     * 每个路由的最大连接数
     */
    private static final int MAX_PER_ROUTE = 50;

    /**
     * 共享的HTTP客户端实例（使用连接池）
     * 使用volatile确保多线程环境下的可见性
     */
    private volatile CloseableHttpClient httpClient;

    /**
     * 获取HTTP客户端实例（单例模式，线程安全）
     *
     * @return CloseableHttpClient
     */
    public CloseableHttpClient getHttpClient() {
        if (httpClient == null) {
            synchronized (this) {
                if (httpClient == null) {
                    httpClient = createHttpClient();
                    logger.info("HTTP客户端创建完成，连接池配置: maxTotal={}, maxPerRoute={}, connectTimeout={}s, readTimeout={}s",
                            MAX_TOTAL_CONNECTIONS, MAX_PER_ROUTE, CONNECTION_TIMEOUT_SECONDS, READ_TIMEOUT_SECONDS);
                }
            }
        }
        return httpClient;
    }

    /**
     * 创建 HTTP 客户端（配置连接池、超时和缓冲区）
     *
     * @return CloseableHttpClient
     */
    private CloseableHttpClient createHttpClient() {
        // 配置连接超时（在连接管理器级别设置）
        ConnectionConfig connectionConfig = ConnectionConfig.custom()
                .setConnectTimeout(Timeout.of(CONNECTION_TIMEOUT_SECONDS, TimeUnit.SECONDS))
                .setSocketTimeout(Timeout.of(READ_TIMEOUT_SECONDS, TimeUnit.SECONDS))
                .build();

        // 配置连接池（包含连接超时配置）
        PoolingHttpClientConnectionManager connectionManager = PoolingHttpClientConnectionManagerBuilder.create()
                .setMaxConnTotal(MAX_TOTAL_CONNECTIONS)
                .setMaxConnPerRoute(MAX_PER_ROUTE)
                .setDefaultConnectionConfig(connectionConfig)
                .build();

        // 配置响应超时（在请求级别设置）
        RequestConfig requestConfig = RequestConfig.custom()
                .setResponseTimeout(Timeout.of(READ_TIMEOUT_SECONDS, TimeUnit.SECONDS))
                .build();

        return HttpClients.custom()
                .setConnectionManager(connectionManager)
                .setDefaultRequestConfig(requestConfig)
                .build();
    }

    /**
     * 关闭HTTP客户端（用于应用关闭时清理资源）
     * 使用@PreDestroy注解确保Spring容器关闭时自动调用
     */
    @PreDestroy
    public void close() {
        if (httpClient != null) {
            try {
                httpClient.close();
                logger.info("HTTP客户端已关闭");
            } catch (Exception e) {
                logger.error("关闭HTTP客户端失败", e);
            }
        }
    }
}
