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
 * @version 1.0, 2025/11/13
 * @since hojoai-websocket-omniasst-sysagent 1.0.0
 */
public class SeatsInfo {

    private Integer restrictions;

    private Integer maxColumnNo;

    private Integer minColumnNo;

    private Integer maxRowNo;

    private  Integer minRowNo;

    private Integer realSeatRowCount;

    private Boolean isExistLoveSeat;

    private List<SeatInfo> seatInfoList;

    private List<AreaPriceInfo> areaPriceInfoList;

    public Integer getRestrictions() {
        return restrictions;
    }

    public void setRestrictions(Integer restrictions) {
        this.restrictions = restrictions;
    }

    public Integer getMaxColumnNo() {
        return maxColumnNo;
    }

    public void setMaxColumnNo(Integer maxColumnNo) {
        this.maxColumnNo = maxColumnNo;
    }

    public Integer getMinColumnNo() {
        return minColumnNo;
    }

    public void setMinColumnNo(Integer minColumnNo) {
        this.minColumnNo = minColumnNo;
    }

    public Integer getMaxRowNo() {
        return maxRowNo;
    }

    public void setMaxRowNo(Integer maxRowNo) {
        this.maxRowNo = maxRowNo;
    }

    public Integer getMinRowNo() {
        return minRowNo;
    }

    public void setMinRowNo(Integer minRowNo) {
        this.minRowNo = minRowNo;
    }

    public Integer getRealSeatRowCount() {
        return realSeatRowCount;
    }

    public void setRealSeatRowCount(Integer realSeatRowCount) {
        this.realSeatRowCount = realSeatRowCount;
    }

    public Boolean getExistLoveSeat() {
        return isExistLoveSeat;
    }

    public void setExistLoveSeat(Boolean existLoveSeat) {
        isExistLoveSeat = existLoveSeat;
    }

    public List<SeatInfo> getSeatInfoList() {
        return seatInfoList;
    }

    public void setSeatInfoList(List<SeatInfo> seatInfoList) {
        this.seatInfoList = seatInfoList;
    }

    public List<AreaPriceInfo> getAreaPriceInfoList() {
        return areaPriceInfoList;
    }

    public void setAreaPriceInfoList(List<AreaPriceInfo> areaPriceInfoList) {
        this.areaPriceInfoList = areaPriceInfoList;
    }

}
