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
package com.hojoai.omniasst.weather.v2.geo;

/**
 * 经纬度解析结果
 *
 * @author wangjian
 * @since 2026-02-02
 */
public class GeoResult {

    /** 经度（WGS84/GCJ02 与上游高德一致） */
    private double longitude;
    /** 纬度 */
    private double latitude;
    /** 展示用地名，可为空 */
    private String locationName;

    /**
     * @param longitude     经度
     * @param latitude      纬度
     * @param locationName  展示名，可为 null
     */
    public GeoResult(double longitude, double latitude, String locationName) {
        this.longitude = longitude;
        this.latitude = latitude;
        this.locationName = locationName;
    }

    /** @return 经度 */
    public double getLongitude() {
        return longitude;
    }

    /** @return 纬度 */
    public double getLatitude() {
        return latitude;
    }

    /** @return 展示用地名，可能为 null */
    public String getLocationName() {
        return locationName;
    }
}
