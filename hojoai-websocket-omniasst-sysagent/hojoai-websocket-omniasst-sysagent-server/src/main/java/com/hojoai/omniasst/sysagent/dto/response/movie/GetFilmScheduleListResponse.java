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

import com.hojoai.central.provider.grpc.GetFilmScheduleListRespDTO;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author My
 * @version 1.0, 2025/11/10
 * @since hojoai-websocket-omniasst-sysagent 1.0.0
 */
public class GetFilmScheduleListResponse {

    /**
     * 电影信息
     */
    private FilmInfo filmInfo;

    /**
     * 排期信息
     */
    private List<ScheduleInfo> scheduleInfoList;

    public static GetFilmScheduleListResponse convert2GetFilmScheduleListResponse(GetFilmScheduleListRespDTO respDTO) {
        GetFilmScheduleListResponse response = new GetFilmScheduleListResponse();

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

        List<ScheduleInfo> scheduleInfoList = new ArrayList<>();

        respDTO.getScheduleInfoListList().forEach(scheduleInfo -> {
            ScheduleInfo schedule = new ScheduleInfo();

            schedule.setShowTime(scheduleInfo.getShowTime());

            scheduleInfoList.add(schedule);
        });

        response.setScheduleInfoList(scheduleInfoList);

        return response;
    }

    public FilmInfo getFilmInfo() {
        return filmInfo;
    }

    public void setFilmInfo(FilmInfo filmInfo) {
        this.filmInfo = filmInfo;
    }

    public List<ScheduleInfo> getScheduleInfoList() {
        return scheduleInfoList;
    }

    public void setScheduleInfoList(List<ScheduleInfo> scheduleInfoList) {
        this.scheduleInfoList = scheduleInfoList;
    }

}
