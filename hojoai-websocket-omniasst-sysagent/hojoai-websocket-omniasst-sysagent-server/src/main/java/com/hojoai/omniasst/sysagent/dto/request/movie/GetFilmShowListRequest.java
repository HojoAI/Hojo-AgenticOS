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
package com.hojoai.omniasst.sysagent.dto.request.movie;

import com.hojoai.common.asserts.Asserts;

/**
 *
 * @author My
 * @version 1.0, 2025/11/13
 * @since hojoai-websocket-omniasst-sysagent 1.0.0
 */
public class GetFilmShowListRequest {

    private String cinemaId;

    private String filmId;

    private String date;

    public String getCinemaId() {
        return cinemaId;
    }

    public void setCinemaId(String cinemaId) {
        this.cinemaId = cinemaId;
    }

    public String getFilmId() {
        return filmId;
    }

    public void setFilmId(String filmId) {
        this.filmId = filmId;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void validate(GetFilmShowListRequest request) {
        Asserts.notBlank(request.getCinemaId(), "cinemaId is blank!");
        Asserts.notBlank(request.getFilmId(), "filmId is blank!");
    }

}
