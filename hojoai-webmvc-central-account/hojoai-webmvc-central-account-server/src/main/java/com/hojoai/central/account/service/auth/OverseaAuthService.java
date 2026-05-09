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
package com.hojoai.central.account.service.auth;

import com.hojoai.central.account.service.auth.bean.OverseaTokenRequest;
import com.hojoai.central.account.service.user.bean.UserTokenInfoResponse;
import com.hojoai.common.response.Response;

/**
 * 海外/第三方账号与令牌服务：OAuth 类身份落地与业务 token 签发、账号注销。
 */
public interface OverseaAuthService {

    /**
     * 根据第三方身份查找或创建用户，同步本地 user_info，并签发 user_token。
     *
     * @param request 须含 provider、providerUserId；platformType 建议传入
     * @return 统一响应，成功时 data 为 {@link com.hojoai.central.account.service.user.bean.UserTokenInfoResponse}
     * @throws Exception 事务或持久化异常
     */
    Response<UserTokenInfoResponse> createOverseaToken(OverseaTokenRequest request) throws Exception;

    /**
     * 软删除海外用户、解绑第三方身份、清理令牌并禁用本地 user_info。
     *
     * @param userId 用户 ID
     * @return 统一响应
     * @throws Exception 事务或持久化异常
     */
    Response<?> deleteOverseaAccount(String userId) throws Exception;
}

