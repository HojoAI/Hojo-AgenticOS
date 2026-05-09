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
package com.hojoai.central.provider.service.order;

import com.hojoai.central.provider.grpc.*;
import com.hojoai.central.provider.service.CentralProviderChannelProvider;

/**
 * 订单相关 gRPC 阻塞客户端（创单、查单、支付宝/盘禾回调等）。
 */
public class OrderClient {

    private final CentralProviderChannelProvider centralProviderChannelProvider;

    public OrderClient(CentralProviderChannelProvider centralProviderChannelProvider) {
        this.centralProviderChannelProvider = centralProviderChannelProvider;
    }

    public CreateOrderRespDTO createOrder(CreateOrderRequestDTO requestDTO) {
        return centralProviderChannelProvider.executeWithChannel(channel -> OrderServiceGrpc.newBlockingStub(channel).createOrder(requestDTO));
    }

    public AlipayNotifyRespDTO alipayNotify(AlipayNotifyRequestDTO requestDTO) {
        return centralProviderChannelProvider.executeWithChannel(channel -> OrderServiceGrpc.newBlockingStub(channel).alipayNotify(requestDTO));
    }

    public PanheNotifyRespDTO panheNotify(PanheNotifyRequestDTO requestDTO) {
        return centralProviderChannelProvider.executeWithChannel(channel -> OrderServiceGrpc.newBlockingStub(channel).panheNotify(requestDTO));
    }

    public GetOrderDetailRespDTO getOrderDetail(GetOrderDetailRequestDTO requestDTO) {
        return centralProviderChannelProvider.executeWithChannel(channel -> OrderServiceGrpc.newBlockingStub(channel).getOrderDetail(requestDTO));
    }

}
