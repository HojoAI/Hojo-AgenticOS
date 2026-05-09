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
package com.hojoai.omniasst.focus.controller.external;

import com.hojoai.common.response.Response;
import com.hojoai.common.response.ResponseStatusBasic;
import com.hojoai.omniasst.focus.dto.response.FlightQueryResponseDTO;
import com.hojoai.omniasst.focus.dto.response.MapQueryResponseDTO;
import com.hojoai.omniasst.focus.dto.response.TrainQueryResponseDTO;
import com.hojoai.omniasst.focus.dto.response.TypeIconQueryResponseDTO;
import com.hojoai.omniasst.focus.service.QueryService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 对外查询类接口：航班、火车、地图、类型图标、影视名等辅助查询，供日程创建与展示使用。
 */
@RestController
@RequestMapping("/api/v2")
public class QueryController {

    private static final Logger logger = LoggerFactory.getLogger(QueryController.class);

    @Resource
    private QueryService queryService;

    @GetMapping(value = "/schedule/flight")
    public Response<FlightQueryResponseDTO> flightQuery(@RequestParam("flightNo") String flightNo, @RequestParam("date") String date,
                                                        @RequestParam(value = "lang", required = false) String lang) {

        if (StringUtils.isBlank(flightNo) || StringUtils.isBlank(date)) {
            return Response.newInstance(ResponseStatusBasic.ILLEGAL_ARGUMENT);
        }

        FlightQueryResponseDTO result = queryService.flightQuery(flightNo, date, lang);
        return Response.newInstance(ResponseStatusBasic.OK, result);
    }

    @GetMapping(value = "/schedule/train")
    public Response<TrainQueryResponseDTO> trainQuery(@RequestParam("trainNo") String trainNo, @RequestParam("date") String date,
                                                      @RequestParam("depStan") String depStan, @RequestParam("arrStan") String arrStan) {
        if (StringUtils.isBlank(trainNo) || StringUtils.isBlank(date) ||  StringUtils.isBlank(depStan) || StringUtils.isBlank(arrStan)) {
            return Response.newInstance(ResponseStatusBasic.ILLEGAL_ARGUMENT);
        }

        TrainQueryResponseDTO result = queryService.trainQuery(trainNo, date, depStan, arrStan);
        return Response.newInstance(ResponseStatusBasic.OK, result);
    }

    @GetMapping(value = "/schedule/map")
    public Response<MapQueryResponseDTO> mapQuery(@RequestParam("userId") String userId) {
        if (StringUtils.isBlank(userId)) {
            return Response.newInstance(ResponseStatusBasic.ILLEGAL_ARGUMENT, null);
        }

        MapQueryResponseDTO responseDTO = queryService.mapQuery(userId);

        return Response.newInstance(ResponseStatusBasic.OK, responseDTO);
    }

    @GetMapping(value = "/schedule/type-icon")
    public Response<TypeIconQueryResponseDTO> typeIconQuery(@RequestParam("title") String title) {

        if (StringUtils.isBlank(title)) {
            return Response.newInstance(ResponseStatusBasic.ILLEGAL_ARGUMENT);
        }

        TypeIconQueryResponseDTO responseDTO = queryService.getTypeIconByTitle(title);

        return Response.newInstance(ResponseStatusBasic.OK, responseDTO);
    }

    @GetMapping(value = "/schedule/movie-name")
    public Response<String> movieNameQuery(@RequestParam("title") String title) {

        if (StringUtils.isBlank(title)) {
            return Response.newInstance(ResponseStatusBasic.ILLEGAL_ARGUMENT);
        }

        String movieName = queryService.getMovieNameByTitle(title);

        return Response.newInstance(ResponseStatusBasic.OK, movieName);
    }

}
