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

import com.hojoai.central.account.service.user.UserTokenInfoService;
import com.hojoai.central.account.service.user.bean.UserTokenInfoRequest;
import com.hojoai.central.account.service.user.bean.UserTokenInfoResponse;
import com.hojoai.common.asserts.Asserts;
import com.hojoai.common.response.Response;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

/**
 * 用户访问令牌接口：凭手机验证码签发/删除/校验 token（按业务类型与平台维度区分）。
 */
@RestController
@RequestMapping(value = "/central-account")
public class UserTokenInfoController {

    @Resource
    private UserTokenInfoService userTokenInfoService;

    /**
     * 校验手机验证码后，为（必要时自动注册的）用户签发或更新指定业务/平台下的 userToken。
     *
     * @param userMobile     手机号，查询参数
     * @param mobileCaptcha  短信验证码，查询参数
     * @param request        请求体，须含 businessType、platformType（正整数）
     * @return 统一响应，成功时 data 为 {@link UserTokenInfoResponse}
     * @throws IllegalArgumentException 当 userMobile、mobileCaptcha 为空或 businessType、platformType 非法时（Asserts）
     * @throws Exception                业务或持久化异常
     */
    @PostMapping(value = "/tokens", params = {"userMobile", "mobileCaptcha"})
    public Response<UserTokenInfoResponse> createUserTokenByUserMobileAndMobileCaptchaToken(@RequestParam String userMobile, @RequestParam String mobileCaptcha, @RequestBody UserTokenInfoRequest request) throws Exception {
        Asserts.notBlank(userMobile, "userMobile");
        Asserts.notBlank(mobileCaptcha, "mobileCaptcha");
        Asserts.gtZero(request.getBusinessType(), "businessType");
        Asserts.gtZero(request.getPlatformType(), "platformType");

        return userTokenInfoService.addUserTokenInfoByUserMobileAndMobileCaptcha(userMobile, mobileCaptcha, request);
    }

    /**
     * 删除某用户下全部业务/平台的访问令牌记录（按实现语义为按 userId 清理）。
     *
     * @param userId 用户 ID，查询参数
     * @return 统一响应，成功时 data 可能为空对象或仅状态
     * @throws IllegalArgumentException 当 userId 为空时（Asserts）
     * @throws Exception                持久化异常
     */
    @DeleteMapping(value = "/tokens", params = {"userId"})
    public Response<UserTokenInfoResponse> deleteUserTokenInfoByUserId(@RequestParam String userId) throws Exception {
        Asserts.notBlank(userId, "userId");

        return userTokenInfoService.deleteUserTokenInfoByUserId(userId);
    }

    /**
     * 删除指定用户、业务类型与平台下的单条令牌记录。
     *
     * @param userId       用户 ID
     * @param businessType 业务类型，正整数
     * @param platformType 平台类型，正整数
     * @return 统一响应
     * @throws IllegalArgumentException 当 userId 为空或 businessType、platformType 非法时（Asserts）
     * @throws Exception                持久化异常
     */
    @DeleteMapping(value = "/tokens", params = {"userId", "businessType", "platformType"})
    public Response<UserTokenInfoResponse> deleteUserTokenInfoByUserIdAndBusinessTypeAndPlatformType(@RequestParam String userId, @RequestParam Integer businessType, @RequestParam Integer platformType) throws Exception {
        Asserts.notBlank(userId, "userId");
        Asserts.gtZero(businessType, "businessType");
        Asserts.gtZero(platformType, "platformType");

        return userTokenInfoService.deleteUserTokenInfoByUserIdAndBusinessTypeAndPlatformType(userId, businessType, platformType);
    }

    /**
     * 校验 userId、businessType、platformType 与 userToken 是否与库中一致（用于鉴权或会话校验）。
     *
     * @param userId       用户 ID
     * @param businessType 业务类型，正整数
     * @param platformType 平台类型，正整数
     * @param userToken    待校验的令牌字符串
     * @return 统一响应，校验通过时 data 含完整令牌信息；不匹配或无记录时返回业务错误码
     * @throws IllegalArgumentException 当 userId 为空或 businessType、platformType、userToken 非法时（Asserts）
     * @throws Exception                查询或持久化异常
     */
    @GetMapping(value = "/tokens", params = {"userId", "businessType", "platformType", "userToken"})
    public Response<UserTokenInfoResponse> queryUserTokenInfoByUserIdAndBusinessTypeAndPlatformTypeAndUserToken(@RequestParam String userId, @RequestParam Integer businessType, @RequestParam Integer platformType, @RequestParam String userToken) throws Exception {
        Asserts.notBlank(userId, "userId");
        Asserts.gtZero(businessType, "businessType");
        Asserts.gtZero(platformType, "platformType");
        Asserts.notBlank(userToken, "userToken");

        return userTokenInfoService.queryUserTokenInfoByUserIdAndBusinessTypeAndPlatformTypeAndUserToken(userId, businessType, platformType, userToken);
    }
}
