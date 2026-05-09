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
package com.hojoai.omniasst.sysagent.configure;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * The configuration class.
 *
 * @author kevanwong
 * @version 1.0, 2025/10/23
 * @since hojoai-websocket-omniasst-sysagent 1.0.0
 */
@Component
public class IConfiguration {

    /** 阿里云 RTC 应用 ID。 */
    @Value("${aliyun.rtc.app-id}")
    private String rtcAppId;

    /** 阿里云 RTC 应用密钥。 */
    @Value("${aliyun.rtc.app-key}")
    private String rtcAppKey;

    /** RTC Token 过期时间戳或与 SDK 约定的时间参数。 */
    @Value("${aliyun.rtc.token-expired-timestamp}")
    private int rtcTokenExpiredTimestamp;

    public String getRtcAppId() {
        return rtcAppId;
    }

    public void setRtcAppId(String rtcAppId) {
        this.rtcAppId = rtcAppId;
    }

    public String getRtcAppKey() {
        return rtcAppKey;
    }

    public void setRtcAppKey(String rtcAppKey) {
        this.rtcAppKey = rtcAppKey;
    }

    public int getRtcTokenExpiredTimestamp() {
        return rtcTokenExpiredTimestamp;
    }

    public void setRtcTokenExpiredTimestamp(int rtcTokenExpiredTimestamp) {
        this.rtcTokenExpiredTimestamp = rtcTokenExpiredTimestamp;
    }
}
