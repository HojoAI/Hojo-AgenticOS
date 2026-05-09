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
 * 签发或操作访问令牌时的请求体：创建 token 接口须携带业务类型与平台类型。
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserTokenInfoRequest {

    /** 用户 ID（部分接口由参数传递，体中可选） */
    private String userId;
    /** 业务类型，正整数，与产品约定一致 */
    private Integer businessType;
    /** 平台类型，正整数，如 iOS/Android/Web */
    private Integer platformType;
    /** 访问令牌（查询/删除等场景使用，创建接口通常由服务端生成） */
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
