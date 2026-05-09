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
package com.hojoai.omniasst.account.factory.auth;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import jakarta.annotation.PostConstruct;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.Arrays;

/**
 * Google ID Token 校验组件，使用官方 SDK 校验 audience，并支持可选 HTTP 代理访问 Google。
 */
@Component
public class GoogleTokenVerifier {

    private static final Logger logger = LoggerFactory.getLogger(GoogleTokenVerifier.class);
    private static final String LOG = "[Google登录] ";

    @Value("${oversea.auth.google.client-id-ios}")
    private String iosClientId;

    @Value("${oversea.auth.google.client-id-android}")
    private String androidClientId;

    @Value("${oversea.auth.google.client-id-web}")
    private String webClientId;

    @Value("${oversea.auth.google.proxy-host:}")
    private String proxyHost;

    @Value("${oversea.auth.google.proxy-port:0}")
    private int proxyPort;

    private HttpTransport httpTransport;

    @PostConstruct
    public void init() {
        if (StringUtils.isNotBlank(proxyHost) && proxyPort > 0) {
            Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost, proxyPort));
            httpTransport = new NetHttpTransport.Builder().setProxy(proxy).build();
            logger.info("{}已配置正向代理, host={}, port={}", LOG, proxyHost, proxyPort);
        } else {
            httpTransport = new NetHttpTransport();
            logger.info("{}未配置代理, 使用直连", LOG);
        }
    }

    /**
     * 校验 Google 登录返回的 idToken，成功则返回载荷信息。
     *
     * @param idTokenString 客户端上报的 Google ID Token
     * @return 校验成功时的 {@link GoogleIdToken.Payload}，失败或异常时为 {@code null}
     */
    public GoogleIdToken.Payload verify(String idTokenString) {
        try {
            logger.info("{}开始验证 idToken, audience=[ios={}, android={}], proxy={}",
                    LOG, iosClientId, androidClientId,
                    StringUtils.isNotBlank(proxyHost) ? proxyHost + ":" + proxyPort : "无");

            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                    httpTransport,
                    GsonFactory.getDefaultInstance())
                    .setAudience(Arrays.asList(iosClientId, androidClientId, webClientId))
                    .build();

            GoogleIdToken idToken = verifier.verify(idTokenString);
            if (idToken == null) {
                logger.warn("{}Google SDK 返回 null, token签名/audience/过期校验未通过", LOG);
                return null;
            }

            GoogleIdToken.Payload payload = idToken.getPayload();
            logger.info("{}Google SDK 验证通过, sub={}, email={}, emailVerified={}",
                    LOG, payload.getSubject(), payload.getEmail(), payload.getEmailVerified());
            return payload;
        } catch (Exception e) {
            logger.error("{}Google SDK 验证异常: {}", LOG, e.getMessage(), e);
            return null;
        }
    }
}
