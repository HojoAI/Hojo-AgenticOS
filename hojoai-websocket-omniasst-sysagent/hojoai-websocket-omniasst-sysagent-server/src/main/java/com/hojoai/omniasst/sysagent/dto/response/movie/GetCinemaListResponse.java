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

import com.hojoai.central.provider.grpc.GetCinemaListRespDTO;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author My
 * @version 1.0, 2025/11/10
 * @since hojoai-websocket-omniasst-sysagent 1.0.0
 */
public class GetCinemaListResponse {

    /**
     * 总影院数
     */
    private Integer total;

    /**
     * 影院列表
     */
    private List<CinemaInfo> cinemaInfoList;

    public static GetCinemaListResponse convert2GetCinemaListResponse(GetCinemaListRespDTO respDTO) {
        GetCinemaListResponse response = new GetCinemaListResponse();
        response.setTotal(respDTO.getTotal());

        List<CinemaInfo> cinemaInfoList = new ArrayList<>();

        respDTO.getCinemaListList().forEach(cinemaVal -> {
            CinemaInfo cinemaInfo = new CinemaInfo();

            cinemaInfo.setCinemaId(cinemaVal.getCinemaId());
            cinemaInfo.setCinemaName(cinemaVal.getCinemaName());
            cinemaInfo.setCityName(cinemaVal.getCityName());
            cinemaInfo.setCinemaAddress(cinemaVal.getCinemaAddress());
            cinemaInfo.setCinemaTel(cinemaVal.getTel());
            cinemaInfo.setLongitude(cinemaVal.getLongitude());
            cinemaInfo.setLatitude(cinemaVal.getLatitude());
            cinemaInfo.setDistanceText(cinemaVal.getDistanceText());
            cinemaInfo.setDownPrice(cinemaVal.getDownPrice());

            cinemaInfoList.add(cinemaInfo);
        });

        response.setCinemaInfoList(cinemaInfoList);

        return response;
    }

    public Integer getTotal() {
        return total;
    }

    public void setTotal(Integer total) {
        this.total = total;
    }

    public List<CinemaInfo> getCinemaInfoList() {
        return cinemaInfoList;
    }

    public void setCinemaInfoList(List<CinemaInfo> cinemaInfoList) {
        this.cinemaInfoList = cinemaInfoList;
    }

}
