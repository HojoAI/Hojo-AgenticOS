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
package com.hojoai.central.provider.dto.request.movie;

import com.hojoai.central.provider.repository.po.MovieOrderDetailPO;

import java.math.BigDecimal;
import java.util.List;

/**
 *
 * @author My
 * @version 1.0, 2025/11/25
 * @since hojoai-grpc-central-provider 1.0.0
 */
public class HttpPanheCreateOrderRequest {

    private String appKey;
    /**
     * 商户订单号
     */
    private String customerOrderNo;

    /**
     * 订单总金额（后台会校验，传错会下单失败）
     */
    private BigDecimal orderAmount;

    /**
     * 影院ID
     */
    private String cinemaId;

    /**
     * 影片ID
     */
    private String filmId;

    /**
     * 场次ID
     */
    private String showId;

    /**
     * 选择的座位ID集合
     */
    private List<String> seatIds;

    /**
     * 当选择的座位已售时，是否接受换座出票
     */
    private Boolean acceptChangeSeat;

    /**
     * 订票手机号
     */
    private String mobile;

    /**
     * 订单状态变更异步回调地址
     */
    private String callBackUrl;

    public static HttpPanheCreateOrderRequest convert2HttpPanheCreateOrderRequest(MovieOrderDetailPO movieOrderDetailPO) {
        HttpPanheCreateOrderRequest httpPanheCreateOrderRequest = new HttpPanheCreateOrderRequest();

        httpPanheCreateOrderRequest.setOrderAmount(movieOrderDetailPO.getOrderAmount());
        httpPanheCreateOrderRequest.setCinemaId(movieOrderDetailPO.getCinemaId());
        httpPanheCreateOrderRequest.setFilmId(movieOrderDetailPO.getFilmId());
        httpPanheCreateOrderRequest.setShowId(movieOrderDetailPO.getShowId());
        // httpPanheCreateOrderRequest.setSeatIds(movieOrderDetailPO.getSeatIds());
        httpPanheCreateOrderRequest.setAcceptChangeSeat(movieOrderDetailPO.getAcceptChangeSeat());
        httpPanheCreateOrderRequest.setMobile(movieOrderDetailPO.getPhone());
        httpPanheCreateOrderRequest.setCallBackUrl(movieOrderDetailPO.getCallbackUrl());
        httpPanheCreateOrderRequest.setCustomerOrderNo(movieOrderDetailPO.getOrderNo());
        httpPanheCreateOrderRequest.setOrderAmount(movieOrderDetailPO.getOrderAmount());

        return httpPanheCreateOrderRequest;
    }

    public String getAppKey() {
        return appKey;
    }

    public void setAppKey(String appKey) {
        this.appKey = appKey;
    }

    public String getCustomerOrderNo() {
        return customerOrderNo;
    }

    public void setCustomerOrderNo(String customerOrderNo) {
        this.customerOrderNo = customerOrderNo;
    }

    public BigDecimal getOrderAmount() {
        return orderAmount;
    }

    public void setOrderAmount(BigDecimal orderAmount) {
        this.orderAmount = orderAmount;
    }

    public String getCinemaId() {
        return cinemaId;
    }

    public void setCinemaId(String cinemaId) {
        this.cinemaId = cinemaId;
    }

    public String getFilmId() {
        return filmId;
    }

    public void setFilmId(String filmId) {
        this.filmId = filmId;
    }

    public String getShowId() {
        return showId;
    }

    public void setShowId(String showId) {
        this.showId = showId;
    }

    public List<String> getSeatIds() {
        return seatIds;
    }

    public void setSeatIds(List<String> seatIds) {
        this.seatIds = seatIds;
    }

    public Boolean getAcceptChangeSeat() {
        return acceptChangeSeat;
    }

    public void setAcceptChangeSeat(Boolean acceptChangeSeat) {
        this.acceptChangeSeat = acceptChangeSeat;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getCallBackUrl() {
        return callBackUrl;
    }

    public void setCallBackUrl(String callBackUrl) {
        this.callBackUrl = callBackUrl;
    }

}
