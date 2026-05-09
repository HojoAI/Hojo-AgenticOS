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
package com.hojoai.central.account.controller.auth;

import com.hojoai.central.account.service.auth.OverseaAuthService;
import com.hojoai.central.account.service.auth.bean.OverseaTokenRequest;
import com.hojoai.central.account.service.user.bean.UserTokenInfoResponse;
import com.hojoai.common.asserts.Asserts;
import com.hojoai.common.response.Response;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

/**
 * 海外账号与认证相关 HTTP 接口（令牌签发、账号注销等）。
 */
@RestController
@RequestMapping(value = "/central-account/auth")
public class OverseaAuthController {

    @Resource
    private OverseaAuthService overseaAuthService;

    /**
     * 根据第三方身份创建或刷新业务侧访问令牌。
     *
     * @param request 请求体，须含 provider、providerUserId、platformType；email、nickname、avatar 可选
     * @return 统一响应，成功时 data 含 userId、userToken、businessType、platformType
     * @throws IllegalArgumentException 当 provider、providerUserId 为空或 platformType 非法时（Asserts）
     * @throws Exception 事务、数据库或下游调用异常
     */
    @PostMapping(value = "/oversea-tokens")
    public Response<UserTokenInfoResponse> createOverseaToken(@RequestBody OverseaTokenRequest request) throws Exception {
        Asserts.notBlank(request.getProvider(), "provider");
        Asserts.notBlank(request.getProviderUserId(), "providerUserId");
        Asserts.gtZero(request.getPlatformType(), "platformType");
        return overseaAuthService.createOverseaToken(request);
    }

    /**
     * 注销海外账号：清理第三方绑定、令牌，并禁用本地 user_info。
     *
     * @param userId 用户 ID，查询参数，不可为空
     * @return 统一响应，成功时一般为 OK 无业务体
     * @throws IllegalArgumentException 当 userId 为空时（Asserts）
     * @throws Exception 事务或数据库异常
     */
    @DeleteMapping(value = "/oversea-account", params = {"userId"})
    public Response<?> deleteOverseaAccount(@RequestParam String userId) throws Exception {
        Asserts.notBlank(userId, "userId");
        return overseaAuthService.deleteOverseaAccount(userId);
    }
}
