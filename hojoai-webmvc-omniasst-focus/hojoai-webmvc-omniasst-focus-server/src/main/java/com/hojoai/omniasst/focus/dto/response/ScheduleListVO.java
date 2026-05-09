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
package com.hojoai.omniasst.focus.dto.response;

import java.util.List;

/**
 * 查询日程列表响应 data 结构（API 文档 5.3.2）
 *
 * @author My
 * @version 1.0 2026/03/10
 * @since hojoai-webmvc-omniasst.focus 1.0.0
 */
public class ScheduleListVO {

    /** 航班列表 */
    private List<FlightVO> flightList;
    /** 日程列表 */
    private List<ScheduleVO> scheduleList;
    /** 火车列表 */
    private List<TrainVO> trainList;
    /** 酒店列表（可选，暂无数据返回空） */
    private List<?> hotelList;
    /** 门票列表（可选，暂无数据返回空） */
    private List<?> ticketList;
    /** 分页 */
    private PagingVO paging;

    public List<FlightVO> getFlightList() {
        return flightList;
    }

    public void setFlightList(List<FlightVO> flightList) {
        this.flightList = flightList;
    }

    public List<ScheduleVO> getScheduleList() {
        return scheduleList;
    }

    public void setScheduleList(List<ScheduleVO> scheduleList) {
        this.scheduleList = scheduleList;
    }

    public List<TrainVO> getTrainList() {
        return trainList;
    }

    public void setTrainList(List<TrainVO> trainList) {
        this.trainList = trainList;
    }

    public List<?> getHotelList() {
        return hotelList;
    }

    public void setHotelList(List<?> hotelList) {
        this.hotelList = hotelList;
    }

    public List<?> getTicketList() {
        return ticketList;
    }

    public void setTicketList(List<?> ticketList) {
        this.ticketList = ticketList;
    }

    public PagingVO getPaging() {
        return paging;
    }

    public void setPaging(PagingVO paging) {
        this.paging = paging;
    }
}
