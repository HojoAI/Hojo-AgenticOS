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
package com.hojoai.central.provider.service.train.impl;

import com.hojoai.central.provider.basic.GrpcService;
import com.hojoai.central.provider.domain.VariflightTrainDomainService;
import com.hojoai.central.provider.dto.response.train.TrainListItem;
import com.hojoai.central.provider.dto.response.train.VariflightTrainTableData;
import com.hojoai.central.provider.dto.response.train.VariflightTrainTableResponse;
import com.hojoai.central.provider.grpc.CitiesDateRequest;
import com.hojoai.central.provider.grpc.StationsDateRequest;
import com.hojoai.central.provider.grpc.TrainServiceGrpc;
import com.hojoai.central.provider.grpc.TrainNumberStationsRequest;
import com.hojoai.central.provider.grpc.TrainTableResponse;
import com.hojoai.central.provider.grpc.TrainTableUnifiedRequest;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * 火车时刻表 gRPC 服务：支持车次+站、站到站、城市到城市等查询维度。
 */
@GrpcService
public class TrainServiceImpl extends TrainServiceGrpc.TrainServiceImplBase {

    private static final Logger logger = LoggerFactory.getLogger(TrainServiceImpl.class);

    @Resource
    private VariflightTrainDomainService variflightTrainDomainService;

    /** 车次号 + 出发/到达站 + 日期查询。 */
    @Override
    public void getByTrainNumberAndStations(TrainNumberStationsRequest request, StreamObserver<TrainTableResponse> responseObserver) {
        handle(() -> variflightTrainDomainService.getByTrainNumberAndStations(
                request.getTnum(), request.getTdep(), request.getTarr(), request.getDate()), responseObserver);
    }

    /** 出发/到达站 + 日期查询（无车次）。 */
    @Override
    public void getByStationsAndDate(StationsDateRequest request, StreamObserver<TrainTableResponse> responseObserver) {
        handle(() -> variflightTrainDomainService.getByStationsAndDate(
                request.getTdep(), request.getTarr(), request.getDate()), responseObserver);
    }

    /** 出发/到达城市 + 日期查询。 */
    @Override
    public void getByCitiesAndDate(CitiesDateRequest request, StreamObserver<TrainTableResponse> responseObserver) {
        handle(() -> variflightTrainDomainService.getByCitiesAndDate(
                request.getCdep(), request.getCarr(), request.getDate()), responseObserver);
    }

    /**
     * 统一入口：根据请求中是否提供车次号、站名或城市名路由到对应领域查询。
     */
    @Override
    public void getTrainTable(TrainTableUnifiedRequest request, StreamObserver<TrainTableResponse> responseObserver) {
        String tnum = request.getTnum();
        String tdep = request.getTdep();
        String tarr = request.getTarr();
        String cdep = request.getCdep();
        String carr = request.getCarr();
        String date = request.getDate();
        if (date == null || date.isBlank()) {
            responseObserver.onError(Status.INVALID_ARGUMENT.withDescription("date is required").asRuntimeException());
            return;
        }
        try {
            VariflightTrainTableResponse r;
            if (tnum != null && !tnum.isBlank() && tdep != null && !tdep.isBlank() && tarr != null && !tarr.isBlank()) {
                r = variflightTrainDomainService.getByTrainNumberAndStations(tnum, tdep, tarr, date);
            } else if (tdep != null && !tdep.isBlank() && tarr != null && !tarr.isBlank()) {
                r = variflightTrainDomainService.getByStationsAndDate(tdep, tarr, date);
            } else if (cdep != null && !cdep.isBlank() && carr != null && !carr.isBlank()) {
                r = variflightTrainDomainService.getByCitiesAndDate(cdep, carr, date);
            } else {
                responseObserver.onError(Status.INVALID_ARGUMENT.withDescription("require (tnum+tdep+tarr) or (tdep+tarr) or (cdep+carr) with date").asRuntimeException());
                return;
            }
            responseObserver.onNext(toProto(r));
            responseObserver.onCompleted();
        } catch (IllegalArgumentException e) {
            logger.warn("getTrainTable invalid: {}", e.getMessage());
            responseObserver.onError(Status.INVALID_ARGUMENT.withDescription(e.getMessage()).asRuntimeException());
        } catch (Exception e) {
            logger.error("getTrainTable error", e);
            responseObserver.onError(Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
        }
    }

    private void handle(java.util.function.Supplier<VariflightTrainTableResponse> supplier, StreamObserver<TrainTableResponse> responseObserver) {
        try {
            VariflightTrainTableResponse r = supplier.get();
            responseObserver.onNext(toProto(r));
            responseObserver.onCompleted();
        } catch (IllegalArgumentException e) {
            logger.warn("train request invalid: {}", e.getMessage());
            responseObserver.onError(Status.INVALID_ARGUMENT.withDescription(e.getMessage()).asRuntimeException());
        } catch (Exception e) {
            logger.error("train request error", e);
            responseObserver.onError(Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
        }
    }

    private static TrainTableResponse toProto(VariflightTrainTableResponse r) {
        TrainTableResponse.Builder b = TrainTableResponse.newBuilder();
        if (r.getCode() != null) b.setCode(r.getCode());
        if (r.getMsg() != null) b.setMsg(r.getMsg());
        if (r.getData() != null) {
            VariflightTrainTableData d = r.getData();
            com.hojoai.central.provider.grpc.TrainTableData.Builder dataBuilder = com.hojoai.central.provider.grpc.TrainTableData.newBuilder();
            if (d.getCount() != null) dataBuilder.setCount(d.getCount());
            List<TrainListItem> list = d.getTrainlist();
            if (list != null) {
                for (TrainListItem item : list) {
                    dataBuilder.addTrainlist(com.hojoai.central.provider.grpc.TrainListItem.newBuilder()
                            .setTrainnum(item.getTrainnum() != null ? item.getTrainnum() : "")
                            .setDepstan(item.getDepstan() != null ? item.getDepstan() : "")
                            .setArrstan(item.getArrstan() != null ? item.getArrstan() : "")
                            .setDepstancode(item.getDepstancode() != null ? item.getDepstancode() : "")
                            .setArrstancode(item.getArrstancode() != null ? item.getArrstancode() : "")
                            .setPlandeptime(item.getPlandeptime() != null ? item.getPlandeptime() : "")
                            .setPlanarrtime(item.getPlanarrtime() != null ? item.getPlanarrtime() : "")
                            .setDuration(item.getDuration() != null ? item.getDuration() : 0)
                            .build());
                }
            }
            b.setData(dataBuilder.build());
        }
        return b.build();
    }
}
