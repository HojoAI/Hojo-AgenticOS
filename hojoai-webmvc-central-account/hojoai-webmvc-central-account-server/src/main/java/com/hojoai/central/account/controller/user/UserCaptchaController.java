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
package com.hojoai.central.account.controller.user;

import com.hojoai.central.account.service.user.UserMoblieCaptchaService;
import com.hojoai.central.account.service.user.bean.UserMobileCaptchaRequest;
import com.hojoai.central.account.service.user.bean.UserMobileCaptchaResponse;
import com.hojoai.common.asserts.Asserts;
import com.hojoai.common.response.Response;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 手机验证码相关接口：发送短信验证码（含频控与渠道结果）。
 */
@RestController
@RequestMapping(value = "/central-account")
public class UserCaptchaController {

    @Resource
    private UserMoblieCaptchaService userMoblieCaptchaService;

    /**
     * 向指定手机号发送验证码（具体类型由 mobileCaptchaType 区分业务场景）。
     *
     * @param request 请求体，须含 userMobile、mobileCaptchaType（与 {@link com.hojoai.central.account.factory.captcha.CaptchaFactoryService.MobileCaptchaType} 的 code 对应）
     * @return 统一响应；成功时通常无额外字段，失败时携带业务错误码（如次数上限、发送失败）
     * @throws IllegalArgumentException 当 userMobile 或 mobileCaptchaType 为空时（Asserts）
     * @throws Exception 短信发送或 Redis 等异常封装后可能抛出
     */
    @PostMapping(value = "/mobile-captchas")
    public Response<UserMobileCaptchaResponse> createUserMobileCaptcha(@RequestBody UserMobileCaptchaRequest request) throws Exception {
        Asserts.notBlank(request.getUserMobile(), "userMobile");
        Asserts.notBlank(request.getMobileCaptchaType(), "mobileCaptchaType");

        return userMoblieCaptchaService.createUserMobileCaptcha(request);
    }
}
