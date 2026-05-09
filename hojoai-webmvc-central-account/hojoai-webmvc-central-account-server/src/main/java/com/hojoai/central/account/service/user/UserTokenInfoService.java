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
package com.hojoai.central.account.service.user;

import com.hojoai.central.account.service.user.bean.UserTokenInfoRequest;
import com.hojoai.central.account.service.user.bean.UserTokenInfoResponse;
import com.hojoai.common.response.Response;

/**
 * 用户访问令牌领域服务：签发、按维度删除、校验令牌有效性。
 */
public interface UserTokenInfoService {

    /**
     * 校验手机验证码后签发或更新指定业务/平台下的令牌；若手机号无用户则自动注册并分配昵称。
     *
     * @param userMobile     手机号
     * @param mobileCaptcha  短信验证码
     * @param request        须含 businessType、platformType
     * @return 统一响应，成功时含 {@link UserTokenInfoResponse}
     * @throws Exception 持久化或依赖异常
     */
    Response<UserTokenInfoResponse> addUserTokenInfoByUserMobileAndMobileCaptcha(String userMobile, String mobileCaptcha, UserTokenInfoRequest request) throws Exception;

    /**
     * 按用户 ID 删除其下全部令牌记录（实现以 DAO 为准）。
     *
     * @param userId 用户 ID
     * @return 统一响应
     * @throws Exception 持久化异常
     */
    Response<UserTokenInfoResponse> deleteUserTokenInfoByUserId(String userId) throws Exception;

    /**
     * 按用户 ID + 业务类型 + 平台类型删除单条令牌。
     *
     * @param userId       用户 ID
     * @param businessType 业务类型
     * @param platformType 平台类型
     * @return 统一响应
     * @throws Exception 持久化异常
     */
    Response<UserTokenInfoResponse> deleteUserTokenInfoByUserIdAndBusinessTypeAndPlatformType(String userId, Integer businessType, Integer platformType) throws Exception;

    /**
     * 校验库中存储的令牌是否与入参 userToken 一致。
     *
     * @param userId       用户 ID
     * @param businessType 业务类型
     * @param platformType 平台类型
     * @param userToken    待校验令牌
     * @return 统一响应，不一致或无记录时返回业务错误码
     * @throws Exception 持久化异常
     */
    Response<UserTokenInfoResponse> queryUserTokenInfoByUserIdAndBusinessTypeAndPlatformTypeAndUserToken(String userId, Integer businessType, Integer platformType, String userToken) throws Exception;
}
