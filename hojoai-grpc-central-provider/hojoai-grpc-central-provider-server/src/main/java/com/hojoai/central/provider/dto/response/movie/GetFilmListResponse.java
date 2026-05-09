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

import java.util.List;

/**
 *
 * @author My
 * @version 1.0, 2025/11/10
 * @since hojoai-grpc-central-provider 1.0.0
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
