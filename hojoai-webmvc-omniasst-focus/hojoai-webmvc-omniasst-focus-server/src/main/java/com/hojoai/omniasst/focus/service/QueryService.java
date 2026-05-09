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
package com.hojoai.omniasst.focus.service;

import com.hojoai.omniasst.focus.dto.response.FlightQueryResponseDTO;
import com.hojoai.omniasst.focus.dto.response.MapQueryResponseDTO;
import com.hojoai.omniasst.focus.dto.response.TrainQueryResponseDTO;
import com.hojoai.omniasst.focus.dto.response.TypeIconQueryResponseDTO;

/**
 * The TripService class for TripService.
 *
 * @author My
 * @version 1.0, 2026/03/06
 * @since hojoai-webmvc-omniasst-focus 1.0.0
 */
public interface QueryService {

    /**
     * 根据航班号和日期查询航班信息
     *
     * @param flightNo 航班号
     * @param date     日期
     * @param lang     语言（可选）
     * @return 航班查询结果
     */
    FlightQueryResponseDTO flightQuery(String flightNo, String date, String lang);

    /**
     * 根据车次、日期、出发站、到达站查询火车信息
     *
     * @param trainNo 车次
     * @param date    日期
     * @param depStan 出发站
     * @param arrStan 到达站
     * @return 火车查询结果
     */
    TrainQueryResponseDTO trainQuery(String trainNo, String date, String depStan, String arrStan);

    /**
     * 根据userId查询最近地图
     *
     * @param userId
     * @return
     */
    MapQueryResponseDTO mapQuery(String userId);

    /**
     * 根据title查询type和icon
     *
     * @param title
     * @return
     */
    TypeIconQueryResponseDTO getTypeIconByTitle(String title);

    /**
     * 根据title查询movieName
     *
     * @param title
     * @return
     */
    String getMovieNameByTitle(String title);

}
