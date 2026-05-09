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
package com.hojoai.gateway.omniasst.voice.configure;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurationSupport;
import xyz.downgoon.snowflake.Snowflake;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.security.SecureRandom;
import java.util.Enumeration;

/**
 * 扩展 Spring WebSocket 基础设施，提供基于本机 IP 的 Snowflake 工作机器号。
 * 在无法解析站点本地地址时回退为随机 worker id（0–31）。
 */
@Configuration
@EnableWebSocket
public class IWebSocketConfigurationSupport extends WebSocketConfigurationSupport {

    /**
     * 生成全局唯一 ID 用的 Snowflake 实例。
     *
     * @return Snowflake
     * @throws SocketException 枚举网卡失败时
     */
    @Bean
    public Snowflake snowflake() throws SocketException {
        String localAddress = null;
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
}

