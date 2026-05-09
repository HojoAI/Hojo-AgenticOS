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
 * 访问令牌查询或签发结果，对应 user_token_info 中对外暴露的字段。
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserTokenInfoResponse {

    /** 业务用户 ID */
    private String userId;
    /** 业务类型 */
    private Integer businessType;
    /** 平台类型 */
    private Integer platformType;
    /** 访问令牌密文（客户端保存用于后续请求携带） */
    private String userToken;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
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

    public String getUserToken() {
        return userToken;
    }

    public void setUserToken(String userToken) {
        this.userToken = userToken;
    }
}
