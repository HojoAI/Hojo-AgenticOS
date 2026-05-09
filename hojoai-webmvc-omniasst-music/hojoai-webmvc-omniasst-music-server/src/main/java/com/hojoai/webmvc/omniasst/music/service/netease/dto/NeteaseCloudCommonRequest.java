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
package com.hojoai.webmvc.omniasst.music.service.netease.dto;

/**
 * 网易云音乐公共请求参数
 * 
 * @author shengxianfei
 * @since 2026-01-13
 */
public class NeteaseCloudCommonRequest {
    
    /**
     * Application ID assigned by NetEase Cloud Music to developers
     */
    private String appId;
    
    /**
     * Signature algorithm type used to generate signature string
     * Currently supports RSA_SHA256
     */
    private String signType;
    
    /**
     * Signature string of request parameters
     * Generated according to RSA encryption method
     */
    private String sign;
    
    /**
     * UNIX timestamp in milliseconds
     */
    private Long timestamp;
    
    /**
     * Collection of request parameters in JSON format
     * Needs to be encoded after JSON formatting
     * All business request parameters except common parameters must be passed in this parameter
     */
    private String bizContent;
    
    /**
     * User login token
     * Not required for some login interfaces, mandatory for other interfaces
     */
    private String accessToken;
    
    /**
     * Encryption method
     * client: front-end encryption
     * server: back-end encryption (default)
     */
    private String encryptType = "server";
    
    /**
     * Device information in JSON format
     * Needs to be encoded after JSON formatting
     */
    private String device;

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getSignType() {
        return signType;
    }

    public void setSignType(String signType) {
        this.signType = signType;
    }

    public String getSign() {
        return sign;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public String getBizContent() {
        return bizContent;
    }

    public void setBizContent(String bizContent) {
        this.bizContent = bizContent;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getEncryptType() {
        return encryptType;
    }

    public void setEncryptType(String encryptType) {
        this.encryptType = encryptType;
    }

    public String getDevice() {
        return device;
    }

    public void setDevice(String device) {
        this.device = device;
    }
}
