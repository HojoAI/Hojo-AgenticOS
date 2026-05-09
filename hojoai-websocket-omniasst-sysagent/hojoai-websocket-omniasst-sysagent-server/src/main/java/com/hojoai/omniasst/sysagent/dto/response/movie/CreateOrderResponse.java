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
package com.hojoai.omniasst.sysagent.dto.response.movie;

import com.hojoai.central.provider.grpc.CreateOrderRespDTO;

/**
 *
 * @author My
 * @version 1.0, 2025/11/24
 * @since hojoai-websocket-omniasst-sysagent 1.0.0
 */
public class CreateOrderResponse {

    private String orderNo;

    private String orderStr;

    private Long expireTime;

    public static CreateOrderResponse convert2CreateOrderResponse(CreateOrderRespDTO respDTO) {
        CreateOrderResponse response = new CreateOrderResponse();

        response.setOrderNo(respDTO.getOrderNo());
        response.setOrderStr(respDTO.getOrderStr());
        response.setExpireTime(respDTO.getExpireTime());

        return response;
    }

    public String getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }

    public String getOrderStr() {
        return orderStr;
    }

    public void setOrderStr(String orderStr) {
        this.orderStr = orderStr;
    }

    public Long getExpireTime() {
        return expireTime;
    }

    public void setExpireTime(Long expireTime) {
        this.expireTime = expireTime;
    }

}
