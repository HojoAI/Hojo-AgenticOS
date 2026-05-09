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
 * 客户端经 WebSocket 请求签发 RTC Token 时的消息体。
 */
public class BuildRTCTokenMessage {

    /** 设备唯一标识，用于生成 channelId/userId 后缀。 */
    private String deviceId;
    /** 语音服务区域等扩展参数（依 SDK 约定）。 */
    private String voiceRegion;

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getVoiceRegion() {
        return voiceRegion;
    }

    public void setVoiceRegion(String voiceRegion) {
        this.voiceRegion = voiceRegion;
    }
}
