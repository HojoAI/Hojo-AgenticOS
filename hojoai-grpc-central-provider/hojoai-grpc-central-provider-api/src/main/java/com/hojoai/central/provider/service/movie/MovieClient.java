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
package com.hojoai.central.provider.service.movie;

import com.hojoai.central.provider.grpc.*;
import com.hojoai.central.provider.service.CentralProviderChannelProvider;

/**
 * 电影票务 gRPC 阻塞客户端。
 */
public class MovieClient {

    private final CentralProviderChannelProvider centralProviderChannelProvider;

    public MovieClient(CentralProviderChannelProvider centralProviderChannelProvider) {
        this.centralProviderChannelProvider = centralProviderChannelProvider;
    }

    public GetFilmListRespDTO getFilmList(GetFilmListRequestDTO requestDTO) {
        return centralProviderChannelProvider.executeWithChannel(channel -> MovieServiceGrpc.newBlockingStub(channel).getFilmList(requestDTO));
    }

    public GetScheduleCinemaListRespDTO getScheduleCinemaList(GetScheduleCinemaListRequestDTO requestDTO) {
        return centralProviderChannelProvider.executeWithChannel(channel -> MovieServiceGrpc.newBlockingStub(channel).getScheduleCinemaList(requestDTO));
    }

    public GetFilmShowListRespDTO getFilmShowList(GetFilmShowListRequestDTO requestDTO) {
        return centralProviderChannelProvider.executeWithChannel(channel -> MovieServiceGrpc.newBlockingStub(channel).getFilmShowList(requestDTO));
    }

    public GetShowSeatListRespDTO getShowSeatList(GetShowSeatListRequestDTO requestDTO) {
        return centralProviderChannelProvider.executeWithChannel(channel -> MovieServiceGrpc.newBlockingStub(channel).getShowSeatList(requestDTO));
    }

    public GetCinemaListRespDTO getCinemaList(GetCinemaListRequestDTO requestDTO) {
        return centralProviderChannelProvider.executeWithChannel(channel -> MovieServiceGrpc.newBlockingStub(channel).getCinemaList(requestDTO));
    }

    public GetCinemaFilmListRespDTO getCinemaFilmList(GetCinemaFilmListRequestDTO requestDTO) {
        return centralProviderChannelProvider.executeWithChannel(channel -> MovieServiceGrpc.newBlockingStub(channel).getCinemaFilmList(requestDTO));
    }

    public GetFilmScheduleListRespDTO getFilmScheduleList(GetFilmScheduleListRequestDTO requestDTO) {
        return centralProviderChannelProvider.executeWithChannel(channel -> MovieServiceGrpc.newBlockingStub(channel).getFilmScheduleList(requestDTO));
    }

}
