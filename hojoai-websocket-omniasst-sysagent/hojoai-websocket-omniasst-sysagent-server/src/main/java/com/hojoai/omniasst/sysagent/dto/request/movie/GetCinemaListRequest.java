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
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;

/**
 *
 * @author My
 * @version 1.0, 2025/11/12
 * @since hojoai-websocket-omniasst-sysagent 1.0.0
 */
public class GetCinemaListRequest {

    /**
     * 城市代码(必传)
     */
    private String cityCode;

    /**
     * 城市名称
     */
    private String cityName;

    /**
     * 区县代码
     */
    private String regionCode;

    /**
     * 区县名称
     */
    private String regionName;

    /**
     * 搜索影院名称 模糊搜索，如：万达
     */
    private String keyword;

    /**
     * 经度
     */
    private BigDecimal longitude;

    /**
     * 纬度
     */
    private BigDecimal latitude;

    /**
     * 页码
     */
    private Integer pageIndex = 1;

    /**
     * 页大小
     */
    private Integer pageSize = 5;

    public String getCityCode() {
        return cityCode;
    }

    public void setCityCode(String cityCode) {
        this.cityCode = cityCode;
    }

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    public String getRegionCode() {
        return regionCode;
    }

    public void setRegionCode(String regionCode) {
        this.regionCode = regionCode;
    }

    public String getRegionName() {
        return regionName;
    }

    public void setRegionName(String regionName) {
        this.regionName = regionName;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public BigDecimal getLongitude() {
        return longitude;
    }

    public void setLongitude(BigDecimal longitude) {
        this.longitude = longitude;
    }

    public BigDecimal getLatitude() {
        return latitude;
    }

    public void setLatitude(BigDecimal latitude) {
        this.latitude = latitude;
    }

    public Integer getPageIndex() {
        return pageIndex;
    }

    public void setPageIndex(Integer pageIndex) {
        this.pageIndex = pageIndex;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    public void validate(GetCinemaListRequest request) {
        if (StringUtils.isBlank(request.getCityCode()) && StringUtils.isBlank(request.getCityName())) {
            Asserts.notNull(request.getCityCode(), "cityCode or cityName is null!");
        }
    }

}
