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
package com.hojoai.central.provider.service.common.enums;

/**
 *
 * @author My
 * @version 1.0, 2025/11/26
 * @since hojoai-grpc-central-provider 1.0.0
 */
public enum AlipayTradeStatusEnum implements CommonEnum {

    TRADE_CLOSED("TRADE_CLOSED", "未付款交易超时关闭"),
    TRADE_SUCCESS("TRADE_SUCCESS", "交易支付成功"),
    TRADE_FINISHED("TRADE_FINISHED", "交易结束"),
    ;

    private final String code;

    private final String value;

    AlipayTradeStatusEnum(String code, String value) {
        this.code = code;
        this.value = value;
    }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getValue() {
        return value;
    }

}
