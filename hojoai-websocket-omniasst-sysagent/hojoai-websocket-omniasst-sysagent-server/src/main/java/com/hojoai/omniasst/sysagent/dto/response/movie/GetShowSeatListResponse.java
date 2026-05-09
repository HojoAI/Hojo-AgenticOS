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

import com.hojoai.central.provider.grpc.GetShowSeatListRespDTO;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author My
 * @version 1.0, 2025/11/13
 * @since hojoai-websocket-omniasst-sysagent 1.0.0
 */
public class GetShowSeatListResponse {

    private FilmInfo filmInfo;

    private FilmShowInfo filmShowInfo;

    private SeatsInfo seatsInfo;

    public static GetShowSeatListResponse convert2GetShowSeatListResponse(GetShowSeatListRespDTO respDTO) {
        GetShowSeatListResponse response = new GetShowSeatListResponse();

        FilmInfo filmInfo = new FilmInfo();

        filmInfo.setFilmId(respDTO.getFilmInfo().getFilmId());
        filmInfo.setFilmCode(respDTO.getFilmInfo().getFilmCode());
        filmInfo.setName(respDTO.getFilmInfo().getName());
        filmInfo.setPicUrl(respDTO.getFilmInfo().getPic());
        filmInfo.setGrade(respDTO.getFilmInfo().getGrade());
        filmInfo.setLike(respDTO.getFilmInfo().getLike());
        filmInfo.setDirector(respDTO.getFilmInfo().getDirector());
        filmInfo.setLeadingRole(respDTO.getFilmInfo().getLeadingRole());
        filmInfo.setPublishDate(respDTO.getFilmInfo().getPublishDate());
        filmInfo.setDuration(respDTO.getFilmInfo().getDuration());
        filmInfo.setFilmTypes(respDTO.getFilmInfo().getFilmTypes());
        filmInfo.setMovieArea(respDTO.getFilmInfo().getMovieArea());
        filmInfo.setLanguage(respDTO.getFilmInfo().getLanguage());
        filmInfo.setIntro(respDTO.getFilmInfo().getIntro());

        response.setFilmInfo(filmInfo);

        FilmShowInfo filmShowInfo = new FilmShowInfo();
        filmShowInfo.setShowId(respDTO.getShowInfo().getShowId());
        filmShowInfo.setHallName(respDTO.getShowInfo().getHallName());
        filmShowInfo.setCinemaId(respDTO.getShowInfo().getCinemaId());
        filmShowInfo.setFilmId(respDTO.getShowInfo().getFilmId());
        filmShowInfo.setFilmCode(respDTO.getShowInfo().getFilmCode());
        filmShowInfo.setFilmName(respDTO.getShowInfo().getFilmName());
        filmShowInfo.setDuration(respDTO.getShowInfo().getDuration());
        filmShowInfo.setShowTime(respDTO.getShowInfo().getShowTime());
        filmShowInfo.setStopSellTime(respDTO.getShowInfo().getStopSellTime());
        filmShowInfo.setLanguage(respDTO.getShowInfo().getLanguage());
        filmShowInfo.setShowVersionType(respDTO.getShowInfo().getShowVersionType());
        filmShowInfo.setPlanType(respDTO.getShowInfo().getPlanType());
        filmShowInfo.setDownNetPrice(new BigDecimal(respDTO.getShowInfo().getDownNetPrice()));
        filmShowInfo.setDownSalePrice(new BigDecimal(respDTO.getShowInfo().getDownSalePrice()));

        response.setFilmShowInfo(filmShowInfo);

        SeatsInfo seatsInfo = new SeatsInfo();
        seatsInfo.setRestrictions(respDTO.getSeatsInfo().getRestrictions());
        seatsInfo.setMaxColumnNo(respDTO.getSeatsInfo().getMaxColumnNo());
        seatsInfo.setMinColumnNo(respDTO.getSeatsInfo().getMinColumnNo());
        seatsInfo.setMaxRowNo(respDTO.getSeatsInfo().getMaxRowNo());
        seatsInfo.setMinRowNo(respDTO.getSeatsInfo().getMinRowNo());
        seatsInfo.setRealSeatRowCount(respDTO.getSeatsInfo().getRealSeatRowCount());
        seatsInfo.setExistLoveSeat(respDTO.getSeatsInfo().getIsExistLoveSeat());

        List<SeatInfo> setInfoList = new ArrayList<>();
        respDTO.getSeatsInfo().getSeatsList().forEach(seatVal -> {
            SeatInfo seatInfo = new SeatInfo();

            seatInfo.setSeatId(seatVal.getSeatId());
            seatInfo.setSeatNo(seatVal.getSeatNo());
            seatInfo.setAreaId(seatVal.getAreaId());
            seatInfo.setColumnNo(seatVal.getColumnNo());
            seatInfo.setStatus(seatVal.getStatus());
            seatInfo.setRowNo(seatVal.getRowNo());
            seatInfo.setLoveStatus(seatVal.getLoveStatus());
            seatInfo.setRowName(seatVal.getRowName());
            seatInfo.setNetPrice(new BigDecimal(seatVal.getNetPrice()));
            seatInfo.setSalePrice(new BigDecimal(seatVal.getSalePrice()));

            setInfoList.add(seatInfo);
        });


        List<AreaPriceInfo> areaPriceInfoList = new ArrayList<>();
        respDTO.getSeatsInfo().getAreaPricesList().forEach(areaPriceVal -> {
            AreaPriceInfo areaPriceInfo = new AreaPriceInfo();

            areaPriceInfo.setAreaId(areaPriceVal.getAreaId());
            areaPriceInfo.setNetPrice(new BigDecimal(areaPriceVal.getNetPrice()));
            areaPriceInfo.setSalePrice(new BigDecimal(areaPriceVal.getSalePrice()));

            areaPriceInfoList.add(areaPriceInfo);
        });

        seatsInfo.setAreaPriceInfoList(areaPriceInfoList);

        seatsInfo.setSeatInfoList(setInfoList);

        response.setSeatsInfo(seatsInfo);

        return response;
    }

    public FilmInfo getFilmInfo() {
        return filmInfo;
    }

    public void setFilmInfo(FilmInfo filmInfo) {
        this.filmInfo = filmInfo;
    }

    public FilmShowInfo getFilmShowInfo() {
        return filmShowInfo;
    }

    public void setFilmShowInfo(FilmShowInfo filmShowInfo) {
        this.filmShowInfo = filmShowInfo;
    }

    public SeatsInfo getSeatsInfo() {
        return seatsInfo;
    }

    public void setSeatsInfo(SeatsInfo seatsInfo) {
        this.seatsInfo = seatsInfo;
    }

}
