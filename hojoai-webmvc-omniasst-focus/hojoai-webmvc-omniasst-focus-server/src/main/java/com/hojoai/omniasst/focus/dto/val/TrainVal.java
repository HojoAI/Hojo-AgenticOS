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
package com.hojoai.omniasst.focus.dto.val;

import org.apache.commons.lang3.StringUtils;

import java.util.Date;

/**
 * 火车 Value DTO
 *
 * @author My
 * @version 1.0 2026/03/02
 * @since hojoai-webmvc-omniasst.focus 1.0.0
 */
public class TrainVal {

    /**
     * 计划出发时间（必填）
     */
    private Date depPlanTime;

    /**
     * 计划到达时间（必填）
     */
    private Date arrPlanTime;

    /**
     * 火车班次（必填）
     */
    private String trainNo;

    /**
     * 座位号（可选）
     */
    private String seatNo;

    /**
     * 检票口（可选）
     */
    private String ticketGate;

    /**
     * 出发地车站（必填）
     */
    private String depStan;

    /**
     * 目的地车站（必填）
     */
    private String arrStan;

    /**
     * 出发地车站三字码（可选）
     */
    private String depCode;

    /**
     * 目的地车站三字码（可选）
     */
    private String arrCode;

    /**
     * 火车运行时间（可选）
     */
    private Integer duration;

    public TrainVal() {
    }

    public boolean isInvalid() {
        return StringUtils.isBlank(trainNo);
    }

    public Date getDepPlanTime() {
        return depPlanTime;
    }

    public void setDepPlanTime(Date depPlanTime) {
        this.depPlanTime = depPlanTime;
    }

    public Date getArrPlanTime() {
        return arrPlanTime;
    }

    public void setArrPlanTime(Date arrPlanTime) {
        this.arrPlanTime = arrPlanTime;
    }

    public String getTrainNo() {
        return trainNo;
    }

    public void setTrainNo(String trainNo) {
        this.trainNo = trainNo;
    }

    public String getSeatNo() {
        return seatNo;
    }

    public void setSeatNo(String seatNo) {
        this.seatNo = seatNo;
    }

    public String getTicketGate() {
        return ticketGate;
    }

    public void setTicketGate(String ticketGate) {
        this.ticketGate = ticketGate;
    }

    public String getDepStan() {
        return depStan;
    }

    public void setDepStan(String depStan) {
        this.depStan = depStan;
    }

    public String getArrStan() {
        return arrStan;
    }

    public void setArrStan(String arrStan) {
        this.arrStan = arrStan;
    }

    public String getDepCode() {
        return depCode;
    }

    public void setDepCode(String depCode) {
        this.depCode = depCode;
    }

    public String getArrCode() {
        return arrCode;
    }

    public void setArrCode(String arrCode) {
        this.arrCode = arrCode;
    }

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }
}
