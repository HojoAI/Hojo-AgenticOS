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
 * 日程火车表PO
 *
 * @author My
 * @version 1.0 2026/03/02
 * @since hojoai-webmvc-omniasst.focus 1.0.0
 */
@TableName("focus_schedule_train")
public class ScheduleTrainPO {

    /** 主键id（Snowflake 生成） */
    @TableId(value = "id", type = IdType.INPUT)
    private Long id;

    /** 日程ID */
    private Long scheduleId;

    /** 火车班次 */
    private String trainNo;

    /** 座位号 */
    private String seatNo;

    /** 检票口 */
    private String ticketGate;

    /** 计划出发时间 */
    private LocalDateTime depPlanTime;

    /** 计划到达时间 */
    private LocalDateTime arrPlanTime;

    /** 出发地车站 */
    private String depStan;

    /** 目的地车站 */
    private String arrStan;

    /** 出发车站三字码 */
    private String depCode;

    /** 目的车站三字码 */
    private String arrCode;

    /** 火车运行时间 */
    private Integer duration;

    public ScheduleTrainPO() {
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
