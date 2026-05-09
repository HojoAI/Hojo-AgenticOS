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

import com.hojoai.central.provider.dto.response.movie.CommonResponse;
import com.hojoai.central.provider.dto.response.movie.GetCinemaFilmListResponse;
import com.hojoai.central.provider.dto.response.movie.GetShowSeatListResponse;
import com.hojoai.central.provider.dto.response.movie.HttpPanheGetOrderDetailResponse;

import java.util.List;

/**
 * The PanheDomainService class for panhe.
 *
 * @author My
 * @version 1.0, 2025/11/24
 * @since hojoai-grpc-central-provider 1.0.0
 */
public interface PanheDomainService {

    /**
     * 获取电影排期信息
     *
     * @param showId
     * @param seatIdList
     * @return
     */
    String getShowList(String showId, List<String> seatIdList);

    /**
     * 创建支付订单
     *
     * @param orderNo
     * @return
     */
    String createPayOrder(String orderNo);

    /**
     * 支付
     *
     * @param panheOrderNo
     * @param totalAmount
     * @return
     */
    Boolean pay(String panheOrderNo, String totalAmount);

    /**
     * 获取订单详情
     *
     * @param orderNo
     * @return
     */
    HttpPanheGetOrderDetailResponse getOrderDetail(String orderNo);

    /**
     * 获取排期座位信息
     *
     * @param showId
     * @return
     */
    CommonResponse<GetShowSeatListResponse> getShowSeatList(String showId);

    /**
     * 获取影院电影列表
     *
     * @param cinemaId
     * @return
     */
    CommonResponse<GetCinemaFilmListResponse> getCinemaFilmList(String cinemaId);

}
