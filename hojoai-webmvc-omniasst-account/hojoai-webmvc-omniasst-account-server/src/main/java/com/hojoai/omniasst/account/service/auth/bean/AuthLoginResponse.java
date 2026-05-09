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
package com.hojoai.omniasst.account.service.auth.bean;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * 登录或 Magic Link 轮询返回体：普通登录成功时填充用户与 token；Magic Link 轮询时使用 status 三态。
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AuthLoginResponse {

    /** 用户 ID */
    private String userId;
    /** 会话 token */
    private String userToken;
    /** 业务线类型 */
    private Integer businessType;
    /** 端侧平台类型 */
    private Integer platformType;

    /**
     * Magic Link 轮询专用：{@code pending}（待验证）、{@code success}、{@code failed}（见 {@link #failureReason}）。
     */
    private String status;

    /** 仅当 status=failed：{@code expired} 或 {@code consumed} */
    private String failureReason;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserToken() {
        return userToken;
    }

    public void setUserToken(String userToken) {
        this.userToken = userToken;
    }

    public Integer getBusinessType() {
        return businessType;
    }

    public void setBusinessType(Integer businessType) {
        this.businessType = businessType;
    }

    public Integer getPlatformType() {
        return platformType;
    }

    public void setPlatformType(Integer platformType) {
        this.platformType = platformType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getFailureReason() {
        return failureReason;
    }

    public void setFailureReason(String failureReason) {
        this.failureReason = failureReason;
    }
}
