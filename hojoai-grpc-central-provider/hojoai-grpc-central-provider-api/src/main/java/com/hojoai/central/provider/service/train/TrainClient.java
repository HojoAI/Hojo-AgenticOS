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
package com.hojoai.central.provider.service.train;

import com.hojoai.central.provider.grpc.CitiesDateRequest;
import com.hojoai.central.provider.grpc.StationsDateRequest;
import com.hojoai.central.provider.grpc.TrainServiceGrpc;
import com.hojoai.central.provider.grpc.TrainNumberStationsRequest;
import com.hojoai.central.provider.grpc.TrainTableResponse;
import com.hojoai.central.provider.grpc.TrainTableUnifiedRequest;
import com.hojoai.central.provider.service.CentralProviderChannelProvider;

/**
 * 火车时刻表 gRPC 阻塞客户端。
 */
public class TrainClient {

    private final CentralProviderChannelProvider centralProviderChannelProvider;

    public TrainClient(CentralProviderChannelProvider centralProviderChannelProvider) {
        this.centralProviderChannelProvider = centralProviderChannelProvider;
    }

    public TrainTableResponse getByTrainNumberAndStations(TrainNumberStationsRequest request) {
        return centralProviderChannelProvider.executeWithChannel(channel ->
                TrainServiceGrpc.newBlockingStub(channel).getByTrainNumberAndStations(request));
    }

    public TrainTableResponse getByStationsAndDate(StationsDateRequest request) {
        return centralProviderChannelProvider.executeWithChannel(channel ->
                TrainServiceGrpc.newBlockingStub(channel).getByStationsAndDate(request));
    }

    public TrainTableResponse getByCitiesAndDate(CitiesDateRequest request) {
        return centralProviderChannelProvider.executeWithChannel(channel ->
                TrainServiceGrpc.newBlockingStub(channel).getByCitiesAndDate(request));
    }

    public TrainTableResponse getTrainTable(TrainTableUnifiedRequest request) {
        return centralProviderChannelProvider.executeWithChannel(channel ->
                TrainServiceGrpc.newBlockingStub(channel).getTrainTable(request));
    }
}
