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
package com.hojoai.webmvc.omniasst.meeting.configure;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import xyz.downgoon.snowflake.Snowflake;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.security.SecureRandom;
import java.util.Enumeration;

/**
 * 分布式 ID：基于本机网卡生成 Snowflake 工作机器标识，供业务主键等使用。
 */
@Configuration
public class SnowflakeConfig {
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

