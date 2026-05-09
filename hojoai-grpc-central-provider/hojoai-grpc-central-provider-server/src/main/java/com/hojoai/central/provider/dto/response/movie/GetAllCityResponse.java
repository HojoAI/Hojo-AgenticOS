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

/**
 *
 * @author My
 * @version 1.0, 2025/11/10
 * @since hojoai-grpc-central-provider 1.0.0
 */
public class GetAllCityResponse {

    /**
     * 城市代码
     */
    private String cityCode;

    /**
     * 城市名称
     */
    private String cityName;

    /**
     * 城市拼音
     */
    private String cityPinYin;

    /**
     * 是否热门城市
     */
    private Boolean isHotCity;

    /**
     * 城市拼音首字母
     */
    private String initials;

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

    public String getCityPinYin() {
        return cityPinYin;
    }

    public void setCityPinYin(String cityPinYin) {
        this.cityPinYin = cityPinYin;
    }

    public Boolean getHotCity() {
        return isHotCity;
    }

    public void setHotCity(Boolean hotCity) {
        isHotCity = hotCity;
    }

    public String getInitials() {
        return initials;
    }

    public void setInitials(String initials) {
        this.initials = initials;
    }

}
