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

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.proc.JWSVerificationKeySelector;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.proc.ConfigurableJWTProcessor;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URL;

/**
 * Apple identityToken 校验组件，从 Apple JWKS 拉取公钥并校验 JWT 签名、issuer 与 audience（bundleId）。
 */
@Component
public class AppleTokenVerifier {

    private static final Logger logger = LoggerFactory.getLogger(AppleTokenVerifier.class);
    private static final String LOG = "[Apple登录] ";

    private static final String APPLE_JWKS_URL = "https://appleid.apple.com/auth/keys";
    private static final String APPLE_ISSUER = "https://appleid.apple.com";

    @Value("${oversea.auth.apple.bundle-id}")
    private String bundleId;

    /**
     * 校验 Apple 登录返回的 identityToken。
     *
     * @param identityTokenString 客户端上报的 Apple identity JWT
     * @return 校验成功时的声明集合，失败或异常时为 {@code null}
     */
    public JWTClaimsSet verify(String identityTokenString) {
        try {
            logger.info("{}开始验证 identityToken, bundleId={}", LOG, bundleId);
            logger.info("{}从 Apple JWKS 端点获取公钥: {}", LOG, APPLE_JWKS_URL);

            JWKSet jwkSet = JWKSet.load(new URL(APPLE_JWKS_URL));
            logger.info("{}JWKS 公钥加载成功, keyCount={}", LOG, jwkSet.getKeys().size());

            ConfigurableJWTProcessor<SecurityContext> jwtProcessor = new DefaultJWTProcessor<>();
            jwtProcessor.setJWSKeySelector(
                    new JWSVerificationKeySelector<>(JWSAlgorithm.RS256, new ImmutableJWKSet<>(jwkSet))
            );

            JWTClaimsSet claims = jwtProcessor.process(identityTokenString, null);
            logger.info("{}JWT 签名验证通过, sub={}, iss={}, aud={}", LOG, claims.getSubject(), claims.getIssuer(), claims.getAudience());

            if (!APPLE_ISSUER.equals(claims.getIssuer())) {
                logger.warn("{}issuer 不匹配, 期望={}, 实际={}", LOG, APPLE_ISSUER, claims.getIssuer());
                return null;
            }
            if (!claims.getAudience().contains(bundleId)) {
                logger.warn("{}audience 不匹配, 期望包含={}, 实际={}", LOG, bundleId, claims.getAudience());
                return null;
            }

            logger.info("{}验证全部通过, sub={}, email={}", LOG, claims.getSubject(), claims.getStringClaim("email"));
            return claims;
        } catch (Exception e) {
            logger.error("{}验证异常: {}", LOG, e.getMessage(), e);
            return null;
        }
    }
}
