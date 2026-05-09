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
package com.hojoai.omniasst.account.factory.email;

import com.hojoai.omniasst.account.common.CacheKey;
import com.hojoai.common.jackson.JacksonUtil;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.*;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 邮件侧验证码与 Magic Link 能力：通过 AWS SES 发信，使用 Redis 存储 OTP、限流与 Magic Link 状态。
 */
@Component
public class EmailCaptchaFactory {

    private static final Logger logger = LoggerFactory.getLogger(EmailCaptchaFactory.class);

    private static final String LOG_OTP = "[邮箱OTP] ";
    private static final String LOG_MAGIC = "[MagicLink] ";
    private static final String LOG_SES = "[SES邮件] ";

    private static final int OTP_LENGTH = 6;
    private static final int OTP_EXPIRE_MINUTES = 5;
    private static final int RATE_LIMIT_SECONDS = 60;
    private static final int MAX_ATTEMPTS = 5;
    private static final int MAGIC_LINK_EXPIRE_MINUTES = 15;
    /** 轮询取走登录凭证后写入短时墓碑，便于与「从未验证即过期」区分 */
    private static final int MAGIC_LINK_CONSUMED_TOMBSTONE_SECONDS = 120;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Resource
    private SesClient sesClient;

    @Value("${oversea.auth.ses.from-address}")
    private String fromAddress;

    @Value("${oversea.auth.magic-link.base-url}")
    private String magicLinkBaseUrl;

    @Value("${oversea.auth.mock.magic-link-on-send-failure:false}")
    private boolean mockMagicLinkOnSendFailure;

    private String magicLinkEmailTemplate;

    @PostConstruct
    private void init() {
        try {
            ClassPathResource resource = new ClassPathResource("templates/magic-link-email.html");
            try (InputStream is = resource.getInputStream()) {
                magicLinkEmailTemplate = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            }
            logger.info("{}邮件 HTML 模板加载成功", LOG_MAGIC);
        } catch (Exception e) {
            logger.warn("{}邮件 HTML 模板加载失败, 将使用纯文本发送", LOG_MAGIC, e);
            magicLinkEmailTemplate = null;
        }
    }

    // ========== OTP ==========

    /**
     * 发送邮箱 OTP；成功返回 {@code null}，限流或发送失败返回错误码字符串。
     *
     * @param email 收件邮箱
     * @return {@code null} 表示已写入 Redis；{@code RATE_LIMITED} 表示限流；{@code SEND_FAILED} 表示 SES 失败
     */
    public String sendOtp(String email) {
        logger.info("{}准备发送验证码, email={}", LOG_OTP, email);

        String rateKey = CacheKey.EMAIL_OTP_RATE + email;
        if (Boolean.TRUE.equals(redisTemplate.hasKey(rateKey))) {
            logger.warn("{}被限流, email={}, 60秒内已发送过", LOG_OTP, email);
            return "RATE_LIMITED";
        }

        String code = String.valueOf(RandomUtils.insecure().randomInt(100000, 999999));
        logger.info("{}验证码已生成, email={}", LOG_OTP, email);

        try {
            sendEmail(email, "Your verification code", "Your verification code is: " + code);
            logger.info("{}SES发送成功, email={}", LOG_OTP, email);
        } catch (Exception e) {
            logger.error("{}SES发送失败, email={}, error={}", LOG_OTP, email, e.getMessage(), e);
            return "SEND_FAILED";
        }

        String otpKey = CacheKey.EMAIL_OTP + email;
        String attemptKey = CacheKey.EMAIL_OTP_ATTEMPT + email;
        redisTemplate.opsForValue().set(otpKey, code, OTP_EXPIRE_MINUTES, TimeUnit.MINUTES);
        redisTemplate.opsForValue().set(rateKey, 1, RATE_LIMIT_SECONDS, TimeUnit.SECONDS);
        redisTemplate.delete(attemptKey);

        logger.info("{}Redis已写入, email={}, otpExpire={}min, rateLimit={}s", LOG_OTP, email, OTP_EXPIRE_MINUTES, RATE_LIMIT_SECONDS);
        return null;
    }

    /**
     * 校验邮箱 OTP。
     *
     * @param email 邮箱
     * @param code  用户输入的验证码
     * @return {@code null} 表示通过；否则为 {@code OTP_EXPIRED}、{@code OTP_ATTEMPTS_EXCEEDED}、{@code INVALID_OTP} 等
     */
    public String verifyOtp(String email, String code) {
        logger.info("{}校验验证码, email={}", LOG_OTP, email);

        String otpKey = CacheKey.EMAIL_OTP + email;
        String attemptKey = CacheKey.EMAIL_OTP_ATTEMPT + email;

        String storedCode = (String) redisTemplate.opsForValue().get(otpKey);
        if (StringUtils.isBlank(storedCode)) {
            logger.warn("{}验证码已过期或不存在, email={}", LOG_OTP, email);
            return "OTP_EXPIRED";
        }

        Integer attempts = (Integer) redisTemplate.opsForValue().get(attemptKey);
        if (attempts != null && attempts >= MAX_ATTEMPTS) {
            redisTemplate.delete(otpKey);
            redisTemplate.delete(attemptKey);
            logger.warn("{}尝试次数超限, email={}, attempts={}", LOG_OTP, email, attempts);
            return "OTP_ATTEMPTS_EXCEEDED";
        }

        if (!storedCode.equals(code)) {
            redisTemplate.opsForValue().increment(attemptKey);
            if (attempts == null) {
                redisTemplate.expire(attemptKey, OTP_EXPIRE_MINUTES, TimeUnit.MINUTES);
            }
            logger.warn("{}验证码不匹配, email={}, currentAttempts={}", LOG_OTP, email, (attempts != null ? attempts + 1 : 1));
            return "INVALID_OTP";
        }

        redisTemplate.delete(otpKey);
        redisTemplate.delete(attemptKey);
        redisTemplate.delete(CacheKey.EMAIL_OTP_RATE + email);
        logger.info("{}验证码校验通过, email={}", LOG_OTP, email);
        return null;
    }

    // ========== Magic Link ==========

    /**
     * 发送 Magic Link 邮件并写入 Redis；返回用于客户端轮询的 loginToken，失败返回 {@code null}。
     */
    public String sendMagicLink(String email, Integer platformType) {
        logger.info("{}准备发送, email={}, platformType={}", LOG_MAGIC, email, platformType);

        String rateKey = CacheKey.EMAIL_OTP_RATE + email;
        if (Boolean.TRUE.equals(redisTemplate.hasKey(rateKey))) {
            logger.warn("{}被限流, email={}, 60秒内已发送过", LOG_MAGIC, email);
            return null;
        }

        String loginToken = UUID.randomUUID().toString();
        String link = magicLinkBaseUrl + "?verifyToken=" + loginToken;
        logger.info("{}链接已生成, email={}, loginToken={}, link={}", LOG_MAGIC, email, loginToken, link);

        try {
            if (magicLinkEmailTemplate != null) {
                String htmlBody = magicLinkEmailTemplate.replace("{{MAGIC_LINK_URL}}", link);
                sendHtmlEmail(email, "Sign in to CRSPY", htmlBody);
            } else {
                sendEmail(email, "Log in to your account", "Click the link to log in: " + link);
            }
            logger.info("{}SES发送成功, email={}, loginToken={}", LOG_MAGIC, email, loginToken);
        } catch (Exception e) {
            if (!mockMagicLinkOnSendFailure) {
                logger.error("{}SES发送失败, email={}, error={}", LOG_MAGIC, email, e.getMessage(), e);
                return null;
            }
            logger.warn("{}SES发送失败但mock模式开启, email={}, loginToken={}, error={}", LOG_MAGIC, email, loginToken, e.getMessage());
        }

        Map<String, Object> state = new HashMap<>();
        state.put("email", email);
        state.put("status", "pending");
        state.put("platformType", platformType);

        String magicKey = CacheKey.MAGIC_LINK + loginToken;
        redisTemplate.opsForValue().set(magicKey, JacksonUtil.bean2Json(state), MAGIC_LINK_EXPIRE_MINUTES, TimeUnit.MINUTES);
        redisTemplate.opsForValue().set(rateKey, 1, RATE_LIMIT_SECONDS, TimeUnit.SECONDS);

        logger.info("{}Redis已写入, email={}, loginToken={}, magicExpire={}min", LOG_MAGIC, email, loginToken, MAGIC_LINK_EXPIRE_MINUTES);
        return loginToken;
    }

    /**
     * 读取 Magic Link 在 Redis 中的状态快照（反序列化为 Map）。
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> getMagicLinkState(String loginToken) {
        String magicKey = CacheKey.MAGIC_LINK + loginToken;
        String json = (String) redisTemplate.opsForValue().get(magicKey);
        if (StringUtils.isBlank(json)) {
            return null;
        }
        return JacksonUtil.json2Bean(json, Map.class);
    }

    /**
     * 在 central-account 签发 token 成功后，将 Magic Link 状态更新为已验证并保留原 TTL。
     */
    public void completeMagicLink(String loginToken, String userToken, String userId, int businessType, int platformType) {
        String magicKey = CacheKey.MAGIC_LINK + loginToken;

        Map<String, Object> state = new HashMap<>();
        state.put("status", "verified");
        state.put("userToken", userToken);
        state.put("userId", userId);
        state.put("businessType", businessType);
        state.put("platformType", platformType);

        Long ttl = redisTemplate.getExpire(magicKey, TimeUnit.SECONDS);
        if (ttl != null && ttl > 0) {
            redisTemplate.opsForValue().set(magicKey, JacksonUtil.bean2Json(state), ttl, TimeUnit.SECONDS);
        }
        logger.info("{}状态已更新为verified, loginToken={}, userId={}", LOG_MAGIC, loginToken, userId);
    }

    /**
     * 轮询成功取走登录凭证后，写入短时墓碑状态，区分「已消费」与「从未验证即过期」。
     */
    public void consumeMagicLink(String loginToken) {
        String magicKey = CacheKey.MAGIC_LINK + loginToken;
        Map<String, Object> tombstone = new HashMap<>();
        tombstone.put("status", "failed");
        tombstone.put("failureReason", "consumed");
        redisTemplate.opsForValue().set(magicKey, JacksonUtil.bean2Json(tombstone),
                MAGIC_LINK_CONSUMED_TOMBSTONE_SECONDS, TimeUnit.SECONDS);
        logger.info("{}已消费, 写入consumed墓碑 {}s, loginToken={}", LOG_MAGIC, MAGIC_LINK_CONSUMED_TOMBSTONE_SECONDS, loginToken);
    }

    // ========== 内部方法 ==========

    private void sendEmail(String to, String subject, String textBody) {
        logger.info("{}发送纯文本邮件, to={}, subject={}", LOG_SES, to, subject);
        SendEmailRequest request = SendEmailRequest.builder()
                .source(fromAddress)
                .destination(Destination.builder().toAddresses(to).build())
                .message(Message.builder()
                        .subject(Content.builder().data(subject).charset("UTF-8").build())
                        .body(Body.builder()
                                .text(Content.builder().data(textBody).charset("UTF-8").build())
                                .build())
                        .build())
                .build();

        SendEmailResponse response = sesClient.sendEmail(request);
        logger.info("{}纯文本邮件发送完成, to={}, messageId={}", LOG_SES, to, response.messageId());
    }

    private void sendHtmlEmail(String to, String subject, String htmlBody) {
        logger.info("{}发送HTML邮件, to={}, subject={}", LOG_SES, to, subject);
        SendEmailRequest request = SendEmailRequest.builder()
                .source(fromAddress)
                .destination(Destination.builder().toAddresses(to).build())
                .message(Message.builder()
                        .subject(Content.builder().data(subject).charset("UTF-8").build())
                        .body(Body.builder()
                                .html(Content.builder().data(htmlBody).charset("UTF-8").build())
                                .build())
                        .build())
                .build();

        SendEmailResponse response = sesClient.sendEmail(request);
        logger.info("{}HTML邮件发送完成, to={}, messageId={}", LOG_SES, to, response.messageId());
    }
}
