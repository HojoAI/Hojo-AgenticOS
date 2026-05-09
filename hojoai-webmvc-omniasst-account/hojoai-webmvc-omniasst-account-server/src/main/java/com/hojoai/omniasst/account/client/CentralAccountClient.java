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
package com.hojoai.omniasst.account.client;

import com.hojoai.common.response.Response;
import com.hojoai.omniasst.account.service.auth.bean.OverseaTokenRequest;
import com.hojoai.omniasst.account.service.user.bean.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

/**
 * Feign client for central-account service.
 * Service name is configurable via ${central.account.service.name} to support
 * both domestic and overseas deployments on the same cluster.
 */
@FeignClient(name = "${central.account.service.name}", path = "/central-account")
public interface CentralAccountClient {

    @PostMapping(value = "/mobile-captchas")
    Response<UserMobileCaptchaResponse> createUserMobileCaptcha(@RequestBody UserMobileCaptchaRequest request);

    @PostMapping(value = "/tokens")
    Response<UserTokenInfoResponse> createUserToken(
            @RequestParam("userMobile") String userMobile,
            @RequestParam("mobileCaptcha") String mobileCaptcha,
            @RequestBody UserTokenInfoRequest request);

    @DeleteMapping(value = "/tokens")
    Response<UserTokenInfoResponse> deleteUserToken(
            @RequestParam("userId") String userId,
            @RequestParam(value = "businessType", required = false) Integer businessType,
            @RequestParam(value = "platformType", required = false) Integer platformType);

    @GetMapping(value = "/tokens")
    Response<UserTokenInfoResponse> queryUserToken(
            @RequestParam("userId") String userId,
            @RequestParam("businessType") Integer businessType,
            @RequestParam("platformType") Integer platformType,
            @RequestParam("userToken") String userToken);

    @PostMapping(value = "/users")
    Response<UserInfoResponse> addUserInfo(
            @RequestParam("mobileCaptcha") String mobileCaptcha,
            @RequestBody UserInfoRequest request);

    @PatchMapping(value = "/users/{userId}")
    Response<UserInfoResponse> updateUserInfo(
            @PathVariable("userId") String userId,
            @RequestParam("patchType") String patchType,
            @RequestParam(value = "newMobileCaptcha", required = false) String newMobileCaptcha,
            @RequestParam(value = "oldMobileCaptcha", required = false) String oldMobileCaptcha,
            @RequestBody UserInfoRequest request);

    @GetMapping(value = "/users/{userId}")
    Response<UserInfoResponse> queryUserInfoByUserId(@PathVariable("userId") String userId);

    @GetMapping(value = "/users")
    Response<UserInfoResponse> queryUserInfoByUserMobile(@RequestParam("userMobile") String userMobile);

    @PostMapping(value = "/auth/oversea-tokens")
    Response<UserTokenInfoResponse> createOverseaToken(@RequestBody OverseaTokenRequest request);

    @DeleteMapping(value = "/auth/oversea-account")
    Response<?> deleteOverseaAccount(@RequestParam("userId") String userId);
}
