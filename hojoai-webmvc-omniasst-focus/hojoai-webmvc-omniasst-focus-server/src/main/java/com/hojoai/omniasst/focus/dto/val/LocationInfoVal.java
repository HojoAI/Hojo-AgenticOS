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
package com.hojoai.omniasst.focus.dto.val;

import java.math.BigDecimal;

/**
 * 位置信息 Value DTO
 *
 * @author My
 * @version 1.0 2026/03/03
 * @since hojoai-webmvc-omniasst-focus 1.0.0
 */
public class LocationInfoVal {

    /**
     * 位置名称
     */
    private String name;

    /**
     * 位置信息（可选）
     */
    private String location;

    /**
     * 经度（可选）
     */
    private BigDecimal longitude;

    /**
     * 纬度（可选）
     */
    private BigDecimal latitude;

    /**
     * 省（可选）
     */
    private String province;

    /**
     * 市（可选）
     */
    private String city;

    /**
     * 区县（可选）
     */
    private String district;

    /**
     * 乡镇/街道（可选）
     */
    private String township;

    public LocationInfoVal() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
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

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public String getTownship() {
        return township;
    }

    public void setTownship(String township) {
        this.township = township;
    }
}
