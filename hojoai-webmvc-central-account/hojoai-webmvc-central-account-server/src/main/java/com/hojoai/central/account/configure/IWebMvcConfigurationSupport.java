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
package com.hojoai.central.account.configure;

import com.aliyun.dysmsapi20170525.Client;
import com.baomidou.mybatisplus.extension.spring.MybatisSqlSessionFactoryBean;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;
import xyz.downgoon.snowflake.Snowflake;

import javax.sql.DataSource;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.security.SecureRandom;
import java.util.Enumeration;

/**
 * The web mvc configuration support class.
 *
 * @author kevanwong
 * @version 1.0, 2017/10/21
 * @since hojoai-webmvc-central-account 1.0.0
 */
@EnableWebMvc
@Configuration
public class IWebMvcConfigurationSupport extends WebMvcConfigurationSupport {

    private static final String ALIYUN_SMS_ENDPOINT = "dysmsapi.aliyuncs.com";

    @Value("${rest-template.connect-timeout}")
    private Integer restTemplateConnectTimeout;

    @Value("${rest-template.read-timeout}")
    private Integer restTemplateReadTimeout;

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

    @Bean
    public RestTemplate restTemplate() {
        HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
        requestFactory.setConnectTimeout(restTemplateConnectTimeout);
        requestFactory.setReadTimeout(restTemplateReadTimeout);

        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setRequestFactory(requestFactory);
        return restTemplate;
    }

    @Bean
    public SqlSessionFactory sqlSessionFactory(DataSource dataSource) throws Exception {
        MybatisSqlSessionFactoryBean mybatisSqlSessionFactoryBean = new MybatisSqlSessionFactoryBean();
        mybatisSqlSessionFactoryBean.setDataSource(dataSource);
        return mybatisSqlSessionFactoryBean.getObject();
    }

    @Bean
    public SqlSessionTemplate sqlSessionTemplate(SqlSessionFactory sqlSessionFactory) {
        return new SqlSessionTemplate(sqlSessionFactory);
    }

    @Bean
    public Client client() throws Exception {
        com.aliyun.credentials.Client credential = new com.aliyun.credentials.Client();
        com.aliyun.teaopenapi.models.Config config = new com.aliyun.teaopenapi.models.Config().setCredential(credential);
        config.endpoint = ALIYUN_SMS_ENDPOINT;
        return new Client(config);
    }
}
