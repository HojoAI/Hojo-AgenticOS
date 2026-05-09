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
package com.hojoai.omniasst.account.configure;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ses.SesClient;

/**
 * 海外认证相关 Bean 配置，当前负责基于配置项装配 AWS SES 客户端，用于发送邮件验证码与 Magic Link。
 */
@Configuration
public class OverseaAuthConfiguration {

    @Value("${oversea.auth.ses.region}")
    private String sesRegion;

    @Value("${oversea.auth.ses.access-key}")
    private String sesAccessKey;

    @Value("${oversea.auth.ses.secret-key}")
    private String sesSecretKey;

    @Bean
    public SesClient sesClient() {
        return SesClient.builder()
                .region(Region.of(sesRegion))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(sesAccessKey, sesSecretKey)))
                .build();
    }
}
