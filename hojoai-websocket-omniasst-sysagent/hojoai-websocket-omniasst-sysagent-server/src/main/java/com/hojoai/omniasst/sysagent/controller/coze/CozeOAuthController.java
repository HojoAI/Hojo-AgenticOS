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
import com.hojoai.common.response.Response;
import com.hojoai.common.response.ResponseStatusBasic;
import jakarta.annotation.Resource;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Coze 平台 JWT OAuth 令牌对外接口，供客户端获取 access token。
 */
@RestController
@RequestMapping(value = "/omniasst-sysagent")
public class CozeOAuthController {

    @Resource
    private CozeOAuthService cozeOAuthService;

    /**
     * 获取 Coze OAuth access token（由 {@link CozeOAuthService} 经 SDK 签发）。
     *
     * @return 统一响应体，{@code resContent} 为 {@link OAuthTokenResponse}
     */
    @PostMapping(value = "/coze-tokens", produces = MediaType.APPLICATION_JSON_VALUE)
    public Response<OAuthTokenResponse> getToken() {
        OAuthToken token = cozeOAuthService.getAccessToken();
        OAuthTokenResponse response = new OAuthTokenResponse(token.getAccessToken());
        return Response.newInstance(ResponseStatusBasic.OK, response);
    }
}
