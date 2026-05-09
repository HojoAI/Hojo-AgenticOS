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
package com.hojoai.central.account.dao.auth;

import com.hojoai.central.account.dao.auth.po.OverseaAuthIdentity;

public interface OverseaAuthIdentityDAO {

    void insertIdentity(OverseaAuthIdentity identity);

    OverseaAuthIdentity selectByProviderAndProviderUserId(String provider, String providerUserId);

    /**
     * 注销账号时删除该用户下全部第三方/邮箱绑定，便于同一凭证再次登录时创建新用户。
     */
    void deleteByUserId(Long userId);
}
