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
package com.hojoai.omniasst.focus.service.impl;

import com.hojoai.omniasst.focus.common.enums.UrlEnum;
import com.hojoai.omniasst.focus.domain.AIDomainService;
import com.hojoai.omniasst.focus.domain.ExternalDomainService;
import com.hojoai.omniasst.focus.domain.MapDomainService;
import com.hojoai.omniasst.focus.dto.request.AITitleHttpRequestDTO;
import com.hojoai.omniasst.focus.dto.response.*;
import com.hojoai.omniasst.focus.dto.val.LocationInfoVal;
import com.hojoai.omniasst.focus.service.QueryService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * The TripServiceImpl class for TripServiceImpl.
 *
 * @author My
 * @version 1.0, 2026/03/06
 * @since hojoai-webmvc-omniasst-focus 1.0.0
 */
@Service
public class QueryServiceImpl implements QueryService {

    @Resource
    private ExternalDomainService externalDomainService;

    @Resource
    private MapDomainService mapDomainService;

    @Resource
    private AIDomainService aIDomainService;

    @Override
    public FlightQueryResponseDTO flightQuery(String flightNo, String date, String lang) {
        return externalDomainService.flightQuery(flightNo, date, lang);
    }

    @Override
    public TrainQueryResponseDTO trainQuery(String trainNo, String date, String depStan, String arrStan) {
        return externalDomainService.trainQuery(trainNo, date, depStan, arrStan);
    }

    @Override
    public MapQueryResponseDTO mapQuery(String userId) {
        MapQueryResponseDTO responseDTO = new MapQueryResponseDTO();

        List<LocationInfoVal> locationInfoValList = mapDomainService.listRecent(userId);
        responseDTO.setMapInfoList(locationInfoValList);

        return responseDTO;
    }

    @Override
    public TypeIconQueryResponseDTO getTypeIconByTitle(String title) {
        AITitleHttpRequestDTO aiTitleHttpRequestDTO = new AITitleHttpRequestDTO();
        aiTitleHttpRequestDTO.setEnteredTitle(title);

        return aIDomainService.chat(UrlEnum.SCHEDULE_MATCH_TITLE_ICON, aiTitleHttpRequestDTO, TypeIconQueryResponseDTO.class);
    }

    @Override
    public String getMovieNameByTitle(String title) {
        AITitleHttpRequestDTO aiTitleHttpRequestDTO = new AITitleHttpRequestDTO();
        aiTitleHttpRequestDTO.setEnteredTitle(title);

        return aIDomainService.chat(UrlEnum.SCHEDULE_GET_MOVIE_NAME, aiTitleHttpRequestDTO, AIMovieNameResponseDTO.class).getMovieName();
    }

}
