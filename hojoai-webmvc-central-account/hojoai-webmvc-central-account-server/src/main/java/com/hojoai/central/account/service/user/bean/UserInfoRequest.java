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
package com.hojoai.central.account.service.user.bean;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * 用户资料写入请求体：用于注册、换绑手机、更新昵称/简介/头像等场景；不同接口使用的字段子集不同。
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserInfoRequest {

    /** 用户 ID（部分更新场景由路径传入，体中可不带） */
    private String userId;
    /** 手机号 */
    private String userMobile;
    /** 用户昵称 */
    private String userNickname;
    /** 个人简介 */
    private String userDescription;
    /** 头像 URL */
    private String userHeadImage;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserMobile() {
        return userMobile;
    }

    public void setUserMobile(String userMobile) {
        this.userMobile = userMobile;
    }

    public String getUserNickname() {
        return userNickname;
    }

    public void setUserNickname(String userNickname) {
        this.userNickname = userNickname;
    }

    public String getUserDescription() {
        return userDescription;
    }

    public void setUserDescription(String userDescription) {
        this.userDescription = userDescription;
    }

    public String getUserHeadImage() {
        return userHeadImage;
    }

    public void setUserHeadImage(String userHeadImage) {
        this.userHeadImage = userHeadImage;
    }
}
