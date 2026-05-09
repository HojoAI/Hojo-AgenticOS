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
package com.hojoai.omniasst.sysagent.service.rtctoken.bean;

/**
 * 签发 RTC Token 后通过 WebSocket 回传给客户端的结果结构（服务端或客户端角色各发一条）。
 */
public class BuildRTCTokenResultMessage {

    /** 设备 ID。 */
    private String deviceId;
    /** 阿里云 RTC AppId。 */
    private String appId;
    /** 频道 ID。 */
    private String channelId;
    /** 当前角色在频道内的用户 ID。 */
    private String userId;
    /** 对应角色的 RTC Token 字符串。 */
    private String rtcToken;

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getChannelId() {
        return channelId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getRtcToken() {
        return rtcToken;
    }

    public void setRtcToken(String rtcToken) {
        this.rtcToken = rtcToken;
    }
}
