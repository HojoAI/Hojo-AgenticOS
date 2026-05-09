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

import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Proxy;

/**
 * Firebase Admin SDK init for FCM.
 * <p>
 * 凭证文件：默认 {@code classpath:firebase-service-account.json}（需由 {@code firebase-service-account.json.example} 复制并填写，勿提交私钥）。
 * 可通过 {@code firebase.service-account.location} 覆盖为文件系统或外部路径。
 * </p>
 *
 * @author David
 * @version 1.0, 2026/03/17
 * @since hojoai-grpc-central-provider 1.0.0
 */
@Configuration
public class FirebaseConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(FirebaseConfiguration.class);

    @Bean
    @ConditionalOnProperty(name = "firebase.service-account.enabled", havingValue = "true")
    public FirebaseApp firebaseApp(
            ResourceLoader resourceLoader,
            @Value("${firebase.service-account.location:classpath:firebase-service-account.json}") String location,
            @Value("${firebase.proxy.enabled:false}") boolean proxyEnabled,
            @Value("${firebase.proxy.host:}") String proxyHost,
            @Value("${firebase.proxy.port:0}") int proxyPort) throws IOException {
        Resource resource = resourceLoader.getResource(location);
        if (!resource.exists()) {
            throw new IllegalStateException(
                    "Firebase credential file missing: " + location
                            + ". Copy firebase-service-account.json.example to firebase-service-account.json and fill in real values from Firebase console.");
        }
        try (InputStream in = resource.getInputStream()) {
            FirebaseOptions.Builder optionsBuilder = FirebaseOptions.builder();

            if (proxyEnabled && StringUtils.isNotBlank(proxyHost) && proxyPort > 0) {
                // 同一个代理工厂同时传给 GoogleCredentials（OAuth2 token 刷新）
                // 和 FirebaseOptions（FCM API 调用），两条链路都走代理
                Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost, proxyPort));
                NetHttpTransport transport = new NetHttpTransport.Builder().setProxy(proxy).build();
                GoogleCredentials credentials = GoogleCredentials.fromStream(in, () -> transport);
                optionsBuilder.setCredentials(credentials).setHttpTransport(transport);
                logger.info("FirebaseApp proxy enabled: {}:{}", proxyHost, proxyPort);
            } else {
                optionsBuilder.setCredentials(GoogleCredentials.fromStream(in));
            }

            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp app = FirebaseApp.initializeApp(optionsBuilder.build());
                logger.info("FirebaseApp initialized, projectId={}", app.getOptions().getProjectId());
                return app;
            }
            return FirebaseApp.getInstance();
        }
    }

    @Bean
    @ConditionalOnProperty(name = "firebase.service-account.enabled", havingValue = "true")
    public FirebaseMessaging firebaseMessaging(FirebaseApp firebaseApp) {
        return FirebaseMessaging.getInstance(firebaseApp);
    }
}
