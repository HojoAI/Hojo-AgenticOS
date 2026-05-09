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
package com.hojoai.omniasst.focus.repository.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

/**
 * 日程航班表PO
 *
 * @author My
 * @version 1.0 2026/03/02
 * @since hojoai-webmvc-omniasst.focus 1.0.0
 */
@TableName("focus_schedule_flight")
public class ScheduleFlightPO {

    /** 主键id（Snowflake 生成） */
    @TableId(value = "id", type = IdType.INPUT)
    private Long id;

    /** 日程ID */
    private Long scheduleId;

    /** 航班号 */
    private String flightNo;

    /**
     * 机型简称
     */
    private String flightType;

    /** 座位号 */
    private String seatNo;

    /** 登机口 */
    private String boardingGate;

    /** 航班属性（0：国内=国内；1：国内=国际；2：国内=地区；3：地区=国际；4：国际=国际；5：未知（=表示往返）） */
    private String category;

    /** 航空公司名称 */
    private String company;

    /** 出发地时区 */
    private String depTimezone;

    /** 目的地时区 */
    private String arrTimezone;

    /** 出发地机场三字码 */
    private String depCode;

    /** 目的地机场三字码 */
    private String arrCode;

    /** 出发地机场名 */
    private String depAirport;

    /** 目的地机场名 */
    private String arrAirport;

    /** 出发地城市名 */
    private String depCity;

    /** 目的地城市名 */
    private String arrCity;

    /** 出发地航站楼 */
    private String depTerminal;

    /** 目的地航站楼 */
    private String arrTerminal;

    /** 计划起飞时间 */
    private LocalDateTime depPlanTime;

    /** 计划到达时间 */
    private LocalDateTime arrPlanTime;

    /** 预计起飞时间 */
    private LocalDateTime depReadyTime;

    /** 预计到达时间 */
    private LocalDateTime arrReadyTime;

    /** 实际起飞时间 */
    private LocalDateTime depTime;

    /** 实际到达时间 */
    private LocalDateTime arrTime;

    /** 航班状态 */
    private String state;

    /** 经停次数（0:不经停；1:经停1次；2：经停2次；n：经停n次） */
    private String stopFlag;

    /** 航段标识（0:计划航段；1:因备降而产生的航段；2:因返航而产生的航段） */
    private String legFlag;

    /** 历史准点率 */
    private String ontimeRate;

    /** 平均飞行时长 */
    private Integer duration;

    public ScheduleFlightPO() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getScheduleId() {
        return scheduleId;
    }

    public void setScheduleId(Long scheduleId) {
        this.scheduleId = scheduleId;
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

    public LocalDateTime getDepPlanTime() {
        return depPlanTime;
    }

    public void setDepPlanTime(LocalDateTime depPlanTime) {
        this.depPlanTime = depPlanTime;
    }

    public LocalDateTime getArrPlanTime() {
        return arrPlanTime;
    }

    public void setArrPlanTime(LocalDateTime arrPlanTime) {
        this.arrPlanTime = arrPlanTime;
    }

    public LocalDateTime getDepReadyTime() {
        return depReadyTime;
    }

    public void setDepReadyTime(LocalDateTime depReadyTime) {
        this.depReadyTime = depReadyTime;
    }

    public LocalDateTime getArrReadyTime() {
        return arrReadyTime;
    }

    public void setArrReadyTime(LocalDateTime arrReadyTime) {
        this.arrReadyTime = arrReadyTime;
    }

    public LocalDateTime getDepTime() {
        return depTime;
    }

    public void setDepTime(LocalDateTime depTime) {
        this.depTime = depTime;
    }

    public LocalDateTime getArrTime() {
        return arrTime;
    }

    public void setArrTime(LocalDateTime arrTime) {
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

    public String getLegFlag() {
        return legFlag;
    }

    public void setLegFlag(String legFlag) {
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
