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
package com.hojoai.central.provider.dto.response.flight;

import com.fasterxml.jackson.annotation.JsonProperty;

public class VariflightFlightResponse {
    @JsonProperty("fcategory") private String fcategory;
    @JsonProperty("ftype") private String ftype;
    @JsonProperty("fservice") private String fservice;
    @JsonProperty("FlightNo") private String flightNo;
    @JsonProperty("FlightCompany") private String flightCompany;
    @JsonProperty("FlightDepcode") private String flightDepcode;
    @JsonProperty("FlightArrcode") private String flightArrcode;
    @JsonProperty("AircraftNumber") private String aircraftNumber;
    @JsonProperty("FlightDeptimePlanDate") private String flightDeptimePlanDate;
    @JsonProperty("FlightArrtimePlanDate") private String flightArrtimePlanDate;
    @JsonProperty("FlightDeptimeReadyDate") private String flightDeptimeReadyDate;
    @JsonProperty("FlightArrtimeReadyDate") private String flightArrtimeReadyDate;
    @JsonProperty("FlightDeptimeDate") private String flightDeptimeDate;
    @JsonProperty("FlightArrtimeDate") private String flightArrtimeDate;
    @JsonProperty("BoardGate") private String boardGate;
    @JsonProperty("ReachExit") private String reachExit;
    @JsonProperty("BaggageID") private String baggageId;
    @JsonProperty("BoardState") private String boardState;
    @JsonProperty("FlightState") private String flightState;
    @JsonProperty("FlightHTerminal") private String flightHTerminal;
    @JsonProperty("FlightTerminal") private String flightTerminal;
    @JsonProperty("FlightDep") private String flightDep;
    @JsonProperty("FlightArr") private String flightArr;
    @JsonProperty("FlightDepAirport") private String flightDepAirport;
    @JsonProperty("FlightArrAirport") private String flightArrAirport;
    @JsonProperty("dst_timezone") private String dstTimezone;
    @JsonProperty("org_timezone") private String orgTimezone;
    @JsonProperty("StopFlag") private String stopFlag;
    @JsonProperty("ShareFlightNo") private String shareFlightNo;
    @JsonProperty("FillFlightInfo") private FillFlightInfo fillFlightInfo;

    public String getFcategory() { return fcategory; }
    public void setFcategory(String fcategory) { this.fcategory = fcategory; }
    public String getFtype() { return ftype; }
    public void setFtype(String ftype) { this.ftype = ftype; }
    public String getFservice() { return fservice; }
    public void setFservice(String fservice) { this.fservice = fservice; }
    public String getFlightNo() { return flightNo; }
    public void setFlightNo(String flightNo) { this.flightNo = flightNo; }
    public String getFlightCompany() { return flightCompany; }
    public void setFlightCompany(String flightCompany) { this.flightCompany = flightCompany; }
    public String getFlightDepcode() { return flightDepcode; }
    public void setFlightDepcode(String flightDepcode) { this.flightDepcode = flightDepcode; }
    public String getFlightArrcode() { return flightArrcode; }
    public void setFlightArrcode(String flightArrcode) { this.flightArrcode = flightArrcode; }
    public String getAircraftNumber() { return aircraftNumber; }
    public void setAircraftNumber(String aircraftNumber) { this.aircraftNumber = aircraftNumber; }
    public String getFlightDeptimePlanDate() { return flightDeptimePlanDate; }
    public void setFlightDeptimePlanDate(String flightDeptimePlanDate) { this.flightDeptimePlanDate = flightDeptimePlanDate; }
    public String getFlightArrtimePlanDate() { return flightArrtimePlanDate; }
    public void setFlightArrtimePlanDate(String flightArrtimePlanDate) { this.flightArrtimePlanDate = flightArrtimePlanDate; }
    public String getFlightDeptimeReadyDate() { return flightDeptimeReadyDate; }
    public void setFlightDeptimeReadyDate(String flightDeptimeReadyDate) { this.flightDeptimeReadyDate = flightDeptimeReadyDate; }
    public String getFlightArrtimeReadyDate() { return flightArrtimeReadyDate; }
    public void setFlightArrtimeReadyDate(String flightArrtimeReadyDate) { this.flightArrtimeReadyDate = flightArrtimeReadyDate; }
    public String getFlightDeptimeDate() { return flightDeptimeDate; }
    public void setFlightDeptimeDate(String flightDeptimeDate) { this.flightDeptimeDate = flightDeptimeDate; }
    public String getFlightArrtimeDate() { return flightArrtimeDate; }
    public void setFlightArrtimeDate(String flightArrtimeDate) { this.flightArrtimeDate = flightArrtimeDate; }
    public String getBoardGate() { return boardGate; }
    public void setBoardGate(String boardGate) { this.boardGate = boardGate; }
    public String getReachExit() { return reachExit; }
    public void setReachExit(String reachExit) { this.reachExit = reachExit; }
    public String getBaggageId() { return baggageId; }
    public void setBaggageId(String baggageId) { this.baggageId = baggageId; }
    public String getBoardState() { return boardState; }
    public void setBoardState(String boardState) { this.boardState = boardState; }
    public String getFlightState() { return flightState; }
    public void setFlightState(String flightState) { this.flightState = flightState; }
    public String getFlightHTerminal() { return flightHTerminal; }
    public void setFlightHTerminal(String flightHTerminal) { this.flightHTerminal = flightHTerminal; }
    public String getFlightTerminal() { return flightTerminal; }
    public void setFlightTerminal(String flightTerminal) { this.flightTerminal = flightTerminal; }
    public String getFlightDep() { return flightDep; }
    public void setFlightDep(String flightDep) { this.flightDep = flightDep; }
    public String getFlightArr() { return flightArr; }
    public void setFlightArr(String flightArr) { this.flightArr = flightArr; }
    public String getFlightDepAirport() { return flightDepAirport; }
    public void setFlightDepAirport(String flightDepAirport) { this.flightDepAirport = flightDepAirport; }
    public String getFlightArrAirport() { return flightArrAirport; }
    public void setFlightArrAirport(String flightArrAirport) { this.flightArrAirport = flightArrAirport; }
    public String getDstTimezone() { return dstTimezone; }
    public void setDstTimezone(String dstTimezone) { this.dstTimezone = dstTimezone; }
    public String getOrgTimezone() { return orgTimezone; }
    public void setOrgTimezone(String orgTimezone) { this.orgTimezone = orgTimezone; }
    public String getStopFlag() { return stopFlag; }
    public void setStopFlag(String stopFlag) { this.stopFlag = stopFlag; }
    public String getShareFlightNo() { return shareFlightNo; }
    public void setShareFlightNo(String shareFlightNo) { this.shareFlightNo = shareFlightNo; }
    public FillFlightInfo getFillFlightInfo() { return fillFlightInfo; }
    public void setFillFlightInfo(FillFlightInfo fillFlightInfo) { this.fillFlightInfo = fillFlightInfo; }
}
