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

import java.util.List;

/**
 *
 * @author My
 * @version 1.0, 2025/11/28
 * @since hojoai-websocket-omniasst-sysagent 1.0.0
 */
public class GetOrderDetailResponse {

    /**
     * 订单编号
     */
    private String orderNo;

    /**
     * 影院名称
     */
    private String cinemaName;

    /**
     * 影厅名称
     */
    private String hallName;

    /**
     * 电影名称
     */
    private String filmName;

    /**
     * 订单状态
     */
    private String orderStatus;

    /**
     * 影票张数
     */
    private Integer ticketCount;

    /**
     * 影片海报url
     */
    private String filmImgUrl;

    /**
     * 开场时间
     */
    private String showTime;

    /**
     * 座位名称
     */
    private String seatNames;

    /**
     * 订单金额
     */
    private String orderAmount;

    /**
     * 影票信息
     */
    private List<TicketData> ticketsData;

    public String getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }

    public String getCinemaName() {
        return cinemaName;
    }

    public void setCinemaName(String cinemaName) {
        this.cinemaName = cinemaName;
    }

    public String getHallName() {
        return hallName;
    }

    public void setHallName(String hallName) {
        this.hallName = hallName;
    }

    public String getFilmName() {
        return filmName;
    }

    public void setFilmName(String filmName) {
        this.filmName = filmName;
    }

    public String getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(String orderStatus) {
        this.orderStatus = orderStatus;
    }

    public Integer getTicketCount() {
        return ticketCount;
    }

    public void setTicketCount(Integer ticketCount) {
        this.ticketCount = ticketCount;
    }

    public String getFilmImgUrl() {
        return filmImgUrl;
    }

    public void setFilmImgUrl(String filmImgUrl) {
        this.filmImgUrl = filmImgUrl;
    }

    public String getShowTime() {
        return showTime;
    }

    public void setShowTime(String showTime) {
        this.showTime = showTime;
    }

    public String getSeatNames() {
        return seatNames;
    }

    public void setSeatNames(String seatNames) {
        this.seatNames = seatNames;
    }

    public String getOrderAmount() {
        return orderAmount;
    }

    public void setOrderAmount(String orderAmount) {
        this.orderAmount = orderAmount;
    }

    public List<TicketData> getTicketsData() {
        return ticketsData;
    }

    public void setTicketsData(List<TicketData> ticketsData) {
        this.ticketsData = ticketsData;
    }

}
