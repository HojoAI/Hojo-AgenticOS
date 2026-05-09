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

import com.hojoai.central.provider.grpc.GetFilmListRespDTO;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author My
 * @version 1.0, 2025/11/10
 * @since hojoai-websocket-omniasst-sysagent 1.0.0
 */
public class GetFilmListResponse {

    /**
     * 电影总数
     */
    private Integer total;

    /**
     * 影片列表
     */
    private List<FilmInfo> filmList;

    public static GetFilmListResponse convert2GetFilmListResponse(GetFilmListRespDTO respDTO) {
        GetFilmListResponse response = new GetFilmListResponse();

        response.setTotal(respDTO.getTotal());

        List<FilmInfo> filmInfoList = new ArrayList<>();
        respDTO.getFilmListList().forEach(filmVal -> {
            FilmInfo filmInfo = new FilmInfo();

            filmInfo.setFilmId(filmVal.getFilmId());
            filmInfo.setFilmCode(filmVal.getFilmCode());
            filmInfo.setName(filmVal.getName());
            filmInfo.setPicUrl(filmVal.getPic());
            filmInfo.setGrade(filmVal.getGrade());
            filmInfo.setLike(filmVal.getLike());
            filmInfo.setDirector(filmVal.getDirector());
            filmInfo.setLeadingRole(filmVal.getLeadingRole());
            filmInfo.setPublishDate(filmVal.getPublishDate());
            filmInfo.setDuration(filmVal.getDuration());
            filmInfo.setFilmTypes(filmVal.getFilmTypes());
            filmInfo.setMovieArea(filmVal.getMovieArea());
            filmInfo.setLanguage(filmVal.getLanguage());
            filmInfo.setIntro(filmVal.getIntro());

            filmInfoList.add(filmInfo);
        });

        response.setFilmList(filmInfoList);

        return response;
    }

    public Integer getTotal() {
        return total;
    }

    public void setTotal(Integer total) {
        this.total = total;
    }

    public List<FilmInfo> getFilmList() {
        return filmList;
    }

    public void setFilmList(List<FilmInfo> filmList) {
        this.filmList = filmList;
    }

}
