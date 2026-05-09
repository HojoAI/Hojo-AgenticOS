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
package com.hojoai.omniasst.account.controller.user;

import com.hojoai.common.asserts.Asserts;
import com.hojoai.common.response.Response;
import com.hojoai.omniasst.account.client.CentralAccountClient;
import com.hojoai.omniasst.account.service.user.bean.UserInfoRequest;
import com.hojoai.omniasst.account.service.user.bean.UserInfoResponse;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

/**
 * The controller class for the user info.
 *
 * @author kevanwong
 * @version 1.0, 2017/10/21
 * @since hojoai-webmvc-omniasst-account 1.0.0
 */
@RestController
@RequestMapping(value = "/omniasst-account")
public class UserInfoController {

    @Resource
    private CentralAccountClient centralAccountClient;

    /**
     * 使用短信验证码新增用户资料。
     */
    @PostMapping(value = "/users", params = {"mobileCaptcha"})
    public Response<UserInfoResponse> addUserInfoByUserMobile(@RequestParam String mobileCaptcha, @RequestBody UserInfoRequest request) throws Exception {
        Asserts.notBlank(mobileCaptcha, "mobileCaptcha");
        Asserts.notBlank(request.getUserMobile(), "userMobile");

        return centralAccountClient.addUserInfo(mobileCaptcha, request);
    }

    /**
     * 按用户 ID 部分更新资料（如换绑手机等，由 patchType 区分）。
     */
    @PatchMapping(value = "/users/{userId}", params = {"patchType"})
    public Response<UserInfoResponse> updateUserInfoByUserId(@PathVariable String userId, @RequestParam String patchType, @RequestParam(required = false) String newMobileCaptcha, @RequestParam(required = false) String oldMobileCaptcha, @RequestBody UserInfoRequest request) throws Exception {
        Asserts.notBlank(userId, "userId");
        Asserts.notBlank(patchType, "patchType");

        return centralAccountClient.updateUserInfo(userId, patchType, newMobileCaptcha, oldMobileCaptcha, request);
    }

    /**
     * 按用户 ID 查询资料。
     */
    @GetMapping(value = "/users/{userId}")
    public Response<UserInfoResponse> queryUserInfoByUserId(@PathVariable String userId) throws Exception {
        Asserts.notBlank(userId, "userId");

        return centralAccountClient.queryUserInfoByUserId(userId);
    }

    /**
     * 按手机号查询资料。
     */
    @GetMapping(value = "/users", params = {"userMobile"})
    public Response<UserInfoResponse> queryUserInfoByUserMobile(@RequestParam String userMobile) throws Exception {
        Asserts.notBlank(userMobile, "userMobile");

        return centralAccountClient.queryUserInfoByUserMobile(userMobile);
    }
}
