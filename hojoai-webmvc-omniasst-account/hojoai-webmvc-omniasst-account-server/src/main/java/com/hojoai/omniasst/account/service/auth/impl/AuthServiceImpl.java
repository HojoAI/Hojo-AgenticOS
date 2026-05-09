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
package com.hojoai.omniasst.account.service.auth.impl;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.hojoai.omniasst.account.client.CentralAccountClient;
import com.hojoai.omniasst.account.common.ResponseStatusExtra;
import com.hojoai.omniasst.account.factory.auth.AppleTokenVerifier;
import com.hojoai.omniasst.account.factory.auth.GoogleTokenVerifier;
import com.hojoai.omniasst.account.factory.email.EmailCaptchaFactory;
import com.hojoai.omniasst.account.service.auth.AuthService;
import com.hojoai.omniasst.account.service.auth.bean.*;
import com.hojoai.common.response.Response;
import com.hojoai.common.response.ResponseStatusBasic;
import com.hojoai.omniasst.account.service.user.bean.UserTokenInfoResponse;
import com.nimbusds.jwt.JWTClaimsSet;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * {@link AuthService} 实现：聚合 Google/Apple 校验、邮箱 OTP 与 Magic Link，并调用 {@link CentralAccountClient}。
 */
@Service
public class AuthServiceImpl implements AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthServiceImpl.class);

    private static final String PROVIDER_GOOGLE = "google";
    private static final String PROVIDER_APPLE = "apple";
    private static final String PROVIDER_EMAIL = "email";

    private static final String LOG_GOOGLE = "[Google登录] ";
    private static final String LOG_APPLE = "[Apple登录] ";
    private static final String LOG_EMAIL_OTP = "[邮箱OTP] ";
    private static final String LOG_MAGIC = "[MagicLink] ";

    /** 轮询接口对外三态（与 Redis 内 verified/pending/failed 区分） */
    private static final String ML_POLL_PENDING = "pending";
    private static final String ML_POLL_SUCCESS = "success";
    private static final String ML_POLL_FAILED = "failed";
    private static final String ML_REASON_EXPIRED = "expired";
    private static final String ML_REASON_CONSUMED = "consumed";
    private static final String ML_REDIS_VERIFIED = "verified";
    private static final String ML_REDIS_FAILED = "failed";
    private static final String LOG_LOGOUT = "[登出] ";
    private static final String LOG_DELETE = "[账号注销] ";

    @Resource
    private GoogleTokenVerifier googleTokenVerifier;

    @Resource
    private AppleTokenVerifier appleTokenVerifier;

    @Resource
    private EmailCaptchaFactory emailCaptchaFactory;

    @Resource
    private CentralAccountClient centralAccountClient;

    // ======================== Google 登录 ========================

    @Override
    public Response<AuthLoginResponse> loginByGoogle(GoogleLoginRequest request) throws Exception {
        logger.info("{}接口入口, platformType={}", LOG_GOOGLE, request.getPlatformType());

        logger.info("{}调用 Google SDK 验证 idToken...", LOG_GOOGLE);
        GoogleIdToken.Payload payload = googleTokenVerifier.verify(request.getIdToken());
        if (payload == null) {
            logger.warn("{}Google SDK 验证失败, idToken 无效", LOG_GOOGLE);
            return Response.newInstance(ResponseStatusExtra.INVALID_GOOGLE_TOKEN);
        }

        String sub = payload.getSubject();
        String email = payload.getEmail();
        String name = (String) payload.get("name");
        String picture = (String) payload.get("picture");
        logger.info("{}Google SDK 验证成功, sub={}, email={}, name={}", LOG_GOOGLE, sub, email, name);

        Response<AuthLoginResponse> response = callCentralCreateToken(LOG_GOOGLE, PROVIDER_GOOGLE, sub, email, name, picture, request.getPlatformType());
        logger.info("{}最终响应, resCode={}, userId={}", LOG_GOOGLE, response.getResCode(),
                response.getResContent() != null ? response.getResContent().getUserId() : "null");
        return response;
    }

    // ======================== Apple 登录 ========================

    @Override
    public Response<AuthLoginResponse> loginByApple(AppleLoginRequest request) throws Exception {
        logger.info("{}接口入口, platformType={}, hasFullName={}, hasEmail={}",
                LOG_APPLE, request.getPlatformType(), request.getFullName() != null, request.getEmail() != null);

        logger.info("{}调用 Apple JWKS 验证 identityToken...", LOG_APPLE);
        JWTClaimsSet claims = appleTokenVerifier.verify(request.getIdentityToken());
        if (claims == null) {
            logger.warn("{}Apple JWKS 验证失败, identityToken 无效", LOG_APPLE);
            return Response.newInstance(ResponseStatusExtra.INVALID_APPLE_TOKEN);
        }

        String sub = claims.getSubject();
        String email = claims.getStringClaim("email");
        if (email == null) {
            email = request.getEmail();
        }
        logger.info("{}Apple JWKS 验证成功, sub={}, email={}", LOG_APPLE, sub, email);

        Response<AuthLoginResponse> response = callCentralCreateToken(LOG_APPLE, PROVIDER_APPLE, sub, email, request.getFullName(), null, request.getPlatformType());
        logger.info("{}最终响应, resCode={}, userId={}", LOG_APPLE, response.getResCode(),
                response.getResContent() != null ? response.getResContent().getUserId() : "null");
        return response;
    }

    // ======================== Email OTP ========================

    @Override
    public Response<?> sendEmailCode(EmailSendCodeRequest request) throws Exception {
        logger.info("{}发送验证码入口, email={}", LOG_EMAIL_OTP, request.getEmail());

        String result = emailCaptchaFactory.sendOtp(request.getEmail());
        if (result == null) {
            logger.info("{}验证码发送成功, email={}", LOG_EMAIL_OTP, request.getEmail());
            return Response.newInstance(ResponseStatusBasic.OK);
        }
        if ("RATE_LIMITED".equals(result)) {
            logger.warn("{}发送被限流, email={}", LOG_EMAIL_OTP, request.getEmail());
            return Response.newInstance(ResponseStatusExtra.OTP_RATE_LIMITED);
        }
        logger.error("{}发送失败, email={}, result={}", LOG_EMAIL_OTP, request.getEmail(), result);
        return Response.newInstance(ResponseStatusBasic.GENERAL_ERROR);
    }

    @Override
    public Response<AuthLoginResponse> loginByEmailOtp(EmailLoginRequest request) throws Exception {
        logger.info("{}验证码登录入口, email={}, platformType={}", LOG_EMAIL_OTP, request.getEmail(), request.getPlatformType());

        String verifyResult = emailCaptchaFactory.verifyOtp(request.getEmail(), request.getCode());
        if (verifyResult != null) {
            logger.warn("{}验证码校验失败, email={}, reason={}", LOG_EMAIL_OTP, request.getEmail(), verifyResult);
            return mapOtpError(verifyResult);
        }
        logger.info("{}验证码校验通过, email={}", LOG_EMAIL_OTP, request.getEmail());

        Response<AuthLoginResponse> response = callCentralCreateToken(LOG_EMAIL_OTP, PROVIDER_EMAIL, request.getEmail(), request.getEmail(), null, null, request.getPlatformType());
        logger.info("{}最终响应, resCode={}, userId={}", LOG_EMAIL_OTP, response.getResCode(),
                response.getResContent() != null ? response.getResContent().getUserId() : "null");
        return response;
    }

    // ======================== Magic Link ========================

    @Override
    public Response<MagicLinkSendResponse> sendMagicLink(EmailSendCodeRequest request) throws Exception {
        logger.info("{}发送入口, email={}, platformType={}", LOG_MAGIC, request.getEmail(), request.getPlatformType());

        String loginToken = emailCaptchaFactory.sendMagicLink(request.getEmail(), request.getPlatformType());
        if (loginToken == null) {
            logger.warn("{}发送失败(限流或SES异常), email={}", LOG_MAGIC, request.getEmail());
            return Response.newInstance(ResponseStatusExtra.OTP_RATE_LIMITED);
        }

        logger.info("{}发送成功, email={}, loginToken={}", LOG_MAGIC, request.getEmail(), loginToken);
        MagicLinkSendResponse data = new MagicLinkSendResponse();
        data.setLoginToken(loginToken);
        return Response.newInstance(ResponseStatusBasic.OK, data);
    }

    @Override
    public Response<AuthLoginResponse> queryMagicLinkStatus(String loginToken) throws Exception {
        logger.info("{}轮询状态, loginToken={}", LOG_MAGIC, loginToken);

        Map<String, Object> state = emailCaptchaFactory.getMagicLinkState(loginToken);
        if (state == null) {
            logger.warn("{}轮询: 无状态(过期或无效), loginToken={}", LOG_MAGIC, loginToken);
            AuthLoginResponse failed = new AuthLoginResponse();
            failed.setStatus(ML_POLL_FAILED);
            failed.setFailureReason(ML_REASON_EXPIRED);
            return Response.newInstance(ResponseStatusBasic.OK, failed);
        }

        String redisStatus = (String) state.get("status");
        AuthLoginResponse data = new AuthLoginResponse();

        if (ML_REDIS_FAILED.equals(redisStatus)) {
            String reason = (String) state.get("failureReason");
            if (ML_REASON_CONSUMED.equals(reason)) {
                data.setStatus(ML_POLL_FAILED);
                data.setFailureReason(ML_REASON_CONSUMED);
                logger.info("{}轮询: 失败(consumed), loginToken={}", LOG_MAGIC, loginToken);
            } else {
                data.setStatus(ML_POLL_FAILED);
                data.setFailureReason(ML_REASON_EXPIRED);
                logger.info("{}轮询: 失败(未知原因), loginToken={}", LOG_MAGIC, loginToken);
            }
            return Response.newInstance(ResponseStatusBasic.OK, data);
        }

        if (ML_REDIS_VERIFIED.equals(redisStatus)) {
            data.setStatus(ML_POLL_SUCCESS);
            data.setUserToken((String) state.get("userToken"));
            data.setUserId((String) state.get("userId"));
            data.setBusinessType((Integer) state.get("businessType"));
            data.setPlatformType((Integer) state.get("platformType"));
            emailCaptchaFactory.consumeMagicLink(loginToken);
            logger.info("{}轮询: 成功, loginToken={}, userId={}", LOG_MAGIC, loginToken, data.getUserId());
            return Response.newInstance(ResponseStatusBasic.OK, data);
        }

        data.setStatus(ML_POLL_PENDING);
        logger.info("{}轮询: 待验证, loginToken={}", LOG_MAGIC, loginToken);
        return Response.newInstance(ResponseStatusBasic.OK, data);
    }

    @Override
    public Response<?> verifyMagicLink(MagicLinkVerifyRequest request) throws Exception {
        logger.info("{}验证入口, token={}", LOG_MAGIC, request.getToken());

        Map<String, Object> state = emailCaptchaFactory.getMagicLinkState(request.getToken());
        if (state == null) {
            logger.warn("{}验证: token已过期或不存在, token={}", LOG_MAGIC, request.getToken());
            AuthLoginResponse expired = new AuthLoginResponse();
            expired.setStatus(ML_POLL_FAILED);
            expired.setFailureReason(ML_REASON_EXPIRED);
            return Response.newInstance(ResponseStatusBasic.OK, expired);
        }

        String status = (String) state.get("status");
        if (ML_REDIS_FAILED.equals(status)) {
            String reason = (String) state.get("failureReason");
            logger.warn("{}验证: token已失效, reason={}, token={}", LOG_MAGIC, reason, request.getToken());
            AuthLoginResponse failed = new AuthLoginResponse();
            failed.setStatus(ML_POLL_FAILED);
            failed.setFailureReason(ML_REASON_CONSUMED.equals(reason) ? ML_REASON_CONSUMED : ML_REASON_EXPIRED);
            return Response.newInstance(ResponseStatusBasic.OK, failed);
        }
        if (ML_REDIS_VERIFIED.equals(status)) {
            logger.warn("{}验证: token已被使用(verified), token={}", LOG_MAGIC, request.getToken());
            AuthLoginResponse consumed = new AuthLoginResponse();
            consumed.setStatus(ML_POLL_FAILED);
            consumed.setFailureReason(ML_REASON_CONSUMED);
            return Response.newInstance(ResponseStatusBasic.OK, consumed);
        }

        String email = (String) state.get("email");
        Integer platformType = (Integer) state.get("platformType");
        if (platformType == null) {
            platformType = 1;
        }

        logger.info("{}验证: 开始创建用户token, email={}, platformType={}", LOG_MAGIC, email, platformType);
        Response<AuthLoginResponse> loginResponse = callCentralCreateToken(LOG_MAGIC, PROVIDER_EMAIL, email, email, null, null, platformType);
        if (!"OK".equals(loginResponse.getResCode())) {
            logger.warn("{}验证: central-account创建token失败, resCode={}", LOG_MAGIC, loginResponse.getResCode());
            return loginResponse;
        }

        AuthLoginResponse loginData = loginResponse.getResContent();
        emailCaptchaFactory.completeMagicLink(
                request.getToken(), loginData.getUserToken(), loginData.getUserId(),
                loginData.getBusinessType(), loginData.getPlatformType());
        logger.info("{}验证完成, token={}, userId={}", LOG_MAGIC, request.getToken(), loginData.getUserId());

        AuthLoginResponse verifyResult = new AuthLoginResponse();
        verifyResult.setStatus(ML_POLL_SUCCESS);
        return Response.newInstance(ResponseStatusBasic.OK, verifyResult);
    }

    // ======================== 登出 ========================

    @Override
    public Response<?> logout(String userId) throws Exception {
        logger.info("{}userId={}", LOG_LOGOUT, userId);
        Response<?> response = centralAccountClient.deleteUserToken(userId, null, null);
        logger.info("{}响应, userId={}, resCode={}", LOG_LOGOUT, userId, response != null ? response.getResCode() : "null");
        return response;
    }

    // ======================== 账号注销 ========================

    @Override
    public Response<?> deleteAccount(String userId) throws Exception {
        logger.info("{}userId={}", LOG_DELETE, userId);
        Response<?> response = centralAccountClient.deleteOverseaAccount(userId);
        logger.info("{}响应, userId={}, resCode={}", LOG_DELETE, userId, response != null ? response.getResCode() : "null");
        return response;
    }

    // ======================== 内部方法 ========================

    private Response<AuthLoginResponse> callCentralCreateToken(String logPrefix, String provider, String providerUserId,
                                                                String email, String nickname, String avatar,
                                                                Integer platformType) {
        logger.info("{}调用 central-account 创建token, provider={}, providerUserId={}, email={}, platformType={}",
                logPrefix, provider, providerUserId, email, platformType);

        OverseaTokenRequest request = new OverseaTokenRequest();
        request.setProvider(provider);
        request.setProviderUserId(providerUserId);
        request.setEmail(email);
        request.setNickname(nickname);
        request.setAvatar(avatar);
        request.setPlatformType(platformType);

        Response<UserTokenInfoResponse> centralResponse = centralAccountClient.createOverseaToken(request);

        if (centralResponse == null) {
            logger.error("{}central-account 返回 null", logPrefix);
            return Response.newInstance(ResponseStatusBasic.GENERAL_ERROR);
        }

        logger.info("{}central-account 响应, resCode={}", logPrefix, centralResponse.getResCode());

        if (!"OK".equals(centralResponse.getResCode())) {
            logger.warn("{}central-account 业务失败, resCode={}", logPrefix, centralResponse.getResCode());
            return switch (centralResponse.getResCode()) {
                case "ACCOUNT_DISABLED" -> Response.newInstance(ResponseStatusExtra.ACCOUNT_DISABLED);
                case "ACCOUNT_DELETED" -> Response.newInstance(ResponseStatusExtra.ACCOUNT_DELETED);
                default -> Response.newInstance(ResponseStatusBasic.GENERAL_ERROR);
            };
        }

        UserTokenInfoResponse tokenInfo = centralResponse.getResContent();
        logger.info("{}central-account 成功, userId={}, businessType={}, platformType={}",
                logPrefix, tokenInfo.getUserId(), tokenInfo.getBusinessType(), tokenInfo.getPlatformType());

        AuthLoginResponse data = new AuthLoginResponse();
        data.setUserId(tokenInfo.getUserId());
        data.setUserToken(tokenInfo.getUserToken());
        data.setBusinessType(tokenInfo.getBusinessType());
        data.setPlatformType(tokenInfo.getPlatformType());
        return Response.newInstance(ResponseStatusBasic.OK, data);
    }

    private Response<AuthLoginResponse> mapOtpError(String errorCode) {
        return switch (errorCode) {
            case "OTP_EXPIRED" -> Response.newInstance(ResponseStatusExtra.OTP_EXPIRED);
            case "OTP_ATTEMPTS_EXCEEDED" -> Response.newInstance(ResponseStatusExtra.OTP_ATTEMPTS_EXCEEDED);
            case "INVALID_OTP" -> Response.newInstance(ResponseStatusExtra.INVALID_OTP);
            default -> Response.newInstance(ResponseStatusBasic.GENERAL_ERROR);
        };
    }
}
