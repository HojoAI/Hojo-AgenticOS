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
package com.hojoai.central.account.service.user.impl;

import com.hojoai.central.account.common.ResponseStatusExtra;
import com.hojoai.central.account.factory.captcha.CaptchaFactoryService;
import com.hojoai.central.account.service.user.UserMoblieCaptchaService;
import com.hojoai.central.account.service.user.bean.UserMobileCaptchaRequest;
import com.hojoai.central.account.service.user.bean.UserMobileCaptchaResponse;
import com.hojoai.common.response.Response;
import com.hojoai.common.response.ResponseStatusBasic;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

/**
 * The service implements class for user mobile captcha.
 *
 * @author kevanwong
 * @version 1.0, 2025/11/03
 * @since hojoai-webmvc-central-account 1.0.0
 */
@Service
public class UserMoblieCaptchaServiceImpl implements UserMoblieCaptchaService {

    @Resource
    private CaptchaFactoryService captchaFactoryService;

    @Override
    public Response<UserMobileCaptchaResponse> createUserMobileCaptcha(UserMobileCaptchaRequest request) throws Exception {
        CaptchaFactoryService.Result result = captchaFactoryService.createMobileCaptcha(request.getUserMobile(), request.getMobileCaptchaType());
        if (result == null) {
            return Response.newInstance(ResponseStatusBasic.GENERAL_ERROR);
        }
        if (CaptchaFactoryService.ResultStatus.OK.equals(result.getResultStatus())) {
            return Response.newInstance(ResponseStatusBasic.OK);
        }
        if (CaptchaFactoryService.ResultStatus.MOBILE_CAPTCHA_COUNT_LIMIT.equals(result.getResultStatus())) {
            return Response.newInstance(ResponseStatusExtra.MOBILE_CAPTCHA_COUNT_LIMIT);
        }
        if (CaptchaFactoryService.ResultStatus.MOBILE_CAPTCHA_SENDING_FAILED.equals(result.getResultStatus())) {
            return Response.newInstance(ResponseStatusExtra.MOBILE_CAPTCHA_SENDING_FAILED);
        }

        return Response.newInstance(ResponseStatusBasic.GENERAL_ERROR);
    }
}
