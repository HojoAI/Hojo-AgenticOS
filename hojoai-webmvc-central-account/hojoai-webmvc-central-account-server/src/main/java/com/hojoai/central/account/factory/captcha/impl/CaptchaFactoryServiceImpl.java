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
package com.hojoai.central.account.factory.captcha.impl;

import com.aliyun.dysmsapi20170525.Client;
import com.aliyun.dysmsapi20170525.models.SendSmsRequest;
import com.aliyun.dysmsapi20170525.models.SendSmsResponse;
import com.hojoai.central.account.common.CacheKey;
import com.hojoai.central.account.factory.captcha.CaptchaFactoryService;
import com.hojoai.common.jackson.JacksonUtil;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.RandomUtils;
import org.springframework.beans.factory.annotation.Value;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * 短信验证码工厂实现：限流计数（Redis）、调用阿里云短信、校验验证码。
 */
@Component
public class CaptchaFactoryServiceImpl implements CaptchaFactoryService {

    private static final Logger logger = LoggerFactory.getLogger(CaptchaFactoryServiceImpl.class);

    private static final String SMS_TEMPLATE_PARAM_FORMAT = "{\"code\":\"%s\"}";

    private static final Integer CAPTCHA_EXPIRE_TIME = 5;

    @Value("${captcha.mock.user-mobile}")
    private String mockUserMobile;

    @Value("${captcha.mock.mobile-captcha}")
    private String mockMobileCaptcha;

    @Value("${aliyun.sms.sign-name}")
    private String smsSignName;

    @Value("${aliyun.sms.template-code}")
    private String smsTemplateCode;

    private static final Integer THRESHOLD_DAY = 40;
    private static final Integer THRESHOLD_HOUR = 40;
    private static final Integer THRESHOLD_MINUTE = 40;

    private static final Duration EXPIRE_TIME_DAY = Duration.ofDays(1);
    private static final Duration EXPIRE_TIME_HOUR = Duration.ofHours(1);
    private static final Duration EXPIRE_TIME_MINUTE = Duration.ofMinutes(1);

    private static final int SMS_RESPONSE_OK = 200;
    private static final String SMS_RESULT_OK = "OK";
    private static final String SMS_RESULT_DAY_LIMIT = "isv.DAY_LIMIT_CONTROL";
    private static final String SMS_RESULT_MONTH_LIMIT = "isv.MONTH_LIMIT_CONTROL";
    private static final String SMS_RESULT_BUSINESS_LIMIT = "isv.BUSINESS_LIMIT_CONTROL";

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Resource
    private Client client;

    @Override
    public Result createMobileCaptcha(String userMobile, String mobileCaptchaType) {
        if (mockUserMobile.equals(userMobile)) {
            logger.info("mock mobile detected, skip sms sending, userMobile: {}", userMobile);
            return Result.newInstance(ResultStatus.OK);
        }

        String captchaCountByMobileDayKey = CacheKey.CAPTCHA_COUNT_BY_MOBILE_DAY + userMobile + CacheKey.DELIMITER + mobileCaptchaType;
        Integer captchaCountByMobileDay = (Integer) redisTemplate.opsForValue().get(captchaCountByMobileDayKey);
        if (captchaCountByMobileDay != null && captchaCountByMobileDay >= THRESHOLD_DAY) {
            return Result.newInstance(ResultStatus.MOBILE_CAPTCHA_COUNT_LIMIT);
        }

        String captchaCountByMobileHourKey = CacheKey.CAPTCHA_COUNT_BY_MOBILE_HOUR + userMobile + CacheKey.DELIMITER + mobileCaptchaType;
        Integer captchaCountByMobileHour = (Integer) redisTemplate.opsForValue().get(captchaCountByMobileHourKey);
        if (captchaCountByMobileHour != null && captchaCountByMobileHour >= THRESHOLD_HOUR) {
            return Result.newInstance(ResultStatus.MOBILE_CAPTCHA_COUNT_LIMIT);
        }

        String captchaCountByMobileMintueKey = CacheKey.CAPTCHA_COUNT_BY_MOBILE_MINITUE + userMobile + CacheKey.DELIMITER + mobileCaptchaType;
        Integer captchaCountByMobileMintue = (Integer) redisTemplate.opsForValue().get(captchaCountByMobileMintueKey);
        if (captchaCountByMobileMintue != null && captchaCountByMobileMintue >= THRESHOLD_MINUTE) {
            return Result.newInstance(ResultStatus.MOBILE_CAPTCHA_COUNT_LIMIT);
        }

        try {
            String captcha = String.valueOf(RandomUtils.insecure().randomInt(100000, 999999));

            SendSmsRequest sendSmsRequest = new SendSmsRequest();
            sendSmsRequest.setPhoneNumbers(userMobile);
            sendSmsRequest.setSignName(smsSignName);
            sendSmsRequest.setTemplateCode(smsTemplateCode);
            sendSmsRequest.setTemplateParam(String.format(SMS_TEMPLATE_PARAM_FORMAT, captcha));
            SendSmsResponse sendSmsResponse = client.sendSms(sendSmsRequest);

            if (SMS_RESPONSE_OK != sendSmsResponse.statusCode) {
                logger.warn("send sms failed, send sms response: {}", JacksonUtil.bean2Json(sendSmsResponse));
                return Result.newInstance(ResultStatus.MOBILE_CAPTCHA_SENDING_FAILED);
            }
            if (!SMS_RESULT_OK.equals(sendSmsResponse.body.getCode())) {
                logger.warn("send sms failed, send sms response: {}", JacksonUtil.bean2Json(sendSmsResponse));
                if (SMS_RESULT_DAY_LIMIT.equals(sendSmsResponse.body.getCode())) {
                    return Result.newInstance(ResultStatus.MOBILE_CAPTCHA_COUNT_LIMIT);
                }
                if (SMS_RESULT_MONTH_LIMIT.equals(sendSmsResponse.body.getCode())) {
                    return Result.newInstance(ResultStatus.MOBILE_CAPTCHA_COUNT_LIMIT);
                }
                if (SMS_RESULT_BUSINESS_LIMIT.equals(sendSmsResponse.body.getCode())) {
                    return Result.newInstance(ResultStatus.MOBILE_CAPTCHA_COUNT_LIMIT);
                }
                return Result.newInstance(ResultStatus.MOBILE_CAPTCHA_SENDING_FAILED);
            }

            String captchaByMobileKey = CacheKey.CAPTCHA_BY_MOBILE + userMobile + CacheKey.DELIMITER + mobileCaptchaType;
            redisTemplate.opsForValue().set(captchaByMobileKey, captcha, CAPTCHA_EXPIRE_TIME, TimeUnit.MINUTES);

            redisTemplate.opsForValue().set(captchaCountByMobileDayKey, Objects.requireNonNullElse(captchaCountByMobileDay, 0) + 1, EXPIRE_TIME_DAY);
            redisTemplate.opsForValue().set(captchaCountByMobileHourKey, Objects.requireNonNullElse(captchaCountByMobileHour, 0) + 1, EXPIRE_TIME_HOUR);
            redisTemplate.opsForValue().set(captchaCountByMobileMintueKey, Objects.requireNonNullElse(captchaCountByMobileMintue, 0) + 1, EXPIRE_TIME_MINUTE);

            return Result.newInstance(ResultStatus.OK, JacksonUtil.bean2Json(sendSmsResponse));
        } catch (Exception e) {
            throw new UnsupportedOperationException("create user mobile captcha error", e);
        }
    }

    @Override
    public Boolean verifyMobileCaptcha(String userMobile, String mobileCaptchaType, String mobileCaptcha) {
        if (mockUserMobile.equals(userMobile)) {
            logger.info("mock mobile detected, verify with fixed captcha, userMobile: {}", userMobile);
            return mockMobileCaptcha.equals(mobileCaptcha);
        }

        String captchaByMobileKey = CacheKey.CAPTCHA_BY_MOBILE + userMobile + CacheKey.DELIMITER + mobileCaptchaType;

        String mobileCaptchaFromRedis = (String) redisTemplate.opsForValue().get(captchaByMobileKey);
        if (StringUtils.isNotBlank(mobileCaptchaFromRedis) && mobileCaptchaFromRedis.equals(mobileCaptcha)) {
            redisTemplate.delete(captchaByMobileKey);
            return true;
        }

        return false;
    }
}
