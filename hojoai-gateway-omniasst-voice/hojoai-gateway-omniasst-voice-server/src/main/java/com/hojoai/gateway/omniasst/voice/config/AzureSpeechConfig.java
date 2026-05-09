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
package com.hojoai.gateway.omniasst.voice.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Azure语音服务配置
 * 用于管理Azure Speech Service的连接参数
 */
@Configuration
@ConfigurationProperties(prefix = "azure.speech")
public class AzureSpeechConfig {

    /**
     * Azure语音服务订阅密钥
     */
    private String subscriptionKey;

    /**
     * Azure服务区域，如eastasia、chinaeast2等
     */
    private String region;

    /**
     * 会话超时时间，单位毫秒，默认5分钟
     */
    private long sessionTimeoutMs = 5 * 60 * 1000;

    /**
     * 等待Azure recognized事件的超时时间，单位毫秒，默认2秒
     */
    private long recognizedWaitTimeoutMs = 2000;

    public String getSubscriptionKey() {
        return subscriptionKey;
    }

    public void setSubscriptionKey(String subscriptionKey) {
        this.subscriptionKey = subscriptionKey;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public long getSessionTimeoutMs() {
        return sessionTimeoutMs;
    }

    public void setSessionTimeoutMs(long sessionTimeoutMs) {
        this.sessionTimeoutMs = sessionTimeoutMs;
    }

    public long getRecognizedWaitTimeoutMs() {
        return recognizedWaitTimeoutMs;
    }

    public void setRecognizedWaitTimeoutMs(long recognizedWaitTimeoutMs) {
        this.recognizedWaitTimeoutMs = recognizedWaitTimeoutMs;
    }
}
