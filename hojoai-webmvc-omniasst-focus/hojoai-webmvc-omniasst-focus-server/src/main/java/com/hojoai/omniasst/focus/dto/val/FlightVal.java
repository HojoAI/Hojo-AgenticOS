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

import com.fasterxml.jackson.annotation.JsonAlias;
import org.apache.commons.lang3.StringUtils;

import java.util.Date;

/**
 * 航班 Value DTO
 *
 * @author My
 * @version 1.0 2026/03/02
 * @since hojoai-webmvc-omniasst.focus 1.0.0
 */
public class FlightVal {

    /**
     * 航班号（必填）
     */
    private String flightNo;

    /**
     * 机型简称
     */
    private String flightType;

    /**
     * 座位号（可选），创建时表单字段名为 flightSeatNumber，详情返回 seatNo
     */
    @JsonAlias("flightSeatNumber")
    private String seatNo;

    /**
     * 登机口（可选），创建时表单字段名为 flightGate，详情返回 boardingGate
     */
    @JsonAlias("flightGate")
    private String boardingGate;

    /**
     * 航班属性（可选）
     * 0：国内=国内；1：国内=国际；2：国内=地区；3：地区=国际；4：国际=国际；5：未知（=表示往返）
     */
    private String category;

    /**
     * 航空公司名称（可选）
     */
    private String company;

    /**
     * 出发地时区（可选）
     */
    private String depTimezone;

    /**
     * 目的地时区（可选）
     */
    private String arrTimezone;

    /**
     * 出发地机场三字码（可选）
     */
    private String depCode;

    /**
     * 目的地机场三字码（可选）
     */
    private String arrCode;

    /**
     * 出发地机场名（必填）
     */
    private String depAirport;

    /**
     * 目的地机场名（必填）
     */
    private String arrAirport;

    /**
     * 出发地城市名（可选）
     */
    private String depCity;

    /**
     * 目的地城市名（可选）
     */
    private String arrCity;

    /**
     * 出发地航站楼（可选）
     */
    private String depTerminal;

    /**
     * 目的地航站楼（可选）
     */
    private String arrTerminal;

    /**
     * 计划起飞时间（必填）
     */
    private Date depPlanTime;

    /**
     * 计划到达时间（必填）
     */
    private Date arrPlanTime;

    /**
     * 预计起飞时间（可选）
     */
    private Date depReadyTime;

    /**
     * 预计到达时间（可选）
     */
    private Date arrReadyTime;

    /**
     * 实际起飞时间（可选）
     */
    private Date depTime;

    /**
     * 实际到达时间（可选）
     */
    private Date arrTime;

    /**
     * 航班状态（可选）
     */
    private String state;

    /**
     * 经停次数（必填）
     * 0:不经停；1:经停1次；2:经停2次；n:经停n次
     */
    private String stopFlag;

    /**
     * 航段标识（必填）
     * 0-计划航段；1-因备降而产生的航段；2-因返航而产生的航段
     */
    private Integer legFlag;

    /**
     * 历史准点率（可选）
     */
    private String ontimeRate;

    /**
     * 平均飞行时长（可选）
     */
    private Integer duration;

    public FlightVal() {
    }

    public boolean isInvalid() {
        return StringUtils.isBlank(flightNo);
    }

    public String getFlightNo() {
        return flightNo;
    }

    public void setFlightNo(String flightNo) {
        this.flightNo = flightNo;
    }

    public String getFlightType() {
        return flightType;
    }

    public void setFlightType(String flightType) {
        this.flightType = flightType;
    }

    public String getSeatNo() {
        return seatNo;
    }

    public void setSeatNo(String seatNo) {
        this.seatNo = seatNo;
    }

    public String getBoardingGate() {
        return boardingGate;
    }

    public void setBoardingGate(String boardingGate) {
        this.boardingGate = boardingGate;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getDepTimezone() {
        return depTimezone;
    }

    public void setDepTimezone(String depTimezone) {
        this.depTimezone = depTimezone;
    }

    public String getArrTimezone() {
        return arrTimezone;
    }

    public void setArrTimezone(String arrTimezone) {
        this.arrTimezone = arrTimezone;
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

    public String getDepAirport() {
        return depAirport;
    }

    public void setDepAirport(String depAirport) {
        this.depAirport = depAirport;
    }

    public String getArrAirport() {
        return arrAirport;
    }

    public void setArrAirport(String arrAirport) {
        this.arrAirport = arrAirport;
    }

    public String getDepCity() {
        return depCity;
    }

    public void setDepCity(String depCity) {
        this.depCity = depCity;
    }

    public String getArrCity() {
        return arrCity;
    }

    public void setArrCity(String arrCity) {
        this.arrCity = arrCity;
    }

    public String getDepTerminal() {
        return depTerminal;
    }

    public void setDepTerminal(String depTerminal) {
        this.depTerminal = depTerminal;
    }

    public String getArrTerminal() {
        return arrTerminal;
    }

    public void setArrTerminal(String arrTerminal) {
        this.arrTerminal = arrTerminal;
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

    public Date getDepReadyTime() {
        return depReadyTime;
    }

    public void setDepReadyTime(Date depReadyTime) {
        this.depReadyTime = depReadyTime;
    }

    public Date getArrReadyTime() {
        return arrReadyTime;
    }

    public void setArrReadyTime(Date arrReadyTime) {
        this.arrReadyTime = arrReadyTime;
    }

    public Date getDepTime() {
        return depTime;
    }

    public void setDepTime(Date depTime) {
        this.depTime = depTime;
    }

    public Date getArrTime() {
        return arrTime;
    }

    public void setArrTime(Date arrTime) {
        this.arrTime = arrTime;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getStopFlag() {
        return stopFlag;
    }

    public void setStopFlag(String stopFlag) {
        this.stopFlag = stopFlag;
    }

    public Integer getLegFlag() {
        return legFlag;
    }

    public void setLegFlag(Integer legFlag) {
        this.legFlag = legFlag;
    }

    public String getOntimeRate() {
        return ontimeRate;
    }

    public void setOntimeRate(String ontimeRate) {
        this.ontimeRate = ontimeRate;
    }

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }
}
