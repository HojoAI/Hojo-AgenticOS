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
package com.hojoai.omniasst.account.service.auth;

import com.hojoai.omniasst.account.service.auth.bean.*;
import com.hojoai.common.response.Response;

/**
 * 海外与邮箱相关认证业务：第三方登录、邮箱 OTP、Magic Link、登出与注销，内部通过 central-account 签发 token。
 */
public interface AuthService {

    /**
     * Google 登录：校验 idToken 后在 central-account 创建会话。
     *
     * @param request 含 idToken、platformType
     * @return 登录结果，失败时携带业务错误码
     * @throws Exception 下游或框架异常
     */
    Response<AuthLoginResponse> loginByGoogle(GoogleLoginRequest request) throws Exception;

    /**
     * Apple 登录：校验 identityToken 后在 central-account 创建会话。
     *
     * @param request 含 identityToken、platformType 等
     * @return 登录结果
     * @throws Exception 下游或框架异常
     */
    Response<AuthLoginResponse> loginByApple(AppleLoginRequest request) throws Exception;

    /**
     * 发送邮箱 OTP（受 Redis 限流约束）。
     *
     * @param request 收件邮箱
     * @return 成功或限流/错误码
     * @throws Exception 下游或框架异常
     */
    Response<?> sendEmailCode(EmailSendCodeRequest request) throws Exception;

    /**
     * 邮箱 OTP 登录。
     *
     * @param request 邮箱、验证码、platformType
     * @return 登录结果
     * @throws Exception 下游或框架异常
     */
    Response<AuthLoginResponse> loginByEmailOtp(EmailLoginRequest request) throws Exception;

    /**
     * 发送 Magic Link 邮件，返回供客户端轮询的 loginToken。
     *
     * @param request 邮箱、platformType
     * @return 含 loginToken 或错误
     * @throws Exception 下游或框架异常
     */
    Response<MagicLinkSendResponse> sendMagicLink(EmailSendCodeRequest request) throws Exception;

    /**
     * 根据 loginToken 轮询 Magic Link 验证状态（pending / success / failed）。
     *
     * @param loginToken 发送 Magic Link 时返回的 token
     * @return 状态与凭证，失败时见 {@link AuthLoginResponse#getFailureReason()}
     * @throws Exception 下游或框架异常
     */
    Response<AuthLoginResponse> queryMagicLinkStatus(String loginToken) throws Exception;

    /**
     * 用户点击邮件链接或 App 提交 token 时完成 Magic Link 验证并签发会话。
     *
     * @param request 含邮件中的 verify token
     * @return 验证结果或登录数据
     * @throws Exception 下游或框架异常
     */
    Response<?> verifyMagicLink(MagicLinkVerifyRequest request) throws Exception;

    /**
     * 登出：删除 central-account 侧用户 token。
     *
     * @param userId 用户 ID
     * @return 下游响应
     * @throws Exception 下游或框架异常
     */
    Response<?> logout(String userId) throws Exception;

    /**
     * 注销海外账号（业务侧定义）。
     *
     * @param userId 用户 ID
     * @return 下游响应
     * @throws Exception 下游或框架异常
     */
    Response<?> deleteAccount(String userId) throws Exception;
}
