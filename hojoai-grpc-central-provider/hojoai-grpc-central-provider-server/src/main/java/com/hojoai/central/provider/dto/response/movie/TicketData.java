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
package com.hojoai.central.provider.dto.response.movie;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 *
 * @author My
 * @version 1.0, 2025/11/25
 * @since hojoai-grpc-central-provider 1.0.0
 */
public class TicketData {

    /**
     * 取票码(有的平台又叫序列号)
     */
    @JsonProperty("serialNumber")
    private String serialNumber;

    /**
     * 验证码
     */
    @JsonProperty("verificationCode")
    private String verificationCode;

    /**
     * 取票二维码
     */
    @JsonProperty("qrCodeUrl")
    private String qrCodeUrl;

    // Getter和Setter方法
    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public String getVerificationCode() {
        return verificationCode;
    }

    public void setVerificationCode(String verificationCode) {
        this.verificationCode = verificationCode;
    }

    public String getQrCodeUrl() {
        return qrCodeUrl;
    }

    public void setQrCodeUrl(String qrCodeUrl) {
        this.qrCodeUrl = qrCodeUrl;
    }

}
