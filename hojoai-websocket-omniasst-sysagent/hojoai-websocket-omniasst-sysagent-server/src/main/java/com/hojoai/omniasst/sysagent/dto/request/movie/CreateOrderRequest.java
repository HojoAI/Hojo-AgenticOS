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
package com.hojoai.omniasst.sysagent.dto.request.movie;

import com.hojoai.common.asserts.Asserts;
import org.apache.commons.collections4.CollectionUtils;

import java.util.List;

/**
 *
 * @author My
 * @version 1.0, 2025/11/24
 * @since hojoai-websocket-omniasst-sysagent 1.0.0
 */
public class CreateOrderRequest {

    /**
     * 用户ID - 发起预订的用户标识
     */
    private String userId;

    /**
     * 影院ID - 放映电影的影院标识
     */
    private String cinemaId;

    /**
     * 电影ID - 所选电影的标识
     */
    private String filmId;

    /**
     * 场次ID - 具体放映场次的标识
     */
    private String showId;

    /**
     * 座位ID列表 - 用户选择的座位标识集合
     */
    private List<String> seatIdList;

    /**
     * 是否接受换座出票 - 当所选座位已售时是否接受更换座位
     * true: 接受换座, false: 不接受换座
     */
    private Boolean acceptChangeSeat;

    /**
     * 预订手机号 - 用于票务确认和通知的联系电话
     */
    private String phone;

    /**
     * 支付类型 - 交易使用的支付方式
     * 支持值: "APP" (手机应用支付), "WAP" (移动端网页支付)
     */
    private String alipayType;

    public void validate(CreateOrderRequest request) {
        Asserts.notBlank(request.getUserId(), "userId is blank!");
        Asserts.notBlank(request.getCinemaId(), "cinemaId is blank!");
        Asserts.notBlank(request.getFilmId(), "filmId is blank!");
        Asserts.notBlank(request.getShowId(), "showId is blank!");
        Asserts.notNull(request.isAcceptChangeSeat(), "acceptChangeSeat is null!");
        Asserts.notBlank(request.getPhone(), "phone is blank!");
        Asserts.notBlank(request.getAlipayType(), "alipayType is blank!");

        if (CollectionUtils.isEmpty(request.getSeatIdList())) {
            throw new IllegalArgumentException("seatIdList is empty!");
        }
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
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

    public List<String> getSeatIdList() {
        return seatIdList;
    }

    public void setSeatIdList(List<String> seatIdList) {
        this.seatIdList = seatIdList;
    }

    public Boolean isAcceptChangeSeat() {
        return acceptChangeSeat;
    }

    public void setAcceptChangeSeat(Boolean acceptChangeSeat) {
        this.acceptChangeSeat = acceptChangeSeat;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAlipayType() {
        return alipayType;
    }

    public void setAlipayType(String alipayType) {
        this.alipayType = alipayType;
    }
    
}
