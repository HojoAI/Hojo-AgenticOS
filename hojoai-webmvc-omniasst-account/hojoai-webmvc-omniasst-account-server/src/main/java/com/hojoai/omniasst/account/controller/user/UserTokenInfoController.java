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
import com.hojoai.omniasst.account.service.user.bean.UserTokenInfoRequest;
import com.hojoai.omniasst.account.service.user.bean.UserTokenInfoResponse;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

/**
 * The controller class for the user token info.
 *
 * @author kevanwong
 * @version 1.0, 2017/10/21
 * @since hojoai-webmvc-omniasst-account 1.0.0
 */
@RestController
@RequestMapping(value = "/omniasst-account")
public class UserTokenInfoController {

    @Resource
    private CentralAccountClient centralAccountClient;

    /**
     * 手机号 + 短信验证码创建用户 token（businessType 固定为 1）。
     */
    @PostMapping(value = "/tokens", params = {"userMobile", "mobileCaptcha"})
    public Response<UserTokenInfoResponse> createUserTokenByUserMobileAndMobileCaptchaToken(@RequestParam String userMobile, @RequestParam String mobileCaptcha, @RequestBody UserTokenInfoRequest request) throws Exception {
        Asserts.notBlank(userMobile, "userMobile");
        Asserts.notBlank(mobileCaptcha, "mobileCaptcha");
        Asserts.gtZero(request.getPlatformType(), "platformType");

        request.setBusinessType(1);

        return centralAccountClient.createUserToken(userMobile, mobileCaptcha, request);
    }

    /**
     * 按用户 ID 删除 token（全业务/全平台语义由 central-account 解释）。
     */
    @DeleteMapping(value = "/tokens", params = {"userId"})
    public Response<UserTokenInfoResponse> deleteUserTokenInfoByUserId(@RequestParam String userId) throws Exception {
        Asserts.notBlank(userId, "userId");

        return centralAccountClient.deleteUserToken(userId, null, null);
    }

    /**
     * 按用户 ID 与平台类型删除 token（businessType 固定为 1）。
     */
    @DeleteMapping(value = "/tokens", params = {"userId", "platformType"})
    public Response<UserTokenInfoResponse> deleteUserTokenInfoByUserIdAndBusinessTypeAndPlatformType(@RequestParam String userId, @RequestParam Integer platformType) throws Exception {
        Asserts.notBlank(userId, "userId");
        Asserts.gtZero(platformType, "platformType");

        return centralAccountClient.deleteUserToken(userId, 1, platformType);
    }

    /**
     * 校验并查询用户 token（businessType 固定为 1）。
     */
    @GetMapping(value = "/tokens", params = {"userId", "platformType", "userToken"})
    public Response<UserTokenInfoResponse> queryUserTokenInfoByUserIdAndBusinessTypeAndPlatformTypeAndUserToken(@RequestParam String userId, @RequestParam Integer platformType, @RequestParam String userToken) throws Exception {
        Asserts.notBlank(userId, "userId");
        Asserts.gtZero(platformType, "platformType");
        Asserts.notBlank(userToken, "userToken");

        return centralAccountClient.queryUserToken(userId, 1, platformType, userToken);
    }
}
