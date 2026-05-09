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

public class FillFlightInfo {
    @JsonProperty("FlightNo") private String flightNo;
    @JsonProperty("FlightDepcode") private String flightDepcode;
    @JsonProperty("FlightArrcode") private String flightArrcode;
    @JsonProperty("FlightState") private String flightState;
    @JsonProperty("FlightDeptimePlanDate") private String flightDeptimePlanDate;
    @JsonProperty("FlightArrtimePlanDate") private String flightArrtimePlanDate;
    @JsonProperty("FlightDeptimeDate") private String flightDeptimeDate;
    @JsonProperty("FlightArrtimeDate") private String flightArrtimeDate;

    public String getFlightNo() { return flightNo; }
    public void setFlightNo(String flightNo) { this.flightNo = flightNo; }
    public String getFlightDepcode() { return flightDepcode; }
    public void setFlightDepcode(String flightDepcode) { this.flightDepcode = flightDepcode; }
    public String getFlightArrcode() { return flightArrcode; }
    public void setFlightArrcode(String flightArrcode) { this.flightArrcode = flightArrcode; }
    public String getFlightState() { return flightState; }
    public void setFlightState(String flightState) { this.flightState = flightState; }
    public String getFlightDeptimePlanDate() { return flightDeptimePlanDate; }
    public void setFlightDeptimePlanDate(String flightDeptimePlanDate) { this.flightDeptimePlanDate = flightDeptimePlanDate; }
    public String getFlightArrtimePlanDate() { return flightArrtimePlanDate; }
    public void setFlightArrtimePlanDate(String flightArrtimePlanDate) { this.flightArrtimePlanDate = flightArrtimePlanDate; }
    public String getFlightDeptimeDate() { return flightDeptimeDate; }
    public void setFlightDeptimeDate(String flightDeptimeDate) { this.flightDeptimeDate = flightDeptimeDate; }
    public String getFlightArrtimeDate() { return flightArrtimeDate; }
    public void setFlightArrtimeDate(String flightArrtimeDate) { this.flightArrtimeDate = flightArrtimeDate; }
}
