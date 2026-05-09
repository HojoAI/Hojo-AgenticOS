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
package com.hojoai.central.provider.service.flight.impl;

import com.hojoai.central.provider.basic.GrpcService;
import com.hojoai.central.provider.domain.VariflightDomainService;
import com.hojoai.central.provider.dto.response.flight.FillFlightInfo;
import com.hojoai.central.provider.dto.response.flight.VariflightFlightResponse;
import com.hojoai.central.provider.grpc.FlightServiceGrpc;
import com.hojoai.central.provider.grpc.GetFlightByNumberAndDateRequest;
import com.hojoai.central.provider.grpc.GetFlightByNumberAndDateResponse;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 航班动态 gRPC 服务：封装飞常准领域查询并映射为 Proto 响应。
 */
@GrpcService
public class FlightServiceImpl extends FlightServiceGrpc.FlightServiceImplBase {

    private static final Logger logger = LoggerFactory.getLogger(FlightServiceImpl.class);

    @Resource
    private VariflightDomainService variflightDomainService;

    /**
     * 按航班号与日期查询航班动态。
     *
     * @param request          含 fnum、date、可选 lang
     * @param responseObserver gRPC 响应
     */
    @Override
    public void getFlightByNumberAndDate(GetFlightByNumberAndDateRequest request, StreamObserver<GetFlightByNumberAndDateResponse> responseObserver) {
        try {
            VariflightFlightResponse r = variflightDomainService.getFlightByNumberAndDate(
                    request.getFnum(), request.getDate(), request.getLang());
            responseObserver.onNext(toProto(r));
            responseObserver.onCompleted();
        } catch (IllegalArgumentException e) {
            logger.warn("getFlightByNumberAndDate invalid: {}", e.getMessage());
            responseObserver.onError(Status.INVALID_ARGUMENT.withDescription(e.getMessage()).asRuntimeException());
        } catch (Exception e) {
            logger.error("getFlightByNumberAndDate error", e);
            responseObserver.onError(Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
        }
    }

    private static GetFlightByNumberAndDateResponse toProto(VariflightFlightResponse r) {
        GetFlightByNumberAndDateResponse.Builder b = GetFlightByNumberAndDateResponse.newBuilder();
        if (r.getFcategory() != null) b.setFcategory(r.getFcategory());
        if (r.getFtype() != null) b.setFtype(r.getFtype());
        if (r.getFservice() != null) b.setFservice(r.getFservice());
        if (r.getFlightNo() != null) b.setFlightNo(r.getFlightNo());
        if (r.getFlightCompany() != null) b.setFlightCompany(r.getFlightCompany());
        if (r.getFlightDepcode() != null) b.setFlightDepcode(r.getFlightDepcode());
        if (r.getFlightArrcode() != null) b.setFlightArrcode(r.getFlightArrcode());
        if (r.getAircraftNumber() != null) b.setAircraftNumber(r.getAircraftNumber());
        if (r.getFlightDeptimePlanDate() != null) b.setFlightDeptimePlanDate(r.getFlightDeptimePlanDate());
        if (r.getFlightArrtimePlanDate() != null) b.setFlightArrtimePlanDate(r.getFlightArrtimePlanDate());
        if (r.getFlightDeptimeReadyDate() != null) b.setFlightDeptimeReadyDate(r.getFlightDeptimeReadyDate());
        if (r.getFlightArrtimeReadyDate() != null) b.setFlightArrtimeReadyDate(r.getFlightArrtimeReadyDate());
        if (r.getFlightDeptimeDate() != null) b.setFlightDeptimeDate(r.getFlightDeptimeDate());
        if (r.getFlightArrtimeDate() != null) b.setFlightArrtimeDate(r.getFlightArrtimeDate());
        if (r.getBoardGate() != null) b.setBoardGate(r.getBoardGate());
        if (r.getReachExit() != null) b.setReachExit(r.getReachExit());
        if (r.getBaggageId() != null) b.setBaggageId(r.getBaggageId());
        if (r.getBoardState() != null) b.setBoardState(r.getBoardState());
        if (r.getFlightState() != null) b.setFlightState(r.getFlightState());
        if (r.getFlightHTerminal() != null) b.setFlightHTerminal(r.getFlightHTerminal());
        if (r.getFlightTerminal() != null) b.setFlightTerminal(r.getFlightTerminal());
        if (r.getFlightDep() != null) b.setFlightDep(r.getFlightDep());
        if (r.getFlightArr() != null) b.setFlightArr(r.getFlightArr());
        if (r.getFlightDepAirport() != null) b.setFlightDepAirport(r.getFlightDepAirport());
        if (r.getFlightArrAirport() != null) b.setFlightArrAirport(r.getFlightArrAirport());
        if (r.getDstTimezone() != null) b.setDstTimezone(r.getDstTimezone());
        if (r.getOrgTimezone() != null) b.setOrgTimezone(r.getOrgTimezone());
        if (r.getStopFlag() != null) b.setStopFlag(r.getStopFlag());
        if (r.getShareFlightNo() != null) b.setShareFlightNo(r.getShareFlightNo());
        if (r.getFillFlightInfo() != null) b.setFillFlightInfo(toFillProto(r.getFillFlightInfo()));
        return b.build();
    }

    private static com.hojoai.central.provider.grpc.FillFlightInfo toFillProto(FillFlightInfo f) {
        com.hojoai.central.provider.grpc.FillFlightInfo.Builder b = com.hojoai.central.provider.grpc.FillFlightInfo.newBuilder();
        if (f.getFlightNo() != null) b.setFlightNo(f.getFlightNo());
        if (f.getFlightDepcode() != null) b.setFlightDepcode(f.getFlightDepcode());
        if (f.getFlightArrcode() != null) b.setFlightArrcode(f.getFlightArrcode());
        if (f.getFlightState() != null) b.setFlightState(f.getFlightState());
        if (f.getFlightDeptimePlanDate() != null) b.setFlightDeptimePlanDate(f.getFlightDeptimePlanDate());
        if (f.getFlightArrtimePlanDate() != null) b.setFlightArrtimePlanDate(f.getFlightArrtimePlanDate());
        if (f.getFlightDeptimeDate() != null) b.setFlightDeptimeDate(f.getFlightDeptimeDate());
        if (f.getFlightArrtimeDate() != null) b.setFlightArrtimeDate(f.getFlightArrtimeDate());
        return b.build();
    }
}
