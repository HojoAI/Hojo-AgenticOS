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

/**
 * 航班值对象（API 文档 FlightVO）
 *
 * @author My
 * @version 1.0 2026/03/10
 * @since hojoai-webmvc-omniasst.focus 1.0.0
 */
public class FlightVO {

    /** 航班号 */
    private String flightNo;
    /** 机型（如 359，对应创建时航班卡片的机型显示） */
    private String flightType;
    /** 舱位 */
    private String cabin;
    /** 起飞地（起飞地-降落地） */
    private String boardingPlace;
    /** 降落地 */
    private String landingPlace;
    /** 起飞时间 HH:mm */
    private String takeoffTime;
    /** 降落时间 HH:mm */
    private String landingTime;
    /** 机场编码 */
    private String airportCode;
    /** 城市编码 */
    private String cityCode;
    /** 出发机场 */
    private String departureAirport;
    /** 到达机场 */
    private String arrivalAirport;
    /** 出发日期 */
    private String departureDate;
    /** 到达日期 */
    private String arrivalDate;
    /** 承运人 */
    private String carrier;
    /** 共享承运人 */
    private String shareCarrier;
    /** 中转停留 */
    private String transferStop;
    /** 餐食类型 */
    private String mealType;
    /** 价格 */
    private String price;
    /** 税费 */
    private String tax;
    /** 货币 */
    private String currency;

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

    public String getCabin() {
        return cabin;
    }

    public void setCabin(String cabin) {
        this.cabin = cabin;
    }

    public String getBoardingPlace() {
        return boardingPlace;
    }

    public void setBoardingPlace(String boardingPlace) {
        this.boardingPlace = boardingPlace;
    }

    public String getLandingPlace() {
        return landingPlace;
    }

    public void setLandingPlace(String landingPlace) {
        this.landingPlace = landingPlace;
    }

    public String getTakeoffTime() {
        return takeoffTime;
    }

    public void setTakeoffTime(String takeoffTime) {
        this.takeoffTime = takeoffTime;
    }

    public String getLandingTime() {
        return landingTime;
    }

    public void setLandingTime(String landingTime) {
        this.landingTime = landingTime;
    }

    public String getAirportCode() {
        return airportCode;
    }

    public void setAirportCode(String airportCode) {
        this.airportCode = airportCode;
    }

    public String getCityCode() {
        return cityCode;
    }

    public void setCityCode(String cityCode) {
        this.cityCode = cityCode;
    }

    public String getDepartureAirport() {
        return departureAirport;
    }

    public void setDepartureAirport(String departureAirport) {
        this.departureAirport = departureAirport;
    }

    public String getArrivalAirport() {
        return arrivalAirport;
    }

    public void setArrivalAirport(String arrivalAirport) {
        this.arrivalAirport = arrivalAirport;
    }

    public String getDepartureDate() {
        return departureDate;
    }

    public void setDepartureDate(String departureDate) {
        this.departureDate = departureDate;
    }

    public String getArrivalDate() {
        return arrivalDate;
    }

    public void setArrivalDate(String arrivalDate) {
        this.arrivalDate = arrivalDate;
    }

    public String getCarrier() {
        return carrier;
    }

    public void setCarrier(String carrier) {
        this.carrier = carrier;
    }

    public String getShareCarrier() {
        return shareCarrier;
    }

    public void setShareCarrier(String shareCarrier) {
        this.shareCarrier = shareCarrier;
    }

    public String getTransferStop() {
        return transferStop;
    }

    public void setTransferStop(String transferStop) {
        this.transferStop = transferStop;
    }

    public String getMealType() {
        return mealType;
    }

    public void setMealType(String mealType) {
        this.mealType = mealType;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getTax() {
        return tax;
    }

    public void setTax(String tax) {
        this.tax = tax;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }
}
