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

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

/**
 *
 * @author My
 * @version 1.0, 2025/11/13
 * @since hojoai-grpc-central-provider 1.0.0
 */
public class SeatInfo {

    /**
     * 座位 id
     */
    private String seatId;

    /**
     * 纵坐标
     */
    private String columnNo;

    /**
     * 横坐标
     */
    private String rowNo;

    /**
     * 座位名称
     */
    private String seatNo;

    /**
     * 售罄状态: N：可售 LK：已售 W：座位维护
     */
    private String status;

    /**
     * 是否情侣座 0为非情侣座 1为情侣座左 2为情侣座右
     */
    @JsonProperty("lovestatus")
    private Integer loveStatus;

    private String rowName;

    private BigDecimal netPrice;

    private BigDecimal salePrice;

    /**
     * 区域id
     */
    private String areaId;

    public String getSeatId() {
        return seatId;
    }

    public void setSeatId(String seatId) {
        this.seatId = seatId;
    }

    public String getColumnNo() {
        return columnNo;
    }

    public void setColumnNo(String columnNo) {
        this.columnNo = columnNo;
    }

    public String getRowNo() {
        return rowNo;
    }

    public void setRowNo(String rowNo) {
        this.rowNo = rowNo;
    }

    public String getSeatNo() {
        return seatNo;
    }

    public void setSeatNo(String seatNo) {
        this.seatNo = seatNo;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getLoveStatus() {
        return loveStatus;
    }

    public void setLoveStatus(Integer loveStatus) {
        this.loveStatus = loveStatus;
    }

    public String getRowName() {
        return rowName;
    }

    public void setRowName(String rowName) {
        this.rowName = rowName;
    }

    public BigDecimal getNetPrice() {
        return netPrice;
    }

    public void setNetPrice(BigDecimal netPrice) {
        this.netPrice = netPrice;
    }

    public BigDecimal getSalePrice() {
        return salePrice;
    }

    public void setSalePrice(BigDecimal salePrice) {
        this.salePrice = salePrice;
    }

    public String getAreaId() {
        return areaId;
    }

    public void setAreaId(String areaId) {
        this.areaId = areaId;
    }

}
