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
package com.hojoai.central.provider.configure;

import com.alipay.api.AlipayConfig;
import com.baomidou.mybatisplus.extension.spring.MybatisSqlSessionFactoryBean;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

import javax.sql.DataSource;

/**
 * Web MVC 与中间件相关 Bean 定义：RestTemplate 超时、盘禾/飞常准/支付宝等第三方 URL 与密钥 Bean、MyBatis SqlSession。
 */
@EnableWebMvc
@Configuration
public class IWebMvcConfigurationSupport extends WebMvcConfigurationSupport {

    // =========================================== REST TEMPLATE ===========================================

    @Value("${rest-template.connect-timeout}")
    private Integer restTemplateConnectTimeout;

    @Value("${rest-template.read-timeout}")
    private Integer restTemplateReadTimeout;

    @Bean
    public RestTemplate restTemplate() {
        HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
        requestFactory.setConnectTimeout(restTemplateConnectTimeout);
        requestFactory.setReadTimeout(restTemplateReadTimeout);

        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setRequestFactory(requestFactory);
        return restTemplate;
    }

    // =========================================== PANHE ===========================================

    @Value("${panhe.request.http.url}")
    private String panheUrl;

    @Value("${panhe.request.http.appkey}")
    private String panheAppKey;

    @Bean
    public String panheUrl() {
        return panheUrl;
    }

    @Bean
    public String panheAppKey() {
        return panheAppKey;
    }

    // =========================================== VARIFLIGHT FLIGHT ===========================================

    @Value("${flight.variflight.base-url}")
    private String variflightBaseUrl;

    @Value("${flight.variflight.app-id}")
    private String variflightAppId;

    @Value("${flight.variflight.app-security}")
    private String variflightAppSecurity;

    @Bean
    public String variflightBaseUrl() {
        return variflightBaseUrl;
    }

    @Bean
    public String variflightAppId() {
        return variflightAppId;
    }

    @Bean
    public String variflightAppSecurity() {
        return variflightAppSecurity;
    }

    // =========================================== VARIFLIGHT TRAIN ===========================================

    @Value("${train.variflight.base-url}")
    private String variflightTrainBaseUrl;

    @Bean
    public String variflightTrainBaseUrl() {
        return variflightTrainBaseUrl;
    }

    // =========================================== ALIPAY ===========================================

    @Value("${alipay.app.id}")
    private String alipayAppId;

    @Value("${alipay.app.private.key}")
    private String appPrivateKey;

    @Value("${alipay.public.key}")
    private String alipayPublicKey;

    @Value("${alipay.open.api.url}")
    private String alipayOpenApiUrl;

    @Bean
    public AlipayConfig alipayConfig() {
        AlipayConfig alipayConfig = new AlipayConfig();

        alipayConfig.setAppId(alipayAppId);
        alipayConfig.setPrivateKey(appPrivateKey);
        alipayConfig.setAlipayPublicKey(alipayPublicKey);
        alipayConfig.setServerUrl(alipayOpenApiUrl);
        alipayConfig.setFormat("json");
        alipayConfig.setCharset("UTF-8");
        alipayConfig.setSignType("RSA2");

        return alipayConfig;
    }

    // =========================================== SQL SESSION FACTORY ===========================================

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

}
