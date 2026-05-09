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
package com.hojoai.omniasst.sysagent.service.movie.impl;

import com.hojoai.central.provider.grpc.*;
import com.hojoai.common.response.Response;
import com.hojoai.common.response.ResponseStatusBasic;
import com.hojoai.omniasst.sysagent.configure.IConfigurationForGrpc;
import com.hojoai.omniasst.sysagent.dto.request.movie.*;
import com.hojoai.omniasst.sysagent.dto.response.movie.*;
import com.hojoai.omniasst.sysagent.service.movie.MovieService;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

/**
 * The MovieServiceImpl class for Movie Server.
 *
 * @author My
 * @version 1.0, 2025/11/10
 * @since hojoai-websocket-omniasst-sysagent 1.0.0
 */
@Service
public class MovieServiceImpl implements MovieService {

    @Resource
    private IConfigurationForGrpc iConfigurationForGrpc;

    /**
     * {@inheritDoc}
     */
    @Override
    public Response<GetFilmListResponse> getFilmList(GetFilmListRequest request) {
        GetFilmListRequestDTO requestDTO = GetFilmListRequestDTO.newBuilder()
                .setType(request.getType())
                .setKeyWord(StringUtils.isEmpty(request.getKeyword()) ? StringUtils.EMPTY : request.getKeyword())
                .setPageIndex(request.getPageIndex())
                .setPageSize(request.getPageSize())
                .build();

        GetFilmListRespDTO respDTO = iConfigurationForGrpc.movieClient().getFilmList(requestDTO);

        return respDTO.getSuccess()
                ? Response.newInstance(ResponseStatusBasic.OK, GetFilmListResponse.convert2GetFilmListResponse(respDTO))
                : Response.newInstance(ResponseStatusBasic.GENERAL_ERROR.getResCode(), respDTO.getMsg(), null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Response<GetScheduleCinemaListResponse> getScheduleCinemaList(GetScheduleCinemaListRequest request) {
        if (Objects.isNull(request.getDate())) {
            request.setDate(this.getCurrentDateStr());
        }

        GetScheduleCinemaListRequestDTO requestDTO = GetScheduleCinemaListRequestDTO.newBuilder()
                .setCityCode(StringUtils.defaultString((request.getCityCode())))
                .setCityName(StringUtils.defaultString(request.getCityName()))
                .setFilmId(StringUtils.defaultString(request.getFilmId()))
                .setRegionCode(StringUtils.defaultString((request.getRegionCode())))
                .setRegionName(StringUtils.defaultString((request.getRegionName())))
                .setKeyword(StringUtils.defaultString((request.getKeyword())))
                .setLatitude(Objects.isNull(request.getLatitude()) ? StringUtils.EMPTY  : request.getLatitude().toString())
                .setLongitude(Objects.isNull(request.getLongitude()) ? StringUtils.EMPTY  : request.getLongitude().toString())
                .setPageIndex(request.getPageIndex())
                .setPageSize(request.getPageSize())
                .setDate(request.getDate())
                .build();

        GetScheduleCinemaListRespDTO respDTO = iConfigurationForGrpc.movieClient().getScheduleCinemaList(requestDTO);

        return respDTO.getSuccess()
                ? Response.newInstance(ResponseStatusBasic.OK, GetScheduleCinemaListResponse.convert2GetScheduleCinemaListResponse(respDTO))
                : Response.newInstance(ResponseStatusBasic.GENERAL_ERROR.getResCode(), respDTO.getMsg(), null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Response<GetFilmShowListResponse> getFilmShowList(GetFilmShowListRequest request) {
        if (Objects.isNull(request.getDate())) {
            request.setDate(this.getCurrentDateStr());
        }

        GetFilmShowListRequestDTO requestDTO = GetFilmShowListRequestDTO.newBuilder()
                .setFilmId(request.getFilmId())
                .setCinemaId(request.getCinemaId())
                .setDate(request.getDate())
                .build();

        GetFilmShowListRespDTO respDTO = iConfigurationForGrpc.movieClient().getFilmShowList(requestDTO);

        return respDTO.getSuccess()
                ? Response.newInstance(ResponseStatusBasic.OK, GetFilmShowListResponse.convert2GetFilmShowListResponse(respDTO))
                : Response.newInstance(ResponseStatusBasic.GENERAL_ERROR.getResCode(), respDTO.getMsg(), null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Response<GetShowSeatListResponse> getShowSeatList(GetShowSeatListRequest request) {
        GetShowSeatListRequestDTO requestDTO = GetShowSeatListRequestDTO.newBuilder()
                .setShowId(request.getShowId())
                .build();

        GetShowSeatListRespDTO respDTO = iConfigurationForGrpc.movieClient().getShowSeatList(requestDTO);

        return respDTO.getSuccess()
                ? Response.newInstance(ResponseStatusBasic.OK, GetShowSeatListResponse.convert2GetShowSeatListResponse(respDTO))
                : Response.newInstance(ResponseStatusBasic.GENERAL_ERROR.getResCode(), respDTO.getMsg(), null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Response<GetCinemaListResponse> getCinemaList(GetCinemaListRequest request) {
        GetCinemaListRequestDTO requestDTO = GetCinemaListRequestDTO.newBuilder()
                .setCityCode(StringUtils.defaultString((request.getCityCode())))
                .setCityName(StringUtils.defaultString((request.getCityName())))
                .setRegionCode(StringUtils.defaultString((request.getRegionCode())))
                .setRegionName(StringUtils.defaultString((request.getRegionName())))
                .setKeyword(StringUtils.defaultString((request.getKeyword())))
                .setLatitude(Objects.isNull(request.getLatitude()) ? StringUtils.EMPTY  : request.getLatitude().toString())
                .setLongitude(Objects.isNull(request.getLongitude()) ? StringUtils.EMPTY  : request.getLongitude().toString())
                .setPageIndex(request.getPageIndex())
                .setPageSize(request.getPageSize())
                .build();

        GetCinemaListRespDTO respDTO = iConfigurationForGrpc.movieClient().getCinemaList(requestDTO);

        return respDTO.getSuccess()
                ? Response.newInstance(ResponseStatusBasic.OK, GetCinemaListResponse.convert2GetCinemaListResponse(respDTO))
                : Response.newInstance(ResponseStatusBasic.GENERAL_ERROR.getResCode(), respDTO.getMsg(), null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Response<GetCinemaFilmListResponse> getCinemaFilmList(String cinemaId) {
        GetCinemaFilmListRequestDTO requestDTO = GetCinemaFilmListRequestDTO.newBuilder()
                .setCinemaId(cinemaId)
                .build();

        GetCinemaFilmListRespDTO respDTO = iConfigurationForGrpc.movieClient().getCinemaFilmList(requestDTO);

        return respDTO.getSuccess()
                ? Response.newInstance(ResponseStatusBasic.OK, GetCinemaFilmListResponse.convert2GetCinemaFilmListResponse(respDTO))
                : Response.newInstance(ResponseStatusBasic.GENERAL_ERROR.getResCode(), respDTO.getMsg(), null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Response<GetFilmScheduleListResponse> getFilmScheduleList(GetFilmScheduleListRequest request) {
        GetFilmScheduleListRequestDTO requestDTO = GetFilmScheduleListRequestDTO.newBuilder()
                .setFilmId(request.getFilmId())
                .setCityCode(StringUtils.defaultString((request.getCityCode())))
                .setCityName(StringUtils.defaultString(request.getCityName()))
                .build();

        GetFilmScheduleListRespDTO respDTO = iConfigurationForGrpc.movieClient().getFilmScheduleList(requestDTO);

        return respDTO.getSuccess()
                ? Response.newInstance(ResponseStatusBasic.OK, GetFilmScheduleListResponse.convert2GetFilmScheduleListResponse(respDTO))
                : Response.newInstance(ResponseStatusBasic.GENERAL_ERROR.getResCode(), respDTO.getMsg(), null);
    }

    private String getCurrentDateStr() {
        LocalDate today = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return today.format(formatter);
    }

}
