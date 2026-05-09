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

import com.hojoai.central.account.service.user.bean.UserMobileCaptchaRequest;
import com.hojoai.central.account.service.user.bean.UserMobileCaptchaResponse;
import com.hojoai.common.response.Response;

/**
 * 手机验证码领域服务：调用短信渠道并维护 Redis 限流与验证码存储。
 */
public interface UserMoblieCaptchaService {

    /**
     * 向指定手机号发送验证码，受日/时/分钟频控及渠道返回约束。
     *
     * @param request 须含 userMobile、mobileCaptchaType
     * @return 统一响应；次数超限或发送失败时返回对应业务状态
     * @throws Exception 短信发送或缓存异常
     */
    Response<UserMobileCaptchaResponse> createUserMobileCaptcha(UserMobileCaptchaRequest request) throws Exception;
}
