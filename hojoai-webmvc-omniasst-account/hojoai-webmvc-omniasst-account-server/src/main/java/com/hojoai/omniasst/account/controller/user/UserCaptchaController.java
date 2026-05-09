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
package com.hojoai.omniasst.account.controller.user;

import com.hojoai.common.asserts.Asserts;
import com.hojoai.common.response.Response;
import com.hojoai.omniasst.account.client.CentralAccountClient;
import com.hojoai.omniasst.account.service.user.bean.UserMobileCaptchaRequest;
import com.hojoai.omniasst.account.service.user.bean.UserMobileCaptchaResponse;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * The controller class for the user captcha.
 *
 * @author kevanwong
 * @version 1.0, 2025/11/03
 * @since hojoai-webmvc-omniasst-account 1.0.0
 */
@RestController
@RequestMapping(value = "/omniasst-account")
public class UserCaptchaController {

    @Resource
    private CentralAccountClient centralAccountClient;

    /**
     * 申请手机验证码。
     */
    @PostMapping(value = "/mobile-captchas")
    public Response<UserMobileCaptchaResponse> createUserMobileCaptcha(@RequestBody UserMobileCaptchaRequest request) throws Exception {
        Asserts.notBlank(request.getUserMobile(), "userMobile");
        Asserts.notBlank(request.getMobileCaptchaType(), "mobileCaptchaType");

        return centralAccountClient.createUserMobileCaptcha(request);
    }
}
