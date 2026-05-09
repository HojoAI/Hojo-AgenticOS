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
 * 友盟推送配置类
 */
@Component
public class UmengPushProperties {
    /** 友盟应用 AppKey。 */
    @Value("${umeng.push.app-key}")
    private String appKey;

    /** 消息密钥（与友盟控制台配置一致）。 */
    @Value("${umeng.push.message-secret}")
    private String messageSecret;

    /** Master Secret，用于服务端签名请求。 */
    @Value("${umeng.push.master-secret}")
    private String masterSecret;

    /** 是否走生产环境推送通道；{@code false} 一般为测试/开发模式。 */
    @Value("${umeng.push.production-mode}")
    private boolean productionMode = true;

    /** 友盟 OpenAPI 主机地址。 */
    @Value("${umeng.push.host}")
    private String host;

    public String getAppKey() {
        return appKey;
    }

    public String getMessageSecret() {
        return messageSecret;
    }

    public String getMasterSecret() {
        return masterSecret;
    }

    public boolean isProductionMode() {
        return productionMode;
    }

    public String getHost() {
        return host;
    }

    public void setAppKey(String appKey) {
        this.appKey = appKey;
    }

    public void setMessageSecret(String messageSecret) {
        this.messageSecret = messageSecret;
    }

    public void setMasterSecret(String masterSecret) {
        this.masterSecret = masterSecret;
    }

    public void setProductionMode(boolean productionMode) {
        this.productionMode = productionMode;
    }

    public void setHost(String host) {
        this.host = host;
    }



}