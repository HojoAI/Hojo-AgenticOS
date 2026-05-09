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
package com.hojoai.omniasst.focus.common.assembler;

import com.hojoai.central.provider.grpc.GetFlightByNumberAndDateResponse;
import com.hojoai.omniasst.focus.dto.response.FlightQueryResponseDTO;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 将 gRPC 航班查询响应转换为 FlightQueryResponseDTO（属性名与 gRPC 不一致时使用显式映射）
 *
 * @author My
 * @version 1.0, 2026/03/06
 * @since hojoai-webmvc-omniasst-focus 1.0.0
 */
public final class FlightQueryAssembler {

    private static final String COMPANY_1 = "股份有限公司";

    private static final String COMPANY_2 = "有限公司";

    private FlightQueryAssembler() {
    }

    /**
     * 从 gRPC GetFlightByNumberAndDateResponse 映射到 FlightQueryResponseDTO
     * 若 gRPC 响应字段名与 FlightVal 不同，在此处做一一对应赋值
     */
    public static FlightQueryResponseDTO fromGrpcResponse(GetFlightByNumberAndDateResponse resp) {
        if (resp == null) {
            return new FlightQueryResponseDTO();
        }
        FlightQueryResponseDTO dto = new FlightQueryResponseDTO();
        dto.setFlightNo(emptyToNull(resp.getFlightNo()));
        // dto.setSeatNo(emptyToNull(resp.getSeatNo()));
        dto.setFlightType(emptyToNull(resp.getFtype()));
        dto.setBoardingGate(emptyToNull(resp.getBoardGate()));
        dto.setCategory(emptyToNull(resp.getFcategory()));
        dto.setCompany(emptyToNull(stripCompanySuffix(resp.getFlightCompany())));
        dto.setDepTimezone(emptyToNull(resp.getOrgTimezone()));
        dto.setArrTimezone(emptyToNull(resp.getDstTimezone()));
        dto.setDepCode(emptyToNull(resp.getFlightDepcode()));
        dto.setArrCode(emptyToNull(resp.getFlightArrcode()));
        dto.setDepAirport(emptyToNull(resp.getFlightDepAirport()));
        dto.setArrAirport(emptyToNull(resp.getFlightArrAirport()));
        dto.setDepCity(emptyToNull(resp.getFlightDep()));
        dto.setArrCity(emptyToNull(resp.getFlightArr()));
        dto.setDepTerminal(emptyToNull(resp.getFlightHTerminal()));
        dto.setArrTerminal(emptyToNull(resp.getFlightTerminal()));
        dto.setDepPlanTime(stringToDate(resp.getFlightDeptimePlanDate()));
        dto.setArrPlanTime(stringToDate(resp.getFlightArrtimePlanDate()));
        dto.setDepReadyTime(stringToDate(resp.getFlightDeptimeReadyDate()));
        dto.setArrReadyTime(stringToDate(resp.getFlightArrtimeReadyDate()));
        dto.setDepTime(stringToDate(resp.getFlightDeptimeDate()));
        dto.setArrTime(stringToDate(resp.getFlightArrtimeDate()));
        dto.setState(emptyToNull(resp.getFlightState()));
        dto.setStopFlag(emptyToNull(resp.getStopFlag()));
        // dto.setLegFlag(resp.getFlightLe() != 0 ? resp.getLegFlag() : null);
        // dto.setOntimeRate(emptyToNull(resp.getOntimeRate()));
        // dto.setDuration(resp.getDuration() != 0 ? resp.getDuration() : null);
        return dto;
    }

    /**
     * 若公司名称包含 COMPANY_1 或 COMPANY_2 则去除后再返回
     */
    private static String stripCompanySuffix(String s) {
        if (s == null || s.isEmpty()) {
            return s;
        }
        String result = s;
        if (result.contains(COMPANY_1)) {
            result = result.replace(COMPANY_1, "");
        }
        if (result.contains(COMPANY_2)) {
            result = result.replace(COMPANY_2, "");
        }
        return result;
    }

    private static String emptyToNull(String s) {
        return (s == null || s.isEmpty()) ? null : s;
    }

    private static final String DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";

    /**
     * 将 "yyyy-MM-dd HH:mm:ss" 格式的字符串转为 Date
     */
    private static Date stringToDate(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) {
            return null;
        }
        try {
            return new SimpleDateFormat(DATE_TIME_PATTERN).parse(dateStr);
        } catch (Exception e) {
            return null;
        }
    }
}
