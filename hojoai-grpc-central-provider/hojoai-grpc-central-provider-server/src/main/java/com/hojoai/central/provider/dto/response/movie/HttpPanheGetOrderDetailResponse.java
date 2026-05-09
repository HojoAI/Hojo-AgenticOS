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

import java.math.BigDecimal;

/**
 *
 * @author My
 * @version 1.0, 2025/11/25
 * @since hojoai-grpc-central-provider 1.0.0
 */
public class HttpPanheGetOrderDetailResponse {

    /**
     * 磐河电影票订单号
     */
    private String orderNo;

    /**
     * 采购商本地订单号
     */
    private String customerOrderNo;

    /**
     * 影院ID
     */
    private String cinemaId;

    /**
     * 影院名称
     */
    private String cinemaName;

    /**
     * 影院地址
     */
    private String cinemaAddress;

    /**
     * 影院电话
     */
    private String cinemaTel;

    /**
     * 城市名称
     */
    private String cityName;

    /**
     * 影院经度
     */
    private String longitude;

    /**
     * 影院纬度
     */
    private String latitude;

    /**
     * 影片ID
     */
    private String filmId;

    /**
     * 影片名称
     */
    private String filmName;

    /**
     * 电影海报图片
     */
    private String filmImg;

    /**
     * 场次ID
     */
    private String showId;

    /**
     * 购买座位的 seatId 集合,多个英文逗号分隔
     */
    private String seatIds;

    /**
     * 影厅名称
     */
    private String hallName;

    /**
     * 开场时间
     */
    private String showTime;

    /**
     * 订票手机号
     */
    private String mobile;

    /**
     * 是否允许换座
     */
    private Boolean acceptChangeSeat;

    /**
     * 影票张数
     */
    private Integer ticketNum;

    /**
     * 出票成功后的座位名称，多个座位","隔开（如果换座出票的，值跟OriginalSeatNos就会不一样）
     */
    private String seatNos;

    /**
     * 用户下单时选择的座位名称，多个座位","隔开
     */
    private String originalSeatNos;

    /**
     * 影票数据，出票成功才有值。json数组格式
     */
    private String ticketsData;

    /**
     * 出票成功时间
     */
    private String ticketTime;

    /**
     * 订单总金额
     */
    private BigDecimal orderAmount;

    /**
     * 电影票订单状态值
     * 新订单待支付 = 1 , 已支付待出票 = 2 , 出票中 = 3,已出票 = 4,出票失败 = 5,
     * 已取消 = 6, 退票中 = 7,已退票 = 8, 退票失败 = 9
     */
    private Integer orderStatus;

    /**
     * 支付状态,对应枚举PayStatus值（0未支付 1已支付）
     */
    private Integer payStatus;

    /**
     * 支付时间
     */
    private String payTime;

    /**
     * 支付交易单号,可用于查账
     */
    private String transactionNo;

    /**
     * 出票失败/退票成功时 给客户的退款金额
     */
    private BigDecimal refundAmount;

    /**
     * 出票失败/退票成功后，退款成功时间
     */
    private String refundSuccessTime;

    /**
     * 出票失败/退票成功后，退款交易单号,可用于查账
     */
    private String refundTransactionNo;

    /**
     * 订单回调地址（由采购商提交订单时传入的）
     */
    private String callbackUrl;

    /**
     * 最晚支付截止时间，超过此值将无法支付，默认值：下单时间createTime + 15分钟
     */
    private String payLimitTime;

    /**
     * 出票失败原因（当订单状态为 退票失败 =9 时， 就是退票失败原因）
     */
    private String unFinishedReason;

    /**
     * 下单时间
     */
    private String createTime;

    public String getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }

    public String getCustomerOrderNo() {
        return customerOrderNo;
    }

    public void setCustomerOrderNo(String customerOrderNo) {
        this.customerOrderNo = customerOrderNo;
    }

    public String getCinemaId() {
        return cinemaId;
    }

    public void setCinemaId(String cinemaId) {
        this.cinemaId = cinemaId;
    }

    public String getCinemaName() {
        return cinemaName;
    }

    public void setCinemaName(String cinemaName) {
        this.cinemaName = cinemaName;
    }

    public String getCinemaAddress() {
        return cinemaAddress;
    }

    public void setCinemaAddress(String cinemaAddress) {
        this.cinemaAddress = cinemaAddress;
    }

    public String getCinemaTel() {
        return cinemaTel;
    }

    public void setCinemaTel(String cinemaTel) {
        this.cinemaTel = cinemaTel;
    }

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getFilmId() {
        return filmId;
    }

    public void setFilmId(String filmId) {
        this.filmId = filmId;
    }

    public String getFilmName() {
        return filmName;
    }

    public void setFilmName(String filmName) {
        this.filmName = filmName;
    }

    public String getFilmImg() {
        return filmImg;
    }

    public void setFilmImg(String filmImg) {
        this.filmImg = filmImg;
    }

    public String getShowId() {
        return showId;
    }

    public void setShowId(String showId) {
        this.showId = showId;
    }

    public String getSeatIds() {
        return seatIds;
    }

    public void setSeatIds(String seatIds) {
        this.seatIds = seatIds;
    }

    public String getHallName() {
        return hallName;
    }

    public void setHallName(String hallName) {
        this.hallName = hallName;
    }

    public String getShowTime() {
        return showTime;
    }

    public void setShowTime(String showTime) {
        this.showTime = showTime;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public Boolean getAcceptChangeSeat() {
        return acceptChangeSeat;
    }

    public void setAcceptChangeSeat(Boolean acceptChangeSeat) {
        this.acceptChangeSeat = acceptChangeSeat;
    }

    public Integer getTicketNum() {
        return ticketNum;
    }

    public void setTicketNum(Integer ticketNum) {
        this.ticketNum = ticketNum;
    }

    public String getSeatNos() {
        return seatNos;
    }

    public void setSeatNos(String seatNos) {
        this.seatNos = seatNos;
    }

    public String getOriginalSeatNos() {
        return originalSeatNos;
    }

    public void setOriginalSeatNos(String originalSeatNos) {
        this.originalSeatNos = originalSeatNos;
    }

    public String getTicketsData() {
        return ticketsData;
    }

    public void setTicketsData(String ticketsData) {
        this.ticketsData = ticketsData;
    }

    public String getTicketTime() {
        return ticketTime;
    }

    public void setTicketTime(String ticketTime) {
        this.ticketTime = ticketTime;
    }

    public BigDecimal getOrderAmount() {
        return orderAmount;
    }

    public void setOrderAmount(BigDecimal orderAmount) {
        this.orderAmount = orderAmount;
    }

    public Integer getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(Integer orderStatus) {
        this.orderStatus = orderStatus;
    }

    public Integer getPayStatus() {
        return payStatus;
    }

    public void setPayStatus(Integer payStatus) {
        this.payStatus = payStatus;
    }

    public String getPayTime() {
        return payTime;
    }

    public void setPayTime(String payTime) {
        this.payTime = payTime;
    }

    public String getTransactionNo() {
        return transactionNo;
    }

    public void setTransactionNo(String transactionNo) {
        this.transactionNo = transactionNo;
    }

    public BigDecimal getRefundAmount() {
        return refundAmount;
    }

    public void setRefundAmount(BigDecimal refundAmount) {
        this.refundAmount = refundAmount;
    }

    public String getRefundSuccessTime() {
        return refundSuccessTime;
    }

    public void setRefundSuccessTime(String refundSuccessTime) {
        this.refundSuccessTime = refundSuccessTime;
    }

    public String getRefundTransactionNo() {
        return refundTransactionNo;
    }

    public void setRefundTransactionNo(String refundTransactionNo) {
        this.refundTransactionNo = refundTransactionNo;
    }

    public String getCallbackUrl() {
        return callbackUrl;
    }

    public void setCallbackUrl(String callbackUrl) {
        this.callbackUrl = callbackUrl;
    }

    public String getPayLimitTime() {
        return payLimitTime;
    }

    public void setPayLimitTime(String payLimitTime) {
        this.payLimitTime = payLimitTime;
    }

    public String getUnFinishedReason() {
        return unFinishedReason;
    }

    public void setUnFinishedReason(String unFinishedReason) {
        this.unFinishedReason = unFinishedReason;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

}
