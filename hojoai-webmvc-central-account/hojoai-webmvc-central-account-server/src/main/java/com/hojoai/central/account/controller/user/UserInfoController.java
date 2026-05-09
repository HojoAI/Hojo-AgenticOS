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

import com.hojoai.central.account.service.user.UserInfoService;
import com.hojoai.central.account.service.user.bean.UserInfoRequest;
import com.hojoai.central.account.service.user.bean.UserInfoResponse;
import com.hojoai.common.asserts.Asserts;
import com.hojoai.common.response.Response;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

/**
 * 用户资料接口：注册、按 ID/手机查询、按 patchType 局部更新（昵称、手机、简介、头像等）。
 */
@RestController
@RequestMapping(value = "/central-account")
public class UserInfoController {

    @Resource
    private UserInfoService userInfoService;

    /**
     * 使用短信验证码注册新用户（手机号未注册时创建）。
     *
     * @param mobileCaptcha 短信验证码，查询参数
     * @param request       请求体，须含 userMobile
     * @return 统一响应，成功时 data 为 {@link UserInfoResponse}
     * @throws IllegalArgumentException 当 mobileCaptcha、userMobile 为空时（Asserts）
     * @throws Exception                业务或持久化异常
     */
    @PostMapping(value = "/users", params = {"mobileCaptcha"})
    public Response<UserInfoResponse> addUserInfoByUserMobile(@RequestParam String mobileCaptcha, @RequestBody UserInfoRequest request) throws Exception {
        Asserts.notBlank(mobileCaptcha, "mobileCaptcha");
        Asserts.notBlank(request.getUserMobile(), "userMobile");

        return userInfoService.addUserInfoByUserMobile(mobileCaptcha, request);
    }

    /**
     * 按用户 ID 局部更新资料；patchType 决定必填字段（换绑手机需新旧验证码等）。
     *
     * @param userId          用户 ID，路径变量
     * @param patchType       更新类型，见 {@link UserInfoService.PatchType} 的 code
     * @param newMobileCaptcha 换绑新手机时：新手机收到的验证码；其他场景可空
     * @param oldMobileCaptcha 换绑新手机时：旧手机收到的验证码；其他场景可空
     * @param request         请求体，按 patchType 提供 userMobile、userNickname 等
     * @return 统一响应，成功时 data 为更新后的 {@link UserInfoResponse}
     * @throws IllegalArgumentException 当 userId、patchType 为空，或各 patchType 对应必填字段缺失时（Asserts）
     * @throws Exception                业务或持久化异常
     */
    @PatchMapping(value = "/users/{userId}", params = {"patchType"})
    public Response<UserInfoResponse> updateUserInfoByUserId(@PathVariable String userId, @RequestParam String patchType, @RequestParam(required = false) String newMobileCaptcha, @RequestParam(required = false) String oldMobileCaptcha, @RequestBody UserInfoRequest request) throws Exception {
        Asserts.notBlank(userId, "userId");
        Asserts.notBlank(patchType, "patchType");

        if (UserInfoService.PatchType.UPDATE_BINDING_USER_MOBILE.getCode().equals(patchType)) {
            Asserts.notBlank(newMobileCaptcha, "newMobileCaptcha");
            Asserts.notBlank(oldMobileCaptcha, "oldMobileCaptcha");
            Asserts.notBlank(request.getUserMobile(), "userMobile");
        }
        if (UserInfoService.PatchType.UPDATE_USER_NICKNAME.getCode().equals(patchType)) {
            Asserts.notBlank(request.getUserNickname(), "userNickname");
        }
        if (UserInfoService.PatchType.UPDATE_USER_DESCRIPTION.getCode().equals(patchType)) {
            Asserts.notBlank(request.getUserDescription(), "userDescription");
        }
        if (UserInfoService.PatchType.UPDATE_USER_HEAD_IMAGE.getCode().equals(patchType)) {
            Asserts.notBlank(request.getUserHeadImage(), "userHeadImage");
        }

        return userInfoService.updateUserInfoByUserId(userId, patchType, newMobileCaptcha, oldMobileCaptcha, request);
    }

    /**
     * 按用户 ID 查询用户资料。
     *
     * @param userId 用户 ID，路径变量
     * @return 统一响应，成功时 data 为 {@link UserInfoResponse}；未注册时返回业务错误码
     * @throws IllegalArgumentException 当 userId 为空时（Asserts）
     * @throws Exception                查询或持久化异常
     */
    @GetMapping(value = "/users/{userId}")
    public Response<UserInfoResponse> queryUserInfoByUserId(@PathVariable String userId) throws Exception {
        Asserts.notBlank(userId, "userId");

        return userInfoService.queryUserInfoByUserId(userId);
    }

    /**
     * 按手机号查询用户资料。
     *
     * @param userMobile 手机号，查询参数
     * @return 统一响应，成功时 data 为 {@link UserInfoResponse}；未注册时返回业务错误码
     * @throws IllegalArgumentException 当 userMobile 为空时（Asserts）
     * @throws Exception                查询或持久化异常
     */
    @GetMapping(value = "/users", params = {"userMobile"})
    public Response<UserInfoResponse> queryUserInfoByUserMobile(@RequestParam String userMobile) throws Exception {
        Asserts.notBlank(userMobile, "userMobile");

        return userInfoService.queryUserInfoByUserMobile(userMobile);
    }
}
