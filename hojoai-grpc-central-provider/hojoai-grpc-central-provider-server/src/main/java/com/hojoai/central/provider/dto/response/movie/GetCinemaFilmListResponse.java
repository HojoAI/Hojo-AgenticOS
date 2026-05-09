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
package com.hojoai.central.provider.dto.response.movie;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 *
 * @author My
 * @version 1.0, 2025/11/10
 * @since hojoai-grpc-central-provider 1.0.0
 */
public class GetCinemaFilmListResponse {

    /**
     * 影院信息
     */
    private CinemaInfo cinemaInfo;

    /**
     * 电影列表
     */
    @JsonProperty("filmList")
    private List<FilmInfo> filmInfoList;

    public CinemaInfo getCinemaInfo() {
        return cinemaInfo;
    }

    public void setCinemaInfo(CinemaInfo cinemaInfo) {
        this.cinemaInfo = cinemaInfo;
    }

    public List<FilmInfo> getFilmInfoList() {
        return filmInfoList;
    }

    public void setFilmInfoList(List<FilmInfo> filmInfoList) {
        this.filmInfoList = filmInfoList;
    }

}
