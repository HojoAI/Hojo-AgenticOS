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
 * 电影 Value DTO
 *
 * @author My
 * @version 1.0 2026/03/02
 * @since hojoai-webmvc-omniasst.focus 1.0.0
 */
public class MovieVal {

    /**
     * 电影名称（列表/详情展示，可与日程 title 一致）
     */
    private String movieName;

    /**
     * 电影开始时间（必填）
     */
    private Date startTime;

    /**
     * 电影结束时间（必填）
     */
    private Date endTime;

    /**
     * 影院信息（必填）
     */
    private String cinemaInfo;

    /**
     * 座位号（可选）
     */
    private String seatNo;

    /**
     * 海报url
     */
    private String picUrl;

    /**
     * 电影时长
     */
    private Integer duration;

    public MovieVal() {
    }

    public boolean isInvalid() {
        return StringUtils.isBlank(movieName);
    }

    public String getMovieName() {
        return movieName;
    }

    public void setMovieName(String movieName) {
        this.movieName = movieName;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public String getCinemaInfo() {
        return cinemaInfo;
    }

    public void setCinemaInfo(String cinemaInfo) {
        this.cinemaInfo = cinemaInfo;
    }

    public String getSeatNo() {
        return seatNo;
    }

    public void setSeatNo(String seatNo) {
        this.seatNo = seatNo;
    }

    public String getPicUrl() {
        return picUrl;
    }

    public void setPicUrl(String picUrl) {
        this.picUrl = picUrl;
    }

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

}
