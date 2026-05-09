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
package com.hojoai.routing.engine;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * 用户画像与地理信息 DTO。
 * <p>
 * 对应意图模型请求中的 {@code input} 字段，供各 Agent 使用。
 * </p>
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserInput {

    /** 助手性别（由形象等推导）。 */
    private String assistantGender;

    /** 用户 ID。 */
    private String userId;

    /** 市。 */
    private String cityName;

    /** 区/县。 */
    private String districtName;

    /** 省。 */
    private String provinceName;

    /** 经度。 */
    private String longitude;

    /** 纬度。 */
    private String latitude;

    /** 用户昵称。 */
    private String userName;

    public UserInput() {
    }

    public String getAssistantGender() {
        return assistantGender;
    }

    public void setAssistantGender(String assistantGender) {
        this.assistantGender = assistantGender;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    public String getDistrictName() {
        return districtName;
    }

    public void setDistrictName(String districtName) {
        this.districtName = districtName;
    }

    public String getProvinceName() {
        return provinceName;
    }

    public void setProvinceName(String provinceName) {
        this.provinceName = provinceName;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
}
