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

import com.coze.openapi.service.auth.JWTOAuthClient;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * The OAuthTokenConfig class for cozenfiguring OAuth token settings.
 *
 * @author lianghanyi
 * @version 1.0, 2025/10/31
 * @since hojoai-websocket-omniasst-sysagent 1.0.0
 */

@Configuration
public class OAuthTokenConfig {

    private static final Logger logger = LoggerFactory.getLogger(OAuthTokenConfig.class);

    @Value("${coze.api.base}")
    private String cozeAPIBase;

    @Value("${coze.oauth.client.id}")
    private String oauthClientId;

    @Value("${coze.oauth.private.key.path}")
    private String oauthPrivateKeyPath;

    @Value("${coze.oauth.public.key}")
    private String oauthPublicKey;

    // 默认配置900s
    @Value("${coze.oauth.token.ttl}")
    private Integer ttl;

    private final ResourceLoader resourceLoader;

    public OAuthTokenConfig(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    /**
     * 从配置的 {@link #oauthPrivateKeyPath} 读取 PEM 私钥文本。
     *
     * @return 私钥字符串
     */
    private String loadPrivateKeyFromFile() {
        try {
            Resource resource = resourceLoader.getResource(oauthPrivateKeyPath);
            if (!resource.exists()) {
                throw new RuntimeException("Private key file not found");
            }
            String privateKey = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            return privateKey;
        } catch (IOException e) {
            logger.error("Failed to load private key from file");
            throw new RuntimeException("Failed to load private key from file");
        }
    }

    /**
     * 构建 Coze {@link JWTOAuthClient}，使用应用配置的 baseUrl、公私钥与 token TTL。
     *
     * @return 可用于获取 access token 的客户端
     */
    @Bean
    public JWTOAuthClient jwtOAuthClient() {
        try {
            String oauthPrivateKey = loadPrivateKeyFromFile();
            JWTOAuthClient oauth = new JWTOAuthClient.JWTOAuthBuilder().ttl(ttl).clientID(oauthClientId).privateKey((oauthPrivateKey)).publicKey(oauthPublicKey).baseURL(cozeAPIBase).build();
            logger.info("JWTOAuthClient initialized successfully with clientId: {}", oauthClientId);
            return oauth;
        } catch (Exception e) {
            logger.error("Failed to initialize JWTOAuthClient, error: {}", ExceptionUtils.getStackTrace(e));
            throw new RuntimeException("Failed to initialize JWTOAuthClient", e);
        }
    }
}
