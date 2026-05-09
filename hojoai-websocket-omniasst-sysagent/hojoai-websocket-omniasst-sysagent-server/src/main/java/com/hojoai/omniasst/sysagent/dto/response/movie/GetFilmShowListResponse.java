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
package com.hojoai.omniasst.sysagent.dto.response.movie;

import com.hojoai.central.provider.grpc.GetFilmShowListRespDTO;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author My
 * @version 1.0, 2025/11/13
 * @since hojoai-websocket-omniasst-sysagent 1.0.0
 */
public class GetFilmShowListResponse {

    private List<FilmShowInfo> filmShowInfoList;

    private List<ScheduleInfo> scheduleInfoList;

    public static GetFilmShowListResponse convert2GetFilmShowListResponse(GetFilmShowListRespDTO respDTO) {
        GetFilmShowListResponse response = new GetFilmShowListResponse();

        List<FilmShowInfo> filmShowInfoList = new ArrayList<>();
        respDTO.getShowListList().forEach(dto -> {
            FilmShowInfo filmShowInfo = new FilmShowInfo();
            filmShowInfo.setFilmId(dto.getFilmId());
            filmShowInfo.setHallName(dto.getHallName());
            filmShowInfo.setCinemaId(dto.getCinemaId());
            filmShowInfo.setFilmCode(dto.getFilmCode());
            filmShowInfo.setFilmName(dto.getFilmName());
            filmShowInfo.setDuration(dto.getDuration());
            filmShowInfo.setShowTime(dto.getShowTime());
            filmShowInfo.setStopSellTime(dto.getStopSellTime());
            filmShowInfo.setLanguage(dto.getLanguage());
            filmShowInfo.setShowVersionType(dto.getShowVersionType());
            filmShowInfo.setPlanType(dto.getPlanType());
            filmShowInfo.setDownNetPrice(new BigDecimal(dto.getDownNetPrice()));
            filmShowInfo.setDownSalePrice(new BigDecimal(dto.getDownSalePrice()));
            filmShowInfo.setShowId(dto.getShowId());

            filmShowInfoList.add(filmShowInfo);
        });

        List<ScheduleInfo> scheduleInfoList = new ArrayList<>();
        respDTO.getScheduleListList().forEach(dto -> {
            ScheduleInfo scheduleInfo = new ScheduleInfo();

            scheduleInfo.setShowTime(dto.getShowTime());

            scheduleInfoList.add(scheduleInfo);
        });


        response.setFilmShowInfoList(filmShowInfoList);
        response.setScheduleInfoList(scheduleInfoList);
        return response;
    }

    public List<FilmShowInfo> getFilmShowInfoList() {
        return filmShowInfoList;
    }

    public void setFilmShowInfoList(List<FilmShowInfo> filmShowInfoList) {
        this.filmShowInfoList = filmShowInfoList;
    }

    public List<ScheduleInfo> getScheduleInfoList() {
        return scheduleInfoList;
    }

    public void setScheduleInfoList(List<ScheduleInfo> scheduleInfoList) {
        this.scheduleInfoList = scheduleInfoList;
    }

}
