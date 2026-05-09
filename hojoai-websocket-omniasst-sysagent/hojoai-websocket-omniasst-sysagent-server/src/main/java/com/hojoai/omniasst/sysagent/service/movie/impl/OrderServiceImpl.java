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
package com.hojoai.omniasst.sysagent.service.movie.impl;

import com.hojoai.central.provider.grpc.CreateOrderRequestDTO;
import com.hojoai.central.provider.grpc.CreateOrderRespDTO;
import com.hojoai.central.provider.grpc.GetOrderDetailRequestDTO;
import com.hojoai.central.provider.grpc.GetOrderDetailRespDTO;
import com.hojoai.common.jackson.JacksonUtil;
import com.hojoai.common.response.Response;
import com.hojoai.common.response.ResponseStatusBasic;
import com.hojoai.omniasst.sysagent.configure.IConfigurationForGrpc;
import com.hojoai.omniasst.sysagent.dto.request.movie.CreateOrderRequest;
import com.hojoai.omniasst.sysagent.dto.response.movie.CreateOrderResponse;
import com.hojoai.omniasst.sysagent.dto.response.movie.GetOrderDetailResponse;
import com.hojoai.omniasst.sysagent.service.movie.OrderService;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

/**
 * The OrderServiceImpl class for order.
 *
 * @author My
 * @version 1.0, 2025/11/24
 * @since hojoai-websocket-omniasst-sysagent 1.0.0
 */
@Service
public class OrderServiceImpl implements OrderService {

    @Resource
    private IConfigurationForGrpc iConfigurationForGrpc;

    /**
     * {@inheritDoc}
     */
    @Override
    public Response<CreateOrderResponse> createOrder(CreateOrderRequest request) {

        CreateOrderRequestDTO.Builder builder = CreateOrderRequestDTO.newBuilder();

        builder.setUserId(request.getUserId())
            .setCinemaId(request.getCinemaId())
            .setFilmId(request.getFilmId())
            .setShowId(request.getShowId())
            .setAcceptChangeSeat(request.isAcceptChangeSeat())
            .setPhone(request.getPhone())
            .setAlipayType(request.getAlipayType());

        request.getSeatIdList().forEach(builder::addSeatIdList);

        CreateOrderRespDTO respDTO = iConfigurationForGrpc.orderClient().createOrder(builder.build());

        return Response.newInstance(ResponseStatusBasic.OK, CreateOrderResponse.convert2CreateOrderResponse(respDTO));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Response<GetOrderDetailResponse> getOrderDetail(String userId, String orderNo) {
        GetOrderDetailRequestDTO.Builder builder = GetOrderDetailRequestDTO.newBuilder()
                .setUserId(userId)
                .setOrderNo(orderNo);

        GetOrderDetailRespDTO respDTO = iConfigurationForGrpc.orderClient().getOrderDetail(builder.build());

        if (StringUtils.isBlank(respDTO.getRespJson())) {
            return Response.newInstance(ResponseStatusBasic.OK, null);
        }

        GetOrderDetailResponse getOrderDetailResponse = JacksonUtil.json2Bean(respDTO.getRespJson(), GetOrderDetailResponse.class);

        return Response.newInstance(ResponseStatusBasic.OK, getOrderDetailResponse);
    }

}
