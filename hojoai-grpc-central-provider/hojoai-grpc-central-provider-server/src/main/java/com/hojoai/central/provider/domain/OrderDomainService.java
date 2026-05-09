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
package com.hojoai.central.provider.domain;

import com.alipay.api.AlipayApiException;
import com.hojoai.central.provider.dto.response.movie.GetCinemaFilmListResponse;
import com.hojoai.central.provider.dto.response.movie.GetShowSeatListResponse;
import com.hojoai.central.provider.dto.response.movie.HttpPanheGetOrderDetailResponse;
import com.hojoai.central.provider.grpc.CreateOrderRequestDTO;
import com.hojoai.central.provider.repository.po.BillPO;
import com.hojoai.central.provider.repository.po.MovieOrderDetailPO;
import com.hojoai.central.provider.repository.po.OrderPO;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;
import java.util.Map;

/**
 * The OrderDomainService class for OrderDomain.
 *
 * @author My
 * @version 1.0, 2025/11/21
 * @since hojoai-grpc-central-provider 1.0.0
 */
public interface OrderDomainService {

    /**
     * create order
     * @param requestDTO
     * @return
     */
    OrderPO createOrder(CreateOrderRequestDTO requestDTO);

    /**
     * alipay by order
     *
     * @param orderPO
     * @param aliPayType
     * @return
     * @throws Exception
     */
    Pair<String, Long> alipayPay(OrderPO orderPO, String aliPayType) throws AlipayApiException;

    /**
     * create movie order detail by order
     *
     * @param orderPO
     * @param requestDTO
     * @param getShowSeatListResponse
     * @param getCinemaFilmListResponse
     */
    void createMovieOrderDetail(OrderPO orderPO, CreateOrderRequestDTO requestDTO, GetShowSeatListResponse getShowSeatListResponse, GetCinemaFilmListResponse getCinemaFilmListResponse);

    /**
     * create bill by order
     *
     * @param orderPO
     * @param alipayPair
     */
    void createBill(OrderPO orderPO, Pair<String, Long> alipayPair);

    /**
     * validate
     * @param paramMap
     * @return
     */
    Boolean validate(Map<String, String> paramMap) throws AlipayApiException;

    /**
     * update order status
     *
     * @param orderNo
     * @param orderStatus
     * @param response
     * @param isPanhe
     */
    MovieOrderDetailPO payNotifyUpdateStatus(String orderNo, String orderStatus, HttpPanheGetOrderDetailResponse response, Boolean isPanhe);

    /**
     * get order detail
     *
     * @param userId
     * @param orderNo
     * @return
     */
    MovieOrderDetailPO getOrderDetail(String userId, String orderNo);

    /**
     * update movie order detail
     *
     * @param movieOrderDetailPO
     * @param httpPanheGetOrderDetailResponse
     * @return
     */
    MovieOrderDetailPO updateMovieOrderDetail(MovieOrderDetailPO movieOrderDetailPO, HttpPanheGetOrderDetailResponse httpPanheGetOrderDetailResponse);

    /**
     * get bill by orderNo
     *
     * @param orderNo
     * @return
     */
    List<BillPO> getBillByOrderNo(String orderNo);

}
