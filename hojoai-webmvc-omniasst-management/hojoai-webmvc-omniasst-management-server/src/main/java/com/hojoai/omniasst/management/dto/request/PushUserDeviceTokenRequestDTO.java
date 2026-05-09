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
package com.hojoai.omniasst.management.dto.request;

/**
 *
 * @author My
 * @version 1.0, 2026/01/17
 * @since hojoai-webmvc-omniasst-management 1.0.0
 */
public class PushUserDeviceTokenRequestDTO {

    private String userId;

    private String deviceToken;

    /** fcm：写入 FCM token；未传或 umeng：友盟（默认） */
    private String pushChannel;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getDeviceToken() {
        return deviceToken;
    }

    public void setDeviceToken(String deviceToken) {
        this.deviceToken = deviceToken;
    }

    public String getPushChannel() {
        return pushChannel;
    }

    public void setPushChannel(String pushChannel) {
        this.pushChannel = pushChannel;
    }

}
