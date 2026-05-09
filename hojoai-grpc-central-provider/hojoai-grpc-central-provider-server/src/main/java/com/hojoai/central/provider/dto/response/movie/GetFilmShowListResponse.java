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
 * @version 1.0, 2025/11/13
 * @since hojoai-grpc-central-provider 1.0.0
 */
public class GetFilmShowListResponse {

    @JsonProperty("showList")
    private List<FilmShowInfo> filmShowInfoList;

    @JsonProperty("schedules")
    private List<ScheduleInfo> scheduleInfoList;

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
