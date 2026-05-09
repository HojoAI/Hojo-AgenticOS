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
package com.hojoai.central.provider.service.flight;

import com.hojoai.central.provider.grpc.FlightServiceGrpc;
import com.hojoai.central.provider.grpc.GetFlightByNumberAndDateRequest;
import com.hojoai.central.provider.grpc.GetFlightByNumberAndDateResponse;
import com.hojoai.central.provider.service.CentralProviderChannelProvider;

/**
 * 航班动态 gRPC 阻塞客户端。
 */
public class FlightClient {

    private final CentralProviderChannelProvider centralProviderChannelProvider;

    public FlightClient(CentralProviderChannelProvider centralProviderChannelProvider) {
        this.centralProviderChannelProvider = centralProviderChannelProvider;
    }

    /**
     * 按航班号与日期查询航班动态。
     *
     * @param request 含 fnum、date、可选 lang
     * @return 飞常准解析后的 Proto 响应
     */
    public GetFlightByNumberAndDateResponse getFlightByNumberAndDate(GetFlightByNumberAndDateRequest request) {
        return centralProviderChannelProvider.executeWithChannel(channel ->
                FlightServiceGrpc.newBlockingStub(channel).getFlightByNumberAndDate(request));
    }
}
