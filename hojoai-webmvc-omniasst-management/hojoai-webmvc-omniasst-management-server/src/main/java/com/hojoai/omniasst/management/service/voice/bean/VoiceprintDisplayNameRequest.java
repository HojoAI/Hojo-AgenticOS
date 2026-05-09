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
package com.hojoai.omniasst.management.service.voice.bean;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 仅更新声纹表展示名（user_voiceprints.user_name）
 */
public class VoiceprintDisplayNameRequest {

    @JsonProperty("userId")
    @JsonAlias("user_id")
    private String userId;

    /** 展示名；null 或空串表示清空 */
    @JsonProperty("userName")
    @JsonAlias("user_name")
    private String userName;

    public String getUserId() {
        return userId == null || userId.isBlank() ? null : userId.trim();
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        if (userName == null) {
            return null;
        }
        return userName.isBlank() ? null : userName.trim();
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
}
