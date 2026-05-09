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
package com.hojoai.omniasst.focus.domain.impl;

import com.hojoai.central.provider.grpc.GetFlightByNumberAndDateRequest;
import com.hojoai.central.provider.grpc.TrainNumberStationsRequest;
import com.hojoai.omniasst.focus.common.assembler.FlightQueryAssembler;
import com.hojoai.omniasst.focus.common.assembler.TrainQueryAssembler;
import com.hojoai.omniasst.focus.configure.IConfigurationForGrpc;
import com.hojoai.omniasst.focus.domain.ExternalDomainService;
import com.hojoai.omniasst.focus.dto.response.FlightQueryResponseDTO;
import com.hojoai.omniasst.focus.dto.response.TrainQueryResponseDTO;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.Optional;

/**
 * The TripDomainServiceImpl class for TripDomainServiceImpl.
 *
 * @author My
 * @version 1.0, 2026/03/06
 * @since hojoai-webmvc-omniasst-focus 1.0.0
 */
@Service
public class ExternalDomainServiceImpl implements ExternalDomainService {

    @Resource
    private IConfigurationForGrpc iConfigurationForGrpc;

    @Override
    public FlightQueryResponseDTO flightQuery(String flightNo, String date, String lang) {
        GetFlightByNumberAndDateRequest getFlightByNumberAndDateRequest = GetFlightByNumberAndDateRequest.newBuilder()
                .setFnum(flightNo)
                .setDate(date)
                .setLang(lang != null ? lang : "")
                .build();

        return Optional.ofNullable(iConfigurationForGrpc.flightClient().getFlightByNumberAndDate(getFlightByNumberAndDateRequest))
                .map(FlightQueryAssembler::fromGrpcResponse)
                .orElseGet(FlightQueryResponseDTO::new);
    }

    @Override
    public TrainQueryResponseDTO trainQuery(String trainNo, String date, String depStan, String arrStan) {
        TrainNumberStationsRequest request = TrainNumberStationsRequest.newBuilder()
                .setTnum(trainNo != null ? trainNo : "")
                .setDate(date != null ? date : "")
                .setTdep(depStan != null ? depStan : "")
                .setTarr(arrStan != null ? arrStan : "")
                .build();

        return Optional.ofNullable(iConfigurationForGrpc.trainClient().getByTrainNumberAndStations(request))
                .map(TrainQueryAssembler::fromGrpcResponse)
                .orElseGet(() -> {
                    TrainQueryResponseDTO empty = new TrainQueryResponseDTO();
                    empty.setCount(0);
                    empty.setTrainList(Collections.emptyList());
                    return empty;
                });
    }

}
