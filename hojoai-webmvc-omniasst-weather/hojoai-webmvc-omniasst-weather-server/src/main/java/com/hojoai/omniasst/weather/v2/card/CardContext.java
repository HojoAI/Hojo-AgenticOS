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
package com.hojoai.omniasst.weather.v2.card;

import com.hojoai.omniasst.weather.v2.entity.CaiyunWeatherResult;
import com.hojoai.omniasst.weather.v2.entity.DailyLifeIndexV3Dto;
import com.hojoai.omniasst.weather.v2.geo.GeoResult;
import com.hojoai.omniasst.weather.v2.slot.SlotExtractionResult;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

/**
 * 卡片映射上下文：槽位 + 彩云结果 + 地理信息 + 解析出的日期（可选）+ v3 生活指数（可选）
 *
 * @author wangjian
 * @since 2026-02-02
 */
public class CardContext {

    /** 槽位解析结果（地点、卡片类型、单日/多日等） */
    private final SlotExtractionResult slot;
    /** 按目标日期过滤后的彩云综合结果 */
    private final CaiyunWeatherResult caiyunResult;
    /** 经纬度与展示地名 */
    private final GeoResult geoResult;
    /** 从 query 中解析出的日期：单日为单元素列表，区间为区间内全部日期 */
    private final List<LocalDate> parsedDates;
    /** v3 丰富版生活指数（按日期过滤后），未调用或失败可为空 */
    private final List<DailyLifeIndexV3Dto> filteredLifeIndexV3;

    /**
     * 等价于 {@link #CardContext(SlotExtractionResult, CaiyunWeatherResult, GeoResult, List, List)} 且解析日期与生活指数为空。
     */
    public CardContext(SlotExtractionResult slot, CaiyunWeatherResult caiyunResult, GeoResult geoResult) {
        this(slot, caiyunResult, geoResult, null, null);
    }

    /**
     * 等价于全参构造，生活指数列表为 null。
     *
     * @param parsedDates 从用户 query 解析出的目标日期，可为 null
     */
    public CardContext(SlotExtractionResult slot, CaiyunWeatherResult caiyunResult, GeoResult geoResult, List<LocalDate> parsedDates) {
        this(slot, caiyunResult, geoResult, parsedDates, null);
    }

    /**
     * @param slot                 槽位与卡片类型
     * @param caiyunResult         过滤后的彩云数据
     * @param geoResult            地理信息
     * @param parsedDates          目标日期列表，null 视为空列表
     * @param filteredLifeIndexV3  v3 生活指数按日过滤结果，null 视为空列表
     */
    public CardContext(SlotExtractionResult slot, CaiyunWeatherResult caiyunResult, GeoResult geoResult,
                       List<LocalDate> parsedDates, List<DailyLifeIndexV3Dto> filteredLifeIndexV3) {
        this.slot = slot;
        this.caiyunResult = caiyunResult;
        this.geoResult = geoResult;
        this.parsedDates = parsedDates == null ? List.of() : Collections.unmodifiableList(parsedDates);
        this.filteredLifeIndexV3 = filteredLifeIndexV3 == null ? List.of() : Collections.unmodifiableList(filteredLifeIndexV3);
    }

    public SlotExtractionResult getSlot() {
        return slot;
    }

    public CaiyunWeatherResult getCaiyunResult() {
        return caiyunResult;
    }

    public GeoResult getGeoResult() {
        return geoResult;
    }

    /** 从 query 解析出的日期列表：单日为单元素，区间为区间内全部日期；未解析到则为空列表 */
    public List<LocalDate> getParsedDates() {
        return parsedDates;
    }

    /** v3 生活指数（按 parsedDates 过滤后），未调用或失败为空列表 */
    public List<DailyLifeIndexV3Dto> getFilteredLifeIndexV3() {
        return filteredLifeIndexV3;
    }

    /** 展示用地名：优先 geo 返回的 locationName，其次 slot.location，否则「当前定位」 */
    public String getDisplayName() {
        if (geoResult != null && geoResult.getLocationName() != null && !geoResult.getLocationName().isBlank()) {
            return geoResult.getLocationName();
        }
        if (slot != null && slot.getLocation() != null && !slot.getLocation().isBlank()) {
            return slot.getLocation();
        }
        return "当前定位";
    }
}
