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
package com.hojoai.omniasst.sysagent.configure;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurationSupport;
import xyz.downgoon.snowflake.Snowflake;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.security.SecureRandom;
import java.util.Enumeration;

/**
 * The web socket configuration support class.
 *
 * @author kevanwong
 * @version 1.0, 2025/10/21
 * @since hojoai-websocket-omniasst-sysagent 1.0.0
 */
@Configuration
@EnableWebSocket
public class IWebSocketConfigurationSupport extends WebSocketConfigurationSupport {

    @Value("${rest-template.connect-timeout:10000}")
    private Integer connectTimeout;

    @Value("${rest-template.read-timeout:60000}")
    private Integer readTimeout;

    /**
     * 分布式唯一 ID 生成器：工作机器号由本机站点本地 IP 派生，获取失败时退化为随机数。
     *
     * @return Snowflake 实例
     * @throws SocketException 枚举网卡失败时
     */
    @Bean
    public Snowflake snowflake() throws SocketException {
        String localAddress = null;

        // 遍历网卡，选取非回环的站点本地地址，用于生成较稳定的 workerId 片段
        Enumeration<NetworkInterface> networkInterfaceEnumeration = NetworkInterface.getNetworkInterfaces();
        while (networkInterfaceEnumeration.hasMoreElements()) {
            Enumeration<InetAddress> inetAddressEnumeration = networkInterfaceEnumeration.nextElement().getInetAddresses();
            while (inetAddressEnumeration.hasMoreElements()) {
                InetAddress inetAddress = inetAddressEnumeration.nextElement();
                if (inetAddress.isSiteLocalAddress() && !inetAddress.isLoopbackAddress()) {
                    localAddress = inetAddress.getHostAddress();
                }
            }
        }

        return new Snowflake(0, StringUtils.isNotBlank(localAddress) ? NumberUtils.toLong(localAddress.replaceAll("[^0-9]", ""), 0) % 32 : new SecureRandom().nextInt(32));
    }

    /**
     * 全局 {@link RestTemplate}，使用 Apache HttpClient 工厂并应用连接/读超时配置。
     *
     * @return 配置完成的 RestTemplate
     */
    @Bean
    public RestTemplate restTemplate() {
        HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
        requestFactory.setConnectTimeout(connectTimeout);
        requestFactory.setReadTimeout(readTimeout);

        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setRequestFactory(requestFactory);
        return restTemplate;
    }
}
