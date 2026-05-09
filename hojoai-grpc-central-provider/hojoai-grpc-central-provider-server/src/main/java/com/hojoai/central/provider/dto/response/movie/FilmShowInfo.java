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

import java.math.BigDecimal;

/**
 *
 * @author My
 * @version 1.0, 2025/11/13
 * @since hojoai-grpc-central-provider 1.0.0
 */
public class FilmShowInfo {

    /**
     * 场次 id
     */
    private String showId;

    /**
     * 影厅名称
     */
    private String hallName;

    /**
     * 影院 id
     */
    private String cinemaId;

    /**
     * 影片ID
     */
    private String filmId;

    /**
     * 影片统一编码11位(暂时用不到)
     */
    private String filmCode;

    /**
     * 影片名称
     */
    private String filmName;

    /**
     * 影片时长 分钟
     */
    private Integer duration;

    /**
     * 开场时间
     */
    private String showTime;

    /**
     * 停售时间
     */
    private String stopSellTime;

    /**
     * 影片语言,如：英语
     */
    private String language;

    /**
     * 影片类型语言,如：英语 2D
     */
    private String showVersionType;

    /**
     * 影厅类型 2D 3D（可能为空）
     */
    private String planType;

    /**
     * 原价
     */
    private BigDecimal downNetPrice;

    /**
     * 销售价
     */
    private BigDecimal downSalePrice;

    public String getShowId() {
        return showId;
    }

    public void setShowId(String showId) {
        this.showId = showId;
    }

    public String getHallName() {
        return hallName;
    }

    public void setHallName(String hallName) {
        this.hallName = hallName;
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

    public String getFilmCode() {
        return filmCode;
    }

    public void setFilmCode(String filmCode) {
        this.filmCode = filmCode;
    }

    public String getFilmName() {
        return filmName;
    }

    public void setFilmName(String filmName) {
        this.filmName = filmName;
    }

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    public String getShowTime() {
        return showTime;
    }

    public void setShowTime(String showTime) {
        this.showTime = showTime;
    }

    public String getStopSellTime() {
        return stopSellTime;
    }

    public void setStopSellTime(String stopSellTime) {
        this.stopSellTime = stopSellTime;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getShowVersionType() {
        return showVersionType;
    }

    public void setShowVersionType(String showVersionType) {
        this.showVersionType = showVersionType;
    }

    public String getPlanType() {
        return planType;
    }

    public void setPlanType(String planType) {
        this.planType = planType;
    }

    public BigDecimal getDownNetPrice() {
        return downNetPrice;
    }

    public void setDownNetPrice(BigDecimal downNetPrice) {
        this.downNetPrice = downNetPrice;
    }

    public BigDecimal getDownSalePrice() {
        return downSalePrice;
    }

    public void setDownSalePrice(BigDecimal downSalePrice) {
        this.downSalePrice = downSalePrice;
    }

}
