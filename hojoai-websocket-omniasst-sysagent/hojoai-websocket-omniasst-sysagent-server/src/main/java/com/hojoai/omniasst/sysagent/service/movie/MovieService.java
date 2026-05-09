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
package com.hojoai.omniasst.sysagent.service.movie;

import com.hojoai.common.response.Response;
import com.hojoai.omniasst.sysagent.dto.request.movie.*;
import com.hojoai.omniasst.sysagent.dto.response.movie.*;

/**
 * The MovieService class for movie server.
 *
 * @author My
 * @version 1.0, 2025/11/10
 * @since hojoai-websocket-omniasst-sysagent 1.0.0
 */
public interface MovieService {

    /**
     * 获取电影列表
     *
     * @param request the request
     * @return the response
     */
    Response<GetFilmListResponse> getFilmList(GetFilmListRequest request);

    /**
     * 获取影院列表
     *
     * @param request the request
     * @return the response
     */
    Response<GetScheduleCinemaListResponse> getScheduleCinemaList(GetScheduleCinemaListRequest request);

    /**
     * 获取电影场次信息
     *
     * @param request the request
     * @return the response
     */
    Response<GetFilmShowListResponse> getFilmShowList(GetFilmShowListRequest request);

    /**
     * 获取电影座位信息
     *
     * @param request the request
     * @return the response
     */
    Response<GetShowSeatListResponse> getShowSeatList(GetShowSeatListRequest request);

    /**
     * 获取影院列表
     * @param request
     * @return
     */
    Response<GetCinemaListResponse> getCinemaList(GetCinemaListRequest request);

    /**
     * 获取影院电影列表
     * @param cinemaId
     * @return
     */
    Response<GetCinemaFilmListResponse> getCinemaFilmList(String cinemaId);

    /**
     * 获取影片信息及近期排期
     * @param request
     * @return
     */
    Response<GetFilmScheduleListResponse> getFilmScheduleList(GetFilmScheduleListRequest request);

}
