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
package com.hojoai.omniasst.sysagent.controller.coze;

import com.coze.openapi.client.auth.OAuthToken;
import com.coze.openapi.service.auth.JWTOAuthClient;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

/**
 * Coze OAuth 服务实现，委托 {@link JWTOAuthClient} 获取令牌。
 */
@Service
public class CozeOAuthServiceImpl implements CozeOAuthService {

    @Resource
    private JWTOAuthClient jwtOAuthClient;

    /**
     * {@inheritDoc}
     */
    @Override
    public OAuthToken getAccessToken() {
        return jwtOAuthClient.getAccessToken();
    }
}
