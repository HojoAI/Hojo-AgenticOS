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
package com.hojoai.central.provider.repository.po;

import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 *
 * @author My
 * @version 1.0, 2025/11/24
 * @since hojoai-grpc-central-provider 1.0.0
 */
@TableName("t_pa_movie_order_detail")
public class MovieOrderDetailPO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    private Long id;
    /**
     * 订单id
     */
    private Long orderId;
    /**
     * 订单编号
     */
    private String orderNo;
    /**
     * 外部订单编号
     */
    private String outOrderNo;
    /**
     * 影院id
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
    private BigDecimal longitude;
    /**
     * 影院纬度
     */
    private BigDecimal latitude;
    /**
     * 电影id
     */
    private String filmId;
    /**
     * 电影名称
     */
    private String filmName;
    /**
     * 电影海报url
     */
    private String filmImgUrl;
    /**
     * 场次id
     */
    private String showId;
    /**
     * 座位id集合
     */
    private String seatIds;
    /**
     * 影厅名称
     */
    private String hallName;
    /**
     * 开场时间
     */
    private Date showTime;
    /**
     * 订票手机号
     */
    private String phone;
    /**
     * 是否允许换座
     */
    private Boolean acceptChangeSeat;
    /**
     * 影票张数
     */
    private Integer ticketCount;
    /**
     * 出票成功后的座位名称，多个座位“,”隔开
     */
    private String seatNos;
    /**
     * 用户下单时选择的座位名称，多个座位“,”隔开
     */
    private String originalSeatNos;
    /**
     * 影票数据，出票成功才有值。json数组格式：[{"serialNumber":" 取票码(有的平台又叫序列号)","verificationCode":"验证码","qrCodeUrl":"取票二维码"}]
     */
    private String ticketsData;
    /**
     * 出票成功时间
     */
    private Date ticketTime;
    /**
     * 订单金额
     */
    private BigDecimal orderAmount;
    /**
     * 订单状态（ 新订单待支付 = 1 , 已支付待出票 = 2 , 出票中 = 3,已出票 = 4,出票失败 = 5, 已取消 = 6, 退票中 = 7,已退票 = 8, 退票失败 =9 ）
     */
    private Integer orderStatus;
    /**
     * 支付状态（0未支付 1已支付）
     */
    private Integer payStatus;
    /**
     * 支付时间
     */
    private Date payTime;
    /**
     * 支付交易单号
     */
    private String transactionNo;
    /**
     * 出票失败/退票成功时，给客户的退款金额
     */
    private BigDecimal refundAmount;
    /**
     * 出票失败/退票成功后，退款成功时间
     */
    private Date refundSuccessTime;
    /**
     * 出票失败/退票成功后，退款交易单号
     */
    private String refundTransactionNo;
    /**
     * 订单回调地址（由采购商提交订单时传入的）
     */
    private String callbackUrl;
    /**
     * 最晚支付截止时间，超过此值将无法支付，默认值：下单时间 + 15分钟
     */
    private Date payLimitTime;
    /**
     * 出票失败原因（当订单状态为 退票失败 =9 时， 就是退票失败原因）
     */
    private String unFinishedReason;
    /**
     * 下单时间
     */
    private Date orderTime;
    /**
     * 采购商本地订单号
     */
    private String customerOrderNo;
    /**
     * 用户id
     */
    private String userId;
    /**
     * 版本号
     */
    private Integer version;
    /**
     * 创建时间
     */
    private Date createTime;
    /**
     * 更新时间
     */
    private Date updateTime;
    /**
     * 删除标识
     */
    private Boolean deleteFlag;

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getOrderId() {
        return this.orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public String getOrderNo() {
        return this.orderNo;
    }

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }

    public String getOutOrderNo() {
        return this.outOrderNo;
    }

    public void setOutOrderNo(String outOrderNo) {
        this.outOrderNo = outOrderNo;
    }

    public String getCinemaId() {
        return this.cinemaId;
    }

    public void setCinemaId(String cinemaId) {
        this.cinemaId = cinemaId;
    }

    public String getCinemaName() {
        return this.cinemaName;
    }

    public void setCinemaName(String cinemaName) {
        this.cinemaName = cinemaName;
    }

    public String getCinemaAddress() {
        return this.cinemaAddress;
    }

    public void setCinemaAddress(String cinemaAddress) {
        this.cinemaAddress = cinemaAddress;
    }

    public String getCinemaTel() {
        return this.cinemaTel;
    }

    public void setCinemaTel(String cinemaTel) {
        this.cinemaTel = cinemaTel;
    }

    public String getCityName() {
        return this.cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    public BigDecimal getLongitude() {
        return this.longitude;
    }

    public void setLongitude(BigDecimal longitude) {
        this.longitude = longitude;
    }

    public BigDecimal getLatitude() {
        return this.latitude;
    }

    public void setLatitude(BigDecimal latitude) {
        this.latitude = latitude;
    }

    public String getFilmId() {
        return this.filmId;
    }

    public void setFilmId(String filmId) {
        this.filmId = filmId;
    }

    public String getFilmName() {
        return this.filmName;
    }

    public void setFilmName(String filmName) {
        this.filmName = filmName;
    }

    public String getFilmImgUrl() {
        return this.filmImgUrl;
    }

    public void setFilmImgUrl(String filmImgUrl) {
        this.filmImgUrl = filmImgUrl;
    }

    public String getShowId() {
        return this.showId;
    }

    public void setShowId(String showId) {
        this.showId = showId;
    }

    public String getSeatIds() {
        return this.seatIds;
    }

    public void setSeatIds(String seatIds) {
        this.seatIds = seatIds;
    }

    public String getHallName() {
        return this.hallName;
    }

    public void setHallName(String hallName) {
        this.hallName = hallName;
    }

    public Date getShowTime() {
        return this.showTime;
    }

    public void setShowTime(Date showTime) {
        this.showTime = showTime;
    }

    public String getPhone() {
        return this.phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Boolean getAcceptChangeSeat() {
        return this.acceptChangeSeat;
    }

    public void setAcceptChangeSeat(Boolean acceptChangeSeat) {
        this.acceptChangeSeat = acceptChangeSeat;
    }

    public Integer getTicketCount() {
        return this.ticketCount;
    }

    public void setTicketCount(Integer ticketCount) {
        this.ticketCount = ticketCount;
    }

    public String getSeatNos() {
        return this.seatNos;
    }

    public void setSeatNos(String seatNos) {
        this.seatNos = seatNos;
    }

    public String getOriginalSeatNos() {
        return this.originalSeatNos;
    }

    public void setOriginalSeatNos(String originalSeatNos) {
        this.originalSeatNos = originalSeatNos;
    }

    public String getTicketsData() {
        return this.ticketsData;
    }

    public void setTicketsData(String ticketsData) {
        this.ticketsData = ticketsData;
    }

    public Date getTicketTime() {
        return this.ticketTime;
    }

    public void setTicketTime(Date ticketTime) {
        this.ticketTime = ticketTime;
    }

    public BigDecimal getOrderAmount() {
        return this.orderAmount;
    }

    public void setOrderAmount(BigDecimal orderAmount) {
        this.orderAmount = orderAmount;
    }

    public Integer getOrderStatus() {
        return this.orderStatus;
    }

    public void setOrderStatus(Integer orderStatus) {
        this.orderStatus = orderStatus;
    }

    public Integer getPayStatus() {
        return this.payStatus;
    }

    public void setPayStatus(Integer payStatus) {
        this.payStatus = payStatus;
    }

    public Date getPayTime() {
        return this.payTime;
    }

    public void setPayTime(Date payTime) {
        this.payTime = payTime;
    }

    public String getTransactionNo() {
        return this.transactionNo;
    }

    public void setTransactionNo(String transactionNo) {
        this.transactionNo = transactionNo;
    }

    public BigDecimal getRefundAmount() {
        return this.refundAmount;
    }

    public void setRefundAmount(BigDecimal refundAmount) {
        this.refundAmount = refundAmount;
    }

    public Date getRefundSuccessTime() {
        return this.refundSuccessTime;
    }

    public void setRefundSuccessTime(Date refundSuccessTime) {
        this.refundSuccessTime = refundSuccessTime;
    }

    public String getRefundTransactionNo() {
        return this.refundTransactionNo;
    }

    public void setRefundTransactionNo(String refundTransactionNo) {
        this.refundTransactionNo = refundTransactionNo;
    }

    public String getCallbackUrl() {
        return this.callbackUrl;
    }

    public void setCallbackUrl(String callbackUrl) {
        this.callbackUrl = callbackUrl;
    }

    public Date getPayLimitTime() {
        return this.payLimitTime;
    }

    public void setPayLimitTime(Date payLimitTime) {
        this.payLimitTime = payLimitTime;
    }

    public String getUnFinishedReason() {
        return this.unFinishedReason;
    }

    public void setUnFinishedReason(String unFinishedReason) {
        this.unFinishedReason = unFinishedReason;
    }

    public Date getOrderTime() {
        return this.orderTime;
    }

    public void setOrderTime(Date orderTime) {
        this.orderTime = orderTime;
    }

    public String getCustomerOrderNo() {
        return this.customerOrderNo;
    }

    public void setCustomerOrderNo(String customerOrderNo) {
        this.customerOrderNo = customerOrderNo;
    }

    public String getUserId() {
        return this.userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Integer getVersion() {
        return this.version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public Date getCreateTime() {
        return this.createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return this.updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public Boolean getDeleteFlag() {
        return this.deleteFlag;
    }

    public void setDeleteFlag(Boolean deleteFlag) {
        this.deleteFlag = deleteFlag;
    }
}