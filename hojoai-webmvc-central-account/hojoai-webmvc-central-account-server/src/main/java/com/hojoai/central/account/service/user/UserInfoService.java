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

import com.hojoai.central.account.service.user.bean.UserInfoRequest;
import com.hojoai.central.account.service.user.bean.UserInfoResponse;
import com.hojoai.common.response.Response;

/**
 * 用户资料领域服务：注册、查询、按类型局部更新。
 */
public interface UserInfoService {

    /**
     * 使用短信验证码完成手机号注册；校验通过后写入 user_info 并分配随机昵称。
     *
     * @param mobileCaptcha 短信验证码
     * @param request       须含 userMobile
     * @return 统一响应，成功时含 {@link UserInfoResponse}
     * @throws Exception 持久化或验证码校验异常
     */
    Response<UserInfoResponse> addUserInfoByUserMobile(String mobileCaptcha, UserInfoRequest request) throws Exception;

    /**
     * 按 patchType 更新用户资料；换绑手机需同时校验新旧手机验证码。
     *
     * @param userId           用户 ID
     * @param patchType        {@link PatchType} 的 code
     * @param newMobileCaptcha 换绑场景：新手机验证码
     * @param oldMobileCaptcha 换绑场景：旧手机验证码
     * @param request          携带待更新字段
     * @return 统一响应，成功时含最新 {@link UserInfoResponse}
     * @throws Exception 持久化或验证码异常
     */
    Response<UserInfoResponse> updateUserInfoByUserId(String userId, String patchType, String newMobileCaptcha, String oldMobileCaptcha, UserInfoRequest request) throws Exception;

    /**
     * 按用户 ID 查询资料。
     *
     * @param userId 用户 ID
     * @return 统一响应；未注册时返回业务错误码
     * @throws Exception 查询异常
     */
    Response<UserInfoResponse> queryUserInfoByUserId(String userId) throws Exception;

    /**
     * 按手机号查询资料。
     *
     * @param userMobile 手机号
     * @return 统一响应；未注册时返回业务错误码
     * @throws Exception 查询异常
     */
    Response<UserInfoResponse> queryUserInfoByUserMobile(String userMobile) throws Exception;

    /**
     * PATCH 更新类型枚举，code 与接口 query 参数 patchType 一致。
     */
    enum PatchType {

        /** 换绑手机号，需新旧手机验证码 */
        UPDATE_BINDING_USER_MOBILE("UPDATE_BINDING_USER_MOBILE", "换绑用户手机"),

        /** 更新昵称 */
        UPDATE_USER_NICKNAME("UPDATE_USER_NICKNAME", "更新用户昵称"),

        /** 更新个人简介 */
        UPDATE_USER_DESCRIPTION("UPDATE_USER_DESCRIPTION", "更新用户简介"),

        /** 更新头像 URL */
        UPDATE_USER_HEAD_IMAGE("UPDATE_USER_HEAD_IMAGE", "更新用户头像");

        private String code;
        private String desc;

        PatchType(String code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getDesc() {
            return desc;
        }

        public void setDesc(String desc) {
            this.desc = desc;
        }
    }
}
